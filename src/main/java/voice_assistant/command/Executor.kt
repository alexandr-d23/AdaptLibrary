package voice_assistant.command

import android.util.Log
import voice_assistant.command_search.CommandSearcher
import voice_assistant.model.ViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Executor(
    private val commandSearcher: CommandSearcher
) : CommandExecutor {

    override suspend fun executeCommand(commands: List<ViewData>, voiceCommand: String) {
        val filteredCommands = commands.filter {
            it.description.isNotEmpty()
        }
        val commandsDescription = filteredCommands.map {
            it.description
        }
        Log.d("Executor", "Commands: $filteredCommands, voiceCommand: $voiceCommand")
        val commandIndex = commandSearcher.searchCommand(
            commandsDescription,
            "далее"
        )
        Log.d("Executor", "Result index: $commandIndex")
        withContext(Dispatchers.Main) {
            if (commandIndex >= 0) {
                filteredCommands[commandIndex].view.callOnClick()
            }
        }
    }
}