# 🎯 Quick Reference Card - Ronin Multi-Platform

## 🚀 Getting Started (Windows)

### 1. One-Click Setup
```bash
setup.bat
```

### 2. Start Development
```bash
npm run dev
```

### 3. Access Applications
- **Backend**: http://localhost:8080
- **Web App**: http://localhost:5173
- **Desktop**: Electron window opens automatically

---

## 📂 Key Folders

```
ronin/
├── backend/       Java/Spring Boot API
├── frontend/      React web app (shared)
├── desktop/       Electron desktop app
└── test-runner/   Test automation
```

---

## 🔧 Essential Commands

| Task | Command |
|------|---------|
| Setup | `setup.bat` |
| All Services | `npm run dev` |
| Backend Only | `npm run dev:backend` |
| Web Only | `npm run dev:web` |
| Desktop Only | `npm run dev:desktop` |
| Build All | `npm run build` |
| Run Tests | `npm run test` |
| Build Installers | `npm run desktop:dist` |
| Portable Exe | `npm run desktop:portable` |

---

## 📚 Documentation Guide

**Choose your path:**

| Need | Document | Time |
|------|----------|------|
| 5-min setup | [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) | 5 min |
| Overview | [README.md](README.md) | 10 min |
| Development | [DEVELOPMENT.md](DEVELOPMENT.md) | 30 min |
| Architecture | [ARCHITECTURE.md](ARCHITECTURE.md) | 20 min |
| Desktop | [desktop/README.md](desktop/README.md) | 10 min |
| Release | [DESKTOP_BUILD_CHECKLIST.md](DESKTOP_BUILD_CHECKLIST.md) | 45 min |
| Navigation | [INDEX.md](INDEX.md) | 5 min |

---

## 🖥️ Desktop Development

### Start Desktop Dev
```bash
cd desktop
npm run dev
```

### Build Desktop
```bash
cd desktop
npm run build
```

### Create Installers
```bash
cd desktop
npm run dist:win           # Full installer
npm run dist:portable      # Portable .exe
```

### Files to Know
- Main process: `desktop/electron/main.ts`
- Window mgmt: `desktop/electron/window.ts`
- IPC bridge: `desktop/electron/preload.ts`
- Config: `desktop/package.json`

---

## 🔌 API Endpoints (Quick Reference)

```
Authentication:
  POST   /auth/register
  POST   /auth/login
  POST   /auth/logout

Projects:
  GET    /projects
  POST   /projects
  GET    /projects/{id}
  PUT    /projects/{id}

Tests:
  POST   /tests/{projectId}/run
  GET    /tests/{runId}/results

LLM:
  GET    /llm/providers
  POST   /llm/switch
```

See backend README for full API docs.

---

## 🐛 Quick Troubleshooting

### Port in Use?
```bash
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Backend Won't Start?
1. Check: `java -version`
2. Check: `mvn -version`
3. Try: `cd backend && mvn clean package`

### Frontend Issues?
```bash
cd frontend
rm -rf node_modules dist
npm install
npm run build
```

### Desktop Issues?
```bash
cd desktop
rm -rf node_modules dist
npm install
npm run build
```

---

## 📊 Project Structure at a Glance

```
Web App Path:
  Frontend (React)
  ├─ /frontend/src
  ├─ http://localhost:5173
  └─ Build: frontend/dist/

