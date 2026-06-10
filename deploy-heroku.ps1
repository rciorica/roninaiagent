# Heroku Deployment Script for Windows PowerShell
# Usage: .\deploy-heroku.ps1

param(
    [string]$BackendApp = "ronin-backend",
    [string]$FrontendApp = "ronin-frontend"
)

$herokuInstallDir = "C:\Program Files\heroku\bin"
if (Test-Path $herokuInstallDir) {
    $env:Path = "$herokuInstallDir;$env:Path"
}

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Test-Command {
    param([string]$Command)
    try {
        & $Command --version | Out-Null
        return $true
    } catch {
        return $false
    }
}

# Verify tools
Write-Header "Verifying Prerequisites"

if (-not (Test-Command git)) {
    Write-Error "Git not found. Please install Git from https://git-scm.com/download/win"
    exit 1
}
Write-Success "Git is installed"

if (-not (Test-Command heroku)) {
    Write-Error "Heroku CLI not found. Please install from https://devcenter.heroku.com/articles/heroku-cli"
    exit 1
}
Write-Success "Heroku CLI is installed"

# Navigate to project
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot
Write-Success "Working directory: $ProjectRoot"

# Check git status
Write-Header "Checking Git Status"
$gitStatus = git status --short
if ($gitStatus) {
    Write-Host "Uncommitted changes detected:" -ForegroundColor Yellow
    Write-Host $gitStatus
    Write-Host ""
    $commit = Read-Host "Commit these changes? (y/n)"
    if ($commit -eq 'y') {
        git add .
        $message = Read-Host "Commit message"
        git commit -m $message
        Write-Success "Changes committed"
    } else {
        Write-Error "Please commit changes before deploying"
        exit 1
    }
}
Write-Success "Git repository is clean"

# Create/Save app names
Write-Header "Heroku Apps Configuration"
Write-Host "Backend app: $BackendApp" -ForegroundColor Yellow
Write-Host "Frontend app: $FrontendApp" -ForegroundColor Yellow

$BackendApp | Out-File -FilePath ".heroku-backend" -Encoding UTF8 -NoNewline
$FrontendApp | Out-File -FilePath ".heroku-frontend" -Encoding UTF8 -NoNewline
Write-Success "App names saved"

# Create backend app
Write-Header "Creating Backend App"
try {
    heroku apps:create $BackendApp --buildpack heroku/java 2>&1 | Where-Object { $_ -notmatch "is already taken" }
    Write-Success "Backend app created or already exists"
} catch {
    Write-Error "Failed to create backend app"
    exit 1
}

# Create frontend app
Write-Header "Creating Frontend App"
try {
    heroku apps:create $FrontendApp --buildpack heroku/nodejs 2>&1 | Where-Object { $_ -notmatch "is already taken" }
    Write-Success "Frontend app created or already exists"
} catch {
    Write-Error "Failed to create frontend app"
    exit 1
}

# Add PostgreSQL database
Write-Header "Adding PostgreSQL Database"
try {
    heroku addons:create heroku-postgresql:mini --app $BackendApp 2>&1 | Where-Object { $_ -notmatch "already in progress" }
    Write-Success "PostgreSQL addon created or already exists"
} catch {
    Write-Host "Database may already exist" -ForegroundColor Yellow
}

# Get database URL
Write-Header "Retrieving Database Configuration"
$dbUrl = heroku config:get DATABASE_URL --app $BackendApp
if ($dbUrl) {
    Write-Success "Database configured: $($dbUrl.Substring(0, 30))..."
} else {
    Write-Host "Database URL not yet available (may take a moment)" -ForegroundColor Yellow
}

# Retrieve actual Heroku app URLs
Write-Header "Retrieving actual Heroku app URLs"
$backendInfoJson = heroku apps:info --json --app $BackendApp
$backendUrl = (ConvertFrom-Json $backendInfoJson).web_url.TrimEnd('/')
$frontendInfoJson = heroku apps:info --json --app $FrontendApp
$frontendUrl = (ConvertFrom-Json $frontendInfoJson).web_url.TrimEnd('/')
Write-Success "Backend URL: $backendUrl"
Write-Success "Frontend URL: $frontendUrl"

