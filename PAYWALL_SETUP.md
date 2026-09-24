# Paywall setup — manual steps

The code side of the freemium paywall is complete (`:payments` module + gates in `app`).
These are the account/dashboard steps only the project owner can do, in order.

## 0. Develop without any accounts (works today)

Add to `local.properties` (never committed):

```
SIMULATE_PREMIUM=true
```

Debug builds then behave as premium (unlimited AI review, AI Coach chat unlocked).
Remove the line (or set `false`) to exercise the free tier + paywall UI. Release builds
ignore this flag entirely.

## 1. Upload keystore

```
keytool -genkeypair -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Then in `local.properties`:

```
RELEASE_STORE_FILE=C:/path/to/upload-keystore.jks
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=upload
RELEASE_KEY_PASSWORD=...
```

Without these, `assembleRelease` still builds (debug-signed) but cannot be uploaded to Play.

## 2. Google Play Console

1. Create the app. Its applicationId must match `app/build.gradle.kts` (`dev.rootchess.app`
   currently — change it there first if you want a different one; Play rejects `com.example.*`).
2. Host a privacy policy and set its URL in the store listing (mandatory, and pass the same
   URL to `PaywallScreen(privacyUrl = ...)` in `ChessNavigationGraph.kt`).
3. Upload a signed AAB (`./gradlew :app:bundleRelease`) to **Internal testing** — subscriptions
   can't be purchased before a build is on a track.
4. Monetize → Subscriptions → create:
   - `premium_monthly` with one auto-renewing base plan (e.g. id `monthly`)
   - `premium_yearly` with one auto-renewing base plan (e.g. id `yearly`)
5. Settings → License testing → add your tester Gmail accounts (test purchases are free
   and auto-renew quickly).

## 3. RevenueCat dashboard

1. Create a project → add a **Play Store** app. This needs a Google Cloud service-account
   JSON with the Play "Android Publisher" role — follow RevenueCat's Play Store setup guide.
2. Products: import/attach `premium_monthly` and `premium_yearly`.
3. Entitlements: create **`premium`** (must match `RevenueCatEntitlementRepository.ENTITLEMENT_PREMIUM`)
   and attach both products.
4. Offerings: create/keep **`default`** with a **monthly** package → `premium_monthly` and an
   **annual** package → `premium_yearly` (the paywall reads exactly these two package types).
5. Copy the **public Google Play API key** (starts `goog_`) into `local.properties`:

```
REVENUECAT_API_KEY=goog_xxxxxxxxxxxx
```

## 4. Verify

- Free flow (no SIMULATE_PREMIUM): review one game → AI explanations appear; review a second
  game the same day → deterministic insights + "Go Premium" chip; AI Coach button → paywall.
- Premium flow: license-tester purchase on the paywall → chat unlocks immediately, AI review
  unlimited; "Restore purchases" works after reinstall.
- Every subsequent Play upload needs a `versionCode` bump in `app/build.gradle.kts`.
