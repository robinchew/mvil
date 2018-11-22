package mvil

import android.content.res.ColorStateList
import android.os.Build
import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver

import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import java.lang.Exception

typealias ViewFunction = (View) -> Unit
typealias KeyViewFunction = Pair<String, ViewFunction>

private val cachedAttrValues: MutableMap<View, MutableMap<String, ArrayList<Any>>> = mutableMapOf()

private fun cache(view: View, attrName: String, args: ArrayList<Any>) {
    if (cachedAttrValues[view] == null) {
        cachedAttrValues[view] = mutableMapOf(attrName to args)
    }
    cachedAttrValues[view]!![attrName] = args
}

fun getViewCache(view: View, attrName: String): ArrayList<Any>? {
    val attrs = cachedAttrValues[view]
    if (attrs != null) {
        if (attrs[attrName] != null) {
            return attrs[attrName]
        }
    }
    return null
}

fun removeViewCache(view: View) {
    cachedAttrValues.remove(view)
}

fun getViewState(view: View, attr: String): ArrayList<Any>? {
    return when (attr) {
        "checked" -> arrayListOf((view as CheckBox).isChecked)
        else -> null
    }
}

private val attrsMap: Map<String, (ArrayList<Any>) -> ViewFunction> = mapOf(
    "onCreate" to {view: View, args: ArrayList<Any> ->
        val result = getViewCache(view, "created")
        val isCreated = result != null
        if (! isCreated) {
            (args[0] as (View) -> Unit)(view)
            cache(view, "created", arrayListOf())
        }
    },
    "onUpdate" to {view: View, args: ArrayList<Any> ->
        val result = getViewCache(view, "created")
        val isCreated = result != null
        if (isCreated) {
            (args[0] as (View) -> Unit)(view)
        }
    },
    "onCrup" to {view: View, args: ArrayList<Any> ->
        (args[0] as (View) -> Unit)(view)
    },
    "backgroundColorHex" to {view: View, args: ArrayList<Any> ->
        val hex = args[0] as String
        when (view) {
            is FloatingActionButton ->
                view.backgroundTintList = ColorStateList.valueOf(Color.parseColor(hex))
            else ->
                view.setBackgroundColor(Color.parseColor(hex))
        }
    },
    "checked" to {view: View, args: ArrayList<Any> ->
        (view as CompoundButton).isChecked = args[0] as Boolean
    },
    "clickable" to {view: View, args: ArrayList<Any> ->
        view.isClickable = args[0] as Boolean
    },
    "clipChildren" to {layout: View, args: ArrayList<Any> ->
        (layout as ViewGroup).clipChildren = args[0] as Boolean
    },
    "clipToPadding" to {view: View, args: ArrayList<Any> ->
        (view as ViewGroup).clipToPadding = args[0] as Boolean
    },
    "colorFilterHex" to {view: View, args: ArrayList<Any> ->
        (view as ImageButton).setColorFilter(Color.parseColor(args[0] as String))
    },
    "columnCount" to {layout: View, args: ArrayList<Any> ->
        (layout as GridLayout).columnCount = args[0] as Int
    },
    "columnAlignmentWeight" to {layout: View, args: ArrayList<Any> ->
        val params = layout.layoutParams as GridLayout.LayoutParams
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            params.columnSpec = GridLayout.spec(
                GridLayout.UNDEFINED,
                args[0] as GridLayout.Alignment,
                args[1] as Float)
        }
    },
    "cornerRadius" to {view: View, args: ArrayList<Any> ->
        // https://stackoverflow.com/a/19152013
        val paint = PaintDrawable()
        paint.setCornerRadius(args[0] as Float)
        view.setBackground(paint)
    },
    "elevation" to {view: View, args: ArrayList<Any> ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.elevation = args[0] as Float
        }
    },
    "focusable" to {view: View, args: ArrayList<Any> ->
        val value = args[0] as Boolean;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.focusable = if (value) {
                1
            } else {
                0
            }
        }
    },
    "focusableInTouchMode" to {view: View, args: ArrayList<Any> ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.isFocusableInTouchMode = args[0] as Boolean
        }
    },
    "imageResource" to { view: View, args: ArrayList<Any> ->
        when (view) {
            is FloatingActionButton ->
                view.setImageResource(args[0] as Int)
            is ImageButton ->
                view.setImageResource(args[0] as Int)
            else ->
                throw IllegalArgumentException("imageResource for ${view} is unsupported");
        }
    },
    "innerGravity" to {view: View, args: ArrayList<Any> ->
        when (view) {
            is TextView ->
                view.gravity = args[0] as Int
            is LinearLayout ->
                view.gravity = args[0] as Int
            else ->
                throw IllegalArgumentException("innerGravity for ${view} is unsupported");
        }
    },
    "margin" to  {v: View, args: ArrayList<Any> ->
        val (l, r, t, b) = args as ArrayList<Int>
        val p = v.getLayoutParams() as ViewGroup.MarginLayoutParams
        p.leftMargin = l
        p.rightMargin = r
        p.topMargin = t
        p.bottomMargin = b
        v.setLayoutParams(p)
    },
    "minHeight" to {view: View, args: ArrayList<Any> ->
        (view as TextView).minHeight = args[0] as Int
    },
    "padding" to {view: View, args: ArrayList<Any> ->
        val (l, r, t, b) = args as ArrayList<Int>
        view.setPadding(l, r, t, b)
    },
    "onClick" to {view: View, args: ArrayList<Any> ->
        val f = args[0] as (View) -> Unit
        view.setOnClickListener { a0 ->
            f(a0)
        }
    },
    "onGlobalLayout" to {view: View, args: ArrayList<Any> ->
        val key = "createdGlobalLayout"
        val result = getViewCache(view, key)
        val isCreated = result != null
        val f = (args [0]) as (View) -> Unit

        if (! isCreated) {
            view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    f(view)
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
            cache(view, key, arrayListOf())
        }
    },
    "onTouch" to {view: View, args: ArrayList<Any> ->
        val f = args[0] as (View, MotionEvent) -> Boolean
        view.setOnTouchListener {view, motionEvent ->
            f(view, motionEvent)
        }
    },
    "orientation" to {layout: View, args: ArrayList<Any> ->
        (layout as LinearLayout).orientation = args[0] as Int
    },
    "outerGravity" to {view: View, args: ArrayList<Any> ->
        val params = view.layoutParams
        when (params) {
            is CoordinatorLayout.LayoutParams ->
                params.gravity = args[0] as Int
            is LinearLayout.LayoutParams ->
                params.gravity = args[0] as Int
            is GridLayout.LayoutParams ->
                params.setGravity(args[0] as Int)
            else ->
                throw IllegalArgumentException("outerGravity for $params of ${view} is unsupported");
        }
    },
    "rowCount" to {layout: View, args: ArrayList<Any> ->
        (layout as GridLayout).rowCount = args[0] as Int
    },
    "rowFillWeight" to {layout: View, args: ArrayList<Any> ->
        val params = layout.layoutParams as GridLayout.LayoutParams
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            params.rowSpec = GridLayout.spec(
                GridLayout.UNDEFINED, GridLayout.FILL, args[0] as Float)
        }
    },
    "rule" to {view: View, args: ArrayList<Any> ->
        val params = view.layoutParams as RelativeLayout.LayoutParams
        params.addRule(args[0] as Int)
    },
    "size" to {v: View, args: ArrayList<Any> ->
        val (w, h) = args as ArrayList<Int>
        val p = v.getLayoutParams()
        p.width = w
        p.height = h
        v.setLayoutParams(p)
    },
    "tag" to {v: View, args: ArrayList<Any> ->
        v.tag = args[0] as String
    },
    "text" to {v: View, args: ArrayList<Any> ->
        (v as TextView).text = args[0] as String
    },
    "textColorHex" to {view: View, args: ArrayList<Any> ->
        val hex = args[0] as String
        (view as TextView).setTextColor(Color.parseColor(hex))
    },
    "textSize" to {v: View, args: ArrayList<Any> ->
        (v as TextView).textSize = args[0] as Float
    },
    "weight" to {view: View, args: ArrayList<Any> ->
        val params = view.layoutParams

        when (params) {
            is LinearLayout.LayoutParams ->
                params.weight = args[0] as Float
            // is GridLayout.LayoutParams ->
            //    params.weight = args[0] as Float
            else ->
                throw IllegalArgumentException("weight for ${params} of ${view} is unsupported");
        }
    },
    "z" to {view: View, args: ArrayList<Any> ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.z = args[0] as Float
        }
    }
).entries.associate {
    val f = it.value
    it.key to {args: ArrayList<Any> ->
        {view: View ->
            val lastArgs = getViewCache(view, it.key)
            if (lastArgs != args || (getViewState(view, it.key) ?: args) != args || listOf("onUpdate", "onCrup").contains(it.key)) {
                // This is where state is checked to determine if
                // an attribute update is necessary.
                // Not only is the current state is checked with the previous state,
                // for a view like CheckBox, the 'view attribute' state is checked with
                // the current state as well.
                f(view, args)
                cache(view, it.key, args)
            }
        }
    }
}

