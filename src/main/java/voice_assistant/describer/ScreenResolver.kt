package voice_assistant.describer

import voice_assistant.model.ViewData
import com.github.adriankuta.datastructure.tree.TreeNode
import kotlinx.coroutines.flow.Flow

interface ScreenResolver<T> {
    fun observeScreenDescription(): Flow<List<ViewData>>
    fun resolveScreen(screen: T)
}