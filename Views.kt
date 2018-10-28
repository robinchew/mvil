package mvil

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.max
import kotlin.math.min

fun realiseView(activity: Context, parent: ViewGroup, virtual: Virtual, index: Int): View {
    val view = virtual.cls(activity)
    parent.addView(view, index)
    return view
}

fun removeChild(view: View) {
    val vg = (view.parent as? ViewGroup)
    if (vg != null) {
        vg.removeView(view)
    }
}

fun removeChild(parentView: ViewGroup, childView: View) {
    parentView.removeView(childView)
    removeViewCache(childView)
}

fun reOrderChild(child: View, i: Int) {
    val vg = (child.parent as? ViewGroup)
    if (vg != null) {
        vg.removeView(child)
        vg.addView(child, min(i, vg.childCount))
    }
}

fun pop(many: MutableList<View>): View? {
    if (many.size == 0){
        return null
    }
    val popped = many.get(0)
    many.removeAt(0)
    return popped
}

fun matchOrderOfRealAndVirtual(realChildren: List<View?>,
                               virtualChildren: List<Virtual>): List<View?> {

    val childrenHaveTags = virtualChildren.all {it.tag != ""}
    if (childrenHaveTags) {
        val newChildren = virtualChildren.mapIndexed { i, vChild ->
            val foundChild = realChildren.find { rChild ->
                vChild.tag != "" && vChild.tag == rChild?.tag
            }
            foundChild
        }
        realChildren.filter {! newChildren.contains(it)}.forEach {
            removeChild(it!!)
        }
        return newChildren
    }
    return realChildren
}

fun getChildren(viewGroup: ViewGroup): List<View> {
    return (0..viewGroup.childCount-1).map {i ->
        viewGroup.getChildAt(i)
    }
}

fun getChildren(viewGroup: ViewGroup, expectedLength: Int): List<View?> {
    return (0..expectedLength-1).map {i ->
        viewGroup.getChildAt(i)
    }
}

fun deleteExcessChildren(rootView: ViewGroup, virtuals: ArrayList<Virtual>) {
    val rsize: Int = rootView.childCount
    val vsize = virtuals.size
    val excessSize = max(rsize-vsize, 0)
    for (i in (rsize-excessSize)..(rsize-1)) {
        val child = rootView.getChildAt(i)
        // Child can be null when deleteExcessChildren has already run
        // for parent's children

        removeChild(rootView, child)
    }
}

fun deleteExcessChildrenRecursively(rootView: ViewGroup, virtuals: ArrayList<Virtual>) {
    deleteExcessChildren(rootView, virtuals)
    getChildren(rootView).forEachIndexed {i, child ->
        if (virtuals[i].children.size > 0) {
            deleteExcessChildrenRecursively(child as ViewGroup, virtuals[i].children)
        }
    }
}

class RenderView(val activity: Activity): FrameLayout(activity) {
    fun sync(virtual: Virtual) {
        render(activity, this, arrayListOf(virtual), "")
        deleteExcessChildrenRecursively(this, arrayListOf(virtual))
    }
}