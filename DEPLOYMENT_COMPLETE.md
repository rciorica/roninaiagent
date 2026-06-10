# Ronin Application - Heroku Deployment Complete ✓

## Deployment Summary

Successfully deployed both the frontend and backend applications to Heroku!

### Application URLs

**Frontend (React + Vite):**
- https://ronin-frontend-efbb0cc17886.herokuapp.com/

**Backend (Spring Boot + PostgreSQL):**
- https://ronin-backend.herokuapp.com/
- Health endpoint: https://ronin-backend.herokuapp.com/health

### Environment Configuration

#### Backend (ronin-backend)
- **Java**: Version 21 (Spring Boot 3.2.4)
- **Database**: PostgreSQL (Heroku addon: heroku-postgresql:mini)
- **Environment Variables Set**:
  - DATABASE_URL (auto-set by Heroku PostgreSQL addon)
  - OPENROUTER_API_KEY (if provided)
  - GOOGLE_CLIENT_ID (if provided)
  - GOOGLE_CLIENT_SECRET (if provided)
  - GOOGLE_REDIRECT_URI: `https://ronin-backend.herokuapp.com/auth/oauth2/callback/google`
  - FRONTEND_OAUTH_SUCCESS_URL: `https://ronin-frontend-efbb0cc17886.herokuapp.com/`
  - CORS_ALLOWED_ORIGINS: `https://ronin-frontend-efbb0cc17886.herokuapp.com`

#### Frontend (ronin-frontend)
- **Framework**: React 19 + TypeScript + Vite
- **Environment Variables Set**:
  - VITE_API_URL: `https://ronin-backend.herokuapp.com`

### Deployment Method

Used **git subtree push** for monorepo structure:
- Backend: `git subtree push --prefix backend heroku-backend master:main`
- Frontend: `git subtree push --prefix frontend heroku-frontend master:main`

### Next Steps

1. **Update API Keys** (if needed)
   ```
   heroku config:set OPENROUTER_API_KEY="your-key" --app ronin-backend
   heroku config:set GOOGLE_CLIENT_ID="your-id" --app ronin-backend
   heroku config:set GOOGLE_CLIENT_SECRET="your-secret" --app ronin-backend
   ```

2. **Monitor Logs**
   ```
   heroku logs --app ronin-backend --tail
   heroku logs --app ronin-frontend --tail
   ```

3. **View Database**
   ```
   heroku pg:psql --app ronin-backend
   ```

4. **Future Deployments**
   ```
   .\deploy-monorepo.ps1
   ```
   Or manually:
   ```
   git subtree push --prefix backend heroku-backend main
   git subtree push --prefix frontend heroku-frontend main
   ```

### Key Configuration Files

- **Backend**: 
  - `/backend/Procfile` - Heroku process configuration
  - `/backend/system.properties` - Java version specification
  - `/backend/pom.xml` - Maven dependencies
  - `/backend/.slugignore` - Files to exclude from deployment

- **Frontend**:
  - `/frontend/Procfile` - Heroku process configuration
  - `/frontend/package.json` - Node dependencies + build scripts
  - `/frontend/.slugignore` - Files to exclude from deployment

- **Application Configuration**:
  - `/backend/src/main/resources/application.properties` - Production-ready config
  - `/frontend/src/api.ts` - API client with environment variable support

### Troubleshooting

**Backend Won't Start:**
```bash
heroku logs --app ronin-backend --tail
# Check for database connection issues
heroku pg:psql --app ronin-backend
SELECT * FROM flyway_schema_history;
```

**Frontend Shows Errors:**
```bash
heroku logs --app ronin-frontend --tail
# Check VITE_API_URL is set correctly
heroku config --app ronin-frontend
```

**CORS Errors:**
1. Verify `CORS_ALLOWED_ORIGINS` matches the frontend URL
2. Restart backend: `heroku restart --app ronin-backend`

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│  Ronin Application - Heroku Deployment                 │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Frontend (ronin-frontend)                              │
│  ├─ React 19 + TypeScript + Vite                       │
│  ├─ Runs on Node.js buildpack                          │
│  ├─ Served by nginx/serve                              │
│  └─ Connects to Backend via VITE_API_URL               │
│                                                         │
│                        │                                │
│                 HTTPS & CORS                            │
│                        │                                │
│                        ▼                                │
│  Backend (ronin-backend)                               │
│  ├─ Spring Boot 3.2.4 with Java 21                    │
│  ├─ Runs on Java buildpack                             │
│  ├─ PostgreSQL Database (Heroku addon)                 │
│  ├─ REST API with JWT authentication                   │
│  ├─ Flyway database migrations                         │
│  └─ OpenRouter LLM integration                         │
│                                                         │
│        Backend Database                                │
│  ├─ PostgreSQL (Heroku managed)                        │
│  ├─ Automatic backups                                  │
│  ├─ Connection pooling                                 │
│  └─ SSL/TLS encryption                                 │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

**Deployment Date**: 2026-06-05
**Status**: ✓ Successful
**Apps Created**: 2 (ronin-backend, ronin-frontend)
**Database**: PostgreSQL mini addon
**Build Time**: ~27 seconds (backend)
