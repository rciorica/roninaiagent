# Heroku Monorepo Deployment Script for Windows PowerShell
# This script deploys backend and frontend separately to Heroku using git subtree
# Usage: .\deploy-monorepo.ps1

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
    Write-ErrorMsg "Git not found"
    exit 1
}

# Navigate to project
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot
Write-Success "Working directory: $ProjectRoot"

# Set up git remotes
Write-Header "Setting Up Git Remotes"
& "C:\Program Files\Git\bin\git.exe" remote remove heroku-backend 2>$null
& "C:\Program Files\Git\bin\git.exe" remote remove heroku-frontend 2>$null
& "C:\Program Files\Git\bin\git.exe" remote add heroku-backend "https://git.heroku.com/$BackendApp.git"
& "C:\Program Files\Git\bin\git.exe" remote add heroku-frontend "https://git.heroku.com/$FrontendApp.git"
Write-Success "Git remotes configured"

# Deploy Backend using git subtree
Write-Header "Deploying Backend to $BackendApp"
Write-Host "Running: git subtree push --prefix backend heroku-backend main"
& "C:\Program Files\Git\bin\git.exe" subtree push --prefix backend heroku-backend main
if ($LASTEXITCODE -eq 0) {
    Write-Success "Backend deployed successfully"
} else {
    Write-ErrorMsg "Backend deployment failed (exit code: $LASTEXITCODE)"
    Write-Host ""
    Write-Host "Tip: Check logs with: heroku logs --app $BackendApp --tail" -ForegroundColor Yellow
    # Don't exit, try frontend anyway
}

# Deploy Frontend using git subtree
Write-Header "Deploying Frontend to $FrontendApp"
Write-Host "Running: git subtree push --prefix frontend heroku-frontend main"
& "C:\Program Files\Git\bin\git.exe" subtree push --prefix frontend heroku-frontend main
if ($LASTEXITCODE -eq 0) {
    Write-Success "Frontend deployed successfully"
} else {
    Write-ErrorMsg "Frontend deployment failed (exit code: $LASTEXITCODE)"
    Write-Host ""
    Write-Host "Tip: Check logs with: heroku logs --app $FrontendApp --tail" -ForegroundColor Yellow
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
Write-Host "   .\deploy-monorepo.ps1" -ForegroundColor Yellow
Write-Host ""
