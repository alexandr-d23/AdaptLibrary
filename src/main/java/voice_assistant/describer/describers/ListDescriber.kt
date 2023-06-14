package voice_assistant.describer.describers

import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import voice_assistant.describer.ViewResolver
import voice_assistant.model.ViewData

class ListDescriber : ViewResolver {

    override fun canResolve(view: View): Boolean {
        return view is RecyclerView ||
                view is ListView ||
                (view is ViewGroup && hasViewWithSameId(view))
    }

    private fun hasViewWithSameId(view: ViewGroup): Boolean {
        val differentId = view.children.groupBy {
            it.id
        }.size
        return differentId == 1 && view.childCount > 3
    }

    override fun resolve(view: View): ViewData {
        val desc = (view as? TextView)?.text?.toString() ?: ""
        return ViewData(
            view = view,
            description = "Список элементов $desc",
        )
    }
}