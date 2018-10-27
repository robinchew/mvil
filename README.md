Summary
=======

A Android Kotlin "Virtual DOM". I wanted something like Mithril (https://mithril.js.org) and apply the Meiosis pattern (http://meiosis.js.org) for Android development.

Also inspired by Anvil (https://github.com/zserge/anvil). Hence the name.

So this abomination was born. PR welcomed.

Example
=======
```
typealias m = Virtual

fun attrs(vararg args: (View) -> Unit): ArrayList<(View) -> Unit> =
    arrayListOf(*args)

fun children(vararg args: Virtual): ArrayList<Virtual> = arrayListOf(*args)
```

```
fun myView(activity: Context): Virtual {
    return m(::LinearLayout,
        value.id,
        attrs(
            size(400, WRAP),
            backgroundColorHex("#009999"),
            elevation(1f),
            margin(50),
            onCrup {
                println("x of ${it.x}. Y of ${it.y}")
            },
            onGlobalLayout {view ->
                value.animation.updatePositionOfView(view)
                view.x = 400f
                value.animation.start()
            }
        ),
        children(
            m(::TextView,
                attrs(
                    size(WRAP, MATCH),
                    weight(1f),
                    layoutGravity(CENTER_VERTICAL),
                    padding(5),
                    text("${value.name} ${selectedTop} ${value.checked}"),
                    backgroundColorHex("#000066"),
                    onTouch {v, motionEvent ->
                        true
                    })),
            m(::CheckBox,
                attrs(
                    size(WRAP, WRAP),
                    margin(5),
                    layoutGravity(CENTER_VERTICAL),
                    focusable(false),
                    focusableInTouchMode(false),
                    clickable(false),
                    checked(value.checked),
                    onClick {
                        toggleCheck(value.id)
                    }))))
}
```

```
import mvil.*

class MyActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val renderable = RenderView(this) {LinearLayout(activity)}
        renderable.sync(myView(activity))
        setContentView(renderable)
    }
}
```

Improvements
============

If I was better at Kotlin, it would have looked more like:

```
// EXAMPLE ONLY
// CODE BELOW DOESN'T WORK

m(::LinearLayout, id=value.id) {
    size = 400 x WRAP,
    backgroundColorHex = "#009999"
    elevation = 1f
    margin = 50
    onCrup {
        println("x of ${it.x}. Y of ${it.y}")
    }
    onGlobalLayout { view ->
        value.animation.updatePositionOfView(view)
        view.x = 400f
        value.animation.start()
    }

    m(::TextView) {
        attrs {
            size = WRAP x MATCH
            weight = 1f
            layoutGravity = CENTER_VERTICAL
            padding = 5
            text = "${value.name} ${selectedTop} ${value.checked}"
            backgroundColorHex("#000066"),
            onTouch {v, motionEvent ->
                val view = v.parent as LinearLayout
                true
            }
        }
    }

    m(::CheckBox) {
        size = WRAP x WRAP
        margin = 5
        layoutGravity = CENTER_VERTICAL
        focusable = false
        focusableInTouchMode = false
        clickable = false
        checked = value.checked
        onClick {
            toggleCheck(value.id)
        }
    }
}
```

FAQ
===

Q: Is it fast?

A: Probably not. I'm new to Kotlin/Java development. PR welcomed.
