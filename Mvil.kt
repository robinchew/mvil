package mvil

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup

data class Virtual(val cls: (Context) -> View,
                   val tag: String = "",
                   val attrs: List<KeyViewFunction> = listOf(),
                   val children: List<Virtual> = listOf()){

    constructor(cls: (Context) -> View,
                attrs: List<KeyViewFunction> = listOf(),
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
        val (realChild, justCreated) = if (realChildOrNull == null) {
            Pair(realiseView(activity, rootView, virtualChild, i), true)
        } else {
            reOrderChild(realChildOrNull, i)
            Pair(realChildOrNull, false)
        }
        val newTag = virtualChild.tag
        val cachedTag = getViewCache(realChild, "tag")
        if (cachedTag == null) {
            val (key, tagF) = tag(newTag)
            tagF(realChild)
        }
        val attrsMap = virtualChild.attrs.associate {(key, viewF) ->
            key to viewF
        }
        val onCreate = attrsMap.get("onCreate")
        val onUpdate = attrsMap.get("onUpdate")
        val onCrup = attrsMap.get("onCrup")
        if (justCreated) {
            if (onCreate != null) {
                onCreate(realChild)
            }
        } else {
            if (onUpdate != null) {
                onUpdate(realChild)
            }
        }
        if (onCrup != null) {
            onCrup(realChild)
        }
        for ((key, f) in virtualChild.attrs) {
            if(! listOf("onCreate", "onUpdate", "onCrup").contains(key)) {
                f(realChild)
            }
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