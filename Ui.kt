package lk.ac.jfn.uojana

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private val Teal = Color(0xFF00C9A7)
private val Navy = Color(0xFF0B1620)
private val CardBg = Color(0xFF14212D)

@Composable
fun AppRoot(vm: FormViewModel) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Teal, onPrimary = Color.Black, background = Navy, surface = CardBg,
            onBackground = Color.White, onSurface = Color.White,
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when {
                vm.user == null -> LoginScreen(vm)
                vm.submittedId != null -> DoneScreen(vm)
                vm.started -> WizardScreen(vm)
                else -> DashboardScreen(vm)
            }
        }
    }
}

// ---------------- Login ----------------
@Composable
fun LoginScreen(vm: FormViewModel) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("UoJ Accessibility Survey", style = MaterialTheme.typography.headlineMedium,
            color = Teal, fontWeight = FontWeight.Bold)
        Text("Disability Support Center", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Button({ vm.signIn(email, pass) }, enabled = !vm.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text("Sign in")
        }
        TextButton({ vm.register(email, pass) }, enabled = !vm.busy) { Text("Create account") }
        vm.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (vm.busy) CircularProgressIndicator()
    }
}

// ---------------- Dashboard ----------------
@Composable
fun DashboardScreen(vm: FormViewModel) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Welcome", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            IconButton({ vm.signOut() }) { Icon(Icons.Filled.Logout, contentDescription = "Sign out") }
        }
        Text(vm.user?.email ?: "", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBg)) {
            Column(Modifier.padding(20.dp)) {
                Text("Accessibility Needs Assessment", style = MaterialTheme.typography.titleLarge, color = Teal)
                Spacer(Modifier.height(8.dp))
                Text("Tell the Disability Support Center about your needs. It takes about 15 minutes. " +
                     "Once submitted, answers cannot be edited.")
                Spacer(Modifier.height(16.dp))
                Button({ vm.start() }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Start survey") }
            }
        }
    }
}

// ---------------- Wizard ----------------
@Composable
fun WizardScreen(vm: FormViewModel) {
    val qn = vm.questionnaire
    if (qn == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (vm.busy) CircularProgressIndicator() else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(vm.message ?: "Loading...")
                    TextButton({ vm.reset() }) { Text("Back") }
                }
            }
        }
        return
    }
    val total = qn.sections.size
    val onReview = vm.step >= total
    val title = if (onReview) "Review answers" else qn.sections[vm.step].title

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(if (onReview) "Final step" else "Section ${vm.step + 1} of $total", color = Color.Gray)
            Text(title, style = MaterialTheme.typography.titleLarge, color = Teal, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { vm.step.toFloat() / total }, modifier = Modifier.fillMaxWidth())
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            if (onReview) ReviewList(vm, qn)
            else qn.sections[vm.step].questions.filter { vm.isVisible(it) }.forEach { QuestionCard(vm, it) }
            Spacer(Modifier.height(12.dp))
        }
        vm.message?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton({ vm.back() }, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) { Text("Back") }
            if (onReview) {
                Button({ vm.submit() }, enabled = !vm.busy, modifier = Modifier.weight(2f).heightIn(min = 52.dp)) {
                    Text(if (vm.busy) "Submitting..." else "Confirm & Submit")
                }
            } else {
                Button({ vm.next() }, modifier = Modifier.weight(2f).heightIn(min = 52.dp)) { Text("Continue") }
            }
        }
    }
}

@Composable
fun QuestionCard(vm: FormViewModel, q: Question) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, Color(0xFF1F3A44)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(q.label + if (q.required) " *" else "", fontWeight = FontWeight.SemiBold)
            if (q.hint.isNotBlank()) Text(q.hint, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            when (q.type) {
                "text", "number", "long_text" -> OutlinedTextField(
                    value = vm.answers[q.id] as? String ?: "",
                    onValueChange = { vm.setText(q.id, it) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (q.type == "long_text") 3 else 1,
                    singleLine = q.type != "long_text",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (q.type == "number") KeyboardType.Number else KeyboardType.Text),
                )
                "single" -> q.options.forEach { opt ->
                    val selected = vm.answers[q.id] == opt
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .selectable(selected, role = Role.RadioButton) { vm.setSingle(q.id, opt) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected, onClick = null)
                        Spacer(Modifier.width(12.dp)); Text(opt)
                    }
                }
                "multi" -> {
                    val chosen = (vm.answers[q.id] as? List<*>).orEmpty()
                    q.options.forEach { opt ->
                        val on = opt in chosen
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .toggleable(on, role = Role.Checkbox) { vm.toggleMulti(q.id, opt) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(on, onCheckedChange = null)
                            Spacer(Modifier.width(12.dp)); Text(opt)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewList(vm: FormViewModel, qn: Questionnaire) {
    Text("Read-only review. Submissions are final and cannot be edited.", color = Color(0xFFFFB300))
    Spacer(Modifier.height(8.dp))
    qn.sections.forEach { s ->
        val rows = s.questions.filter { vm.isVisible(it) && vm.answers[it.id] != null }
        if (rows.isEmpty()) return@forEach
        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
            Column(Modifier.padding(16.dp)) {
                Text(s.title, color = Teal, fontWeight = FontWeight.Bold)
                rows.forEach { q ->
                    val a = vm.answers[q.id]
                    val shown = if (a is List<*>) a.joinToString(", ") else a.toString()
                    Spacer(Modifier.height(8.dp))
                    Text(q.label, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    Text(shown)
                }
            }
        }
    }
}

// ---------------- Done ----------------
@Composable
fun DoneScreen(vm: FormViewModel) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Teal, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(12.dp))
        Text("Survey submitted!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Reference ID: ${vm.submittedId}", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        Button({ vm.reset() }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Back to dashboard") }
    }
}
