# Ronin Desktop Application Setup Guide

## Overview

The desktop application extends the Ronin web app to Windows with:
- Native Windows desktop experience using Electron
- Windows installer (NSIS) with uninstaller
- Portable single-exe version
- Seamless integration with existing backend
- Code reuse from React frontend

## Architecture

```
┌─────────────────────────────────────────────────────┐
│              Ronin Desktop Application              │
│                    (Electron)                       │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────────────┐        ┌──────────────────┐  │
│  │  Main Process    │        │ Renderer Process │  │
│  │  (Node.js)       │◄──────►│  (React Frontend)│  │
│  │                  │   IPC   │                  │  │
│  └──────────────────┘        └──────────────────┘  │
│          │                             │            │
│          └─────────────────┬───────────┘            │
│                            │                        │
│                    ┌───────▼────────┐              │
│                    │  Browser API   │              │
│                    │  Fetch, etc.   │              │
│                    └───────┬────────┘              │
│                            │                        │
└────────────────────────────┼────────────────────────┘
                             │ HTTP
                    ┌────────▼─────────┐
                    │ Backend Service  │
                    │ (Java/Spring)    │
                    │ Port 8080        │
                    └──────────────────┘
```

## Setup Instructions

### Prerequisites
- Node.js 16+ and npm
- Java backend running on localhost:8080
- Windows 10 or later

### Installation Steps

1. **Navigate to desktop folder**
```bash
cd desktop
```

2. **Install dependencies**
```bash
npm install
```

3. **Build backend and frontend**
From the root directory:
```bash
cd backend
mvn clean package

cd ../frontend
npm install
npm run build

cd ../desktop
```

4. **Start development**
```bash
npm run dev
```

### Building Installers

1. **Windows Installer + Portable**
```bash
npm run dist:win
```

Outputs:
- `dist/Ronin-{version}-Setup.exe` - Full installer
- `dist/Ronin-{version}.exe` - Portable version

2. **Portable Only**
```bash
npm run dist:portable
```

### Distribution

The installers are located in `desktop/dist/`:
- **Setup.exe**: Full installer with Start Menu entries
- **Portable.exe**: Single executable, no installation needed

## Configuration

### Backend Connection
Edit `electron/main.ts` to change backend URL if needed:
```typescript
// Default: http://localhost:8080
const response = await fetch('http://localhost:8080/actuator/health', {
  timeout: 5000,
});
```

### App Metadata
Edit `desktop/package.json` build section:
- `name`: Application identifier
- `productName`: Display name
- `version`: Application version
- `appId`: Unique identifier for auto-updates

## Development Workflow

### Hot Reload
Changes to React code reload automatically via Vite without restarting Electron.

### Main Process Changes
After modifying `electron/main.ts`:
1. The TypeScript compiles automatically
2. Manually reload Electron window (Ctrl+R)

### Debugging
- **Frontend**: Open DevTools (Ctrl+Shift+I)
- **Main Process**: Output visible in terminal
- **IPC**: Use console.log() in both processes

## Features Implemented

✅ Window management  
✅ IPC bridge for secure communication  
✅ Backend health checks  
✅ Menu bar with standard items  
✅ NSIS installer configuration  
✅ Portable executable support  
✅ TypeScript support  
✅ Development hot reload  
✅ Production optimizations  

## Future Enhancements

- [ ] Auto-update functionality (electron-updater)
- [ ] System tray integration
- [ ] Application settings/preferences
- [ ] Offline capability
- [ ] Native notifications
- [ ] Code signing for official releases
- [ ] Crash reporting (Sentry)
- [ ] Analytics

## Troubleshooting

### Port 5173 In Use
```bash
# Kill process using port
netstat -ano | findstr :5173
taskkill /PID <PID> /F
```

### Backend Not Found
1. Check if backend is running: `curl http://localhost:8080/actuator/health`
2. Verify firewall isn't blocking localhost:8080
3. Check backend logs for errors

### Build Issues
```bash
# Clean and rebuild
rm -r node_modules dist
npm install
npm run build
```

### Installer Issues
- Ensure admin privileges for installation
- Close any running Ronin instances before upgrading
- Check Windows Event Viewer for install errors

## Support

For issues or questions:
1. Check backend logs
2. Check Electron DevTools console
3. Review electron/main.ts for IPC errors
4. Check firewall and port availability
