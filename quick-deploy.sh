#!/bin/bash

# Quick deploy script (after initial setup)
# This script quickly deploys to existing Heroku apps
# Usage: ./quick-deploy.sh

set -e

echo "⚡ Quick Deploy to Heroku"
echo "========================="
echo ""

if [ ! -f .heroku-backend ] || [ ! -f .heroku-frontend ]; then
    echo "❌ Setup files not found. Run ./heroku-setup.sh first"
    exit 1
fi

BACKEND_APP=$(cat .heroku-backend)
FRONTEND_APP=$(cat .heroku-frontend)

# Check for uncommitted changes
if [ -n "$(git status --short)" ]; then
    echo "⚠️  Uncommitted changes detected. Commit first:"
    git status
    exit 1
fi

echo "Deploying to:"
echo "  Backend:  $BACKEND_APP"
echo "  Frontend: $FRONTEND_APP"
echo ""

# Deploy
git push heroku-backend main
git push heroku-frontend main

echo ""
echo "✅ Deployed!"
