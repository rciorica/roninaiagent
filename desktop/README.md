# Ronin Desktop Application

This directory contains the Electron-based desktop application for Ronin - the AI Agent Master Controller.

## Quick Start

### Development

```bash
# Install dependencies
npm install

# Start development (runs both Vite dev server and Electron)
npm run dev
```

The dev command will:
1. Start the Vite dev server on port 5173
2. Start the Electron app pointing to the dev server
3. Open DevTools for debugging

### Building

```bash
# Build both frontend and electron
npm run build

# Create Windows installer and portable exe
npm run dist:win

# Create only portable exe
npm run dist:portable

# Create all distributions
npm run dist
```

## Project Structure

```
desktop/
├── electron/              # Electron main process code
│   ├── main.ts           # Application entry point
│   ├── window.ts         # Window creation and management
│   ├── preload.ts        # IPC bridge between main and renderer
│   └── utils.ts          # Utility functions
├── build/                # Assets for installer (icons, etc.)
├── installers/           # NSIS installer configuration
├── dist/                 # Build output (generated)
└── package.json          # Desktop app configuration
```

## Features

- **Electron Framework**: Native desktop app using Electron 33
- **React Frontend**: Reuses the existing React frontend
- **IPC Communication**: Safe bridge between main and renderer processes
- **Windows Installer**: NSIS-based installer with uninstaller
- **Portable Executable**: Single .exe file without installation
- **Auto-updates**: Ready for electron-updater integration

## Backend Integration

The desktop app connects to the backend server running on `http://localhost:8080`.

The app will:
1. Check backend health on startup
2. Proxy all API requests to the backend
3. Display a warning if backend is unavailable

## Installer Details

The Windows installer includes:
- Start Menu shortcuts
- Desktop shortcut
- Uninstaller
- Registry entries for Add/Remove Programs
- All necessary dependencies bundled

## Development Tips

1. **Hot Reload**: Changes to React components reload automatically via Vite
2. **Main Process Changes**: Reload the Electron window after main process changes
3. **IPC Debugging**: Use DevTools Console to debug IPC communication
4. **Signing**: To sign releases, add certificate details to package.json build.win section

## Building for Production

```bash
# Build with production optimizations
NODE_ENV=production npm run dist:win
```

This creates:
1. `Ronin-{version}-Setup.exe` - NSIS installer
2. `Ronin-{version}.exe` - Portable executable

## Troubleshooting

### Port Already in Use
If port 5173 is in use:
```bash
npm run dev:electron  # Skip Vite, connect to existing server
```

### Backend Not Responding
- Ensure backend is running on port 8080
- Check firewall settings
- Verify backend logs for errors

### Build Fails
1. Clear node_modules and dist: `rm -r node_modules dist`
2. Reinstall: `npm install`
3. Try building again: `npm run build`

## Next Steps

1. Add app icon: Place PNG icon at `build/icon.png`
2. Configure auto-updates: Integrate electron-updater
3. Add analytics: Integrate Sentry or similar
4. Code signing: Set up certificate for official releases
