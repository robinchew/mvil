package mvil

import android.content.Context
import android.view.View
import android.view.ViewGroup
import kotlin.math.max

fun realiseView(activity: Context, parent: ViewGroup, virtual: Virtual): View {
    val view = virtual.cls(activity)
    parent.addView(view)
    return view
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
        rootView.removeView(rootView.getChildAt(i))
    }
}