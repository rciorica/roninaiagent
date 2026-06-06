# Desktop App Build & Distribution Checklist

Use this checklist when building and distributing the Ronin desktop application for Windows.

## Pre-Build Checklist

- [ ] **Environment Setup**
  - [ ] Node.js 16+ installed (`node --version`)
  - [ ] npm updated (`npm --version`)
  - [ ] Java 11+ installed (`java -version`)
  - [ ] Maven 3.6+ installed (`mvn --version`)

- [ ] **Code Quality**
  - [ ] Run linter: `cd frontend && npm run lint`
  - [ ] Run tests: `npm run test`
  - [ ] No ESLint errors
  - [ ] No TypeScript errors: `npm run build`
  - [ ] Code reviewed by team member

- [ ] **Dependencies**
  - [ ] All npm packages updated
  - [ ] No security vulnerabilities (`npm audit`)
  - [ ] Backend Maven dependencies up to date

## Build Process Checklist

- [ ] **Backend**
  - [ ] Build JAR: `cd backend && mvn clean package`
  - [ ] JAR file created: `backend/target/backend-0.0.1-SNAPSHOT.jar`
  - [ ] No build warnings
  - [ ] Tests passed

- [ ] **Frontend**
  - [ ] Build frontend: `cd frontend && npm run build`
  - [ ] `frontend/dist/` created with index.html
  - [ ] Build size reasonable (< 500KB gzipped)
  - [ ] No broken assets

- [ ] **Desktop**
  - [ ] Navigate to: `cd desktop`
  - [ ] Install dependencies: `npm install`
  - [ ] Build Electron: `npm run build`
  - [ ] TypeScript compiled without errors
  - [ ] `desktop/dist/electron/` folder created

## Pre-Release Checklist

- [ ] **Version Management**
  - [ ] Update version in `desktop/package.json`
  - [ ] Match version across: backend, frontend, root
  - [ ] Version follows semver (X.Y.Z)
  - [ ] Update CHANGELOG.md with release notes

- [ ] **Testing**
  - [ ] Launch dev version: `npm run dev:desktop`
  - [ ] Test user login flow
  - [ ] Test project creation
  - [ ] Test all main features
  - [ ] Test with backend offline (should show warning)
  - [ ] Test window resizing and responsiveness
  - [ ] Close and reopen app (no errors on restart)

- [ ] **Documentation**
  - [ ] README.md up to date
  - [ ] SETUP.md covers all steps
  - [ ] Known issues documented
  - [ ] System requirements clear

- [ ] **Assets**
  - [ ] App icon exists: `desktop/build/icon.png` (512x512)
  - [ ] Icon looks good at small sizes (16x16, 32x32)
  - [ ] Windows icon: `desktop/build/icon.ico`
  - [ ] Splash screen (if applicable)

## Build Installers

- [ ] **Create Distribution Files**
  ```bash
  cd desktop
  npm run dist:win
  ```
  
- [ ] **Verify Output Files**
  - [ ] `desktop/dist/Ronin-{version}-Setup.exe` exists
  - [ ] `desktop/dist/Ronin-{version}.exe` (portable) exists
  - [ ] File sizes reasonable (50-200 MB)
  - [ ] All files have correct version numbers

## Installation Testing

### Setup.exe Testing

- [ ] **Installation**
  - [ ] Double-click installer runs
  - [ ] Windows UAC dialog appears (admin access)
  - [ ] License/agreement screen (if configured)
  - [ ] Destination folder selection works
  - [ ] Installation progress shows
  - [ ] Installation completes successfully

- [ ] **Post-Installation**
  - [ ] Application installed to Program Files
  - [ ] Start Menu shortcut created
  - [ ] Desktop shortcut created
  - [ ] Registry entries added (Add/Remove Programs)
  - [ ] Uninstaller created in install directory

- [ ] **First Launch**
  - [ ] App launches from Start Menu
  - [ ] App launches from Desktop shortcut
  - [ ] Window opens with correct size
  - [ ] No console windows or errors
  - [ ] DevTools doesn't auto-open in production
  - [ ] Backend health check works

