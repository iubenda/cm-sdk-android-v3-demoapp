---
title: Burger menu debug window with TC-string test actions
date: 2026-07-29
artifact_contract: ce-unified-plan/v1
artifact_readiness: implementation-ready
product_contract_source: ce-plan-bootstrap
execution: code
---

# Burger menu debug window with TC-string test actions

## Goal Capsule

Add a burger (overflow) menu on the Kotlin Compose demo shell that opens a dedicated Debug screen.
That screen ports the debug/test capabilities from the sibling Flutter mini app at `../cm-sdk-ios-v3-demoapp/docs/cm-tcstring-tests` (read-only reference; do not modify that project).

Authority: user request via LFG; behavioral source of truth is the mini app's Android native IAB helpers plus its Flutter action buttons.
Stop when Demo/Home gains menu → Debug navigation and Debug exposes the mini-app actions with equivalent logging into the existing Logs tab.

## Product Contract

### Summary

Demo engineers need the TC-string corruption / AdMob interstitial / prefs dump tooling that already exists in `cm-tcstring-tests`, without leaving this demo app.
Ship a burger menu entry that opens a Debug window hosting those actions, wired to the same SharedPreferences file the CMP SDK uses (`${packageName}_preferences`).

### Requirements

- R1. After CMP is initialized, the demo shell exposes a burger/overflow menu control.
- R2. The menu includes an item that navigates to a Debug window/screen.
- R3. Debug window provides the mini-app test actions with equivalent behavior:
  - Open consentmanager CMP (`checkAndOpen` via existing `CMPManager`)
  - Load and show Google demo interstitial ad (Android test unit id)
  - Corrupt TC String and CMP ID in SharedPreferences (`IABTCF_TCString` → `+=========---------.......`, `IABTCF_CmpSdkID` → `999999`)
  - Dump prefs to Logcat (and also to the in-app Logs tab)
- R4. Action outcomes and errors append to the existing demo log stream (Home/Logs tab), matching the demo's current logging pattern.
- R5. Busy/disabled button state while an action runs, matching the mini app's per-button busy flags.
- R6. Scope is the Kotlin Compose demo app under `Kotlin/`; Java demo tree is out of scope.

### Actors

- A1. Demo engineer / QA using the Android demo APK
- A2. CMP SDK (`CMPManager`) already initialized by `MainActivity`

### Key Flows

- F1. Open Debug from burger menu
  - **Trigger:** A1 taps burger control on demo shell, then "Debug"
  - **Actors:** A1
  - **Steps:** Menu opens → Debug selected → Debug screen shown → Home/Logs tabs remain reachable or return via back
  - **Covered by:** R1, R2
- F2. Corrupt IAB storage then inspect
  - **Trigger:** A1 taps "Corrupt TC String and CMP ID", optionally then "Dump prefs"
  - **Actors:** A1, A2 (indirect via later CMP calls)
  - **Steps:** Write corrupted keys to `${packageName}_preferences` → log before/after → dump lists keys/types/values
  - **Covered by:** R3, R4, R5
- F3. Load demo interstitial
  - **Trigger:** A1 taps "Load and show demo ad"
  - **Actors:** A1
  - **Steps:** Initialize Mobile Ads SDK → load Android test interstitial → show → log callbacks → clear busy
  - **Covered by:** R3, R4, R5

### Acceptance Examples

- AE1. Burger → Debug
  - **Covers:** R1, R2
  - **Given:** Demo past configuration, Home visible
  - **When:** User opens burger menu and chooses Debug
  - **Then:** Debug screen with the action buttons is visible
- AE2. Corrupt writes
  - **Covers:** R3, R4
  - **Given:** Debug screen open; prefs may or may not already hold IAB keys
  - **When:** User taps Corrupt
  - **Then:** Logs show before/after values; prefs contain the corrupted string/int values
- AE3. Dump lists prefs
  - **Covers:** R3, R4
  - **Given:** Debug screen open
  - **When:** User taps Dump prefs
  - **Then:** Each key/value/type appears in Logcat and the Logs tab
- AE4. Demo ad path
  - **Covers:** R3, R4, R5
  - **Given:** Device/emulator with Play services; network available
  - **When:** User taps Load and show demo ad
  - **Then:** Button disables while busy; load/show callbacks log; button re-enables when finished or failed

### Scope Boundaries

