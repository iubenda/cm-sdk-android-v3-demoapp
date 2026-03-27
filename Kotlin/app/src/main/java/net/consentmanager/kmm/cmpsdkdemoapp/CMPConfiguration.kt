package net.consentmanager.kmm.cmpsdkdemoapp

import android.graphics.Color
import net.consentmanager.cm_sdk_android_v3.ConsentLayerUIConfig
import net.consentmanager.cm_sdk_android_v3.UrlConfig

/**
 * User-facing configuration for the CMP SDK webview.
 */
data class CMPConfiguration(
    val codeId: String,
    val language: String,
    val appName: String,
    val domain: String,
    val position: WebviewPosition,
    val backgroundStyle: WebviewBackgroundStyle,
    val cornerRadius: Float,
    val respectsSafeArea: Boolean,
    val isCancelable: Boolean,
    val allowsOrientationChanges: Boolean
) {
    private val trimmedCodeId: String get() = codeId.trim()

    val isValid: Boolean get() = validationError == null

    val validationError: String?
        get() {
            if (trimmedCodeId.isEmpty()) return "Code-ID is required"
            if (!isValidCodeId(trimmedCodeId)) return "Code-ID must be alphanumeric with hyphens only, max 64 characters"
            if (!isValidDomain(domain)) return "Domain must be a valid hostname (letters, numbers, dots, hyphens only)"
            if (!isValidAppName(appName)) return "App Name must be alphanumeric with spaces/hyphens/underscores, max 128 characters"
            return null
        }

    fun toUrlConfig(): UrlConfig = UrlConfig(
        id = trimmedCodeId,
        domain = domain.trim(),
        language = language,
        appName = appName.trim(),
        noHash = true,
        webViewConnectionTimeoutMillis = 10000L
    )

    fun toConsentLayerUIConfig(): ConsentLayerUIConfig = ConsentLayerUIConfig(
        position = position.toSDKPosition(),
        backgroundStyle = backgroundStyle.toSDKBackgroundStyle(),
        cornerRadius = cornerRadius,
        respectsSafeArea = respectsSafeArea,
        isCancelable = isCancelable,
        allowsOrientationChanges = allowsOrientationChanges
    )

    companion object {
        val default = CMPConfiguration(
            codeId = "f5e3b73592c3c",
            language = "EN",
            appName = "CMDemoAppKotlin",
            domain = "delivery.consentmanager.net",
            position = WebviewPosition.FULL_SCREEN,
            backgroundStyle = WebviewBackgroundStyle.DIMMED_BLACK,
            cornerRadius = 0f,
            respectsSafeArea = false,
            isCancelable = false,
            allowsOrientationChanges = true
        )

        private fun isValidCodeId(value: String): Boolean {
            if (value.length > 64) return false
            return value.all { it.isLetterOrDigit() || it == '-' }
        }

        private fun isValidDomain(value: String): Boolean {
            val trimmed = value.trim()
            if (trimmed.isEmpty() || trimmed.length > 253) return false
            val lower = trimmed.lowercase()
            if (lower.startsWith("javascript:") || lower.startsWith("data:") ||
                lower.startsWith("vbscript:") || lower.startsWith("file:") ||
                lower.startsWith("http://") || lower.startsWith("https://")
            ) return false
            if (!lower.all { it in "abcdefghijklmnopqrstuvwxyz0123456789.-" }) return false
            return !trimmed.any { it in "@/?#%\u0000" }
        }

        private fun isValidAppName(value: String): Boolean {
            val trimmed = value.trim()
            if (trimmed.length > 128) return false
            return trimmed.all { it.isLetterOrDigit() || it in " -_" }
        }
    }
}

enum class WebviewPosition(val label: String) {
    FULL_SCREEN("Full Screen"),
    HALF_SCREEN_TOP("Half Screen (Top)"),
    HALF_SCREEN_BOTTOM("Half Screen (Bottom)")
}

enum class WebviewBackgroundStyle(val label: String) {
    DIMMED_BLACK("Dimmed (Black 50%)"),
    DIMMED_GREY("Dimmed (Grey 75%)"),
    DIMMED_LIGHT("Dimmed (Light 30%)")
}

private fun WebviewPosition.toSDKPosition(): ConsentLayerUIConfig.Position = when (this) {
    WebviewPosition.FULL_SCREEN -> ConsentLayerUIConfig.Position.FULL_SCREEN
    WebviewPosition.HALF_SCREEN_TOP -> ConsentLayerUIConfig.Position.HALF_SCREEN_TOP
    WebviewPosition.HALF_SCREEN_BOTTOM -> ConsentLayerUIConfig.Position.HALF_SCREEN_BOTTOM
}

private fun WebviewBackgroundStyle.toSDKBackgroundStyle(): ConsentLayerUIConfig.BackgroundStyle = when (this) {
    WebviewBackgroundStyle.DIMMED_BLACK -> ConsentLayerUIConfig.BackgroundStyle.dimmed(Color.BLACK, 0.5f)
    WebviewBackgroundStyle.DIMMED_GREY -> ConsentLayerUIConfig.BackgroundStyle.dimmed(Color.GRAY, 0.75f)
    WebviewBackgroundStyle.DIMMED_LIGHT -> ConsentLayerUIConfig.BackgroundStyle.dimmed(Color.LTGRAY, 0.3f)
}
