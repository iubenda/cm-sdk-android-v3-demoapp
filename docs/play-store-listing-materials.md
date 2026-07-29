# Play Store listing materials — Iubenda CMP SDK Demo App

Draft copy for Play Console and a hosted privacy policy. **Legal/compliance must review before publish.** Replace `[PLACEHOLDERS]` with your real values.

| Field | Value |
|-------|--------|
| App name | Iubenda CMP SDK Demo App |
| Package name | `net.consentmanager.kmm.cmpsdkdemoapp` |
| Version | 1.0.0 (versionCode 1) |
| Category suggestion | Tools or Business |
| Price | Free |
| Contains ads | Yes |
| Target audience | Developers / business users (not designed for children) |

---

## 1. Privacy policy (host at public HTTPS URL)

**Suggested URL:** `https://www.iubenda.com/privacy-policy/[YOUR-PAGE-ID]`  
(or consentmanager corporate site — must be publicly reachable)

Paste the text below on that page, then enter the URL in Play Console → **Policy** → **App content** → **Privacy policy**.

---

### Privacy Policy — Iubenda CMP SDK Demo App

**Last updated:** [DATE]  
**App name:** Iubenda CMP SDK Demo App  
**Package name:** net.consentmanager.kmm.cmpsdkdemoapp  
**Publisher:** [Iubenda S.p.A. / consentmanager AB — legal entity name]  
**Contact:** [privacy@iubenda.com or support email]

#### 1. Introduction

This Privacy Policy describes how the **Iubenda CMP SDK Demo App** (“App”) processes information when you install and use it. The App is a **demonstration application** for developers and integrators evaluating the Iubenda/consentmanager Consent Management Platform (CMP) SDK for Android. It is not intended for general consumer use.

#### 2. Who we are

The App is published by **[Legal entity name and address]**.  
For privacy inquiries: **[contact email]**.

#### 3. What the App does

The App lets you:

- Configure and load a CMP consent layer (WebView-based)
- Exercise SDK methods (consent status, vendors, purposes, etc.)
- View SDK log output on device
- Optionally load a demo advertisement through a hidden developer tools area

The App does **not** require you to create an account.

#### 4. Information we process

##### 4.1 Information you provide in the App

- **CMP configuration** you enter (e.g. Code-ID, domain, app name, language, UI options). You may use default demo values or your own test configuration.
- **Consent choices** you make in the CMP layer (accept, reject, customize preferences).

##### 4.2 Information collected automatically

Depending on how you use the App, the following may be processed:

| Data | Purpose | Source |
|------|---------|--------|
| Device and app identifiers (e.g. advertising ID where available) | Analytics, advertising, fraud prevention | Google Firebase Analytics, Google AdMob |
| App interaction and diagnostic events | Analytics, app performance | Google Firebase Analytics |
| IP address and device/network metadata | Deliver CMP content, analytics, ads | CMP SDK, Firebase, AdMob |
| Consent strings and preference signals (e.g. TC String, CMP ID, vendor/purpose choices) | Consent management demonstration | CMP SDK (stored locally and transmitted to configured CMP endpoints) |

##### 4.3 Local storage

The App and integrated SDK may store data on your device, including:

- CMP and IAB-related preference keys in app storage (SharedPreferences)
- Configuration values you set in the App

Developer-only tools (reachable via a hidden unlock gesture) may modify stored consent data for testing purposes.

#### 5. How we use information

We use information to:

- Operate and demonstrate CMP SDK integration
- Measure App usage and stability (Firebase Analytics)
- Display test or production advertisements when you use the ad demo feature (Google AdMob)
- Process consent in line with the CMP configuration you provide

We do **not** use the App to build marketing profiles of end users for unrelated purposes. The App is a **technical demo** for integrators.

#### 6. Legal bases (EEA/UK users)

Where GDPR applies, we rely on:

- **Legitimate interests** — providing and improving the SDK demo, security, analytics in a demo context
- **Consent** — where required for analytics/advertising cookies or identifiers, coordinated through the CMP layer where applicable

