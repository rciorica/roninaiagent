#!/bin/bash

# Heroku Configuration Script
# This script sets environment variables for Heroku apps
# Usage: ./heroku-config.sh

set -e

echo "⚙️  Ronin Heroku Configuration Script"
echo "======================================"
echo ""

# Check if Heroku CLI is installed
if ! command -v heroku &> /dev/null; then
    echo "❌ Heroku CLI is not installed. Please install it from https://devcenter.heroku.com/articles/heroku-cli"
    exit 1
fi

# Read app names from setup
if [ ! -f .heroku-backend ] || [ ! -f .heroku-frontend ]; then
    echo "❌ Setup files not found. Run ./heroku-setup.sh first"
    exit 1
fi

BACKEND_APP=$(cat .heroku-backend)
FRONTEND_APP=$(cat .heroku-frontend)

echo "Backend app: $BACKEND_APP"
echo "Frontend app: $FRONTEND_APP"
echo ""

# Get Heroku PostgreSQL URL (it's automatically set as DATABASE_URL)
echo "✅ DATABASE_URL is automatically set by Heroku PostgreSQL addon"
echo ""

# Configure backend environment variables
echo "🔧 Configuring backend environment variables..."
echo ""

read -sp "Enter OpenRouter API Key: " OPENROUTER_KEY
echo ""
read -p "Enter Google OAuth Client ID: " GOOGLE_ID
read -sp "Enter Google OAuth Client Secret: " GOOGLE_SECRET
echo ""

heroku config:set \
  OPENROUTER_API_KEY="$OPENROUTER_KEY" \
  GOOGLE_CLIENT_ID="$GOOGLE_ID" \
  GOOGLE_CLIENT_SECRET="$GOOGLE_SECRET" \
  GOOGLE_REDIRECT_URI="https://$BACKEND_APP.herokuapp.com/auth/oauth2/callback/google" \
  FRONTEND_OAUTH_SUCCESS_URL="https://$FRONTEND_APP.herokuapp.com/" \
  CORS_ALLOWED_ORIGINS="https://$FRONTEND_APP.herokuapp.com" \
  --app "$BACKEND_APP"

echo ""
echo "✅ Backend configuration complete!"
echo ""

# Configure frontend environment variables
echo "🔧 Configuring frontend environment variables..."
heroku config:set \
  VITE_API_URL="https://$BACKEND_APP.herokuapp.com" \
  --app "$FRONTEND_APP"

echo ""
echo "✅ Frontend configuration complete!"
echo ""

# Show configured variables
echo "📋 Backend Configuration:"
heroku config --app "$BACKEND_APP" | grep -E "(OPENROUTER|GOOGLE|FRONTEND|CORS|DATABASE)" || true

echo ""
echo "📋 Frontend Configuration:"
heroku config --app "$FRONTEND_APP" | grep -E "VITE" || true

echo ""
echo "✅ All configurations complete!"
echo ""
echo "Next step: Run ./deploy-to-heroku.sh"
echo ""
