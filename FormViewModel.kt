package lk.ac.jfn.uojana

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

const val QUESTIONNAIRE_ID = "uoj_ana"

class FormViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var user by mutableStateOf<FirebaseUser?>(auth.currentUser); private set
    var questionnaire by mutableStateOf<Questionnaire?>(null); private set
    var busy by mutableStateOf(false); private set
    var message by mutableStateOf<String?>(null)
    var started by mutableStateOf(false)
    var step by mutableIntStateOf(0)             // 0..sections.size-1 = sections, sections.size = review
    var submittedId by mutableStateOf<String?>(null); private set

    /** single -> String, multi -> List<String>, text/number -> String */
    val answers = mutableStateMapOf<String, Any>()

    // ---------- auth ----------
    fun signIn(email: String, password: String) = authCall { auth.signInWithEmailAndPassword(email.trim(), password) }
    fun register(email: String, password: String) = authCall { auth.createUserWithEmailAndPassword(email.trim(), password) }

    private fun authCall(block: () -> com.google.android.gms.tasks.Task<*>) {
        busy = true; message = null
        block().addOnCompleteListener { t ->
            busy = false
            if (t.isSuccessful) user = auth.currentUser else message = t.exception?.localizedMessage ?: "Sign-in failed"
        }
    }

    fun signOut() {
        auth.signOut(); user = null; reset()
    }

    // ---------- questionnaire ----------
    fun loadQuestionnaire() {
        busy = true; message = null
        db.collection("questionnaires").document(QUESTIONNAIRE_ID).get()
            .addOnSuccessListener { doc ->
                busy = false
                val data = doc.data
                if (data == null) message = "Questionnaire not found. Run seed_schema.py first."
                else questionnaire = parseQuestionnaire(doc.id, data)
            }
            .addOnFailureListener { busy = false; message = it.localizedMessage }
    }

    fun start() { reset(); started = true; if (questionnaire == null) loadQuestionnaire() }

    fun reset() { answers.clear(); step = 0; started = false; submittedId = null; message = null }

    // ---------- logic ----------
    fun isVisible(q: Question): Boolean {
        val id = q.showIfId ?: return true
        val a = answers[id] ?: return false
        return when (a) {
            is List<*> -> a.any { it in q.showIfIn }
            else -> a in q.showIfIn
        }
    }

    fun setSingle(id: String, v: String) { answers[id] = v }
    fun setText(id: String, v: String) { answers[id] = v }
    fun toggleMulti(id: String, option: String) {
        val cur = (answers[id] as? List<*>)?.filterIsInstance<String>().orEmpty()
        answers[id] = if (option in cur) cur - option else cur + option
    }

    /** Returns labels of unanswered required questions in the section. */
    fun missing(section: Section): List<String> = section.questions.filter { q ->
        isVisible(q) && (
            (q.required && isEmpty(answers[q.id])) ||
            (q.mustEqual != null && answers[q.id] != q.mustEqual)
        )
    }.map { it.label }

    private fun isEmpty(v: Any?) = v == null || (v is String && v.isBlank()) || (v is List<*> && v.isEmpty())

    fun next() {
        val qn = questionnaire ?: return
        val miss = if (step < qn.sections.size) missing(qn.sections[step]) else emptyList()
        if (miss.isNotEmpty()) {
            val consentBlocked = qn.sections[step].questions.any { it.mustEqual != null && answers[it.id] != it.mustEqual }
            message = if (consentBlocked) "You must agree to the consent statement to continue."
                      else "Please answer ${miss.size} required question(s)."
            return
        }
        message = null
        step++
    }

    fun back() { message = null; if (step > 0) step-- else started = false }

    // ---------- submit (write-once, enforced by Firestore rules) ----------
    fun submit() {
        val qn = questionnaire ?: return
        val uid = user?.uid ?: return
        // only keep answers of visible questions
        val clean = HashMap<String, Any>()
        qn.sections.flatMap { it.questions }.filter { isVisible(it) }.forEach { q ->
            answers[q.id]?.let { clean[q.id] = it }
        }
        busy = true; message = null
        val doc = hashMapOf(
            "uid" to uid,
            "questionnaireId" to qn.id,
            "version" to qn.version,
            "answers" to clean,
            "submittedAt" to FieldValue.serverTimestamp(),
            "appVersion" to "1.0",
        )
        db.collection("responses").add(doc)
            .addOnSuccessListener { busy = false; submittedId = it.id.take(8).uppercase() }
            .addOnFailureListener { busy = false; message = it.localizedMessage ?: "Could not submit" }
    }
}
