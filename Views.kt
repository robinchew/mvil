package mvil

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.max

fun realiseView(activity: Context, parent: ViewGroup, virtual: Virtual, index: Int): View {
    val view = virtual.cls(activity)
    parent.addView(view, index)
    return view
}

fun removeChild(parentView: ViewGroup, childView: View) {
    parentView.removeView(childView)
    removeViewCache(childView)
}

fun findWithLeftOvers(realChildren: List<View>, virtualChildren: List<Virtual>) {

}

fun pad(many: List<View?>, length: Int): List<View?> {
    return (0..length-1).map {i ->
        many[i]
    }
}

fun matchOrderOfRealAndVirtual(realChildren: List<View?>,
                               virtualChildren: List<Virtual>): List<View?> {
    val newRealChildren = virtualChildren.map {vChild ->
        realChildren.find {rChild ->
            vChild.tag == rChild?.tag
        }
    }
    return newRealChildren.zip(pad(realChildren, newRealChildren.size)).map {(nrChild, rChild) ->
        if (nrChild == null) {
            rChild
        } else {
            nrChild
        }
    }
}

fun getChildren(viewGroup: ViewGroup): List<View> {
    return (0..viewGroup.childCount-1).map {i ->
        viewGroup.getChildAt(i)
    }
}

fun getChildren(viewGroup: ViewGroup, expectedLength: Int): List<View?> {
    return (0..expectedLength).map {i ->
        viewGroup.getChildAt(i)
    }
}

fun deleteExcessChildren(rootView: ViewGroup, virtuals: ArrayList<Virtual>) {
    val rsize: Int = rootView.childCount
    val vsize = virtuals.size
    val excessSize = max(rsize-vsize, 0)
    //println("DELETE EXCESS ${virtuals.size} ${rsize-excessSize}..${rsize-1}")
    for (i in (rsize-excessSize)..(rsize-1)) {
        val child = rootView.getChildAt(i)
        // Child can be null when deleteExcessChildren has already run
        // for parent's children

        removeChild(rootView, child)
    }
}

fun deleteExcessChildrenRecursively(rootView: ViewGroup, virtuals: ArrayList<Virtual>) {
    deleteExcessChildren(rootView, virtuals)
    println("rotviw ${rootView} vsize ${virtuals.size} child ${rootView.childCount}")
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