package voice_assistant.speech.voice

import voice_assistant.model.ViewData

interface VoiceActor {
    fun voice(views: List<ViewData>)
}