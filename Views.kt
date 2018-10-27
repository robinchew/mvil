package mvil

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.max

fun realiseView(activity: Context, parent: ViewGroup, virtual: Virtual): View {
    val view = virtual.cls(activity)
    parent.addView(view)
    return view
}

fun removeChild(parentView: ViewGroup, childView: View) {
    parentView.removeView(childView)
    removeViewCache(childView)
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

    for (i in (rsize-excessSize)..(rsize-1)) {
        removeChild(rootView, rootView.getChildAt(i))
    }
}

class RenderView(val activity: Activity): FrameLayout(activity) {
    fun sync(virtual: Virtual) {
        render(activity, this, arrayListOf(virtual), "")
    }
}