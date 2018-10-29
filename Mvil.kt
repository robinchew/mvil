package mvil

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.collections.ArrayList
import kotlin.math.min

data class Virtual(val cls: (Context) -> View,
                   val tag: String = "",
                   val attrs: ArrayList<AttrSetter> = arrayListOf(),
                   val children: ArrayList<Virtual> = arrayListOf()){

    constructor(cls: (Context) -> View,
                attrs: ArrayList<AttrSetter> = arrayListOf(),
                children: ArrayList<Virtual> = arrayListOf()) : this(cls, "", attrs, children)
}

fun buildTags(tags: List<String>): String {
    return tags.joinToString("|")
}

fun render(activity: Activity, rootView: ViewGroup, virtuals: ArrayList<Virtual>) {
    val realChildren = matchOrderOfRealAndVirtual(
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
        if (virtualChild.children.size > 0) {
            assert(
                realChild is ViewGroup,
                {"If a virtual node has children then its parent must be a ViewGroup."})
            render(activity, realChild as ViewGroup, virtualChild.children);
        }
    }
}