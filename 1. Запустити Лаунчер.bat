@echo off
chcp 65001 >nul
echo ========================================================
echo           Запуск Lunarix Client через Лаунчер
echo ========================================================
echo.
if exist "%~dp0Lunarix Launcher.exe" (
    start "" "%~dp0Lunarix Launcher.exe"
    exit
)
echo У лаунчері Legacy Launcher:
echo  1. У списку версій або профілів виберіть:
echo     "Lunarix Client 1.21.11"
echo  2. Натисніть "Увійти в гру".
echo.
start "" "%APPDATA%\.tlauncher\legacy\Minecraft\LL.exe"
exit
