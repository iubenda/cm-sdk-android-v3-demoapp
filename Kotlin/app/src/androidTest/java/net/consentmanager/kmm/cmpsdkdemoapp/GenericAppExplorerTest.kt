import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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

    companion object {
        private const val TEST_DURATION_MS = 60000 // 1 minute
        private const val STABILITY_CHECK_INTERVAL_MS = 500
        private const val INTERACTION_DELAY_MS = 5000
    }

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
        val viewId: Int
    )

    private val elementInteractions = mutableListOf<UIElementInfo>()
    private lateinit var activityScenario: ActivityScenario<*>

    private enum class ScreenType {
        WEBVIEW, DIALOG, NATIVE
    }

    private data class ApplicationState(
        var isWebViewLoading: Boolean = false,
        var isDialogShowing: Boolean = false,
        var isActivityChanging: Boolean = false,
        var lastWebViewUrl: String? = null,
        var currentActivityName: String? = null,
        var loadingStartTime: Long = System.currentTimeMillis()
    )

    private val applicationState = ApplicationState()
    private val stateChangeListeners = mutableListOf<StateChangeListener>()
    private var currentScreenType: ScreenType = ScreenType.NATIVE

    // Interface to monitor state changes
    private interface StateChangeListener {
        fun onStateChanged(oldState: ApplicationState, newState: ApplicationState)
    }

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
        setupStateMonitoring()
        waitForApplicationStability()
        exploreCurrentApplicationState()
    }

    private fun setupStateMonitoring() {
        activityScenario.onActivity { activity ->
            // Monitor Activity lifecycle changes
            (activity as LifecycleOwner).lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    applicationState.isActivityChanging = true
                    applicationState.currentActivityName = owner.javaClass.simpleName
                    notifyStateChange()
                }

                override fun onResume(owner: LifecycleOwner) {
                    applicationState.isActivityChanging = false
                    notifyStateChange()
                }
            })

            // Monitor WebView loading states
            findWebViews(activity).forEach { webView ->
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        applicationState.isWebViewLoading = true
                        applicationState.lastWebViewUrl = url
                        notifyStateChange()
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        applicationState.isWebViewLoading = false
                        notifyStateChange()
                    }
                }
            }

            // Monitor Dialog visibility
            activity.application.registerActivityLifecycleCallbacks(
                object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                    override fun onActivityStarted(activity: Activity) {}
                    override fun onActivityResumed(activity: Activity) {
                        checkForDialogs(activity)
                    }
                    override fun onActivityPaused(activity: Activity) {}
                    override fun onActivityStopped(activity: Activity) {}
                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                    override fun onActivityDestroyed(activity: Activity) {}
                }
            )
        }
    }

    private fun notifyStateChange() {
        val oldState = applicationState.copy()
        stateChangeListeners.forEach { listener ->
            listener.onStateChanged(oldState, applicationState)
        }
    }

    private fun checkForDialogs(activity: Activity) {
        applicationState.isDialogShowing = findDialogs(activity).isNotEmpty()
        notifyStateChange()
    }

    private fun waitForApplicationStability() {
        val startTime = System.currentTimeMillis()
        var isStable = false

        while (!isStable && (System.currentTimeMillis() - startTime < 60000)) {
            runCatching {
                activityScenario.onActivity {
                    isStable = !applicationState.isWebViewLoading &&
                            !applicationState.isActivityChanging

                    Log.d("AppExplorer", """
                        Stability Check:
                        Activity: ${applicationState.currentActivityName}
                        WebView Loading: ${applicationState.isWebViewLoading}
                        Activity Changing: ${applicationState.isActivityChanging}
                        Dialog Showing: ${applicationState.isDialogShowing}
                        Last URL: ${applicationState.lastWebViewUrl}
                        Time since last change: ${System.currentTimeMillis() - startTime}ms
                    """.trimIndent())
                }
            }

            if (!isStable) {
                SystemClock.sleep(500)
            }
        }

        determineScreenType()
    }

    private fun determineScreenType() {
        activityScenario.onActivity { activity ->
            currentScreenType = when {
                findWebViews(activity).isNotEmpty() -> {
                    Log.i("AppExplorer", "Screen contains WebView with URL: ${applicationState.lastWebViewUrl}")
                    ScreenType.WEBVIEW
                }
                findDialogs(activity).isNotEmpty() -> {
                    Log.i("AppExplorer", "Screen contains Dialog")
                    ScreenType.DIALOG
                }
                else -> {
                    Log.i("AppExplorer", "Screen is native Activity: ${activity.javaClass.simpleName}")
                    ScreenType.NATIVE
                }
            }
        }
    }

    private fun exploreCurrentApplicationState() {
        val endTime = System.currentTimeMillis() + 60000

        while (System.currentTimeMillis() < endTime) {
            activityScenario.onActivity { activity ->
                when (currentScreenType) {
                    ScreenType.WEBVIEW -> exploreWebViewContent(activity)
                    ScreenType.DIALOG -> exploreDialogContent(activity)
                    ScreenType.NATIVE -> exploreViewHierarchy(activity.window.decorView)
                }
            }
        }
        SystemClock.sleep(5000)
        determineScreenType()
    }

    private fun exploreWebViewContent(activity: Activity) {
        findWebViews(activity).forEach { webView ->
            webView.evaluateJavascript(
                """
                (function() {
                    const elements = document.querySelectorAll('*');
                    return Array.from(elements).map(el => ({
                        tagName: el.tagName,
                        id: el.id,
                        className: el.className,
                        text: el.textContent,
                        isClickable: (el.tagName === 'BUTTON' || el.tagName === 'A' || el.onclick != null),
                        rect: el.getBoundingClientRect()
                    }));
                })();
                """.trimIndent()
            ) { result ->
                Log.d("AppExplorer", "WebView elements: $result")
                // Process WebView elements here
            }
        }
    }

    private fun exploreDialogContent(activity: Activity) {
        findDialogs(activity).forEach { dialog ->
            exploreViewHierarchy(dialog.window?.decorView ?: return@forEach)
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

    private fun findWebViews(root: Activity): List<WebView> {
        return findWebViewsInViewHierarchy(root.window.decorView)
    }

    private fun findWebViewsInViewHierarchy(view: View): List<WebView> {
        val webViews = mutableListOf<WebView>()

        if (view is WebView) {
            webViews.add(view)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                webViews.addAll(findWebViewsInViewHierarchy(view.getChildAt(i)))
            }
        }

        return webViews
    }

    private fun findDialogs(activity: Activity): List<Dialog> {
        val dialogs = mutableListOf<Dialog>()
        // Using reflection to find dialog fields in the activity
        activity.javaClass.declaredFields
            .filter { field -> field.type == Dialog::class.java }
            .forEach { field ->
                field.isAccessible = true
                try {
                    (field.get(activity) as? Dialog)?.let { dialog ->
                        if (dialog.isShowing) {
                            dialogs.add(dialog)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GenericAppExplorer", "Error accessing dialog field: ${e.message}")
                }
            }
        return dialogs
    }

    private fun findViewsByType(root: View, type: Class<*>): List<View> {
        val views = mutableListOf<View>()
        if (type.isInstance(root)) {
            views.add(root)
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                views.addAll(findViewsByType(root.getChildAt(i), type))
            }
        }
        return views
    }
    @After
    fun cleanup() {
        activityScenario.close()
    }
}
