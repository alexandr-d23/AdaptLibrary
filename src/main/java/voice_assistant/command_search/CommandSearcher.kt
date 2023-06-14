package voice_assistant.command_search

interface CommandSearcher {
    fun searchCommand(commands: List<String>, voiceCommand: String): Int
}