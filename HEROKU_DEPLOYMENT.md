# Deployment Configuration Reference

## Quick Start - Deployment Steps

### 1. Initial Setup (one time)
```bash
chmod +x heroku-setup.sh
./heroku-setup.sh
```

### 2. Configure Environment Variables
```bash
chmod +x heroku-config.sh
./heroku-config.sh
```

### 3. Deploy Applications
```bash
chmod +x deploy-to-heroku.sh
chmod +x deploy-heroku-helper.sh
./deploy-heroku-helper.sh
```

### 4. Verify Deployment
```bash
# Check backend logs
heroku logs --app ronin-backend --tail

# Check frontend logs
heroku logs --app ronin-frontend --tail

# Test endpoints
curl https://ronin-backend.herokuapp.com/health
```

---

## Manual Heroku Setup (Alternative)

If you prefer to set up manually:

### Step 1: Login & Create Apps
```bash
heroku login
heroku create ronin-backend --buildpack heroku/java
heroku create ronin-frontend --buildpack heroku/nodejs
```

### Step 2: Add Database
```bash
heroku addons:create heroku-postgresql:mini --app ronin-backend
```

### Step 3: Set Configuration Variables
```bash
# Backend
heroku config:set \
  OPENROUTER_API_KEY="your-key" \
  GOOGLE_CLIENT_ID="your-id" \
  GOOGLE_CLIENT_SECRET="your-secret" \
  GOOGLE_REDIRECT_URI="https://ronin-backend.herokuapp.com/auth/oauth2/callback/google" \
  FRONTEND_OAUTH_SUCCESS_URL="https://ronin-frontend.herokuapp.com/" \
  CORS_ALLOWED_ORIGINS="https://ronin-frontend.herokuapp.com" \
  --app ronin-backend

# Frontend
heroku config:set VITE_API_URL="https://ronin-backend.herokuapp.com" --app ronin-frontend
```

### Step 4: Add Git Remotes
```bash
git remote add heroku-backend https://git.heroku.com/ronin-backend.git
git remote add heroku-frontend https://git.heroku.com/ronin-frontend.git
```

### Step 5: Deploy
```bash
git subtree push --prefix backend heroku-backend master:main
git subtree push --prefix frontend heroku-frontend master:main
```

---

## After Deployment

### View Logs
```bash
heroku logs --app ronin-backend --tail
heroku logs --app ronin-frontend --tail
```

### Run Migrations
```bash
heroku ps:exec --app ronin-backend
# Inside the dyno:
# SELECT * FROM flyway_schema_history;
```

### Restart App
```bash
heroku restart --app ronin-backend
heroku restart --app ronin-frontend
```

### Update Config
```bash
heroku config:set KEY=VALUE --app ronin-backend
```

### Open App in Browser
```bash
heroku open --app ronin-frontend
```

---

## Environment Variables Summary

### Backend (`ronin-backend`)
- `DATABASE_URL` - Auto-set by PostgreSQL addon
- `OPENROUTER_API_KEY` - Your OpenRouter API key
- `GOOGLE_CLIENT_ID` - Google OAuth Client ID
- `GOOGLE_CLIENT_SECRET` - Google OAuth Client Secret
- `GOOGLE_REDIRECT_URI` - Should be `https://ronin-backend.herokuapp.com/auth/oauth2/callback/google`
- `FRONTEND_OAUTH_SUCCESS_URL` - Should be `https://ronin-frontend.herokuapp.com/`
- `CORS_ALLOWED_ORIGINS` - Should be `https://ronin-frontend.herokuapp.com`

### Frontend (`ronin-frontend`)
- `VITE_API_URL` - Should be `https://ronin-backend.herokuapp.com`

---

## Troubleshooting

### Build Failures
```bash
# Check build logs
heroku logs --app ronin-backend
heroku logs --app ronin-frontend

# Rebuild
heroku builds:cancel --app ronin-backend
git subtree push --prefix backend heroku-backend master:main
```

### Database Connection Issues
```bash
# Check database URL
heroku config --app ronin-backend | grep DATABASE_URL

# Connect to database
heroku pg:psql --app ronin-backend
```

### CORS Errors in Frontend
1. Check that `CORS_ALLOWED_ORIGINS` matches frontend URL
2. Restart backend: `heroku restart --app ronin-backend`
3. Clear browser cache

### OAuth Issues
1. Verify Google redirect URIs match exactly
2. Check that `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` are correct
3. Restart: `heroku restart --app ronin-backend`

---

## Redeployment

For subsequent deployments, just commit your changes and run:
```bash
./quick-deploy.sh
```

Or manually:
```bash
git subtree push --prefix backend heroku-backend master:main
git subtree push --prefix frontend heroku-frontend master:main
```
