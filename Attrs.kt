package mvil

import android.os.Build
import android.graphics.Color
import android.view.*
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.backgroundColor
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
    "orientation" to {layout: View, args: ArrayList<Any> ->
        (layout as LinearLayout).orientation = args[0] as Int
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
        //view.grav = args[0] as Int
        (view as LinearLayout).gravity = args[0] as Int
    },
    "outerGravity" to {view: View, args: ArrayList<Any> ->
        val params = view.layoutParams as LinearLayout.LayoutParams
        params.gravity = args[0] as Int
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
    "weight" to {view: View, args: ArrayList<Any> ->
        val params = view.layoutParams as LinearLayout.LayoutParams
        params.weight = args[0] as Float
    },
    "z" to {view: View, args: ArrayList<Any> ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.z = args[0] as Float
        }
    },
    "backgroundColorHex" to {view: View, args: ArrayList<Any> ->
        val hex = args[0] as String
        view.backgroundColor = Color.parseColor(hex)
    }
).entries.associate {
    val f = it.value
    it.key to {args: ArrayList<Any> ->
        {view: View ->
            val lastArgs = getViewCache(view, it.key)
            if (lastArgs != args) {
                f(view, args)
                cache(view, it.key, args)
                // println("change ${it.key} $lastArgs -> ${args}")
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
fun onClick(f: AttrSetter): AttrSetter {
    return attr("onClick")(arrayListOf(f))
}
fun onGlobalLayout(f: AttrSetter): AttrSetter {
    return attr("onGlobalLayout")(arrayListOf(f))
}
fun onTouch(f: (View, MotionEvent) -> Boolean): AttrSetter {
    return attr("onTouch")(arrayListOf(f))
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
fun size(w: Int, h: Int): AttrSetter {
    return attr("size")(arrayListOf(w, h))
}
fun tag(s: String): AttrSetter {
    return attr("tag")(arrayListOf(s))
}
fun text(s: String): AttrSetter {
    return attr("text")(arrayListOf(s))
}
fun weight(v: Float): AttrSetter {
    return attr("weight")(arrayListOf(v))
}
fun z(v: Float): AttrSetter {
    return attr("z")(arrayListOf(v))
}
