#!/bin/bash

# Heroku Deployment Script
# This script deploys both frontend and backend to Heroku
# Usage: ./deploy-to-heroku.sh

set -e

echo "🚀 Ronin Heroku Deployment Script"
echo "=================================="
echo ""

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo "❌ Git is not installed. Please install Git."
    exit 1
fi

# Check if Heroku CLI is installed
if ! command -v heroku &> /dev/null; then
    echo "❌ Heroku CLI is not installed. Please install it from https://devcenter.heroku.com/articles/heroku-cli"
    exit 1
fi

# Read app names
if [ ! -f .heroku-backend ] || [ ! -f .heroku-frontend ]; then
    echo "❌ Setup files not found. Run ./heroku-setup.sh and ./heroku-config.sh first"
    exit 1
fi

BACKEND_APP=$(cat .heroku-backend)
FRONTEND_APP=$(cat .heroku-frontend)

echo "Backend app: $BACKEND_APP"
echo "Frontend app: $FRONTEND_APP"
echo ""

# Add Heroku remotes if they don't exist
echo "📡 Setting up git remotes..."
git remote remove heroku-backend 2>/dev/null || true
git remote remove heroku-frontend 2>/dev/null || true
git remote add heroku-backend "https://git.heroku.com/$BACKEND_APP.git"
git remote add heroku-frontend "https://git.heroku.com/$FRONTEND_APP.git"

# Ensure git is clean
echo "🔍 Checking git status..."
if [ -n "$(git status --short)" ]; then
    echo "⚠️  You have uncommitted changes. Please commit them first:"
    echo ""
    git status
    exit 1
fi

# Deploy backend
echo ""
echo "📦 Deploying backend to $BACKEND_APP..."
git subtree push --prefix backend heroku-backend master:main 2>&1 | tail -20

# Deploy frontend
echo ""
echo "📦 Deploying frontend to $FRONTEND_APP..."
git subtree push --prefix frontend heroku-frontend master:main 2>&1 | tail -20

echo ""
echo "✅ Deployment complete!"
echo ""
echo "🔗 URLs:"
echo "   Backend:  https://$BACKEND_APP.herokuapp.com"
echo "   Frontend: https://$FRONTEND_APP.herokuapp.com"
echo ""
echo "📊 View logs:"
echo "   Backend:  heroku logs --app $BACKEND_APP --tail"
echo "   Frontend: heroku logs --app $FRONTEND_APP --tail"
echo ""
