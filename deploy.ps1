# Heroku Deployment Script for Windows PowerShell
# This script adds Git and Heroku CLI to PATH and deploys to Heroku
# Usage: .\deploy.ps1

param(
    [string]$BackendApp = "ronin-backend",
    [string]$FrontendApp = "ronin-frontend"
)

# Add Git and Heroku to PATH
$env:Path = "C:\Program Files\Git\bin;C:\Program Files\heroku\bin;" + $env:Path

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-ErrorMsg {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Verify tools
Write-Header "Verifying Prerequisites"

try {
    & "C:\Program Files\Git\bin\git.exe" --version | Out-Null
    Write-Success "Git is installed"
} catch {
    Write-ErrorMsg "Git not found at C:\Program Files\Git\bin"
    exit 1
}

try {
    & "C:\Program Files\heroku\bin\heroku.cmd" --version | Out-Null
    Write-Success "Heroku CLI is installed"
} catch {
    Write-ErrorMsg "Heroku CLI not found at C:\Program Files\heroku\bin"
    exit 1
}

# Navigate to project
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot
Write-Success "Working directory: $ProjectRoot"

# Check git status
Write-Header "Checking Git Status"
$gitStatus = & "C:\Program Files\Git\bin\git.exe" status --short
if ($gitStatus) {
    Write-Host "Uncommitted changes detected:" -ForegroundColor Yellow
    Write-Host $gitStatus
    Write-Host ""
    $commit = Read-Host "Commit these changes? (y/n)"
    if ($commit -eq 'y') {
        & "C:\Program Files\Git\bin\git.exe" add .
        $message = Read-Host "Commit message"
        & "C:\Program Files\Git\bin\git.exe" commit -m $message
        Write-Success "Changes committed"
    } else {
        Write-ErrorMsg "Please commit changes before deploying"
        exit 1
    }
} else {
    Write-Success "Git repository is clean"
}

# Create/Save app names
Write-Header "Heroku Apps Configuration"
Write-Host "Backend app: $BackendApp" -ForegroundColor Yellow
Write-Host "Frontend app: $FrontendApp" -ForegroundColor Yellow

$BackendApp | Out-File -FilePath ".heroku-backend" -Encoding UTF8 -NoNewline
$FrontendApp | Out-File -FilePath ".heroku-frontend" -Encoding UTF8 -NoNewline
Write-Success "App names saved"

# Create backend app
Write-Header "Creating Backend App"
Write-Host "Running: heroku apps:create $BackendApp --buildpack heroku/java"
& "C:\Program Files\heroku\bin\heroku.cmd" apps:create $BackendApp --buildpack heroku/java 2>&1 | Where-Object { $_ -notmatch "is already taken" }
Write-Success "Backend app created or already exists: $BackendApp"

# Create frontend app
Write-Header "Creating Frontend App"
Write-Host "Running: heroku apps:create $FrontendApp --buildpack heroku/nodejs"
& "C:\Program Files\heroku\bin\heroku.cmd" apps:create $FrontendApp --buildpack heroku/nodejs 2>&1 | Where-Object { $_ -notmatch "is already taken" }
Write-Success "Frontend app created or already exists: $FrontendApp"

# Add PostgreSQL database
Write-Header "Adding PostgreSQL Database"
Write-Host "Running: heroku addons:create heroku-postgresql:mini --app $BackendApp"
& "C:\Program Files\heroku\bin\heroku.cmd" addons:create heroku-postgresql:mini --app $BackendApp 2>&1 | Where-Object { $_ -notmatch "already in progress" }
Write-Success "PostgreSQL addon created or already exists"

# Get database URL
Write-Header "Retrieving Database Configuration"
$dbUrl = & "C:\Program Files\heroku\bin\heroku.cmd" config:get DATABASE_URL --app $BackendApp
if ($dbUrl) {
    Write-Success "Database configured: $($dbUrl.Substring(0, 30))..."
} else {
    Write-Host "Database URL not yet available (may take a moment)" -ForegroundColor Yellow
}

# Configure Backend Environment Variables
Write-Header "Configuring Backend Environment Variables"

$openrouterKey = Read-Host "Enter OpenRouter API Key (or press Enter to skip)"
$googleId = Read-Host "Enter Google OAuth Client ID (or press Enter to skip)"
$googleSecret = Read-Host "Enter Google OAuth Client Secret (or press Enter to skip)"

Write-Host ""
Write-Host "Setting backend config variables..." -ForegroundColor Yellow

if ($openrouterKey) {
    & "C:\Program Files\heroku\bin\heroku.cmd" config:set OPENROUTER_API_KEY=$openrouterKey --app $BackendApp | Out-Null
}
if ($googleId) {
    & "C:\Program Files\heroku\bin\heroku.cmd" config:set GOOGLE_CLIENT_ID=$googleId --app $BackendApp | Out-Null
}
if ($googleSecret) {
    & "C:\Program Files\heroku\bin\heroku.cmd" config:set GOOGLE_CLIENT_SECRET=$googleSecret --app $BackendApp | Out-Null
}

& "C:\Program Files\heroku\bin\heroku.cmd" config:set `
    GOOGLE_REDIRECT_URI="https://$BackendApp.herokuapp.com/auth/oauth2/callback/google" `
    FRONTEND_OAUTH_SUCCESS_URL="https://$FrontendApp.herokuapp.com/" `
    CORS_ALLOWED_ORIGINS="https://$FrontendApp.herokuapp.com" `
    --app $BackendApp | Out-Null

Write-Success "Backend environment variables configured"

# Configure Frontend Environment Variables
Write-Header "Configuring Frontend Environment Variables"
Write-Host "Running: heroku config:set VITE_API_URL=https://$BackendApp.herokuapp.com --app $FrontendApp"
& "C:\Program Files\heroku\bin\heroku.cmd" config:set VITE_API_URL="https://$BackendApp.herokuapp.com" --app $FrontendApp | Out-Null
Write-Success "Frontend environment variables configured"

# Add git remotes
Write-Header "Setting Up Git Remotes"
& "C:\Program Files\Git\bin\git.exe" remote remove heroku-backend 2>$null
& "C:\Program Files\Git\bin\git.exe" remote remove heroku-frontend 2>$null
& "C:\Program Files\Git\bin\git.exe" remote add heroku-backend "https://git.heroku.com/$BackendApp.git"
& "C:\Program Files\Git\bin\git.exe" remote add heroku-frontend "https://git.heroku.com/$FrontendApp.git"
Write-Success "Git remotes configured"

# Deploy Backend
Write-Header "Deploying Backend to $BackendApp"
Write-Host "Running: git subtree push --prefix backend heroku-backend master:main"
& "C:\Program Files\Git\bin\git.exe" subtree push --prefix backend heroku-backend master:main
if ($LASTEXITCODE -eq 0) {
    Write-Success "Backend deployed successfully"
} else {
    Write-ErrorMsg "Backend deployment failed (exit code: $LASTEXITCODE)"
    exit 1
}

# Deploy Frontend
Write-Header "Deploying Frontend to $FrontendApp"
Write-Host "Running: git subtree push --prefix frontend heroku-frontend master:main"
& "C:\Program Files\Git\bin\git.exe" subtree push --prefix frontend heroku-frontend master:main
if ($LASTEXITCODE -eq 0) {
    Write-Success "Frontend deployed successfully"
} else {
    Write-ErrorMsg "Frontend deployment failed (exit code: $LASTEXITCODE)"
    exit 1
}

# Final status
Write-Header "Deployment Complete"
Write-Host ""
Write-Host "Application URLs:" -ForegroundColor Green
Write-Host "   Backend:  https://$BackendApp.herokuapp.com" -ForegroundColor Yellow
Write-Host "   Frontend: https://$FrontendApp.herokuapp.com" -ForegroundColor Yellow
Write-Host ""
Write-Host "View Logs:" -ForegroundColor Green
Write-Host "   Backend:  heroku logs --app $BackendApp --tail" -ForegroundColor Yellow
Write-Host "   Frontend: heroku logs --app $FrontendApp --tail" -ForegroundColor Yellow
Write-Host ""
Write-Host "Next Deploy:" -ForegroundColor Green
Write-Host "   git subtree push --prefix backend heroku-backend master:main" -ForegroundColor Yellow
Write-Host "   git subtree push --prefix frontend heroku-frontend master:main" -ForegroundColor Yellow
Write-Host ""
