package net.consentmanager.kmm.cmpsdkdemoapp

import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.ui.geometry.Rect
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class GenericAppExplorerTest {

    data class SerializableRect(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    ) {
        companion object {
            fun fromComposeRect(rect: Rect): SerializableRect {
                return SerializableRect(
                    left = rect.left,
                    top = rect.top,
                    right = rect.right,
                    bottom = rect.bottom
                )
            }
        }
    }

    // Make our UIElementInfo serializable
    data class UIElementInfo(
        val elementType: String,
        val isClickable: Boolean,
        val bounds: SerializableRect,
        val contentDescription: String?,
        val text: String?,
        val viewId: Int    // Adding viewId will help with targeting specific elements
    )

    private val elementInteractions = mutableListOf<UIElementInfo>()
    private lateinit var activityScenario: ActivityScenario<*>

    @Before
    fun launchTargetApp() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val packageName = targetContext.packageName

        val packageManager = targetContext.packageManager

        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            // Add these flags to ensure a clean launch
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        if (intent == null) {
            throw IllegalStateException("Could not get launch intent for package: $packageName")
        }

        activityScenario = ActivityScenario.launch<Activity>(intent)
    }

    @Test
    fun exploreInitialScreen() {
        // Allow initial layout to stabilize
        SystemClock.sleep(2000)

        activityScenario.onActivity { activity ->
            val rootView = activity.window.decorView
            exploreViewHierarchy(rootView)
            logElementsInMemory()
        }
    }

    private fun exploreViewHierarchy(view: View) {
        if (isInteractiveElement(view)) {
            try {
                val location = IntArray(2)
                view.getLocationInWindow(location)

                val viewRect = Rect(
                    left = location[0].toFloat(),
                    top = location[1].toFloat(),
                    right = (location[0] + view.width).toFloat(),
                    bottom = (location[1] + view.height).toFloat()
                )

                val textContent = when (view) {
                    is TextView -> view.text?.toString()
                    else -> null
                }

                val elementInfo = UIElementInfo(
                    elementType = view.javaClass.simpleName,
                    isClickable = view.isClickable,
                    bounds = SerializableRect.fromComposeRect(viewRect),
                    contentDescription = view.contentDescription?.toString(),
                    text = textContent,
                    viewId = view.id  // Adding the new required viewId field
                )

                elementInteractions.add(elementInfo)

                if (view.isClickable && view.isShown) {
                    try {
                        onView(withId(view.id))
                            .check(matches(isDisplayed()))
                            .perform(click())

                        SystemClock.sleep(500)
                        pressBack()
                    } catch (e: Exception) {
                        Log.e("GenericAppExplorer",
                            "Failed to interact with element: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("GenericAppExplorer",
                    "Error processing view: ${e.message}")
            }
        }

        // Continue exploring child views
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                exploreViewHierarchy(view.getChildAt(i))
            }
        }
    }

    private fun isInteractiveElement(view: View): Boolean {
        return view.isClickable ||
                view is Button ||
                view is ImageButton ||
                (view is TextView && view.isClickable)
    }

    private fun logElementsInMemory() {
        try {
            Log.d("GenericAppExplorer", "Total UI elements found: ${elementInteractions.size}")

            elementInteractions.forEachIndexed { index, element ->
                Log.i("GenericAppExplorer", """
                Element #${index + 1}:
                Type: ${element.elementType}
                Clickable: ${element.isClickable}
                Bounds: (${element.bounds.left}, ${element.bounds.top}, ${element.bounds.right}, ${element.bounds.bottom})
                Content Description: ${element.contentDescription ?: "none"}
                Text: ${element.text ?: "none"}
                """.trimIndent())
            }

        } catch (e: Exception) {
            Log.e("GenericAppExplorer", "Failed to log elements: ${e.message}")
        }
    }

    @After
    fun cleanup() {
        activityScenario.close()
    }
}
