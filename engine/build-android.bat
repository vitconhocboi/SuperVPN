@echo off
REM Builds the netstack engine AAR for Android and copies it into app/libs/.
REM Prerequisites: Go toolchain, gomobile+gobind installed (go install golang.org/x/mobile/cmd/gomobile@latest),
REM ANDROID_HOME + ANDROID_NDK_HOME set. Requires -androidapi 27 because NDK r26+ dropped API 16
REM (gomobile's default) and the app has minSdk 27 anyway.
setlocal
set PATH=%PATH%;%USERPROFILE%\go\bin
set TARGETS=android/arm,android/arm64,android/amd64
set OUT=..\app\libs\netstack.aar

gomobile bind -androidapi 27 -target %TARGETS% -o %OUT% .
if errorlevel 1 exit /b 1
echo Built %OUT%
