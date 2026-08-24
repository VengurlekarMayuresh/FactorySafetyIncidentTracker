@echo off
echo ============================================
echo  Starting Jenkins CI Server
echo  Factory Safety Incident Tracker Project
echo ============================================
echo.

REM Set Jenkins home directory (stores jobs, plugins, config)
set JENKINS_HOME=%~dp0jenkins_home

REM Create JENKINS_HOME if it doesn't exist
if not exist "%JENKINS_HOME%" mkdir "%JENKINS_HOME%"

echo Jenkins Home: %JENKINS_HOME%
echo Jenkins URL:  http://localhost:8082
echo.
echo Starting Jenkins on port 8082...
echo Press Ctrl+C to stop Jenkins.
echo.

java -jar "%~dp0jenkins.war" --httpPort=8082 --prefix=/jenkins