Backend Path:
  API (Java/Spring)
  ├─ /backend/src
  ├─ http://localhost:8080
  └─ Build: backend/target/*.jar

Desktop Path:
  Electron (Node.js + React)
  ├─ /desktop/electron
  ├─ Port: localhost:8080 (backend connection)
  └─ Build: desktop/dist/Ronin-*.exe
```

---

## 🎯 Development Workflow

### Adding a Feature

1. **Create Branch**
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make Changes**
   - Backend: `backend/src/main/...`
   - Frontend: `frontend/src/...`
   - Desktop: `desktop/electron/...`

3. **Test Locally**
   ```bash
   npm run dev           # Development
   npm run test          # Unit tests
   npm run build         # Production build
   ```

4. **Commit & Push**
   ```bash
   git add .
   git commit -m "Add feature: description"
   git push origin feature/my-feature
   ```

5. **Create Pull Request**
   - On GitHub
   - Request review
   - Address feedback
   - Merge when approved

---

## 🚀 Deployment

### Web App (to Heroku)
```bash
git push heroku main
```

### Desktop (Create Installers)
```bash
cd desktop
npm run dist:win
# Files in desktop/dist/
```

### Release Checklist
See: [DESKTOP_BUILD_CHECKLIST.md](DESKTOP_BUILD_CHECKLIST.md)

---

## 🧪 Testing

### Backend Tests
```bash
mvn test
mvn test -Dtest=UserControllerTest
```

### Frontend Tests
```bash
cd frontend
npm run test
npm run test:watch
```

### Full Test Suite
```bash
npm run test
```

---

## 🔐 Security Notes

- ✅ JWT tokens for auth
- ✅ Bcrypt password hashing
- ✅ Parameterized SQL queries
- ✅ CORS configured
- ✅ IPC security bridge
- ✅ No hardcoded secrets

---

## 💡 Pro Tips

1. **Hot Reload**: Changes to React auto-reload in development
2. **Backend Logs**: See Spring Boot logs for API errors
3. **DevTools**: Ctrl+Shift+I in Electron window
4. **API Testing**: Use Postman or curl for endpoint testing
5. **Git Commits**: Use meaningful commit messages
6. **Code Review**: Always get a second opinion before merging

---

## 📞 Getting Help

1. Check relevant README file
2. Search existing documentation
3. Check GitHub Issues
4. Ask team member

---

## 🎓 Learning Resources

- **React**: https://react.dev
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Electron**: https://www.electronjs.org/docs
- **TypeScript**: https://www.typescriptlang.org/
- **Tailwind CSS**: https://tailwindcss.com/

---

## 📋 Pre-Commit Checklist

Before pushing code:
- [ ] Tests pass: `npm run test`
- [ ] Linter passes: `cd frontend && npm run lint`
- [ ] No console errors
- [ ] Tested locally
- [ ] Code reviewed by self
- [ ] Meaningful commit message

---

## 🔄 Version Control

### Useful Git Commands
```bash
# See status
git status

# View changes
git diff

# See log
git log --oneline

# Create branch
git checkout -b feature/name

# Switch branch
git checkout branch-name

# Pull latest
git pull

# Push changes
git push origin branch-name
```

---

## 🎯 First Week Checklist

- [ ] Setup complete (`setup.bat` ran)
- [ ] All services running (`npm run dev`)
- [ ] Created test account
- [ ] Created test project
- [ ] Read QUICKSTART_WINDOWS.md
- [ ] Read DEVELOPMENT.md
- [ ] Explored codebase
- [ ] Made first code change
- [ ] Ran tests successfully
- [ ] Built desktop installer
- [ ] Familiar with main files
- [ ] Know who to ask for help

---

## 🌟 Key Concepts

**Monorepo**: Single git repo with multiple apps (backend, frontend, desktop)

**Hot Reload**: Code changes auto-update without restart

**IPC**: Inter-Process Communication (Electron main ↔ renderer)

**JWT**: JSON Web Tokens (authentication)

**ORM**: Object-Relational Mapping (database abstraction)

**CI/CD**: Continuous Integration/Deployment (automation)

---

## 📌 Bookmarks

Save these for quick access:
- [INDEX.md](INDEX.md) - Navigation
- [QUICKSTART_WINDOWS.md](QUICKSTART_WINDOWS.md) - Setup
- [DEVELOPMENT.md](DEVELOPMENT.md) - Workflow
- [ARCHITECTURE.md](ARCHITECTURE.md) - Design

---

**Print this page. Keep it handy. Reference often.** 📄

---

**Version**: 1.0  
**Updated**: 2026-06-06  
**Status**: ✅ Ready to Use
