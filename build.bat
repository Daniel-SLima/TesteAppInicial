@echo off
setlocal

echo ========================================
echo  TesteAppInicial - Build Android
echo ========================================
echo.

if exist gradlew.bat (
    call gradlew.bat assembleDebug
) else (
    where gradle >nul 2>nul
    if errorlevel 1 (
        echo ERRO: Gradle Wrapper ainda nao foi gerado e Gradle nao esta no PATH.
        echo Instale/configure o Gradle uma vez e execute:
        echo   gradle wrapper --gradle-version 9.6.0
        exit /b 1
    )
    call gradle assembleDebug
)

if errorlevel 1 exit /b 1

echo.
echo APK gerado em:
echo app\build\outputs\apk\debug\app-debug.apk
