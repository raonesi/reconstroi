@echo off
cd /d "%~dp0"
set "CRUD_JAVA=javaw.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" set "CRUD_JAVA=%JAVA_HOME%\bin\javaw.exe"
if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot\bin\javaw.exe" set "CRUD_JAVA=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot\bin\javaw.exe"
if not exist "CrudBasico.jar" (
    echo Compile o projeto primeiro: powershell -File Compilar.ps1
    pause
    exit /b 1
)
start "CRUD basico MVC" "%CRUD_JAVA%" -jar "CrudBasico.jar"
