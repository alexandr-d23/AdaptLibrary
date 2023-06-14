package voice_assistant.describer

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import voice_assistant.model.ViewData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ScreenResolverImpl(
    private val context: Context,
    private val resolvers: List<ViewResolver>
) : ScreenResolver<View> {

    private val currentFlow = MutableStateFlow<List<ViewData>>(listOf())
    private val listener = object :
        ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewAdded(parent: View?, child: View?) {
            Log.d("AndroidScreenResolver", "OnChildViewAdded $parent")
            describe()
        }

        override fun onChildViewRemoved(parent: View?, child: View?) {
            Log.d("AndroidScreenResolver", "OnChildViewRemoved $parent")
            describe()
        }
    }
    lateinit var rootView: View

    override fun resolveScreen(screen: View) {
        rootView = screen
        (screen as ViewGroup).setOnHierarchyChangeListener(listener)
    }

    private fun describe() {
        val viewsDescription =
            mutableListOf<ViewData>()
        viewsDescription.addAll(checksViews(rootView))
        Log.d("AndroidScreenResolver", "Resolved screen $rootView")
        if (viewsDescription.filter {
                it.description.isNotEmpty()
            }.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                currentFlow.emit(viewsDescription)
            }
        }

    }

    private fun checksViews(view: View): List<ViewData> {
        if (view.isVisible) {
            val views = mutableListOf<ViewData>()
            val viewData = resolveView(view)
            viewData?.let { data ->
                views.add(data)
                if (view is ViewGroup) {

                    view.setOnHierarchyChangeListener(listener)
                    var children = view.children.filter {
                        it.visibility == View.VISIBLE
                    }.toList()
                    if (view is FragmentContainerView && children.isNotEmpty()) {
                        children = listOf(children.last())
                    }
                    Log.d(
                        "AndroidScreenResolver", "ViewGroup ${view::class.java.name}:  children: ${
                            children.map {
                                it::class.java.name
                            }.toList()
                        }"
                    )

                    for (child in children) {
                        checksViews(child).forEach { childNode ->
                            views.add(childNode)
                        }
                    }
                }
            }
            return views
        } else {
            return listOf()
        }
    }

    private fun resolveView(view: View): ViewData? {
        val resolver = resolvers.find {
            it.canResolve(view)
        }
        return resolver?.resolve(view)
    }

    override fun observeScreenDescription(): Flow<List<ViewData>> = currentFlow

}