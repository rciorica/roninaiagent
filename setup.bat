@echo off
REM Ronin Multi-Platform Setup Script for Windows

echo.
echo ========================================
echo  Ronin AI Agent - Setup Script
echo ========================================
echo.

REM Check if Node.js is installed
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not installed. Please install Node.js 16+ from https://nodejs.org/
    pause
    exit /b 1
)

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed. Please install Java 11+ from https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed. Please install Maven from https://maven.apache.org/install.html
    pause
    exit /b 1
)

echo [✓] Detected Node.js: 
node --version

echo [✓] Detected Java: 
java -version 2>&1 | findstr /R "version"

echo [✓] Detected Maven: 
mvn --version | findstr /R "Apache Maven"

echo.
echo Installing dependencies...
echo.

REM Install root dependencies
echo [1/3] Installing root dependencies...
call npm install
if %errorlevel% neq 0 goto error

REM Install frontend dependencies
echo [2/3] Installing frontend dependencies...
cd frontend
call npm install
if %errorlevel% neq 0 goto error_frontend
cd ..

REM Install desktop dependencies
echo [3/3] Installing desktop dependencies...
cd desktop
call npm install
if %errorlevel% neq 0 goto error_desktop
cd ..

echo.
echo ========================================
echo  Setup Complete!
echo ========================================
echo.
echo Available commands:
echo.
echo   npm run dev              - Start all services (backend, web, desktop)
echo   npm run dev:backend      - Start only backend
echo   npm run dev:web          - Start only web app
echo   npm run dev:desktop      - Start only desktop app
echo.
echo   npm run build            - Build all components
echo   npm run test             - Run all tests
echo.
echo   npm run desktop:dist     - Create Windows installers
echo   npm run desktop:portable - Create portable .exe
echo.
echo For more information, see MULTI_PLATFORM.md
echo.
pause
exit /b 0

:error
echo [ERROR] Failed to install root dependencies
cd ..
goto error_end

:error_frontend
echo [ERROR] Failed to install frontend dependencies
cd ..
goto error_end

:error_desktop
echo [ERROR] Failed to install desktop dependencies
cd ..
goto error_end

:error_end
echo.
echo Please check the error messages above and try again.
pause
exit /b 1
