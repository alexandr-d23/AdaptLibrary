package voice_assistant.recognizer

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import voice_assistant.speech.voice.VoiceActor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.*

class Recognizer(
    private val context: Context,
    private val voiceActor: VoiceActor
) {
    private val resultListener: (String?) -> Unit = {
        it?.let {
            CoroutineScope(Dispatchers.IO).launch {
                observeRecognizedText.emit(it)
            }
        }
        startListening()
    }

    val observeRecognizedText = MutableSharedFlow<String>()

    fun start() {
        Log.d("SpeechRecognizer", "Recognizer start")
        startListening()
    }

    private fun startListening() {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager;

        val listener = Listener(
            context,
            voiceActor,
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
            resultListener
        )
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault().language
        )
        speechRecognizer.setRecognitionListener(listener)
        speechRecognizer.startListening(speechRecognizerIntent)
    }

}

class Listener(
    private val context: Context,
    private val voiceActor: VoiceActor,
    private val previousStreamVolume: Int,
    private val onResult: (String?) -> Unit
) : RecognitionListener {

    override fun onResults(results: Bundle?) {
        var str = StringBuilder()
        val data: ArrayList<String>? =
            results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        data?.let {
            for (i in 0 until data.size) {
                str.append(data[i])
            }
        }
        Log.d("Speech", "Recognized:  '$str'")
        onResult(str.toString())
    }

    override fun onReadyForSpeech(params: Bundle?) {

    }

    override fun onBeginningOfSpeech() {

    }

    override fun onRmsChanged(rmsdB: Float) {

    }

    override fun onBufferReceived(buffer: ByteArray?) {

    }

    override fun onEndOfSpeech() {

    }

    override fun onError(error: Int) {
        onResult(null)
    }

    override fun onPartialResults(partialResults: Bundle?) {

    }

    override fun onEvent(eventType: Int, params: Bundle?) {

    }

}