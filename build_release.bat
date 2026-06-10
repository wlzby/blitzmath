@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Using JAVA_HOME: %JAVA_HOME%
echo Building Release App Bundle (AAB)...
call gradlew.bat bundleRelease
if %ERRORLEVEL% NEQ 0 (
    echo BUILD FAILED!
    pause
    exit /b %ERRORLEVEL%
)
echo BUILD SUCCESSFUL!
echo Your AAB file is located at: app\build\outputs\bundle\release\app-release.aab
pause