#### 7. Third-party services

The App integrates:

| Provider | Service | Privacy information |
|----------|---------|---------------------|
| consentmanager / Iubenda | CMP SDK & delivery | [https://www.iubenda.com/privacy-policy](https://www.iubenda.com/privacy-policy) |
| Google Firebase | Analytics | [https://firebase.google.com/support/privacy](https://firebase.google.com/support/privacy) |
| Google AdMob | Advertising | [https://policies.google.com/privacy](https://policies.google.com/privacy) |

Third parties may process data under their own policies. Configure your CMP Code-ID and domain to point at your own test or production CMP tenant where applicable.

#### 8. Data sharing

We do not sell your personal information. Data may be shared with:

- **Service providers** listed above, strictly to operate the App
- **CMP infrastructure** associated with the Code-ID/domain you configure
- **Authorities** when required by law

#### 9. International transfers

Data may be processed in countries where we or our providers operate, including outside your country of residence. We use appropriate safeguards where required by law.

#### 10. Retention

- **On-device data** — until you clear app storage or uninstall the App
- **Analytics/ad data** — per Google retention settings and your device controls
- **CMP server-side data** — per your CMP account configuration

#### 11. Your rights

Depending on your location, you may have rights to access, rectify, erase, restrict, or object to processing, and to data portability. You may also withdraw consent where processing is consent-based.

Contact: **[privacy email]**  
You may lodge a complaint with your local supervisory authority.

**Android controls:** Settings → Google → Ads (reset/limit Ad ID); uninstall the App to remove local data.

#### 12. Children

The App is **not directed at children** under 13 (or under 16 in certain jurisdictions). We do not knowingly collect children’s data through this demo App.

#### 13. Security

We apply reasonable technical and organizational measures appropriate for a demonstration application. No method of transmission or storage is 100% secure.

#### 14. Changes

We may update this policy. The “Last updated” date will change. Continued use after changes constitutes acceptance of the updated policy.

#### 15. Contact

**[Legal entity]**  
**[Address]**  
**Email:** [privacy/support email]

---

## 2. Store listing copy (Play Console → Main store listing)

### App name
```
Iubenda CMP SDK Demo App
```

### Short description (max 80 characters)
```
Demo app to explore Iubenda CMP SDK v3 integration on Android Native.
```
*(79 characters)*

### Full description
```
Iubenda CMP SDK Demo App helps developers and integrators evaluate the Iubenda Consent Management Platform (CMP) SDK for Android Native apps.

What you can do:
• Load and interact with a CMP consent layer (WebView)
• Test accept, reject, and customize consent flows
• Try SDK methods for user status, vendors, and purposes
• Change CMP configuration (Code-ID, domain, language, UI options)
• View on-device SDK logs for integration debugging

This is a demonstration app — not a production CMP admin tool. Default settings showcase a sample integration; you can enter your own test Code-ID and domain to validate against your CMP setup.

Technical note: the Play Store listing name reflects Iubenda branding; the Android package identifier is net.consentmanager.kmm.cmpsdkdemoapp.

Documentation: https://help.consentmanager.net/books/cmp/chapter/integration-into-your-app---v3

Privacy policy: [YOUR PRIVACY POLICY URL]
Support: [YOUR SUPPORT EMAIL]
```

### Contact email
```
[developer-support@iubenda.com]
```

---

## 3. Release notes — v1.0.0 (Production release)

Play Console → **Release** → **Production** → **Release notes** (default language):

```
Initial public release of the Iubenda CMP SDK Demo App.

• Explore CMP SDK v3 consent flows on Android
• Configure Code-ID, domain, and consent layer UI options
• On-device SDK log viewer for integration testing
• Firebase Analytics and AdMob integrated for demo purposes
```

---

## 4. Data safety form (Play Console → App content → Data safety)

Use these answers as a starting point. Adjust if your Firebase/AdMob/CMP configuration differs. **Must match the privacy policy.**

### Overview

| Question | Answer |
|----------|--------|
| Does your app collect or share any of the required user data types? | **Yes** |
| Is all of the user data collected by your app encrypted in transit? | **Yes** (HTTPS for network calls) |
| Do you provide a way for users to request that their data is deleted? | **Yes** — uninstall app / clear app storage; contact [support email] for CMP server-side requests tied to configured tenant |

### Data types to declare

#### App activity
| Field | Collect | Share | Purpose | Optional |
|-------|---------|-------|---------|----------|
| App interactions | Yes | Yes (Firebase) | Analytics | No |
| In-app search history | No | — | — | — |
| Other user-generated content | Yes (config you type, consent choices) | Yes (CMP servers per your Code-ID) | App functionality | No |
| Other actions | Yes | Yes | Analytics, advertising (if ad demo used) | No |

#### App info and performance
| Field | Collect | Share | Purpose |
|-------|---------|-------|---------|
| Crash logs | Yes (via Firebase/Google Play) | Yes | Analytics |
| Diagnostics | Yes | Yes | Analytics |

#### Device or other IDs
| Field | Collect | Share | Purpose |
|-------|---------|-------|---------|
| Device or other IDs | Yes | Yes | Analytics, advertising, fraud prevention |

#### Location
| Field | Collect | Share |
|-------|---------|-------|
| Approximate location | No* | — |
| Precise location | No | — |

\*Unless Firebase infers coarse location from IP — if Play asks, declare **Approximate location** as collected indirectly via analytics with purpose **Analytics** only.

### Advertising ID
- **Does your app use advertising ID?** Yes (AdMob SDK present; ads shown only when user unlocks developer ad demo)
- Link to ads declaration: **Yes, contains ads**

### Data handling — for each declared type

- **Collected** = Yes  
- **Shared** = Yes (with Google / CMP infrastructure as applicable)  
- **Processed ephemerally** = No (unless you confirm otherwise)  
- **Required vs optional** = Required for core demo; ad/analytics identifiers tied to integrated SDKs  
- **Purposes:** App functionality, Analytics, Advertising or marketing, Fraud prevention (as applicable per type)

### Account deletion
- App has **no user accounts** → select **No** for in-app account deletion, but provide support email for privacy requests in the policy URL section.

---

## 5. Other App content declarations

### Ads
- **Contains ads:** Yes  
- **Ad format:** Interstitial (demo, developer tools area)

### App access
- **All functionality available without restrictions** — No login required  
- If Play asks for instructions: leave blank or state “No login; open app and use default demo configuration.”

### Content rating (IARC) — expected answers
- Violence, sexuality, language, controlled substances, gambling: **No / None**
- User interaction: **No** (no chat, no UGC sharing between users)
- Shares location: **No**
- Unrestricted internet: **Yes** (WebView loads CMP content)

### Target audience
- **Not designed for children** — target age 18+ or “business/developer audience”  
- **Store listing aimed at children:** No

### News apps / COVID / Government apps
- **No** to all unless applicable

### Families Policy
- **Not participating in Designed for Families**

---

## 6. Checklist before Submit for review

- [ ] Package ownership verified (APK upload with `adi-registration.properties`)
- [ ] Privacy policy URL live and matches Data safety answers
- [ ] Data safety form completed
- [ ] Ads declaration = Yes
- [ ] Content rating questionnaire completed
- [ ] Store listing: short + full description, icon, feature graphic, ≥2 screenshots
- [ ] Contact email set
- [ ] Production AAB uploaded (`Kotlin/app/build/outputs/bundle/release/app-release.aab`)
- [ ] Release notes added for 1.0.0
- [ ] Pre-launch report reviewed (no critical crashes)

---

## 7. Suggested screenshot captions (for designers)

1. **Configuration screen** — “Configure Code-ID, domain, and consent layer UI”
2. **Consent layer** — “Interactive CMP consent experience”
3. **Home / SDK methods** — “Test SDK APIs on device”
4. **Logs tab** — “Debug integration with on-device logs”

Feature graphic text idea: **“Iubenda CMP SDK Demo — Android Native”**