// weight constants
val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

fun attr(key: String, args: ArrayList<Any>): KeyViewFunction {
    val attrMap = attrsMap.get(key)
    if (attrMap != null) {
        return Pair(key, attrMap(args))
    }
    throw Exception("Mvil did not implement '${key}'. Please complain.")
}

// MVIL ATTRIBUTES

fun onCrup(f: (View) -> Unit): KeyViewFunction  {
    return attr("onCrup", arrayListOf(f))
}
fun onCreate(f: (View) -> Unit): KeyViewFunction  {
    return attr("onCreate", arrayListOf(f))
}
fun onUpdate(f: (View) -> Unit): KeyViewFunction  {
    return attr("onUpdate", arrayListOf(f))
}
// ANDROID ATTRIBUTES

fun backgroundColorHex(s: String): KeyViewFunction {
    return attr("backgroundColorHex", arrayListOf(s))
}
fun checked(b: Boolean): KeyViewFunction {
    return attr("checked", arrayListOf(b))
}
fun clickable(b: Boolean): KeyViewFunction {
    return attr("clickable", arrayListOf(b))
}
fun clipChildren(b: Boolean): KeyViewFunction {
    return attr("clipChildren", arrayListOf(b))
}
fun clipToPadding(b: Boolean): KeyViewFunction {
    return attr("clipToPadding", arrayListOf(b))
}
fun colorFilterHex(s: String): KeyViewFunction {
    // https://stackoverflow.com/a/11275373
    return attr("colorFilterHex", arrayListOf(s))
}
fun columnCount(i: Int): KeyViewFunction {
    return attr("columnCount", arrayListOf(i))
}
fun columnAlignmentWeight(alignment: GridLayout.Alignment, f: Float): KeyViewFunction {
    return attr("columnAlignmentWeight", arrayListOf(alignment, f))
}
fun cornerRadius(f: Float): KeyViewFunction {
    return attr("cornerRadius", arrayListOf(f))
}
fun elevation(v: Float): KeyViewFunction {
    return attr("elevation", arrayListOf(v))
}
fun focusable(b: Boolean): KeyViewFunction {
    return attr("focusable", arrayListOf(b))
}
fun focusableInTouchMode(b: Boolean): KeyViewFunction {
    return attr("focusableInTouchMode", arrayListOf(b))
}
fun imageResource(i: Int): KeyViewFunction {
    return attr("imageResource", arrayListOf(i))
}
fun innerGravity(i: Int): KeyViewFunction {
    return attr("innerGravity", arrayListOf(i))
}
fun outerGravity(i: Int): KeyViewFunction {
    return attr("outerGravity", arrayListOf(i))
}
fun margin(all: Int): KeyViewFunction {
    return attr("margin", arrayListOf(all, all, all, all))
}
fun margin(horizontal: Int, vertical: Int): KeyViewFunction {
    return attr("margin", arrayListOf(horizontal, vertical, horizontal, vertical))
}
fun margin(l: Int, r: Int, t: Int, b: Int): KeyViewFunction {
    return attr("margin", arrayListOf(l, r, t, b))
}
fun onClick(f: (View) -> Unit): KeyViewFunction {
    return attr("onClick", arrayListOf(f))
}
fun onGlobalLayout(f: (View) -> Unit): KeyViewFunction {
    return attr("onGlobalLayout", arrayListOf(f))
}
fun onTouch(f: (View, MotionEvent) -> Boolean): KeyViewFunction {
    return attr("onTouch", arrayListOf(f))
}
fun padding(l: Int, r: Int, t: Int, b: Int): KeyViewFunction {
    return attr("padding", arrayListOf(l, r, t, b))
}
fun padding(horizontal: Int, vertical: Int): KeyViewFunction {
    return attr("padding", arrayListOf(horizontal, vertical, horizontal, vertical))
}
fun padding(all: Int): KeyViewFunction {
    return attr("padding", arrayListOf(all, all, all, all))
}
fun orientation(i: Int): KeyViewFunction {
    return attr("orientation", arrayListOf(i))
}
fun rowCount(i: Int): KeyViewFunction {
    return attr("rowCount", arrayListOf(i))
}
fun rowFillWeight(f: Float): KeyViewFunction {
    return attr("rowFillWeight", arrayListOf(f))
}
fun rule(i: Int): KeyViewFunction {
    return attr("rule", arrayListOf(i))
}
fun size(w: Int, h: Int): KeyViewFunction {
    return attr("size", arrayListOf(w, h))
}
fun tag(s: String): KeyViewFunction {
    return attr("tag", arrayListOf(s))
}
fun text(s: String): KeyViewFunction {
    return attr("text", arrayListOf(s))
}
fun textColorHex(s: String): KeyViewFunction {
    return attr("textColorHex", arrayListOf(s))
}
fun textSize(f: Float): KeyViewFunction {
    return attr("textSize", arrayListOf(f))
}
fun weight(v: Float): KeyViewFunction {
    return attr("weight", arrayListOf(v))
}
fun z(v: Float): KeyViewFunction {
    return attr("z", arrayListOf(v))
}
