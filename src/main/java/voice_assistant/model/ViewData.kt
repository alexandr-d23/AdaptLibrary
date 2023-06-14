package voice_assistant.model

import android.view.View

data class ViewData(
    val view: View,
    val description: String,
    val command: String = ""
)