- [ ] **Functionality**
  - [ ] Login page displays
  - [ ] API calls to backend work
  - [ ] All UI elements responsive
  - [ ] No crashes or hangs

- [ ] **Uninstallation**
  - [ ] Uninstall through Add/Remove Programs works
  - [ ] All files removed from Program Files
  - [ ] Shortcuts removed from Start Menu/Desktop
  - [ ] Registry entries cleaned
  - [ ] User data preserved (if applicable)

### Portable.exe Testing

- [ ] **Execution**
  - [ ] Double-click .exe launches app
  - [ ] No installation required
  - [ ] Works from USB drive
  - [ ] Works from network path

- [ ] **Functionality**
  - [ ] Same features as installed version
  - [ ] Can access backend
  - [ ] No registry access needed

- [ ] **Cleanup**
  - [ ] Can simply delete .exe to uninstall
  - [ ] No files left behind

## Windows Compatibility

- [ ] **Test on Multiple Windows Versions**
  - [ ] Windows 10 (2004+)
  - [ ] Windows 11
  - [ ] Different user account types
  - [ ] Different screen resolutions
  - [ ] Different DPI settings (125%, 150%, 200%)

- [ ] **Antivirus/Security**
  - [ ] No false positives reported
  - [ ] Application not flagged
  - [ ] Whitelist request if flagged
  - [ ] Signed executable (future)

## Release Checklist

- [ ] **GitHub Release**
  - [ ] Create release on GitHub
  - [ ] Tag version: `v1.0.0`
  - [ ] Write release notes
  - [ ] Describe new features
  - [ ] Note bug fixes
  - [ ] List system requirements
  - [ ] Attach Setup.exe
  - [ ] Attach Portable.exe

- [ ] **Documentation**
  - [ ] Create Installation Guide PDF
  - [ ] Update website download page
  - [ ] Update support documentation
  - [ ] Announce on channels (Slack, email, etc.)

- [ ] **Backup**
  - [ ] Save installers to release server
  - [ ] Backup to multiple locations
  - [ ] Tag source code with version

## Post-Release

- [ ] **Monitor**
  - [ ] Check GitHub Issues for bug reports
  - [ ] Monitor crash reports (if integrated)
  - [ ] Respond to user feedback
  - [ ] Document common issues

- [ ] **Support**
  - [ ] Provide installation support
  - [ ] Help with troubleshooting
  - [ ] Collect user feedback
  - [ ] Plan next release based on feedback

## Rollback Plan

If issues are found:

- [ ] Have previous version available
- [ ] Instructions for downgrade
- [ ] Clear communication to users
- [ ] Root cause analysis
- [ ] Fix and re-release

## Future Enhancements

- [ ] Code signing certificate
- [ ] Auto-update mechanism (electron-updater)
- [ ] Digital signature verification
- [ ] Crash reporting (Sentry)
- [ ] Usage analytics
- [ ] App Store distribution
- [ ] Windows Store submission
- [ ] System tray integration

## Notes

```
Release Date: _____________
Version: _____________
Tester: _____________
Signed By: _____________
Comments:
___________________________________________________________________________
___________________________________________________________________________
```

## Troubleshooting During Build

### Build Fails with Module Not Found
```bash
cd desktop
rm -rf node_modules package-lock.json
npm install
npm run build
```

### Installer Creation Fails
- Check if NSIS is installed
- Verify paths don't contain special characters
- Check disk space (2GB+ free)
- Run with admin privileges

### Icon Not Showing
- Verify icon files exist in `desktop/build/`
- Check file formats (.png for image, .ico for Windows)
- Icon must be square (512x512)

### Port Already in Use
```bash
# Kill process using port
netstat -ano | findstr :5173
taskkill /PID <PID> /F
```

## Support Contacts

- Lead Developer: [name]
- QA Lead: [name]
- DevOps: [name]
- Manager: [name]

---

**Last Updated**: [Date]  
**Document Version**: 1.0
