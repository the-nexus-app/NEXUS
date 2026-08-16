# Contributing to NEXUS

Found a bug or have an idea for a feature? Open an issue on [this repo's tracker](https://github.com/the-nexus-app/NEXUS/issues) and we'll take a look.

## Working on the code

Pull requests are always welcome. Before picking up an existing issue, drop a comment on it first so nobody else duplicates the work — no need to formally request it or wait for approval.

**What you'll need to know:**
- Android development basics
- Kotlin

This project won't teach these from scratch — some baseline familiarity is expected going in.

**Setup:**
- Android Studio
- A phone (with developer options enabled) or emulator for testing

**Questions while developing?** Come chat on [Discord](https://discord.gg/QGrs2VVyP).

## Forking NEXUS

Go ahead — the license permits it (see [LICENSE](https://github.com/the-nexus-app/NEXUS/blob/main/LICENSE)). A few things to handle so your fork doesn't get mixed up with this one or cause conflicts on shared devices:

**Keep it visually distinct:**
- Swap the app name
- Swap the app icon
- Point (or turn off) the update checker so it doesn't check against NEXUS's releases

**Avoid install conflicts:**
- Give it its own `applicationId` in `build.gradle.kts`

**Keep your analytics separate:**
- If you're enabling Firebase, drop in your own `google-services.json` rather than reusing NEXUS's

## Setting up Google Drive sync (optional)

Heads up: this app doesn't ship with working Google Drive credentials out of the box. If you want that backup option functional, you'll need to register your own OAuth client through Google — here's the walkthrough:

1. Head to the [Google Cloud Console](https://console.cloud.google.com/) and start a new project
2. Under API & Services → Library, find the Google Drive API and enable it
3. Go to API & Services → OAuth consent screen and fill in the basics (app name, support email, developer contact)
4. On the scopes screen, add `.../auth/drive.appdata` and `.../auth/drive.file`
5. Skip adding test users, then publish the app
6. Under API & Services → Credentials, click Create credentials → OAuth client ID
7. Choose Android, name it whatever you like, and set the package name to `eu.kanade.google.oauth`
8. Grab your SHA-1 fingerprint by running `keytool -printcert -jarfile app-standard-universal-release.apk` against your built APK
9. Under advanced settings, turn on Custom URL scheme
10. Download the resulting JSON, rename it `client_secrets.json`, and drop it into `app/src/main/assets/`