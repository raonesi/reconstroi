@echo off
powershell -NoProfile -File "%~dp0Abrir-Banco.ps1"
if errorlevel 1 pause
