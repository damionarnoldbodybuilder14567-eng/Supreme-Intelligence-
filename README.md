# Supreme Intelligence — Android starter

This is the source project for the Supreme Intelligence Android app.

## What is included
- Native Android app built with Kotlin and Jetpack Compose.
- Blended personality: logical, decisive, and conversational.
- Android text-to-speech, with lower pitch and slower speech rate as a starting point.
- Chat screen connected to a separate FastAPI backend.
- GitHub Actions workflow that builds a debug APK in the cloud.
- Backend API key stays on the server, not inside the Android app.

## Build the APK using only your phone
You do not need Termux or Android Studio for the cloud build.

1. Create/sign in to a GitHub account.
2. In GitHub, create a new empty repository named `SupremeIntelligence`.
3. Upload the contents of this ZIP to the repository, keeping the folder structure. The `.github` folder must be included at the repository root.
4. Open the repository's **Actions** tab.
5. Select **Build Supreme Intelligence APK** and tap **Run workflow** (or push to the `main` branch).
6. When the workflow completes, open the run and download the artifact named `supreme-intelligence-debug-apk`.
7. Extract the artifact ZIP on your phone. Tap `app-debug.apk` to install it. Android may ask you to allow installs from your browser or file manager.

This produces a debug APK for testing. It is not a Play Store release.

## Connect the AI backend
The Android app needs a reachable backend URL. The default URL is for the Android emulator (`http://10.0.2.2:8000/`), not a normal phone.

For a real phone, deploy the `server/` folder to a trusted HTTPS hosting provider, then change `API_BASE_URL` in `android/app/build.gradle.kts` to your backend's HTTPS address and rebuild.

Backend setup:
1. Use Python 3.10+ on a server.
2. Install `server/requirements.txt`.
3. Set `OPENAI_API_KEY` as a server-side environment secret.
4. Set `OPENAI_MODEL` to a model identifier available to your API account.
5. Run `uvicorn app:app --host 0.0.0.0 --port 8000` for local testing, or deploy behind HTTPS.

Never put an API key in the Android app, a GitHub commit, or a public repository. Configure it only as a secret on the backend host.

## Important limitations and safety
- This is an initial working codebase, not a completed commercial AI platform.
- No APK is prebuilt in this ZIP; the included GitHub Actions workflow builds one after you upload the project.
- The AI backend must be deployed and configured before chat works on a physical phone.
- The starter can call the configured model API. It cannot take control of unrelated chatbots. Other AI systems can be integrated only through their official APIs and authorized credentials.
- External actions (sending messages, purchases, deleting data, publishing, etc.) must require explicit approval and dedicated integrations.
- The Android project currently allows cleartext traffic for local development. For production, use HTTPS and disable cleartext traffic.