- In scope: Kotlin Compose demo (`Kotlin/app/...`), AdMob dependency + APPLICATION_ID meta-data, Debug UI, IAB prefs helpers, unit tests for prefs corruption/dump helpers, navigation/menu wiring.
- Out of scope: Java demo app; Flutter mini app edits; changing CMP SDK library code; Firebase wiring beyond what already exists; iOS parity work in this repo.
- Deferred: matching mini-app CMP code id / domain / app name hardcodes for "Open CMP" — Debug reuses the already-configured `CMPManager` instance from `MainActivity` (see Assumptions).

### Assumptions

- A-S1. User said "three buttons"; the mini app currently exposes four ElevatedButtons. Plan ports all four capabilities for fidelity; Open CMP on Debug reuses the live `CMPManager` rather than re-running the mini app's hard-coded `setUrlConfig`.
- A-S2. Burger menu lives on the post-init demo shell (`CMPDemoWithNav`), not on ConfigurationScreen.
- A-S3. Debug is a third shell destination (alongside Home and Logs), reachable from the burger menu. Prefer a top app bar with burger on Home/Logs and a back affordance on Debug that returns to the previous tab; bottom nav may hide on Debug or keep Home/Logs selectable — implementer picks the less-disruptive Material3 option that preserves the shared log list.
- A-S4. AdMob uses Google's sample APPLICATION_ID and Android interstitial test unit id from the mini app (`ca-app-pub-3940256099942544/1033173712`).

## Planning Contract

### Key Technical Decisions

- KTD1. Port mini-app Android prefs logic into a small Kotlin helper (not a Flutter MethodChannel).
  - Prefer a pure/testable `IabStorageDebugHelper` (or similar) over burying edits inside a Composable.
  - Source: `../cm-sdk-ios-v3-demoapp/docs/cm-tcstring-tests/android/.../MainActivity.kt` (`corruptIabStorage`, `dumpPrefs`).
  - Prefs file name: `${context.packageName}_preferences` — matches SDK fallback and sibling SDK demo inspector.
- KTD2. Add Google Mobile Ads (`play-services-ads`) via version catalog + `APPLICATION_ID` meta-data in `Kotlin/app/src/main/AndroidManifest.xml`.
  - Demo-only dependency; acceptable for a debug tooling screen.
- KTD3. Navigation: extend `CMPDemoWithNav` with a top-bar burger `DropdownMenu` (or equivalent) that sets a destination index/mode for Debug, rather than a new Activity.
  - Keeps one `CMPManager` and one shared log list.
  - Burger control needs a non-null `contentDescription` (e.g. "Menu") so TalkBack and UI tests can find it; Logs tab already uses a Menu icon for glow — use a distinct top-bar control, do not overload the Logs nav icon.
  - (session-settled: user-directed — chosen over embedding buttons on Home: user asked for burger menu + separate debug window)
- KTD4. Debug button set mirrors mini-app labels; Open CMP calls existing `cmpManager.checkAndOpen` (or the same success/error logging wrapper Home already uses) instead of mini-app hard-coded URL config.
  - (directive challenge outcome) Hard-coded PraxisLife config would fight this demo's ConfigurationScreen — rejected.
- KTD5. Unit-test the prefs helper with Robolectric or instrumented SharedPreferences if Robolectric is absent; prefer JVM unit test with mocked/fake Context prefs when feasible. AdMob UI remains manual/smoke.

### Technical Design

```
CMPDemoWithNav
  ├─ TopAppBar / IconButton(Icons.Default.Menu) → DropdownMenu → "Debug"
  ├─ destination: Home | Logs | Debug
  ├─ shared logLines + appendLog
  └─ DebugScreen(cmpManager, onLog)
        ├─ Open consentmanager CMP
        ├─ Load and show demo ad  → MobileAds + InterstitialAd
        ├─ Corrupt TC String and CMP ID → IabStorageDebugHelper
        └─ Dump prefs to Logcat → IabStorageDebugHelper + onLog
```

### Risks

- AdMob init/show can fail on emulators without Play services — mitigate by logging failures and never crashing the demo shell.
- Corrupting IAB keys can break subsequent consent flows by design; keep action labeled clearly as debug/destructive.
- Prefs file name mismatch would make corrupt/dump no-ops relative to SDK — mitigated by matching SDK's `${packageName}_preferences` convention.

### Sequencing

1. Helper + unit tests for corrupt/dump
2. Gradle/manifest AdMob wiring
3. DebugScreen UI + navigation/burger menu
4. Wire Open CMP + AdMob load/show with busy flags and logging

## Implementation Units

### U1. IAB storage debug helper + unit tests

