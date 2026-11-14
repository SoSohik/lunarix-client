@echo off
chcp 65001 >nul
title Lunarix Client 1.21.11

set "JAVA_EXE=C:\Program Files\Java\jdk-21\bin\javaw.exe"
if not exist "%JAVA_EXE%" (
    set "JAVA_EXE=C:\Program Files\Java\jdk-21\bin\java.exe"
)
if not exist "%JAVA_EXE%" (
    where javaw >nul 2>&1 && set "JAVA_EXE=javaw" || set "JAVA_EXE=java"
)

echo ========================================================
echo               Lunarix Client 1.21.11 (Fabric)
echo ========================================================
echo Запуск клієнта Minecraft 1.21.11 з модами:
echo  - Lunarix Client 1.0
echo  - Fabric API 0.141.6
echo  - Sodium 0.8.14
echo.
echo Java: "%JAVA_EXE%"
echo Game Directory: "%~dp0."
echo ========================================================

if exist "%~dp0Lunarix Launcher.exe" (
    start "" "%~dp0Lunarix Launcher.exe"
    exit
)

echo Запуск через Legacy Launcher...
start "" "%APPDATA%\.tlauncher\legacy\Minecraft\LL.exe"
exit
