package voice_assistant.describer.describers

import android.view.View
import android.view.ViewGroup
import voice_assistant.describer.ViewResolver
import voice_assistant.model.ViewData

class ContainerResolver : ViewResolver {
    override fun canResolve(view: View): Boolean = view is ViewGroup

    override fun resolve(view: View): ViewData {
        return ViewData(view, view.contentDescription?.toString() ?: "")
    }

}