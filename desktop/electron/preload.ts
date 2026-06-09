import { contextBridge, ipcRenderer } from 'electron';

contextBridge.exposeInMainWorld('electron', {
  getAppVersion: () => ipcRenderer.invoke('get-app-version'),
  getAppPath: () => ipcRenderer.invoke('get-app-path'),
  checkBackendHealth: () => ipcRenderer.invoke('check-backend-health'),
  platform: process.platform,
  env: process.env.NODE_ENV,
});

declare global {
  interface Window {
    electron: {
      getAppVersion: () => Promise<string>;
      getAppPath: () => Promise<string>;
      checkBackendHealth: () => Promise<boolean>;
      platform: string;
      env: string;
    };
  }
}
