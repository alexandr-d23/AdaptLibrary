package voice_assistant.describer.describers

import android.view.View
import android.widget.TextView
import voice_assistant.describer.ViewResolver
import voice_assistant.model.ViewData

class TextResolver : ViewResolver {

    override fun canResolve(view: View): Boolean {
        return view is TextView
    }

    override fun resolve(view: View): ViewData {
        view.hasOnClickListeners()
        return (view as? TextView)?.text?.toString()?.let { text ->
            ViewData(
                view,
                text,
                text
            )
        } ?: throw IllegalArgumentException("Incorrect view type")
    }


}