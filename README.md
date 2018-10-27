Summary
=======

A Android Kotlin "Virtual DOM". I wanted something like Mithril (https://mithril.js.org) and apply the Meiosis pattern (http://meiosis.js.org) for Android development.

Also inspired by Anvil (https://github.com/zserge/anvil). Hence the name.

So this abomination was born. PR welcomed.

Buzzwords:
----------
- SPA (Single Page Application)
- Single Activity Architecture
- Functional Reactive Programming (https://github.com/paldepind/flyd)

Example
=======
```kotlin
typealias m = Virtual

fun attrs(vararg args: (View) -> Unit): ArrayList<(View) -> Unit> =
    arrayListOf(*args)

fun children(vararg args: Virtual): ArrayList<Virtual> = arrayListOf(*args)
```

```kotlin
data class MyState(val id: String, val name: String, val checked: Boolean)
```

```kotlin
fun myView(activity: Context, value: MyState): Virtual {
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
                // animation.updatePositionOfView(view)
                view.x = 400f
                // animation.start()
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

Static View
-----------

```kotlin
import mvil.*

class MyActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val renderable = RenderView(this)
        renderable.sync(myView(activity, MyState("id123", "My Title", true)))
        setContentView(renderable)
    }
}
```

View update on state change
---------------------------
What's missing in the following example is a function that causes the state change.

```kotlin
import io.reactivex.subjects.PublishSubject
import mvil.*

class MyActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val renderable = RenderView(this)
        renderable.sync()

        val subject = PublishSubject.create<(MyState) -> MyState>();

        subject.scan(
            myView(activity, MyState("id123", "My Title", true)),
            {old, new -> new(old)}
        ).subscribe {
            renderable.sync(myView(activity, it))
        }
        setContentView(renderable)
    }
}
```

Improvements
============

If I was better at using Kotlin features like:

- https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/to.html
- https://kotlinlang.org/docs/reference/properties.html
- https://kotlinlang.org/docs/reference/type-safe-builders.html

It would have looked more like (credit to @isiahmeadows for the advice):

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

A: Probably not, PR welcomed. I was more focused on laying the foundations for functional and declarative style of programming for Android.
