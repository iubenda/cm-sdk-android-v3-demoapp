# Production release — Iubenda CMP SDK Demo App

Play listing: [Iubenda CMP Demo App](https://play.google.com/store/apps/details?id=net.consentmanager.kmm.cmdemoappkotlin)

| Field | Value |
|-------|--------|
| Package | `net.consentmanager.kmm.cmdemoappkotlin` |
| Firebase (Analytics) | `mobile-sdk-demo-app-c323b` |
| AdMob publisher (`app-ads.txt`) | `pub-9017919202204592` |
| AdMob app ID (Android) | `ca-app-pub-9017919202204592~2786489551` |
| Interstitial unit (AdMob console) | `ca-app-pub-9017919202204592/9403868793` |

## 1. Secrets and local files (gitignored)

Copy examples and fill in real values under `Kotlin/`:

| File | Purpose |
|------|---------|
| `keystore.properties` | Upload keystore + **production** `admobAppId` / `admobInterstitialUnitId` |
| `upload-keystore.jks` | Play App Signing upload key |
| `app/google-services.json` | Firebase Android app for package `net.consentmanager.kmm.cmdemoappkotlin` |
| `adi-registration.properties` | Optional Play package verification snippet (warning only if missing) |

Templates: `keystore.properties.example`, `app/google-services.json.example`, `adi-registration.properties.example`.

**Debug builds** always use Google sample AdMob IDs. **Release builds** fail if sample IDs are used or if `public/app-ads.txt` publisher ≠ AdMob app publisher.

## 2. Build production AAB

```bash
cd Kotlin
./gradlew :app:bundleRelease
```

Output: `Kotlin/app/build/outputs/bundle/release/app-release.aab`

Before each Play upload, increment `versionCode` (and `versionName`) in `app/build.gradle.kts`.

## 3. Host `app-ads.txt`

AdMob seller verification requires `app-ads.txt` on the developer website URL configured in Play Console / AdMob.

```bash
cd ..   # repo root
firebase use mobile-sdk-demo-app-c323b
firebase deploy --only hosting
```

Confirm: `https://<your-hosting-domain>/app-ads.txt` contains the same publisher as release `admobAppId` (`pub-9017919202204592`).

## 4. AdMob (in-app ads, not Google Ads UA campaigns)

1. [AdMob](https://admob.google.com) → Apps → **Iubenda CMP Demo App** / package `net.consentmanager.kmm.cmdemoappkotlin` → status **Ready**.
2. Interstitial ad unit used by the app: value of `admobInterstitialUnitId` in `keystore.properties` (Debug screen → Load ad).
3. Link Play Store app to AdMob if prompted.
4. After deploy, wait for `app-ads.txt` crawl (up to 24h). Check AdMob → **app-ads.txt** tab.
5. Verify on a **release** install (not debug): unlock debug tools → Load ad → Logs tab shows production `adUnitId` and `onAdImpression`.

Low fill is normal on new units until traffic grows.

## 5. Upload to Play Console

1. Play Console → **Iubenda CMP Demo App** → Production (or Internal testing first).
2. Create release → upload `app-release.aab`.
3. Release notes (example for 1.0.1): production AdMob/Firebase alignment, `app-ads.txt` publisher fix.
4. **Policy → Ads**: Contains ads = Yes.
5. **Policy → Data safety**: must declare Firebase Analytics + AdMob processing (live listing currently shows “No data collected” — **update to match** `docs/play-store-listing-materials.md` before next review).
6. Privacy policy URL must match Data safety answers.

## 6. Pre-submit checklist

- [ ] `bundleRelease` succeeds
- [ ] `versionCode` bumped above last Play upload
- [ ] Merged release manifest has production `APPLICATION_ID` (not `3940256099942544`)
- [ ] `app/google-services.json` package = `net.consentmanager.kmm.cmdemoappkotlin`
- [ ] Firebase Hosting deployed with correct `app-ads.txt`
- [ ] AdMob app + interstitial unit active
- [ ] Data safety + privacy policy aligned
- [ ] Release build smoke: consent flow, easter-egg ad load, impressions in AdMob (lag ok)