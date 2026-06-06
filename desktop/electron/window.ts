import { BrowserWindow } from 'electron';
import path from 'path';
import { isDev } from './utils';

export function createWindow(): BrowserWindow {
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 1000,
    minHeight: 600,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: false,
      contextIsolation: true,
    },
    icon: path.join(__dirname, '../../build/icon.ico'),
  });

  if (isDev()) {
    // Load from dev server in development
    win.loadURL('http://localhost:5173');
    win.webContents.openDevTools();
  } else {
    // Load from built files in production
    // Electron main is compiled to dist/electron, so frontend files live one level up.
    win.loadFile(path.join(__dirname, '../index.html'));
  }

  return win;
}
