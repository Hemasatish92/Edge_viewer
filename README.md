
# Full Edge Viewer Project (Windows + VS Code)

## How to Run (No Android Studio Needed)

### 1. Install Required Tools
- Install JDK 17
- Install SDK commandline tools in: `C:\Android\Sdk`
- Run:
  ```
  sdkmanager --install "platform-tools" "platforms;android-34" "build-tools;34.0.0" "cmake;3.22.1" "ndk;26.1.10909125"
  ```
- Set `ANDROID_SDK_ROOT = C:\Android\Sdk`

### 2. Build APK
```
cd app
..\gradlew.bat assembleDebug
```

APK will be in:
```
app\build\outputs\apk\debug\app-debug.apk
```

### 3. Install on Device
```
adb install -r app-debug.apk
```

## Web viewer (sample processed frame)

The repository includes a small web viewer in `web/` that displays a sample processed frame (or an animated fallback pattern).

To use it:

1. Place a screenshot or exported processed frame at `web/assets/sample_processed.png` (create the `assets` folder if missing).

2. From PowerShell:

```powershell
cd c:\Edge_viewer\web
npm install
npm run start
# open http://localhost:8080 in your browser
```

The page `web/index.html` will draw `assets/sample_processed.png` to the canvas if present; otherwise it shows an animated test pattern and FPS/resolution info.

If you plan to re-enable the native OpenCV build for the Android app, follow the steps in the project to add the OpenCV Android SDK and configure `CMakeLists.txt` and `app/build.gradle` accordingly.

## Output (sample screenshot)

Below is the sample processed-frame screenshot used by the web viewer. Place your screenshot at `web/assets/sample_processed.png` and it will appear here when viewing `README.md` on GitHub or your local clone.

![Sample processed frame](web/assets/sample_processed.png)

If the image does not display, make sure the file exists at `web/assets/sample_processed.png` and commit it to the repository.
