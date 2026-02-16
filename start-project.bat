@echo off
setlocal
setlocal EnableDelayedExpansion

set "ROOT=%~dp0"
set "BACKEND_DIR=%ROOT%Pet'sHeavenBackend\AniProjectBackend"
set "FRONTEND_DIR=%ROOT%ani-home"

echo Starting Pets Heaven Project...
echo.

if not exist "%BACKEND_DIR%" (
  echo Backend directory not found: %BACKEND_DIR%
  exit /b 1
)

if not exist "%FRONTEND_DIR%" (
  echo Frontend directory not found: %FRONTEND_DIR%
  exit /b 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
  if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-17"
  ) else if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
  )
)

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo JAVA_HOME is not set to a valid JDK installation.
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Using JAVA_HOME: %JAVA_HOME%
echo.

echo Stopping stale project processes...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-CimInstance Win32_Process -Filter \"name='java.exe'\" | Where-Object { $_.CommandLine -like '*AniHome-api-0.0.1-SNAPSHOT.jar*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-CimInstance Win32_Process -Filter \"name='node.exe'\" | Where-Object { $_.CommandLine -like '*ani-home*' -and $_.CommandLine -like '*react-scripts*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }"
timeout /t 2 /nobreak >nul

echo Starting Backend (Spring Boot)...
start "Backend" cmd /k "cd /d \"%BACKEND_DIR%\" && mvnw.cmd -Djava.version=17 -DskipTests package && java -jar target\AniHome-api-0.0.1-SNAPSHOT.jar"

echo Waiting for backend on port 8080 (up to 150s)...
set "BACKEND_READY="
call :wait_for_port 8080 150 BACKEND_READY
if not defined BACKEND_READY (
  echo Backend did not become reachable on port 8080.
  echo Check the Backend terminal window for errors.
)

echo Starting Frontend (React)...
start "Frontend" cmd /k "cd /d \"%FRONTEND_DIR%\" && (if not exist node_modules (npm install)) && npm start"

echo Waiting for frontend on port 3000 (up to 120s)...
set "FRONTEND_READY="
call :wait_for_port 3000 120 FRONTEND_READY
if not defined FRONTEND_READY (
  echo Frontend did not become reachable on port 3000.
  echo Check the Frontend terminal window for errors.
)

echo.
if defined BACKEND_READY if defined FRONTEND_READY (
  echo Both services are running.
) else (
  echo One or more services failed to start.
)
echo Backend: http://localhost:8080
echo Frontend: http://localhost:3000

exit /b 0

:wait_for_port
set "WAIT_PORT=%~1"
set "WAIT_TIMEOUT=%~2"
set "WAIT_RESULT_VAR=%~3"
set "WAIT_FOUND="

for /L %%i in (1,1,%WAIT_TIMEOUT%) do (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$client = New-Object Net.Sockets.TcpClient; try { $iar = $client.BeginConnect('127.0.0.1', %WAIT_PORT%, $null, $null); if($iar.AsyncWaitHandle.WaitOne(500) -and $client.Connected){ exit 0 } else { exit 1 } } catch { exit 1 } finally { if($client){ $client.Close() } }" >nul 2>&1
  if not errorlevel 1 (
    set "WAIT_FOUND=1"
    goto :wait_done
  )
  timeout /t 1 /nobreak >nul
)

:wait_done
if defined WAIT_FOUND set "%WAIT_RESULT_VAR%=1"
exit /b 0
