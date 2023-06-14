package voice_assistant.describer.describers

import android.view.View
import android.widget.Button
import android.widget.TextView
import voice_assistant.describer.ViewResolver
import voice_assistant.model.ViewData

class ButtonResolver: ViewResolver {
    override fun canResolve(view: View): Boolean {
        return view is Button && view.text.isNotEmpty()
    }

    override fun resolve(view: View): ViewData {
        return (view as? TextView)?.text?.toString()?.let { description ->
            ViewData(
                view,
                "Кнопка $description"
            )
        } ?: throw IllegalArgumentException("Incorrect view type")
    }

}