package voice_assistant

import voice_assistant.describer.describers.ButtonResolver
import voice_assistant.describer.describers.ContainerResolver
import voice_assistant.describer.describers.ListDescriber
import voice_assistant.describer.describers.TextResolver

object Dependencies {
    val resolvers = listOf(
        ButtonResolver(),
        TextResolver(),
        ContainerResolver(),
        ListDescriber()
    )
}