package voice_assistant

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityManager
import voice_assistant.command.CommandExecutor
import voice_assistant.command.Executor
import voice_assistant.command_search.TfIdfSearcherImpl
import voice_assistant.describer.ScreenResolverImpl
import voice_assistant.model.ViewData
import voice_assistant.recognizer.Recognizer
import voice_assistant.speech.voice.SystemVoiceActor
import voice_assistant.speech.voice.VoiceActor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoiceAdapter(
    private val context: Activity,
) {
    private val voiceActor: VoiceActor = SystemVoiceActor(context)
    private val recognizer = Recognizer(context, voiceActor)
    private val resolver = ScreenResolverImpl(context, Dependencies.resolvers)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val commandSearch = TfIdfSearcherImpl(context)

    @Volatile
    private var currentDescription: List<ViewData> = listOf()
    private val executor: CommandExecutor = Executor(commandSearch)

    fun start(view: View) {
        view.hasOnClickListeners()
        resolver.resolveScreen(view)
        recognize()
        coroutineScope.launch {
            resolver.observeScreenDescription().collect() {
                Log.d(
                    "VoiceAdapt", "New screen description: ${
                        it.filter { filterData ->
                            filterData.description.isNotEmpty()
                        }.map { data ->
                            data.description
                        }
                    }"
                )
                currentDescription = it
                if (!isSystemAccessibilityEnabled()) {
                    withContext(Dispatchers.Main) {
                        voiceActor.voice(it)
                    }
                }
            }
        }
        coroutineScope.launch {
            recognizer.observeRecognizedText.collect() {
                executor.executeCommand(currentDescription, it)
            }
        }
    }

    fun recognize() {
        if (!isSystemAccessibilityEnabled()) {
            recognizer.start()
        }
    }

    fun finish() {

    }

    private fun isSystemAccessibilityEnabled(): Boolean {
        val am = context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager?
        val enabledServices =
            am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
        return enabledServices?.isNotEmpty() ?: true
    }
}