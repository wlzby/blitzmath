@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Using JAVA_HOME: %JAVA_HOME%
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo BUILD FAILED!
    exit /b %ERRORLEVEL%
)
echo BUILD SUCCESSFUL!
