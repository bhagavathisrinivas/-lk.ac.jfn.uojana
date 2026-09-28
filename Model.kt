package lk.ac.jfn.uojana

data class Question(
    val id: String,
    val type: String,            // text | long_text | number | single | multi
    val label: String,
    val hint: String,
    val options: List<String>,
    val required: Boolean,
    val mustEqual: String?,
    val showIfId: String?,
    val showIfIn: List<String>,
)

data class Section(val id: String, val title: String, val questions: List<Question>)

data class Questionnaire(val id: String, val title: String, val version: String, val sections: List<Section>)

@Suppress("UNCHECKED_CAST")
fun parseQuestionnaire(id: String, data: Map<String, Any?>): Questionnaire {
    val sections = (data["sections"] as? List<Map<String, Any?>>).orEmpty().map { s ->
        Section(
            id = s["id"] as String,
            title = s["title"] as String,
            questions = (s["questions"] as? List<Map<String, Any?>>).orEmpty().map { q ->
                val showIf = q["show_if"] as? Map<String, Any?>
                Question(
                    id = q["id"] as String,
                    type = q["type"] as String,
                    label = q["label"] as String,
                    hint = q["hint"] as? String ?: "",
                    options = (q["options"] as? List<String>).orEmpty(),
                    required = q["required"] as? Boolean ?: false,
                    mustEqual = q["must_equal"] as? String,
                    showIfId = showIf?.get("id") as? String,
                    showIfIn = (showIf?.get("in") as? List<String>).orEmpty(),
                )
            },
        )
    }
    return Questionnaire(id, data["title"] as? String ?: "", data["version"] as? String ?: "", sections)
}
