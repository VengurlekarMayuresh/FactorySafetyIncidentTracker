@echo off
REM ============================================================
REM  Factory Safety Incident Tracker — Deploy Script
REM  Usage: deploy.bat [dev|staging] [port]
REM  Example: deploy.bat dev 8080
REM           deploy.bat staging 8081
REM ============================================================

REM Prevent Jenkins from terminating background process upon build finish
set JENKINS_NODE_COOKIE=dontKillMe
set BUILD_ID=dontKillMe

set ENV=%1
set PORT=%2

IF "%ENV%"=="" (
    echo [ERROR] Please specify environment: deploy.bat [dev^|staging] [port]
    exit /b 1
)
IF "%PORT%"=="" (
    IF "%ENV%"=="dev" set PORT=8080
    IF "%ENV%"=="staging" set PORT=8081
)

set DEPLOY_DIR=C:\deploy\%ENV%
set JAR_NAME=incident-tracker-0.0.1-SNAPSHOT.jar
set APP_JAR=%DEPLOY_DIR%\incident-tracker-%ENV%.jar
set LOG_FILE=%DEPLOY_DIR%\app.log

echo ============================================
echo  Deploying Incident Tracker
echo  Environment : %ENV%
echo  Port        : %PORT%
echo  Deploy Dir  : %DEPLOY_DIR%
echo ============================================

REM Create deploy directory
if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"

REM Check if JAR already built, otherwise build it
if not exist "target\%JAR_NAME%" (
    echo [1/3] Building application...
    call mvnw.cmd package -DskipTests -Dspring.profiles.active=%ENV%
    IF ERRORLEVEL 1 (
        echo [ERROR] Build FAILED. Aborting deploy.
        exit /b 1
    )
) else (
    echo [1/3] Using existing target\%JAR_NAME% artifact...
)

REM Copy JAR
echo [2/3] Copying JAR to %DEPLOY_DIR%...
copy /Y "target\%JAR_NAME%" "%APP_JAR%"

REM Stop any existing app on that port
echo [3/3] Stopping any existing process on port %PORT%...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%PORT% ^| findstr LISTENING 2^>nul') do (
    echo Killing existing PID %%a
    taskkill /PID %%a /F >nul 2>&1
)

REM Allow port release
timeout /t 2 /nobreak >nul 2>&1

REM Start the application as an independent background daemon
echo Starting application on port %PORT%...
start "SafetyTracker-%ENV%" javaw -jar "%APP_JAR%" --server.port=%PORT% --spring.profiles.active=%ENV% > "%LOG_FILE%" 2>&1

echo.
echo ============================================
echo  Application background startup initiated!
echo  URL  : http://localhost:%PORT%/incidents
echo  Logs : %LOG_FILE%
echo ============================================
