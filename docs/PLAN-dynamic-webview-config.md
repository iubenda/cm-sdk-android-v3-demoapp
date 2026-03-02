# Plan: Dynamic CMP Webview Configuration (Android)

## Overview

Add an initial configuration screen to the Android demo app that collects Code-ID, language, position, background style, and other webview options. After the user enters all info, the CMP SDK is loaded with the dynamic configuration. Mirrors the iOS implementation.

## Target Flow

```
App Launch → ConfigurationScreen (user enters config) → Consent Layer → CMPDemoScreen
```

## Implementation Tasks

### 1. Create CMPConfiguration data class
- Kotlin data class with: codeId, language, appName, domain, position, backgroundStyle, cornerRadius, respectsSafeArea, isCancelable
- Validation: isValid, validationError (Code-ID, domain, app name - same rules as iOS)
- toUrlConfig(), toConsentLayerUIConfig() mapping to SDK types
- Enums: WebviewPosition (FULL_SCREEN, HALF_SCREEN_TOP, HALF_SCREEN_BOTTOM), WebviewBackgroundStyle (BLUR_PROMINENT, DIMMED_BLACK, DIMMED_GREY)

### 2. Create ConfigurationScreen composable
- Form with CMP Settings (Code-ID, Language, App Name, Domain) and Webview Appearance sections
- "Load CMP" button, validation alert when invalid
- contentDescription for accessibility

### 3. Refactor MainActivity
- Defer CMPManager creation until config is available
- State: hasConfiguration, isConsentInitialized
- Flow: if !hasConfiguration → ConfigurationScreen; else if !isConsentInitialized → init CMP + checkAndOpen; else → CMPDemoScreen
- Skip-config: check Intent extras or BuildConfig for UI tests (e.g. --skip-config via instrumentation)

### 4. Security / Code review parity
- #if DEBUG / BuildConfig.DEBUG for sensitive logging (consent strings, SharedPreferences dump)
- CMP string toast truncation (50 chars)
- Defensive validation before applying config

## File Changes

| File | Action |
|------|--------|
| `CMPConfiguration.kt` | Create |
| `ConfigurationScreen.kt` | Create |
| `MainActivity.kt` | Modify - flow, defer CMP init |
| `CMPDemoScreen.kt` | Modify - truncate CMP string toast, guard logging |

## SDK API (from existing code)

- UrlConfig(id, domain, language, appName)
- ConsentLayerUIConfig(position, backgroundStyle, cornerRadius, respectsSafeArea, isCancelable)
- Position: FULL_SCREEN, HALF_SCREEN_TOP, HALF_SCREEN_BOTTOM
- BackgroundStyle: dimmed(Color, Float) - check SDK for blur
