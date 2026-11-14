@echo off
chcp 65001 >nul
title Перезбірка Lunarix Client 1.21.11
echo ========================================================
echo     Компіляція та оновлення Lunarix Client 1.21.11
echo ========================================================
echo.
if exist "%~dp0..\Sours – копія\gradlew.bat" (
    cd /d "%~dp0..\Sours – копія"
    call gradlew.bat build -x test -x verifyTriggerTiming
    if errorlevel 1 (
        echo Помилка компіляції!
        pause
        exit /b 1
    )
    if exist "build\libs\Lunarix-Client-1.21.11-1.0.jar" (
        copy /y "build\libs\Lunarix-Client-1.21.11-1.0.jar" "%~dp0mods\Lunarix-Client-1.21.11-1.0.jar"
        copy /y "build\libs\Lunarix-Client-1.21.11-1.0.jar" "%APPDATA%\.minecraft\mods\Lunarix-Client-1.21.11-1.0.jar"
    )
)
echo.
echo ========================================================
echo  Lunarix Client успішно оновлено в папці та лаунчері!
echo ========================================================
pause
