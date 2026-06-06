# 📖 Documentation Index - Start Here!

Welcome to Ronin! This is your guide to understanding, developing, and deploying the multi-platform AI Agent application.

## 🎯 Find Your Path

### 👤 I'm New to This Project
Start here for the fastest onboarding:
1. **Read**: [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) (5 minutes)
2. **Run**: `setup.bat` then `npm run dev`
3. **Explore**: [README.md](README.md) for project overview
4. **Next**: [DEVELOPMENT.md](DEVELOPMENT.md) for detailed workflows

### 🏗️ I Need to Understand the Architecture
Start with these documents:
1. **[ARCHITECTURE.md](ARCHITECTURE.md)** - System design with diagrams
2. **[MULTI_PLATFORM.md](MULTI_PLATFORM.md)** - Platform comparison
3. **[README.md](README.md)** - Component overview

### 💻 I'm Ready to Develop
Follow this path:
1. **[QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md)** - Set up locally
2. **[DEVELOPMENT.md](DEVELOPMENT.md)** - Development workflows
3. **[backend/README.md](backend/README.md)** - Backend specifics
4. **[frontend/README.md](frontend/README.md)** - Frontend specifics
5. **[desktop/README.md](desktop/README.md)** - Desktop specifics

### 🚀 I Need to Build the Desktop App
Follow this path:
1. **[desktop/README.md](desktop/README.md)** - Overview
2. **[desktop/SETUP.md](desktop/SETUP.md)** - Detailed setup
3. **[DEVELOPMENT.md](DEVELOPMENT.md#building--deployment)** - Build instructions
4. **[DESKTOP_BUILD_CHECKLIST.md](DESKTOP_BUILD_CHECKLIST.md)** - Release checklist

### 📦 I Need to Deploy This
Follow this path:
1. **[DEVELOPMENT.md](DEVELOPMENT.md#deployment)** - Deployment options
2. **[HEROKU_DEPLOYMENT.md](HEROKU_DEPLOYMENT.md)** - Web deployment
3. **[DESKTOP_BUILD_CHECKLIST.md](DESKTOP_BUILD_CHECKLIST.md)** - Desktop release
4. **[desktop/SETUP.md](desktop/SETUP.md)** - Installer configuration

### 🤔 Something's Not Working
Check these resources:
1. **[DEVELOPMENT.md#troubleshooting](DEVELOPMENT.md)** - General troubleshooting
2. **[desktop/README.md#troubleshooting](desktop/README.md)** - Desktop issues
3. **[backend/README.md](backend/README.md)** - Backend issues
4. **[frontend/README.md](frontend/README.md)** - Frontend issues

---

## 📚 Complete Documentation Map

### Core Documentation

| Document | Purpose | Audience | Time |
|----------|---------|----------|------|
| **[README.md](README.md)** | Project overview & features | Everyone | 10 min |
| **[QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md)** | 5-minute Windows setup | New developers | 5 min |
| **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** | What was built | Tech leads | 10 min |

### Development Documentation

| Document | Purpose | Audience | Time |
|----------|---------|----------|------|
| **[DEVELOPMENT.md](DEVELOPMENT.md)** | Complete dev workflow | Developers | 30 min |
| **[ARCHITECTURE.md](ARCHITECTURE.md)** | System design & diagrams | Architects | 20 min |
| **[MULTI_PLATFORM.md](MULTI_PLATFORM.md)** | Web + Desktop overview | Team leads | 15 min |

### Component Documentation

| Document | Purpose | Audience | Time |
|----------|---------|----------|------|
| **[backend/README.md](backend/README.md)** | Backend specifics | Backend devs | 15 min |
| **[frontend/README.md](frontend/README.md)** | Frontend specifics | Frontend devs | 15 min |
| **[desktop/README.md](desktop/README.md)** | Desktop app overview | All devs | 10 min |
| **[desktop/SETUP.md](desktop/SETUP.md)** | Desktop detailed setup | Desktop devs | 20 min |

### Deployment & Release

| Document | Purpose | Audience | Time |
|----------|---------|----------|------|
| **[HEROKU_DEPLOYMENT.md](HEROKU_DEPLOYMENT.md)** | Web app deployment | DevOps | 15 min |
| **[DESKTOP_BUILD_CHECKLIST.md](DESKTOP_BUILD_CHECKLIST.md)** | Release checklist | Release manager | 45 min |

---

## 🗂️ Directory Structure

```
ronin/
│
├── 📄 Documentation (START HERE)
│   ├── README.md                         ⭐ Main overview
│   ├── QUICKSTART_WINDOWS.md            ⭐ 5-minute setup
│   ├── IMPLEMENTATION_SUMMARY.md         ⭐ What was built
│   ├── DEVELOPMENT.md                   Complete workflow guide
│   ├── ARCHITECTURE.md                  System design
│   ├── MULTI_PLATFORM.md                Platform overview
│   ├── DESKTOP_BUILD_CHECKLIST.md       Release guide
│   ├── HEROKU_DEPLOYMENT.md             Web deployment
│   ├── INSTALLATION_GUIDE.md            (existing)
│   ├── AGENTS.md                        (existing)
│   └── INDEX.md                         This file
│
├── 📁 Backend (Java/Spring)
│   ├── README.md
│   ├── pom.xml
│   ├── src/main/
│   ├── src/test/
│   └── target/ (build output)
│
├── 📁 Frontend (React/TypeScript)
│   ├── README.md
│   ├── package.json
│   ├── vite.config.ts
│   ├── src/
│   └── dist/ (build output)
│
├── 📁 Desktop (Electron) ⭐ NEW
│   ├── README.md                        Desktop app overview
│   ├── SETUP.md                         Setup guide
│   ├── package.json                     Electron config
│   ├── tsconfig.json
│   ├── electron/
│   │   ├── main.ts                     Main process entry
│   │   ├── window.ts                   Window management
│   │   ├── preload.ts                  IPC bridge
│   │   └── utils.ts                    Utilities
│   ├── build/                           Assets & icons
│   ├── installers/
│   │   └── installer.nsi                NSIS installer script
│   └── dist/ (build output)
│
├── 📁 Test Runner
│   └── README.md
│
├── 🔧 Scripts
│   ├── setup.bat                        ⭐ One-click Windows setup
│   ├── deploy.ps1
│   ├── start.sh
│   └── (other deployment scripts)
│
├── 📋 Root Files
│   ├── package.json                     Root workspace config
│   └── .gitignore
│
└── 🐳 Container & Deployment
    ├── Dockerfile (backend)
    ├── docker-compose.yml (if exists)
    ├── Procfile
    └── (deployment configs)
```

---

## 🚀 Quick Commands

### Setup & Development
```bash
setup.bat                    # Install all dependencies (Windows)
npm run dev                  # Start all services
npm run build                # Build all components
npm run test                 # Run all tests
```

### Component-Specific
```bash
npm run dev:backend         # Backend only
npm run dev:web             # Web app only
npm run dev:desktop         # Desktop app only
npm run build:backend       # Build backend
npm run build:web           # Build web app
npm run build:desktop       # Build desktop app
```

### Desktop/Installer
```bash
cd desktop && npm run dev           # Desktop development
cd desktop && npm run dist:win       # Create installers
cd desktop && npm run dist:portable  # Create portable .exe
```

---

## 🎯 Learning Path by Role

### Frontend Developer
1. [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) - Setup
2. [frontend/README.md](frontend/README.md) - Frontend details
3. [DEVELOPMENT.md](DEVELOPMENT.md) - Workflow

### Backend Developer
1. [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) - Setup
2. [backend/README.md](backend/README.md) - Backend details
3. [DEVELOPMENT.md](DEVELOPMENT.md) - Workflow

### Desktop Developer
1. [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) - Setup
2. [desktop/README.md](desktop/README.md) - Overview
3. [desktop/SETUP.md](desktop/SETUP.md) - Detailed setup
4. [DEVELOPMENT.md](DEVELOPMENT.md#desktop-application-development) - Workflow

### Full-Stack Developer
1. [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) - Setup
2. [DEVELOPMENT.md](DEVELOPMENT.md) - Complete workflow
3. [ARCHITECTURE.md](ARCHITECTURE.md) - System design
4. Individual component READMEs as needed

### DevOps / Release Manager
1. [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Overview
2. [DESKTOP_BUILD_CHECKLIST.md](DESKTOP_BUILD_CHECKLIST.md) - Release process
3. [HEROKU_DEPLOYMENT.md](HEROKU_DEPLOYMENT.md) - Web deployment
4. [DEVELOPMENT.md#deployment](DEVELOPMENT.md) - Deployment options

### QA / Tester
1. [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) - Setup
2. [DEVELOPMENT.md#testing](DEVELOPMENT.md) - Testing procedures
3. [DESKTOP_BUILD_CHECKLIST.md](DESKTOP_BUILD_CHECKLIST.md) - Release testing

### Project Manager / Tech Lead
1. [README.md](README.md) - Project overview
2. [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - What was built
3. [ARCHITECTURE.md](ARCHITECTURE.md) - System design
4. [MULTI_PLATFORM.md](MULTI_PLATFORM.md) - Platform overview

---

## ❓ FAQ

**Q: Where do I start if I'm new?**  
A: Read [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) then run `setup.bat`

**Q: How do I develop locally?**  
A: See [DEVELOPMENT.md](DEVELOPMENT.md) for complete workflow

**Q: How do I build the desktop app?**  
A: See [desktop/README.md](desktop/README.md) or [DEVELOPMENT.md](DEVELOPMENT.md#building)

**Q: How do I release a new version?**  
A: Follow [DESKTOP_BUILD_CHECKLIST.md](DESKTOP_BUILD_CHECKLIST.md)

**Q: What's the difference between web and desktop?**  
A: See [MULTI_PLATFORM.md](MULTI_PLATFORM.md) for comparison

**Q: Can I deploy just the web version?**  
A: Yes, see [HEROKU_DEPLOYMENT.md](HEROKU_DEPLOYMENT.md)

**Q: What if something breaks?**  
A: Check troubleshooting in [DEVELOPMENT.md](DEVELOPMENT.md)

---

## 🔗 External Resources

### Technologies Used
- **Backend**: [Spring Boot docs](https://spring.io/projects/spring-boot)
- **Frontend**: [React docs](https://react.dev) | [TypeScript](https://www.typescriptlang.org/)
- **Desktop**: [Electron docs](https://www.electronjs.org/)
- **Build**: [Maven](https://maven.apache.org/) | [Vite](https://vitejs.dev/) | [electron-builder](https://www.electron.build/)
- **Styling**: [Tailwind CSS](https://tailwindcss.com/)
- **Testing**: [JUnit](https://junit.org/) | [Vitest](https://vitest.dev/)

### Useful Tools
- Git: https://git-scm.com/
- Node.js: https://nodejs.org/
- Java: https://www.oracle.com/java/technologies/downloads/
- VS Code: https://code.visualstudio.com/
- Postman: https://www.postman.com/

---

## 📞 Getting Help

1. **Check Documentation**: Find your answer in relevant README
2. **Search Issues**: Check GitHub Issues for similar problems
3. **Ask Team**: Reach out to development team
4. **Check Logs**: Enable debug logging for detailed errors

---

## ✅ What's New (Desktop Edition)

New in this version:
- ✅ Windows Electron desktop application
- ✅ Professional NSIS installer
- ✅ Portable .exe version
- ✅ IPC bridge for system access
- ✅ Comprehensive documentation
- ✅ One-command setup script
- ✅ Multi-platform scripts

Everything else remains unchanged - your web version is fully functional!

---

## 📋 Checklist: Your First Day

- [ ] Read [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md)
- [ ] Run `setup.bat`
- [ ] Run `npm run dev`
- [ ] Create test account
- [ ] Create test project
- [ ] Explore dashboard
- [ ] Read [DEVELOPMENT.md](DEVELOPMENT.md)
- [ ] Bookmark this INDEX.md
- [ ] Join team communication channels
- [ ] Ask questions if stuck!

---

## 🎓 Recommended Reading Order

1. This file (INDEX.md) - Navigation guide
2. [README.md](README.md) - Project overview
3. [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) - Get it running
4. [DEVELOPMENT.md](DEVELOPMENT.md) - How to develop
5. [ARCHITECTURE.md](ARCHITECTURE.md) - How it works
6. Component-specific READMEs - Deep dives

---

## 🎉 You're Ready!

Everything you need is documented. Questions? Check the relevant README file.

**Let's build something amazing!** ⚔️

---

**Last Updated**: 2026-06-06  
**Document Version**: 1.0  
**Status**: ✅ Complete
