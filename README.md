# 🥋 Ronin - AI Agent Master Controller

Ronin is a comprehensive AI agent master controller that unifies multiple LLM providers and automatically switches between them based on project needs and token availability. Available as both a **modern web application** and a **native Windows desktop application**.

**Name**: From the Japanese term for exiled samurai - a skilled warrior that masters many disciplines.

## 🌟 Features

### Core Features
- 🤖 **Multi-LLM Management** - Switch between free LLMs automatically when tokens run out
- 📊 **Project Management** - Create and manage AI-assisted projects
- ⚔️ **Ranking System** - Kyokushin belt-style ranking (kyu/dan) based on completed projects
- 🧪 **Automated Testing** - Test project completion and correctness automatically
- 🔐 **User Authentication** - Secure JWT-based authentication
- 💰 **Free Token Management** - Track and optimize LLM token usage

### Deployment Options
- 🌐 **Web Application** - Access from any browser (responsive design)
- 🖥️ **Windows Desktop App** - Native desktop experience with installer
- ☁️ **Cloud Hosting** - Deployed on Heroku with auto-scaling

## 🚀 Quick Start

### Fastest Way (Windows)
```bash
# One-click setup (installs everything)
setup.bat

# Start all services
npm run dev

# Open in browser: http://localhost:5173
```

See [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) for step-by-step guide.

### Manual Setup
```bash
# Install dependencies
npm run setup

# Start development environment
npm run dev

# Or run individual services
npm run dev:backend    # Backend API
npm run dev:web        # Web app
npm run dev:desktop    # Desktop app
```

## 📋 Table of Contents

- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Installation](#installation)
- [Development](#development)
- [Building](#building)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

## 📁 Project Structure

```
ronin/
├── backend/              # Java/Spring Boot REST API
│   ├── src/main/         # Source code
│   ├── src/test/         # Unit tests
│   ├── pom.xml           # Maven configuration
│   └── README.md
│
├── frontend/             # React/TypeScript web application
│   ├── src/              # React components and pages
│   ├── vite.config.ts    # Vite configuration
│   ├── package.json      # npm dependencies
│   └── README.md
│
├── desktop/              # Electron Windows desktop application
│   ├── electron/         # Main process code
│   ├── build/            # App assets and icons
│   ├── installers/       # NSIS installer config
│   ├── package.json      # Electron configuration
│   └── README.md
│
├── test-runner/          # Automated test execution
│   └── README.md
│
├── MULTI_PLATFORM.md     # Architecture overview (WEB + DESKTOP)
├── DEVELOPMENT.md        # Development workflow guide
├── QUICKSTART_WINDOWS.md # Windows quick start
├── ARCHITECTURE.md       # System architecture diagrams
└── DESKTOP_BUILD_CHECKLIST.md # Release checklist
```

## 🏗️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 11+
- **Database**: MySQL / PostgreSQL
- **Build**: Maven
- **Authentication**: JWT
- **ORM**: Hibernate/JPA

### Frontend (Web + Desktop)
- **Framework**: React 19
- **Language**: TypeScript
- **Build**: Vite
- **Styling**: Tailwind CSS
- **Testing**: Vitest + React Testing Library
- **Router**: React Router v7

### Desktop
- **Framework**: Electron 33
- **Packaging**: electron-builder
- **Installer**: NSIS (Windows)
- **IPC**: Electron IPC bridge

### DevOps
- **Web Hosting**: Heroku
- **Container**: Docker
- **CI/CD**: GitHub Actions (ready to integrate)
- **Database Migrations**: Flyway

## 📦 Installation

### Prerequisites
- Node.js 16+ ([download](https://nodejs.org/))
- Java 11+ ([download](https://www.oracle.com/java/technologies/downloads/))
- Maven 3.6+ ([download](https://maven.apache.org/install.html))
- Git

### Setup Steps

1. **Clone Repository**
   ```bash
   git clone https://github.com/your-org/ronin.git
   cd ronin
   ```

2. **Run Setup Script**
   ```bash
   # Windows
   setup.bat
   
   # macOS/Linux
   npm run setup
   ```

3. **Start Development**
   ```bash
   npm run dev
   ```

4. **Access Applications**
   - Web App: http://localhost:5173
   - Backend API: http://localhost:8080
   - Desktop App: Electron window (if enabled)

See [DEVELOPMENT.md](DEVELOPMENT.md) for detailed setup.

## 💻 Development

### Development Commands

```bash
# Start all services together
npm run dev

# Start individual services
npm run dev:backend     # Java/Spring Boot only
npm run dev:web         # React dev server only
npm run dev:desktop     # Electron desktop app

# Build all components
npm run build

# Run all tests
npm run test

# Create desktop installers
npm run desktop:dist
```

### File Structure by Component

**Backend**:
```
backend/src/main/java/com/ronin/
├── auth/              # Authentication logic
├── code/              # Code generation
├── common/            # Shared utilities
├── config/            # Spring configuration
├── llm/               # LLM provider integration
├── projects/          # Project management
├── ranking/           # Ranking system (kyu/dan)
├── tests/             # Test execution
└── users/             # User management
```

**Frontend**:
```
frontend/src/
├── pages/
│   ├── Dashboard.tsx   # Project overview
│   ├── Login.tsx       # User login
│   ├── Signup.tsx      # User registration
│   └── ...
├── components/
│   ├── Header.tsx      # Navigation
│   ├── Sidebar.tsx     # Side menu
│   ├── ProjectCard.tsx # Project display
│   └── ...
├── api.ts             # HTTP client
└── main.tsx           # Entry point
```

**Desktop**:
```
desktop/
├── electron/
│   ├── main.ts        # Main process
│   ├── window.ts      # Window management
│   ├── preload.ts     # IPC bridge
│   └── utils.ts       # Utilities
└── build/             # App icons and assets
```

### Making Changes

1. **Backend Changes** → Auto-reload via Spring Boot Dev Tools
2. **Frontend Changes** → Hot reload via Vite
3. **Desktop Changes** → Manually reload Electron (Ctrl+R)

See [DEVELOPMENT.md](DEVELOPMENT.md) for workflow details.

## 🏗️ Building

### Build All Components
```bash
npm run build
```

This creates:
- `backend/target/backend-0.0.1-SNAPSHOT.jar`
- `frontend/dist/` (static files)
- `desktop/dist/` (Electron app)

### Build Desktop Application

```bash
cd desktop

# Create Windows installers
npm run dist:win

# Create only portable executable
npm run dist:portable
```

**Output Files**:
- `desktop/dist/Ronin-{version}-Setup.exe` - Full installer
- `desktop/dist/Ronin-{version}.exe` - Portable version

See [DESKTOP_BUILD_CHECKLIST.md](DESKTOP_BUILD_CHECKLIST.md) for release checklist.

## 🚀 Deployment

### Web Application (Heroku)

```bash
# Deploy to Heroku
git push heroku main

# View logs
heroku logs --tail
```

See [HEROKU_DEPLOYMENT.md](HEROKU_DEPLOYMENT.md) for detailed setup.

### Desktop Application

1. Build installers: `npm run desktop:dist`
2. Upload to GitHub Releases or distribution server
3. Users download `Ronin-{version}-Setup.exe`
4. Run installer or portable exe

## 📊 Architecture

```
┌─────────────────────────────────────┐
│    Web Browser / Desktop App         │
│    (React + Tailwind CSS)            │
└──────────────────┬──────────────────┘
                   │
        HTTP/REST  │
                   ▼
┌─────────────────────────────────────┐
│   Backend API (Spring Boot)          │
│   • Authentication                   │
│   • Project Management               │
│   • LLM Integration                  │
│   • Test Execution                   │
└──────────────────┬──────────────────┘
                   │
        SQL        │
                   ▼
┌─────────────────────────────────────┐
│   Database (MySQL / PostgreSQL)      │
│   • Users & Ranks                    │
│   • Projects & Tests                 │
│   • LLM Usage Tracking               │
└─────────────────────────────────────┘
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed system architecture.

## 🧪 Testing

### Run All Tests
```bash
npm run test
```

### Test by Component
```bash
npm run test:backend    # Maven/JUnit tests
npm run test:web        # Vitest/React tests
npm run test:desktop    # Electron build test
```

## 🔒 Security

- JWT token-based authentication
- Password hashing with bcrypt
- SQL injection protection via parameterized queries
- CORS properly configured
- Input validation on frontend and backend
- Secure IPC in desktop app

## 📚 Documentation

- [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) - Windows quick start guide
- [DEVELOPMENT.md](DEVELOPMENT.md) - Complete development workflow
- [MULTI_PLATFORM.md](MULTI_PLATFORM.md) - Multi-platform architecture
- [ARCHITECTURE.md](ARCHITECTURE.md) - Detailed system design
- [DESKTOP_BUILD_CHECKLIST.md](DESKTOP_BUILD_CHECKLIST.md) - Release checklist
- [backend/README.md](backend/README.md) - Backend documentation
- [frontend/README.md](frontend/README.md) - Frontend documentation
- [desktop/README.md](desktop/README.md) - Desktop documentation

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/my-feature`
2. Make changes and test: `npm run test`
3. Commit changes: `git commit -m "Add feature"`
4. Push to branch: `git push origin feature/my-feature`
5. Create a Pull Request

## 🐛 Troubleshooting

### Backend Issues
- Check Java is installed: `java -version`
- Check Maven: `mvn -version`
- View logs: `backend/target/spring.log`

### Frontend Issues
- Clear cache: `cd frontend && rm -rf node_modules dist && npm install`
- Check Node.js: `node -v`
- Try: `npm run build`

### Desktop Issues
- Ensure backend is running: `npm run dev:backend`
- Check port 5173 is free
- Clear desktop cache: `cd desktop && rm -rf dist node_modules && npm install`

See [DEVELOPMENT.md](DEVELOPMENT.md) troubleshooting section for more.

## 🎯 Roadmap

### Current Version (v1.0.0)
- ✅ Multi-LLM support with auto-switching
- ✅ Project management
- ✅ Automated testing
- ✅ Web application
- ✅ Windows desktop application
- ✅ Ranking system (kyu/dan)

### Planned Features
- [ ] Auto-update mechanism for desktop app
- [ ] Offline functionality
- [ ] System tray integration
- [ ] Desktop notifications
- [ ] Advanced analytics
- [ ] Mobile application
- [ ] Code signing for releases
- [ ] macOS and Linux desktop versions

## 📞 Support

- GitHub Issues: [Report bugs or request features](../../issues)
- Documentation: See linked README files above
- Development Team: Contact your team lead

## 📄 License

MIT License - See LICENSE file for details

## 👥 Team

- Backend Lead: [Name]
- Frontend Lead: [Name]
- Desktop/DevOps: [Name]
- QA Lead: [Name]

## 🙏 Acknowledgments

- Spring Boot community
- React community
- Electron community
- Tailwind CSS team
- All contributors

---

## Quick Reference

| Command | Purpose |
|---------|---------|
| `npm run setup` | Install all dependencies |
| `npm run dev` | Start all services |
| `npm run build` | Build all components |
| `npm run test` | Run all tests |
| `npm run desktop:dist` | Create Windows installers |

**Ready to get started?** See [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) or [DEVELOPMENT.md](DEVELOPMENT.md).

---

Made with ⚔️ by Ronin Development Team
