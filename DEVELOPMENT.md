# Ronin Multi-Platform Development & Build Guide

This guide provides step-by-step instructions for developers to build and deploy both the web and desktop versions of Ronin.

## Prerequisites

Before you start, ensure you have:
- **Node.js** 16 or higher ([download](https://nodejs.org/))
- **Java** 11 or higher ([download](https://www.oracle.com/java/technologies/downloads/))
- **Maven** 3.6 or higher ([download](https://maven.apache.org/install.html))
- **Git** for version control ([download](https://git-scm.com/))

Verify installations:
```bash
node --version
java -version
mvn --version
```

## Initial Setup

### On Windows
```bash
# Run the setup batch script
setup.bat
```

### On macOS/Linux
```bash
# Run npm setup
npm run setup
```

This command installs all dependencies for:
- Root workspace
- Backend (Maven packages)
- Frontend (npm packages)
- Desktop (npm packages)

## Running the Application

### Development Mode (All Services)

```bash
npm run dev
```

This starts three services in parallel:

1. **Backend** (http://localhost:8080)
   - Java Spring Boot application
   - REST API endpoints
   - Database management
   - Terminal output shows logs

2. **Web App** (http://localhost:5173)
   - React development server
   - Hot module reloading
   - Open browser to http://localhost:5173

3. **Desktop App** (Electron)
   - Native Windows application
   - Connects to backend at localhost:8080
   - DevTools open for debugging

**To stop all services**: Press `Ctrl+C` multiple times or close the terminal.

### Running Individual Services

```bash
# Backend only (useful for testing API separately)
npm run dev:backend
# Runs on http://localhost:8080
# Check health: curl http://localhost:8080/actuator/health

# Web app only (useful with existing backend)
npm run dev:web
# Runs on http://localhost:5173

# Desktop app only (requires backend running separately)
npm run dev:desktop
# Opens Electron window with Dev Tools
```

## Development Workflow

### Backend Development

**File Location**: `backend/src/main/java/com/ronin/`

```bash
cd backend
mvn spring-boot:run
```

**Features**:
- Automatic reload with Spring Boot Dev Tools
- Actuator endpoints for health checks
- H2 or MySQL database
- API documentation at http://localhost:8080/api-docs

**Testing**:
```bash
# Run all backend tests
mvn test

# Run specific test
mvn test -Dtest=UserControllerTest

# Run with coverage
mvn test jacoco:report
```

**Database**:
- Migrations in `backend/src/main/resources/db/migration/`
- Uses Flyway for schema management
- Auto-runs on startup

### Frontend Development

**File Location**: `frontend/src/`

**Components**:
- Pages: `frontend/src/pages/` (Dashboard, Login, Signup)
- Components: `frontend/src/components/` (Header, Sidebar, ProjectCard)
- Styles: Tailwind CSS (see `tailwind.config.js`)
- API client: `frontend/src/api.ts`

**Commands**:
```bash
cd frontend

npm run dev        # Development server with hot reload
npm run build      # Production build to dist/
npm run lint       # Check code style
npm run test       # Run unit tests
npm run preview    # Preview production build locally
```

**Testing**:
```bash
# Run tests in watch mode
npm run test:watch

# Run tests once
npm run test

# Run specific test file
npm run test Dashboard.test.tsx
```

**Building**:
```bash
# Create optimized production build
npm run build
# Output: frontend/dist/

# Build is 100-200KB gzipped
# Tested on: Chrome, Firefox, Safari, Edge
```

### Desktop Application Development

**File Location**: `desktop/`

**Electron Architecture**:
- Main process: `desktop/electron/main.ts` (Node.js, system access)
- Renderer: Shared React frontend (browser sandbox)
- IPC bridge: `desktop/electron/preload.ts` (secure communication)

**Commands**:
```bash
cd desktop

npm run dev        # Start dev server and Electron
npm run build      # Build for production
npm run dist:win   # Create Windows installers
npm run dist:portable # Create portable .exe
```

**Development Features**:
- React hot reload (Ctrl+R in Electron)
- Main process logging to console
- DevTools accessible (Ctrl+Shift+I)
- Auto-reload on file changes

**Adding IPC Handlers**:

In `desktop/electron/main.ts`:
```typescript
ipcMain.handle('my-event', async () => {
  return someSystemInfo;
});
```

In React component (`frontend/src/components/`):
```typescript
const result = await window.electron.myEvent();
```

## Building for Production

### Build All Components

```bash
npm run build
```

Outputs:
- **Backend**: `backend/target/backend-0.0.1-SNAPSHOT.jar`
- **Frontend**: `frontend/dist/` (static files)
- **Desktop**: `desktop/dist/` (packaged app)

### Building the Backend

```bash
cd backend

# Build JAR file
mvn clean package

# Run tests during build
mvn clean verify

# Skip tests
mvn clean package -DskipTests

# Build with specific profile
mvn clean package -Pproduction
```

Output: `backend/target/backend-0.0.1-SNAPSHOT.jar`

### Building the Web App

```bash
cd frontend

# Production build
npm run build

# Check build size
npm run build -- --analyze
```

Output: `frontend/dist/` (ready for static hosting)

### Building the Desktop App

#### Create Windows Installers

```bash
cd desktop

# Full installer + portable exe
npm run dist:win

# Only portable exe (faster)
npm run dist:portable
```

**Outputs**:
- `dist/Ronin-1.0.0-Setup.exe` - Full NSIS installer
- `dist/Ronin-1.0.0.exe` - Portable executable

**Installer Features**:
- Start Menu shortcuts
- Desktop shortcuts
- Add/Remove Programs integration
- Uninstaller
- All dependencies bundled

## Testing

### Run All Tests

```bash
npm run test
```

This runs:
- Backend tests (Maven/JUnit)
- Frontend tests (Vitest/React Testing Library)
- Desktop build test

### Backend Tests

```bash
cd backend
mvn test

# Run specific test class
mvn test -Dtest=ProjectControllerTest

# Run with coverage report
mvn test jacoco:report
# Report: backend/target/site/jacoco/index.html
```

**Test Location**: `backend/src/test/java/com/ronin/`

### Frontend Tests

```bash
cd frontend

# Run tests once
npm run test

# Run in watch mode
npm run test:watch

# Generate coverage report
npm run test -- --coverage
```

**Test Location**: `frontend/src/components/*.test.tsx`

### Desktop Tests

```bash
cd desktop

# Test Electron build
npm run pretest
```

## Deployment

### Web App Deployment (Heroku)

```bash
# Add Heroku remote
heroku login
heroku create ronin-app

# Deploy
git push heroku main

# Check logs
heroku logs --tail
```

See `HEROKU_DEPLOYMENT.md` for detailed setup.

### Desktop App Distribution

1. **Build Installers**:
   ```bash
   cd desktop
   npm run dist:win
   ```

2. **Files to Distribute**:
   - `dist/Ronin-1.0.0-Setup.exe` - Main installer
   - `dist/Ronin-1.0.0.exe` - Portable version

3. **Distribution Options**:
   - GitHub Releases (add to release page)
   - Company website download
   - Windows Store (future enhancement)
   - Auto-update server (future enhancement)

4. **Installation Instructions**:
   - Users run `Ronin-1.0.0-Setup.exe`
   - Choose installation directory
   - Create shortcuts
   - Launch application

## Common Development Tasks

### Add a New npm Package

**Frontend**:
```bash
cd frontend
npm install package-name
npm run build
```

**Desktop**:
```bash
cd desktop
npm install package-name
npm run build
```

### Add a New Database Migration

**Backend**:
```bash
# Create new migration file
touch backend/src/main/resources/db/migration/V<number>__<description>.sql

# File naming: V1__init.sql, V2__add_users.sql, etc.
# Migrations run automatically on startup
```

### Add a New API Endpoint

**Backend**:
```bash
# 1. Create controller: backend/src/main/java/com/ronin/api/MyController.java
# 2. Create service: backend/src/main/java/com/ronin/service/MyService.java
# 3. Create test: backend/src/test/java/com/ronin/api/MyControllerTest.java
# 4. Run tests: mvn test
```

### Add a New React Component

**Frontend**:
```bash
# 1. Create component: frontend/src/components/MyComponent.tsx
# 2. Add styles: Use Tailwind CSS classes
# 3. Create test: frontend/src/components/MyComponent.test.tsx
# 4. Use in page: Import and add to frontend/src/pages/MyPage.tsx
# 5. Test: npm run test
```

## Troubleshooting

### Port Conflicts

```bash
# Find what's using the port (Windows)
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <PID> /F

# Or change port in application.properties or vite.config.ts
```

### Backend Won't Start

```bash
# Check Java installation
java -version

# Check Maven installation
mvn -version

# Try clean build
cd backend
mvn clean package

# Check database connection
# Verify connection string in application.properties
```

### Frontend Build Fails

```bash
# Clear cache
cd frontend
rm -rf node_modules dist package-lock.json

# Reinstall and build
npm install
npm run build
```

### Desktop App Won't Build

```bash
# Ensure backend is running
npm run dev:backend

# Check Node.js version (16+)
node --version

# Clear cache
cd desktop
rm -rf node_modules dist

# Rebuild
npm install
npm run build
```

### Tests Failing

```bash
# Run with verbose output
npm run test -- --reporter=verbose

# Check specific test file
npm run test -- specific-test.test.ts

# View test coverage
npm run test -- --coverage
```

## Git Workflow

### Local Development

```bash
# Create feature branch
git checkout -b feature/my-feature

# Make changes
# ... edit files ...

# Run tests
npm run test

# Commit changes
git add .
git commit -m "Add my feature: description"

# Push to GitHub
git push origin feature/my-feature
```

### Code Review Process

1. Create Pull Request on GitHub
2. Request reviews from team members
3. Address feedback
4. Merge to main branch
5. CI/CD runs tests automatically

## Performance Optimization

### Frontend

```bash
# Check bundle size
npm run build -- --analyze

# Techniques:
# - Code splitting with React.lazy()
# - Image optimization
# - CSS minification (Tailwind)
# - JavaScript tree-shaking
```

### Backend

```bash
# Check startup time
mvn clean spring-boot:run | grep "Started"

# Techniques:
# - Lazy loading of beans
# - Connection pool optimization
# - Query optimization with JPA
```

### Desktop

```bash
# Monitor memory usage (DevTools)
# Check startup time in main.ts logs
# Optimize bundle with electron-builder compression
```

## Monitoring and Logging

### Backend Logs

```bash
# Real-time logs
npm run dev:backend

# Log files (if configured)
tail -f backend/logs/app.log
```

### Frontend Logs

```bash
# Browser console
# Open DevTools: F12 or Right-click > Inspect
# Check Network tab for API calls
```

### Desktop Logs

```bash
# Open DevTools: Ctrl+Shift+I in Electron window
# Main process logs visible in terminal
```

## Security Checklist

- [ ] API endpoints require authentication
- [ ] Sensitive data not logged
- [ ] Database queries use parameterized statements
- [ ] Frontend validates input
- [ ] CORS properly configured
- [ ] Secrets not committed to git
- [ ] Dependencies regularly updated
- [ ] Security headers set in backend

## Next Steps

1. **Start with**: `npm run setup` then `npm run dev`
2. **Read**: MULTI_PLATFORM.md for architecture overview
3. **Explore**: Each component's README (backend/, frontend/, desktop/)
4. **Build**: Create your first feature!
5. **Test**: Run `npm run test` frequently
6. **Deploy**: Follow deployment sections when ready

## Support Resources

- Backend: [backend/README.md](backend/README.md)
- Frontend: [frontend/README.md](frontend/README.md)
- Desktop: [desktop/README.md](desktop/README.md) and [desktop/SETUP.md](desktop/SETUP.md)
- Issues: GitHub Issues section
- Documentation: Individual README files

Happy coding! 🚀
