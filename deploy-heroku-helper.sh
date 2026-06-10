#!/bin/bash

# Heroku monorepo helper deployment script
# This script deploys backend and frontend separately using git subtree and enforces the correct Heroku buildpacks.
# Usage: ./deploy-heroku-helper.sh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

if ! command -v git >/dev/null 2>&1; then
  echo "❌ Git is not installed. Install Git first."
  exit 1
fi

if ! command -v heroku >/dev/null 2>&1; then
  echo "❌ Heroku CLI is not installed. Install it from https://devcenter.heroku.com/articles/heroku-cli"
  exit 1
fi

if [ -n "$(git status --short)" ]; then
  echo "⚠️  You have uncommitted changes. Please commit them before deploying."
  git status --short
  exit 1
fi

if [ ! -f .heroku-backend ] || [ ! -f .heroku-frontend ]; then
  echo "❌ Heroku setup files not found. Run ./heroku-setup.sh first."
  exit 1
fi

BACKEND_APP="$(cat .heroku-backend | tr -d '\r\n')"
FRONTEND_APP="$(cat .heroku-frontend | tr -d '\r\n')"

echo "🚀 Deploying Ronin monorepo to Heroku"
echo "Backend app:  $BACKEND_APP"
echo "Frontend app: $FRONTEND_APP"
echo ""

echo "🔧 Ensuring correct Heroku buildpacks..."
heroku buildpacks:set heroku/java --app "$BACKEND_APP"
heroku buildpacks:set heroku/nodejs --app "$FRONTEND_APP"

echo "📡 Configuring git remotes..."
git remote remove heroku-backend 2>/dev/null || true
git remote remove heroku-frontend 2>/dev/null || true
git remote add heroku-backend "https://git.heroku.com/$BACKEND_APP.git"
git remote add heroku-frontend "https://git.heroku.com/$FRONTEND_APP.git"

echo ""
echo "📦 Deploying backend subtree..."
git subtree push --prefix backend heroku-backend master:main

echo ""
echo "📦 Deploying frontend subtree..."
git subtree push --prefix frontend heroku-frontend master:main

echo ""
echo "✅ Deployment completed."
echo "Backend URL:  https://$BACKEND_APP.herokuapp.com"
echo "Frontend URL: https://$FRONTEND_APP.herokuapp.com"
echo ""
echo "If you need logs, run:"
echo "  heroku logs --app $BACKEND_APP --tail"
echo "  heroku logs --app $FRONTEND_APP --tail"
