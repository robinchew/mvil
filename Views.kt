package mvil

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.max
import kotlin.math.min

typealias RealisedView = View

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

fun orderAndCullViews(activity: Context, parentView: ViewGroup,
                      virtualChildren: List<Virtual>): List<View?> {
    val childrenHaveTags = virtualChildren.all {it.tag != ""}
    if (childrenHaveTags) {
        val allChildren = getChildren(parentView)
        val taggedChildren = virtualChildren.mapIndexed { i, vChild ->
            val foundChild = allChildren.find { rChild ->
                vChild.tag != "" && vChild.tag == rChild?.tag
            }
            foundChild
        }
        allChildren.filter {! taggedChildren.contains(it)}.forEach {
            // This is where child views are removed when tagged views
            // disappear.
            removeChild(it!!)
        }
        return taggedChildren
    }
    // When there are NO tags:
    // - excess child views are removed
    // - views are deleted if virtual and real class do not match
    deleteExcessChildren(parentView, virtualChildren)
    deleteUnmatchedRealAndVirtualClass(parentView, virtualChildren.map {
        it.cls(activity)
    })

    return getChildren(parentView, virtualChildren.size)
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

fun deleteUnmatchedRealAndVirtualClass(realParent: ViewGroup,
                                       virtualChildren: List<RealisedView>) {
    getChildren(realParent).zip(virtualChildren).forEach {(real, realised) ->
        if (real::class != realised::class) {
            removeChild(realParent, real)
        }
    }
}

fun deleteExcessChildren(rootView: ViewGroup, virtuals: List<Virtual>) {
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

class RenderView(val activity: Activity): FrameLayout(activity) {
    fun sync(virtual: Virtual) {
        render(activity, this, arrayListOf(virtual))
    }
}

class StatefulImageView(val activity: Context): ImageView(activity) {
    var layoutBottom: Int = 0
    var layoutRight: Int = 0

    init {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layoutBottom = bottom
                layoutRight = right
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }
}