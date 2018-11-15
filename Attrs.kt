package mvil

import android.content.res.ColorStateList
import android.os.Build
import android.graphics.Color
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton

import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver

import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.colorAttr
import org.jetbrains.anko.textColor
import java.lang.Exception

typealias AttrSetter = (View) -> Unit

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

private val attrs: Map<String, (ArrayList<Any>) -> AttrSetter> = mapOf(
    "onCreate" to {view: View, args: ArrayList<Any> ->
        val result = getViewCache(view, "created")
        val isCreated = result != null
        if (! isCreated) {
            (args[0] as (View) -> Unit)(view)
            cache(view, "created", arrayListOf())
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
                view.backgroundColor = Color.parseColor(hex)
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
        (view as TextView).textColor = Color.parseColor(hex)
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
            if (lastArgs != args || (getViewState(view, it.key) ?: args) != args) {
                // This is where state is checked to determine if
                // an attribute update is necessary.
                // Not only is the current state is checked with the previous state,
                // for a view like CheckBox, the view attribute state is checked with
                // the current state as well.
                f(view, args)
                cache(view, it.key, args)
            }
        }
    }
}
// MVIL ATTRIBUTES

fun onCrup(f: (View) -> Unit): AttrSetter  {
    return attr("onCrup")(arrayListOf(f))
}
fun onCreate(f: (View) -> Unit): AttrSetter  {
    return attr("onCreate")(arrayListOf(f))
}

// ANDROID ATTRIBUTES

// weight constants
val FILL = ViewGroup.LayoutParams.MATCH_PARENT
val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

// gravity constants
val TOP = Gravity.TOP
val BOTTOM = Gravity.BOTTOM
val LEFT = Gravity.LEFT
val RIGHT = Gravity.RIGHT
val CENTER_VERTICAL = Gravity.CENTER_VERTICAL
val GROW_VERTICAL = Gravity.FILL_VERTICAL
val CENTER_HORIZONTAL = Gravity.CENTER_HORIZONTAL
val GROW_HORIZONTAL = Gravity.FILL_HORIZONTAL
val CENTER = CENTER_VERTICAL or CENTER_HORIZONTAL
val GROW = GROW_VERTICAL or GROW_HORIZONTAL
val CLIP_VERTICAL = Gravity.CLIP_VERTICAL
val CLIP_HORIZONTAL = Gravity.CLIP_HORIZONTAL
val START = Gravity.START
val END = Gravity.END

fun attr(key: String): (ArrayList<Any>) -> AttrSetter {
    val f = attrs.get(key)
    if (f != null) {
        return f
    }
    throw Exception("Mvil did not implement '${key}'. Please complain.")
}
fun backgroundColorHex(s: String): AttrSetter {
    return attr("backgroundColorHex")(arrayListOf(s))
}
fun checked(b: Boolean): AttrSetter {
    return attr("checked")(arrayListOf(b))
}
fun clickable(b: Boolean): AttrSetter {
    return attr("clickable")(arrayListOf(b))
}
fun clipChildren(b: Boolean): AttrSetter {
    return attr("clipChildren")(arrayListOf(b))
}
fun clipToPadding(b: Boolean): AttrSetter {
    return attr("clipToPadding")(arrayListOf(b))
}
fun columnCount(i: Int): AttrSetter {
    return attr("columnCount")(arrayListOf(i))
}
fun columnAlignmentWeight(alignment: GridLayout.Alignment, f: Float): AttrSetter {
    return attr("columnAlignmentWeight")(arrayListOf(alignment, f))
}
fun elevation(v: Float): AttrSetter {
    return attr("elevation")(arrayListOf(v))
}
fun focusable(b: Boolean): AttrSetter {
    return attr("focusable")(arrayListOf(b))
}
fun focusableInTouchMode(b: Boolean): AttrSetter {
    return attr("focusableInTouchMode")(arrayListOf(b))
}
fun innerGravity(i: Int): AttrSetter {
    return attr("innerGravity")(arrayListOf(i))
}
fun outerGravity(i: Int): AttrSetter {
    return attr("outerGravity")(arrayListOf(i))
}
fun margin(all: Int): AttrSetter {
    return attr("margin")(arrayListOf(all, all, all, all))
}
fun margin(horizontal: Int, vertical: Int): AttrSetter {
    return attr("margin")(arrayListOf(horizontal, vertical, horizontal, vertical))
}
fun margin(l: Int, r: Int, t: Int, b: Int): AttrSetter {
    return attr("margin")(arrayListOf(l, r, t, b))
}
fun onClick(f: AttrSetter): AttrSetter {
    return attr("onClick")(arrayListOf(f))
}
fun onGlobalLayout(f: AttrSetter): AttrSetter {
    return attr("onGlobalLayout")(arrayListOf(f))
}
fun onTouch(f: (View, MotionEvent) -> Boolean): AttrSetter {
    return attr("onTouch")(arrayListOf(f))
}
fun padding(l: Int, r: Int, t: Int, b: Int): AttrSetter {
    return attr("padding")(arrayListOf(l, r, t, b))
}
fun padding(horizontal: Int, vertical: Int): AttrSetter {
    return attr("padding")(arrayListOf(horizontal, vertical, horizontal, vertical))
}
fun padding(all: Int): AttrSetter {
    return attr("padding")(arrayListOf(all, all, all, all))
}
fun orientation(i: Int): AttrSetter {
    return attr("orientation")(arrayListOf(i))
}
fun rowCount(i: Int): AttrSetter {
    return attr("rowCount")(arrayListOf(i))
}
fun rowFillWeight(f: Float): AttrSetter {
    return attr("rowFillWeight")(arrayListOf(f))
}
fun size(w: Int, h: Int): AttrSetter {
    return attr("size")(arrayListOf(w, h))
}
fun tag(s: String): AttrSetter {
    return attr("tag")(arrayListOf(s))
}
fun text(s: String): AttrSetter {
    return attr("text")(arrayListOf(s))
}
fun textColorHex(s: String): AttrSetter {
    return attr("textColorHex")(arrayListOf(s))
}
fun textSize(f: Float): AttrSetter {
    return attr("textSize")(arrayListOf(f))
}
fun weight(v: Float): AttrSetter {
    return attr("weight")(arrayListOf(v))
}
fun z(v: Float): AttrSetter {
    return attr("z")(arrayListOf(v))
}
