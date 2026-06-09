# Ronin - Multi-Platform AI Agent Master Controller

Ronin is an AI agent master controller that serves as both a **web application** and a **native Windows desktop application**.

## 📋 Table of Contents

- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Development](#development)
- [Building & Deployment](#building--deployment)
- [Web App](#web-app)
- [Desktop App](#desktop-app)

## 📁 Project Structure

```
ronin/
├── backend/              # Java/Spring Boot backend
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── frontend/             # React/TypeScript web frontend
│   ├── src/
│   ├── public/
│   ├── vite.config.ts
│   └── package.json
├── desktop/              # Electron desktop application
│   ├── electron/         # Main process code
│   ├── build/            # Installer assets
│   ├── installers/       # NSIS config
│   ├── package.json
│   ├── README.md
│   └── SETUP.md
├── vscode-extension/     # VS Code extension package
│   ├── src/              # Extension source code
│   ├── package.json
│   └── README.md
├── test-runner/          # Automated test runner
├── package.json          # Root workspace config
└── README.md            # This file
```

## 🚀 Quick Start

### Prerequisites
- Node.js 16+
- Java 11+
- Maven 3.6+
- Windows 10+ (for desktop app)

### One-Command Setup
```bash
npm run setup
```

This installs all dependencies for:
- Backend (Maven)
- Frontend (npm)
- Desktop (npm)

### Run Everything
```bash
npm run dev
```

Starts all three components in parallel:
- Backend on http://localhost:8080
- Web app on http://localhost:5173
- Desktop app (Electron window)

## 💻 Development

### Backend Development
```bash
cd backend
mvn spring-boot:run
```

Health check: `curl http://localhost:8080/actuator/health`

### Web App Development
```bash
cd frontend
npm run dev
```

Runs on http://localhost:5173 with hot reload.

### Desktop App Development
```bash
cd desktop
npm run dev
```

Runs Electron with dev tools open and hot reload for React components.

### Running Individual Services

```bash
# Only backend
npm run dev:backend

# Only web app
npm run dev:web

# Only desktop app (requires backend running)
npm run dev:desktop
```

## 🏗️ Building & Deployment

### Build All Components
```bash
npm run build
```

Outputs:
- `backend/target/backend-0.0.1-SNAPSHOT.jar`
- `frontend/dist/` (static files)
- `desktop/dist/` (Electron app)

### Test All Components
```bash
npm run test
```

Or test individually:
```bash
npm run test:backend  # Maven tests
npm run test:web      # Vitest
npm run test:desktop  # Electron build test
```

### Deploy Web App
The web app is deployed to Heroku (see HEROKU_DEPLOYMENT.md):
```bash
git push heroku main
```

Static files in `frontend/dist/` are served by backend.

### Deploy Desktop App
Create Windows installers:
```bash
npm run desktop:dist    # Full installer + portable
npm run desktop:portable # Portable exe only
```

Outputs in `desktop/dist/`:
- `Ronin-{version}-Setup.exe` - Full installer
- `Ronin-{version}.exe` - Portable executable

## 🌐 Web App

**Technology Stack:**
- React 19 with TypeScript
- Vite (bundler)
- Tailwind CSS (styling)
- React Router (navigation)
- Vitest (testing)

**Features:**
- User authentication
- Project management
- AI LLM integration
- Real-time testing
- Responsive design

**Build:**
```bash
cd frontend
npm run build      # Production build
npm run dev        # Development server
npm run lint       # ESLint checks
npm run test       # Run tests
```

**Deployment:**
- Frontend deployed to Heroku
- Backend serves both web and API
- Static files cached with versioning

[See frontend README](frontend/README.md) for more details.

## 🖥️ Desktop App

**Technology Stack:**
- Electron 33 (native desktop framework)
- React (same frontend code)
- TypeScript
- electron-builder (packaging)
- NSIS (Windows installer)

**Features:**
- Native Windows desktop experience
- Auto-launch on startup
- System tray integration (future)
- Windows installer with uninstaller
- Portable exe version
- IPC for main process communication

**Build:**
```bash
cd desktop
npm run dev        # Development with hot reload
npm run build      # Build for production
npm run dist:win   # Create installers
npm run dist:portable # Create portable exe
```

**Installer Details:**
- NSIS-based Windows installer
- Start Menu and Desktop shortcuts
- Uninstaller with registry cleanup
- Portable version for USB/network deployment

[See desktop README](desktop/README.md) and [SETUP.md](desktop/SETUP.md) for more details.

## 🎯 Key Features

### Web App
✅ Cross-platform (any browser)  
✅ No installation required  
✅ Responsive design  
✅ Real-time collaboration  
✅ Progressive Web App ready  

### Desktop App
✅ Native Windows experience  
✅ Direct system integration  
✅ Offline functionality (planned)  
✅ Portable version available  
✅ Professional installer  
✅ Auto-updates support (planned)  

### Backend
✅ RESTful API  
✅ JWT authentication  
✅ Database persistence  
✅ LLM provider management  
✅ Test execution  
✅ User ranking system  

## 📊 Development Workflow

### Making Changes

1. **Backend Changes**
   - Edit Java files in `backend/src/`
   - Server auto-reloads (Spring Boot dev tools)
   - Test with: `npm run test:backend`

2. **Frontend Changes**
   - Edit React/TypeScript in `frontend/src/`
   - Hot reload in browser automatically
   - Test with: `npm run test:web`

3. **Desktop App Changes**
   - Edit Electron code in `desktop/electron/`
   - Edit React code (shared with web)
   - Reload Electron window for main process changes

### Git Workflow

```bash
# Work on a feature
git checkout -b feature/my-feature

# Make changes and commit
git add .
git commit -m "Add my feature"

# Run tests before pushing
npm run test

# Push to GitHub
git push origin feature/my-feature

# Create pull request
# (on GitHub)
```

## 🔧 Common Tasks

### Add a new npm package

```bash
# Frontend
cd frontend && npm install package-name && cd ..

# Desktop
cd desktop && npm install package-name && cd ..
```

### Update dependencies

```bash
npm run setup   # Reinstall all with latest versions
```

### Clean builds

```bash
# Backend
cd backend && mvn clean && cd ..

# Frontend
cd frontend && rm -rf dist node_modules && npm install && cd ..

# Desktop
cd desktop && rm -rf dist node_modules && npm install && cd ..
```

### Run specific tests

```bash
# Backend (single test class)
cd backend && mvn test -Dtest=UserControllerTest && cd ..

# Frontend (watch mode)
cd frontend && npm run test:watch && cd ..
```

## 📝 Environment Variables

### Backend (backend/src/main/resources/application.properties)
```properties
server.port=8080
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:mysql://localhost:3306/ronin
```

### Frontend (.env)
```
VITE_API_BASE_URL=http://localhost:8080
```

### Desktop (.env)
```
ELECTRON_START_URL=http://localhost:5173
```

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Find process using port
lsof -i :8080          # Backend
lsof -i :5173          # Frontend
lsof -i :9223          # Electron debugger

# Kill process
kill -9 <PID>
```

### Backend Won't Start
1. Check Java version: `java -version`
2. Check Maven: `mvn -v`
3. Check database connection
4. Review logs: `backend/target/spring.log`

### Frontend Build Fails
1. Clear node_modules: `rm -rf frontend/node_modules`
2. Reinstall: `cd frontend && npm install`
3. Try build: `npm run build`

### Desktop App Issues
1. Ensure backend is running
2. Check Electron DevTools (Ctrl+Shift+I)
3. Review terminal output for errors
4. Check port 5173 is not in use

## 📦 Distribution

### Web App
- Hosted on Heroku
- Accessible via browser
- No desktop installation needed

### Desktop App
1. **Setup Installer** (recommended for users)
   - Professional installer experience
   - Start Menu integration
   - Easy uninstall via Control Panel
   - File: `Ronin-{version}-Setup.exe`

2. **Portable Executable** (for advanced users)
   - Single .exe file
   - No installation required
   - Can run from USB drive
   - File: `Ronin-{version}.exe`

## 📚 Additional Resources

- [Backend README](backend/README.md)
- [Frontend README](frontend/README.md)
- [Desktop README](desktop/README.md)
- [Desktop Setup Guide](desktop/SETUP.md)
- [Deployment Guide](HEROKU_DEPLOYMENT.md)
- [Architecture Overview](AGENTS.md)

## 📄 License

MIT License - see LICENSE file for details

## 👥 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `npm run test`
5. Submit a pull request

## 📞 Support

For issues or questions:
1. Check relevant README files
2. Review troubleshooting sections
3. Check GitHub Issues
4. Contact development team
