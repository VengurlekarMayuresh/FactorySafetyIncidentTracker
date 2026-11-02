@echo off
title SafeGuard Pro - Selenium Automated Incident Pipeline
color 0A
cls
echo ======================================================================
echo          SafeGuard Pro - Automated Selenium Incident Pipeline
echo ======================================================================
echo.
echo [1/3] Initializing Selenium WebDriver with Chrome (Headless)...
echo [2/3] Automating User Journey:
echo        - Login to SafeGuard Pro Portal
echo        - Navigate to Log New Incident Form
echo        - Enter Incident Information (Title, Description, Location, Severity)
echo        - Attach Photo Evidence (test-hazard.png)
echo        - Submit and Post Incident to Dashboard
echo        - Verify Incident and Photo rendered on Dashboard
echo.
echo [3/3] Running Selenium Automation via Maven...
echo ----------------------------------------------------------------------

cd /d "%~dp0FactorySafetyIncidentTracker"
call .\mvnw.cmd test -Dtest=IncidentTrackerE2ETest#testCreateNewIncidentJourney

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================================================
    echo   PIPELINE SUCCESS: Incident with Photo successfully posted!
    echo ======================================================================
    echo   Evidence screenshots saved to: 
    echo   %~dp0FactorySafetyIncidentTracker\target\screenshots\
    echo ======================================================================
) else (
    echo.
    echo ======================================================================
    echo   PIPELINE FAILED: Check logs and screenshots above.
    echo ======================================================================
)

echo.
pause
