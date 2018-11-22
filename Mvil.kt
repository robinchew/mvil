package mvil

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup

data class Virtual(val cls: (Context) -> View,
                   val tag: String = "",
                   val attrs: List<ViewFunction> = listOf(),
                   val children: List<Virtual> = listOf()){

    constructor(cls: (Context) -> View,
                attrs: List<ViewFunction> = listOf(),
                children: List<Virtual> = listOf()) : this(cls, "", attrs, children)
}

fun buildTags(tags: List<String>): String {
    return tags.joinToString("|")
}

fun render(activity: Activity, rootView: ViewGroup, virtuals: List<Virtual>) {
    val realChildren = orderAndCullViews(
        rootView,
        virtuals)
    realChildren.zip(virtuals).forEachIndexed {i, (realChildOrNull, virtualChild) ->
        val realChild: View = if (realChildOrNull == null) {
            realiseView(activity, rootView, virtualChild, i)
        } else {
            reOrderChild(realChildOrNull, i)
            realChildOrNull
        }
        val newTag = virtualChild.tag
        val cachedTag = getViewCache(realChild, "tag")
        if (cachedTag == null) {
            tag(newTag)(realChild)
        }
        for (f in virtualChild.attrs) {
            f(realChild)
        }
        if (virtualChild.children.size > 0 || ((realChild as? ViewGroup)?.childCount ?: 0) > 0) {
            // Recur if there are still either virtual or real children to render
            assert(
                realChild is ViewGroup,
                {"If a virtual node has children then its parent must be a ViewGroup."})
            render(activity, realChild as ViewGroup, virtualChild.children);
        }
    }
}