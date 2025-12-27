# Google Play Publishing Guide

This guide walks you through publishing TrackMed to the Google Play Store.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Creating a Developer Account](#creating-a-developer-account)
- [Preparing Your App](#preparing-your-app)
- [App Assets](#app-assets)
- [Store Listing](#store-listing)
- [Content Rating](#content-rating)
- [Privacy Policy](#privacy-policy)
- [Building for Release](#building-for-release)
- [Uploading to Play Console](#uploading-to-play-console)
- [Review Process](#review-process)
- [Post-Launch](#post-launch)

## Prerequisites

Before publishing, ensure you have:

- [ ] A Google account
- [ ] $25 USD for the one-time developer registration fee
- [ ] A signed release APK or Android App Bundle (AAB)
- [ ] App icon in required sizes
- [ ] Feature graphic (1024x500)
- [ ] At least 2 screenshots per device type
- [ ] Short description (80 characters max)
- [ ] Full description (4000 characters max)
- [ ] Privacy policy URL

## Creating a Developer Account

### Step 1: Register

1. Go to [Google Play Console](https://play.google.com/console)
2. Sign in with your Google account
3. Click "Create developer account"
4. Pay the $25 registration fee (one-time)
5. Complete identity verification

### Step 2: Account Setup

1. Add developer name (shown on Play Store)
2. Add contact email (for user support)
3. Set up payments (for future monetization)

> **Note**: Account verification can take 48 hours to several days.

## Preparing Your App

### Update Version Information

In `app/build.gradle.kts`, update:

```kotlin
android {
    defaultConfig {
        versionCode = 1        // Increment for each release
        versionName = "1.0.0"  // User-visible version
    }
}
```

### Version Code Rules

- Must be a positive integer
- Must increase with each release
- Cannot be decreased once published

### Version Name Suggestions

Use semantic versioning: `MAJOR.MINOR.PATCH`
- `1.0.0` - Initial release
- `1.1.0` - New features
- `1.0.1` - Bug fixes

## App Assets

### App Icon

Create your app icon in these sizes:

| Size | Usage |
|------|-------|
| 512x512 px | Play Store listing (required) |
| 192x192 px | XXXHDPI devices |
| 144x144 px | XXHDPI devices |
| 96x96 px | XHDPI devices |
| 72x72 px | HDPI devices |
| 48x48 px | MDPI devices |

**Icon Requirements:**
- PNG format, 32-bit with alpha
- No transparent background for adaptive icons
- Must be unique and not imitate other apps
- No misleading elements

**Tools for Creating Icons:**
- [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/)
- [Figma](https://figma.com) with Android icon templates
- [Canva](https://canva.com) for simpler designs

### Feature Graphic

**Required Size:** 1024 x 500 px

This appears at the top of your Play Store listing. Guidelines:
- PNG or JPEG format
- No text in top/bottom 100px (may be cropped)
- Should represent your app's brand/purpose
- Use bold, simple imagery

**Example Layout:**
```
┌──────────────────────────────────────────┐
│                                          │
│     [App Logo]    TrackMed               │
│                                          │
│     Never miss a dose again              │
│                                          │
│     [Pill icons or health imagery]       │
│                                          │
└──────────────────────────────────────────┘
```

### Screenshots

**Minimum:** 2 screenshots
**Maximum:** 8 screenshots
**Recommended:** 4-6 screenshots showing key features

**Size Requirements:**
- Phone: 16:9 or 9:16 aspect ratio
- Minimum dimension: 320px
- Maximum dimension: 3840px
- PNG or JPEG format

**Recommended Screenshots for TrackMed:**

1. **Home Screen** - Today's medications with progress
2. **Medicine List** - All medications overview
3. **Add Medicine** - Adding a new medication
4. **Schedule Setup** - Flexible scheduling options
5. **Notification** - Reminder notification example
6. **Widget** - Home screen widget in action

**Screenshot Tips:**
- Use real device or emulator captures
- Show actual app content (not placeholder data)
- Consider adding device frames
- Add brief captions if needed

**Tools:**
- Android Studio emulator screenshots
- Device screenshot (Power + Volume Down)
- [Screenshots.pro](https://screenshots.pro) for device frames
- Figma for adding text/frames

## Store Listing

### App Name

**Maximum:** 30 characters
**Suggestion:** `TrackMed - Medication Tracker`

### Short Description

**Maximum:** 80 characters
**Example:**
```
Track medications, vitamins & supplements with smart reminders and stock alerts.
```

### Full Description

**Maximum:** 4000 characters

**Example:**
```
TrackMed helps you stay on top of your health routine with smart medication tracking and reminders.

NEVER MISS A DOSE
• Set up flexible schedules - daily, specific days, or intervals
• Get timely notifications with Take, Skip, and Snooze options
• Follow-up reminders ensure you don't forget

EASY MEDICATION MANAGEMENT
• Add unlimited medications, vitamins, and supplements
• Track multiple schedules per medication
• Monitor stock levels with low-supply alerts
• Add notes for dosage instructions or side effects

FLEXIBLE TIME WINDOWS
• Morning, Noon, Afternoon, Evening, Night
• Customize times to match your routine
• Visual progress tracking throughout the day

QUICK ACCESS
• Home screen widget shows pending medications
• Mark doses as taken directly from notifications
• See daily progress at a glance

YOUR PRIVACY MATTERS
• All data stays on your device
• No account required
• No data collection or tracking
• Backup and restore your own data

VACATION MODE
• Pause all reminders temporarily
• Perfect for travel or breaks

TrackMed is designed for anyone managing:
• Prescription medications
• Daily vitamins
• Supplements
• Any regular health routine

Download TrackMed today and take control of your medication schedule!
```

### Category

**Primary:** Health & Fitness
**Tags:** medication, reminder, health, tracker, pills

## Content Rating

Google requires you to complete a content rating questionnaire.

### For TrackMed, typical answers:

| Question | Answer |
|----------|--------|
| Violence | No |
| Sexual content | No |
| Profanity | No |
| Gambling | No |
| Drugs | References to medications (health context) |
| User-generated content | No |
| Location sharing | No |
| Data collection | No |

**Expected Rating:** PEGI 3 / Everyone

## Privacy Policy

### Why It's Required

Google requires a privacy policy for apps that:
- Request sensitive permissions
- Handle personal/health data
- Target children

TrackMed handles health-related data (medications), so a privacy policy is **required**.

### Privacy Policy Template

Create a privacy policy page (can be a simple website, GitHub page, or Google Doc) with content like:

```
TrackMed Privacy Policy

Last updated: [Date]

TrackMed ("we", "our", or "us") is committed to protecting your privacy.

DATA COLLECTION
TrackMed does NOT collect, store, or transmit any personal data. All information you enter (medications, schedules, notes) is stored locally on your device only.

DATA STORAGE
• All data is stored in a local database on your device
• Data never leaves your device
• We have no access to your information

PERMISSIONS
TrackMed requests the following permissions:
• Notifications: To send medication reminders
• Exact Alarms: For precise reminder timing
• Boot Complete: To restore alarms after device restart

These permissions are used solely for app functionality and do not involve any data collection.

BACKUP & RESTORE
• Backup files are created locally on your device
• You control where backup files are stored
• We never access your backup data

THIRD-PARTY SERVICES
TrackMed does not use any third-party analytics, advertising, or tracking services.

CHANGES TO THIS POLICY
We may update this policy. Changes will be posted here with an updated date.

CONTACT
For questions about this privacy policy, contact: [your email]
```

### Hosting Options

1. **GitHub Pages** (Free)
   - Create a `privacy-policy.md` in your repo
   - Enable GitHub Pages
   - Use URL like: `https://username.github.io/trackmed/privacy-policy`

2. **Google Sites** (Free)
   - Create a simple one-page site
   - Professional URL

3. **Your Website** (If you have one)
   - Add a `/privacy` page

## Building for Release

### Step 1: Create Signing Key

```bash
keytool -genkey -v -keystore trackmed-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias trackmed
```

**IMPORTANT:**
- Keep this keystore file safe - you need it for all future updates
- Never commit to version control
- Back up to secure location

### Step 2: Configure Signing in Gradle

Create `keystore.properties` (DO NOT commit):
```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=trackmed
storeFile=../trackmed-release-key.jks
```

Update `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))

                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### Step 3: Build AAB (Recommended)

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### Step 4: Test Release Build

Install on device:
```bash
./gradlew installRelease
```

Or use bundletool to test AAB:
```bash
bundletool build-apks --bundle=app-release.aab --output=app.apks
bundletool install-apks --apks=app.apks
```

## Uploading to Play Console

### Step 1: Create App

1. Go to Play Console
2. Click "Create app"
3. Fill in:
   - App name: TrackMed
   - Default language: English (US)
   - App or Game: App
   - Free or Paid: Free
4. Accept policies

### Step 2: Set Up Store Listing

1. **Main store listing**
   - Add short description
   - Add full description
   - Upload screenshots
   - Upload feature graphic
   - Upload app icon

2. **Store settings**
   - App category: Health & Fitness
   - Contact email
   - Privacy policy URL

### Step 3: App Content

1. **Privacy policy** - Add URL
2. **Ads** - Select "No, app does not contain ads"
3. **App access** - Select "All functionality available without restrictions"
4. **Content rating** - Complete questionnaire
5. **Target audience** - 18+ (medication apps)
6. **News apps** - Not a news app
7. **COVID-19 apps** - Not a COVID app
8. **Data safety** - Complete the form:
   - Data collection: No
   - Data sharing: No
   - Security practices: Data encrypted in transit (N/A since no data sent)

### Step 4: Release

1. Go to "Production" > "Create new release"
2. Upload your AAB file
3. Add release notes:
   ```
   Initial release of TrackMed!

   • Track medications, vitamins, and supplements
   • Flexible scheduling options
   • Smart notifications with Take/Skip/Snooze
   • Stock tracking with low-supply alerts
   • Home screen widget
   • Backup and restore
   ```
4. Review and roll out

## Review Process

### Timeline

- **Initial review:** 3-7 days (sometimes longer)
- **Updates:** Usually 1-3 days

### Common Rejection Reasons

1. **Incomplete listing** - Missing screenshots, description
2. **Broken functionality** - App crashes
3. **Privacy policy issues** - Missing or inadequate
4. **Deceptive behavior** - App doesn't do what it claims
5. **Permission issues** - Requesting unnecessary permissions

### If Rejected

1. Read the rejection reason carefully
2. Fix the issue
3. Resubmit for review
4. Respond in Play Console if clarification needed

## Post-Launch

### Monitor

- **Ratings & Reviews** - Respond to user feedback
- **Crashes** - Check Android Vitals in Play Console
- **Statistics** - Track installs and retention

### Update Regularly

- Fix bugs promptly
- Add requested features
- Keep dependencies updated

### Version Updates

1. Increment `versionCode` (required)
2. Update `versionName`
3. Build new AAB
4. Upload with release notes

## Adding Ads Later

When ready to monetize:

1. **Set up AdMob account**
2. **Add AdMob SDK** to app
3. **Update privacy policy** to include ad tracking
4. **Update Data safety** form in Play Console
5. **Choose ad formats:**
   - Banner ads (least intrusive)
   - Interstitial ads (between screens)
   - Rewarded ads (user opts in)

**Recommendation:** Use non-intrusive banner ads to maintain good user experience.

## Checklist

### Before Creating Play Console Account
- [ ] Have a Google account ready
- [ ] Have $25 for registration fee

### Before Uploading
- [ ] App icon (512x512) ready
- [ ] Feature graphic (1024x500) ready
- [ ] Screenshots (4-6 recommended) ready
- [ ] Short description written
- [ ] Full description written
- [ ] Privacy policy URL live and accessible
- [ ] Release build tested on real device
- [ ] Signing keystore backed up securely

### In Play Console
- [ ] Store listing complete
- [ ] Content rating completed
- [ ] Data safety form completed
- [ ] App content declarations done
- [ ] AAB uploaded
- [ ] Release notes added
- [ ] Review submitted

## Resources

- [Play Console Help](https://support.google.com/googleplay/android-developer)
- [Launch Checklist](https://developer.android.com/distribute/best-practices/launch/launch-checklist)
- [Store Listing Best Practices](https://developer.android.com/distribute/best-practices/launch/store-listing)
- [App Icon Guidelines](https://developer.android.com/google-play/resources/icon-design-specifications)
