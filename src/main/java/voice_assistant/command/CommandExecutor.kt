package voice_assistant.command

import voice_assistant.model.ViewData

interface CommandExecutor {
    suspend fun executeCommand(commands: List<ViewData>, voiceCommand: String)
}