- **Goal:** Pure Kotlin helper that corrupts and dumps the CMP SharedPreferences file with the same values/keys as the mini app.
- **Requirements:** R3, R4
- **Files:**
  - Create: `Kotlin/app/src/main/java/net/consentmanager/kmm/cmpsdkdemoapp/debug/IabStorageDebugHelper.kt`
  - Create: `Kotlin/app/src/test/java/net/consentmanager/kmm/cmpsdkdemoapp/debug/IabStorageDebugHelperTest.kt`
- **Patterns:** Mirror `MainActivity.kt` corrupt/dump in the Flutter Android host; logging style may reuse demo `appendLog` at the call site.
- **Test scenarios:**
  - Corrupt writes expected string/int values and reports before/after snapshot
  - Dump returns sorted keys with stringified values/types
  - Corrupt succeeds when keys were previously absent
  - Helper targets `${packageName}_preferences`, not a random prefs file
- **Verification:** `./gradlew :app:testDebugUnitTest` from `Kotlin/`

### U2. AdMob dependency and manifest meta-data

- **Goal:** App can initialize Mobile Ads and load the Android test interstitial.
- **Requirements:** R3
- **Files:**
  - Modify: `Kotlin/gradle/libs.versions.toml`
  - Modify: `Kotlin/app/build.gradle.kts`
  - Modify: `Kotlin/app/src/main/AndroidManifest.xml`
- **Patterns:** Mini-app `APPLICATION_ID` meta-data; Google sample app id.
- **Test scenarios:**
  - Manifest contains `com.google.android.gms.ads.APPLICATION_ID`
  - Module resolves `play-services-ads` (compile/sync)
- **Verification:** Gradle sync / assembleDebug compiles with new dependency

### U3. Burger menu, Debug destination, and action UI

- **Goal:** User reaches Debug from burger menu; four actions run with busy flags and log output.
- **Requirements:** R1–R6, F1–F3, AE1–AE4
- **Files:**
  - Modify: `Kotlin/app/src/main/java/net/consentmanager/kmm/cmpsdkdemoapp/CMPDemoNavigation.kt`
  - Create: `Kotlin/app/src/main/java/net/consentmanager/kmm/cmpsdkdemoapp/DebugScreen.kt`
  - Modify: `Kotlin/app/src/main/res/values/strings.xml`
  - Optional test: `Kotlin/app/src/androidTest/...` or unit-level Compose test if practical without full CMP init
- **Patterns:** Existing `Scaffold` + bottom nav in `CMPDemoNavigation.kt`; button/loading patterns in `CMPDemoScreen.kt`; string resources for nav labels.
- **Test scenarios:**
  - Menu item label "Debug" present in strings / UI
  - Selecting Debug shows the four action labels
  - Corrupt/Dump invoke helper and call `onLog`
  - Open CMP invokes `cmpManager` checkAndOpen path and logs success/error
  - Ad button sets busy and clears busy on completion/failure callbacks
- **Verification:** Unit tests for helper (U1); manual smoke on emulator for AdMob + menu navigation; `./gradlew :app:assembleDebug`

## Verification Contract

- Unit: `cd Kotlin && ./gradlew :app:testDebugUnitTest`
- Compile: `cd Kotlin && ./gradlew :app:assembleDebug`
- Manual smoke: launch app → configure CMP → burger → Debug → exercise Corrupt, Dump, Open CMP; AdMob if Play services present
- Quality gate: no crash on AdMob failure; destructive Corrupt clearly labeled

## Definition of Done

- Burger menu reachable on demo shell after init
- Debug destination hosts the mini-app capabilities (Open CMP, demo ad, corrupt IAB, dump prefs)
- Prefs helper covered by unit tests
- AdMob dependency + APPLICATION_ID present
- Actions log into existing Logs tab
- `assembleDebug` and unit tests pass

## Sources & Research

- Mini app UI/actions: `../cm-sdk-ios-v3-demoapp/docs/cm-tcstring-tests/lib/main.dart`
- Mini app Android native: `../cm-sdk-ios-v3-demoapp/docs/cm-tcstring-tests/android/app/src/main/kotlin/com/praxislife/cm_tcstring_tests/MainActivity.kt`
- Demo shell: `Kotlin/app/src/main/java/net/consentmanager/kmm/cmpsdkdemoapp/CMPDemoNavigation.kt`
- Demo home actions: `Kotlin/app/src/main/java/net/consentmanager/kmm/cmpsdkdemoapp/CMPDemoScreen.kt`
- SDK prefs convention: `cm-sdk-android-v3` uses `${packageName}_preferences`
- External research: skipped — local mini-app + SDK demo patterns are sufficient