# Configure Backend Environment Variables
Write-Header "Configuring Backend Environment Variables"

$openrouterKey = Read-Host "Enter OpenRouter API Key (or press Enter to skip)"
$googleId = Read-Host "Enter Google OAuth Client ID (or press Enter to skip)"
$googleSecret = Read-Host "Enter Google OAuth Client Secret (or press Enter to skip)"

$configVars = @{
    "GOOGLE_REDIRECT_URI" = "$backendUrl/auth/oauth2/callback/google"
    "FRONTEND_OAUTH_SUCCESS_URL" = "$frontendUrl/"
    "CORS_ALLOWED_ORIGINS" = "$frontendUrl"
}

if ($openrouterKey) {
    $configVars["OPENROUTER_API_KEY"] = $openrouterKey
}
if ($googleId) {
    $configVars["GOOGLE_CLIENT_ID"] = $googleId
}
if ($googleSecret) {
    $configVars["GOOGLE_CLIENT_SECRET"] = $googleSecret
}

# Set backend config
$configArgs = @($BackendApp)
foreach ($key in $configVars.Keys) {
    $configArgs += "$key=$($configVars[$key])"
}
$configArgs += "--app"
$configArgs += $BackendApp

heroku config:set @$configArgs
Write-Success "Backend environment variables configured"

# Configure Frontend Environment Variables
Write-Header "Configuring Frontend Environment Variables"
heroku config:set VITE_API_URL="$backendUrl" --app $FrontendApp
Write-Success "Frontend environment variables configured"

# Add git remotes
Write-Header "Setting Up Git Remotes"
try {
    git remote remove heroku-backend 2>$null
    git remote remove heroku-frontend 2>$null
    git remote add heroku-backend "https://git.heroku.com/$BackendApp.git"
    git remote add heroku-frontend "https://git.heroku.com/$FrontendApp.git"
    Write-Success "Git remotes configured"
} catch {
    Write-Error "Failed to configure git remotes"
    exit 1
}

# Deploy Backend
Write-Header "Deploying Backend"
Write-Host "Running: git subtree push --prefix backend heroku-backend master:main"
git subtree push --prefix backend heroku-backend master:main
if ($LASTEXITCODE -eq 0) {
    Write-Success "Backend deployed successfully"
} else {
    Write-Error "Backend deployment failed"
    exit 1
}

# Deploy Frontend
Write-Header "Deploying Frontend"
Write-Host "Running: git subtree push --prefix frontend heroku-frontend master:main"
git subtree push --prefix frontend heroku-frontend master:main
if ($LASTEXITCODE -eq 0) {
    Write-Success "Frontend deployed successfully"
} else {
    Write-Error "Frontend deployment failed"
    exit 1
}

# Final status
Write-Header "Deployment Complete!"
Write-Host ""
Write-Host "Application URLs:" -ForegroundColor Green
Write-Host "   Backend:  https://$BackendApp.herokuapp.com" -ForegroundColor Yellow
Write-Host "   Frontend: https://$FrontendApp.herokuapp.com" -ForegroundColor Yellow
Write-Host ""
Write-Host "View Logs:" -ForegroundColor Green
Write-Host "   heroku logs --app $BackendApp --tail" -ForegroundColor Yellow
Write-Host "   heroku logs --app $FrontendApp --tail" -ForegroundColor Yellow
Write-Host ""
Write-Host "Next Deploy:" -ForegroundColor Green
Write-Host "   git subtree push --prefix backend heroku-backend master:main" -ForegroundColor Yellow
Write-Host "   git subtree push --prefix frontend heroku-frontend master:main" -ForegroundColor Yellow
Write-Host ""
