#!/bin/bash

# Heroku Deployment Setup Script
# This script initializes Heroku apps for both frontend and backend
# Usage: ./heroku-setup.sh

set -e

echo "🎌 Ronin Heroku Setup Script"
echo "=============================="
echo ""

# Check if Heroku CLI is installed
if ! command -v heroku &> /dev/null; then
    echo "❌ Heroku CLI is not installed. Please install it from https://devcenter.heroku.com/articles/heroku-cli"
    exit 1
fi

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo "❌ Git is not installed. Please install Git."
    exit 1
fi

# Login to Heroku
echo "🔐 Logging into Heroku..."
heroku login

# Get app names
read -p "Enter backend app name (e.g., ronin-backend): " BACKEND_APP
read -p "Enter frontend app name (e.g., ronin-frontend): " FRONTEND_APP

# Create backend app
echo ""
echo "📦 Creating backend app: $BACKEND_APP"
heroku apps:create "$BACKEND_APP" --buildpack heroku/java 2>/dev/null || echo "App may already exist"

# Create frontend app
echo ""
echo "📦 Creating frontend app: $FRONTEND_APP"
heroku apps:create "$FRONTEND_APP" --buildpack heroku/nodejs 2>/dev/null || echo "App may already exist"

# Add PostgreSQL database to backend
echo ""
echo "🗄️  Adding PostgreSQL database to backend..."
heroku addons:create heroku-postgresql:mini --app "$BACKEND_APP" 2>/dev/null || echo "Database may already exist"

# Save app names for later use
echo "$BACKEND_APP" > .heroku-backend
echo "$FRONTEND_APP" > .heroku-frontend

echo ""
echo "✅ Setup complete!"
echo ""
echo "Next steps:"
echo "1. Run: ./heroku-config.sh"
echo "2. Then run: ./deploy-to-heroku.sh"
echo ""
