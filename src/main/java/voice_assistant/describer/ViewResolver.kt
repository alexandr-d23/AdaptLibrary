package voice_assistant.describer

import android.view.View
import voice_assistant.model.ViewData

interface ViewResolver {

    fun canResolve(view: View): Boolean

    fun resolve(view: View): ViewData
}