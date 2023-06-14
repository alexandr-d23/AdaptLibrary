package voice_assistant.speech.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.TextToSpeech.QUEUE_ADD
import android.util.Log
import android.widget.Toast
import voice_assistant.model.ViewData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.*

class SystemVoiceActor(
    private val context: Context
) : VoiceActor {
    lateinit var tts: TextToSpeech

    private val flow = MutableSharedFlow<String>(replay = 10)

    private val speechScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val listener = OnInitListener { initStatus ->
        if (initStatus == TextToSpeech.SUCCESS) {
            with(tts) {
                language = if (isLanguageAvailable(Locale(Locale.getDefault().language))
                    == TextToSpeech.LANG_AVAILABLE
                ) {
                    Locale(Locale.getDefault().language)
                } else {
                    Locale.US
                }
            }
            tts.setPitch(0.5f)
            tts.setSpeechRate(1f)
            Log.d("MYTAG", "Speech success init")
            observeSpeechText()
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
            Log.d("MYTAG", "Error")
        }
    }.also {
        tts = TextToSpeech(context, it)
    }

    private fun observeSpeechText() {
        speechScope.launch {
            flow.collect() { text ->
                withContext(Dispatchers.Main) {
                    Log.d("MYTAG", "Speaking $text")
                    tts.speak(text, QUEUE_ADD, null)
                }
            }
        }
    }

    override fun voice(views: List<ViewData>) {
        val text = StringBuilder()
        views.forEach {
            it.description?.let { desc ->
                text.appendLine(desc)
            }
        }
        flow.tryEmit(text.toString())
        Log.d("MYTAG", "try to speak $text")
    }
}