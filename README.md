
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
