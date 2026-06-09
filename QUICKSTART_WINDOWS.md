# Quick Start Guide - Windows

Get Ronin (web + desktop) running on Windows in 5 minutes.

## 1. Prerequisites (2 minutes)

Download and install:
- **Node.js 16+**: https://nodejs.org/ (LTS recommended)
- **Java 11+**: https://www.oracle.com/java/technologies/downloads/
- **Maven 3.6+**: https://maven.apache.org/install.html (or use bundled mvnw)

Verify installation:
```cmd
node --version
java -version
mvn --version
```

## 2. Clone & Setup (2 minutes)

```cmd
REM Navigate to your projects directory
cd C:\Projects

REM Clone the repository
git clone https://github.com/your-org/ronin.git
cd ronin

REM Run setup script (installs all dependencies)
setup.bat
```

Or manually:
```cmd
npm install
cd frontend && npm install && cd ..
cd desktop && npm install && cd ..
```

## 3. Start Development (1 minute)

```cmd
REM Start all services (backend, web, desktop)
npm run dev
```

This will:
1. Start **Backend** on http://localhost:8080
2. Start **Web App** on http://localhost:5173
3. Open **Desktop App** (Electron window)

**To stop**: Press `Ctrl+C`

## 4. Create an Account

1. Open http://localhost:5173 in your browser
2. Click "Sign Up"
3. Enter email and password
4. Create your first project!

## 5. Build Desktop App

```cmd
REM Navigate to desktop folder
cd desktop

REM Create Windows installers
npm run dist:win
```

**Outputs in `desktop/dist/`:**
- `Ronin-1.0.0-Setup.exe` - Full installer
- `Ronin-1.0.0.exe` - Portable version

## What's Running?

| Component | URL | Status |
|-----------|-----|--------|
| Backend API | http://localhost:8080 | Running in background |
| Web App | http://localhost:5173 | Open in browser |
| Desktop App | Electron Window | Native app |

## Common Commands

```cmd
REM Start only backend
npm run dev:backend

REM Start only web app
npm run dev:web

REM Start only desktop app (requires backend running)
npm run dev:desktop

REM Run all tests
npm run test

REM Build installers
cd desktop && npm run dist:win

REM Clean everything
rm -r node_modules frontend/node_modules desktop/node_modules
npm run setup
```

## Troubleshooting

**Port 8080 in use?**
```cmd
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Port 5173 in use?**
```cmd
netstat -ano | findstr :5173
taskkill /PID <PID> /F
```

**Backend won't start?**
1. Verify Java: `java -version`
2. Check logs: `backend/target/spring.log`
3. Try: `cd backend && mvn clean package`

**Node modules issues?**
```cmd
rm -r node_modules package-lock.json
npm install
```

## Next Steps

- Read [MULTI_PLATFORM.md](MULTI_PLATFORM.md) for architecture overview
- Read [DEVELOPMENT.md](DEVELOPMENT.md) for detailed development guide
- Read [desktop/README.md](desktop/README.md) for desktop-specific info
- Check [ARCHITECTURE.md](ARCHITECTURE.md) for system design

## Directory Structure

```
ronin/
├── backend/          ← Java/Spring Boot API
├── frontend/         ← React web app
├── desktop/          ← Electron desktop app
└── README.md         ← Start here
```

## Video Tutorials (if available)

- [ ] Installation walkthrough
- [ ] First project creation
- [ ] Using LLM features
- [ ] Building the desktop app

## Get Help

1. Check relevant README files
2. Search GitHub Issues
3. Contact development team
4. Check documentation wiki

---

**That's it!** You now have Ronin running locally with both web and desktop versions. 🚀

For detailed development instructions, see [DEVELOPMENT.md](DEVELOPMENT.md).
