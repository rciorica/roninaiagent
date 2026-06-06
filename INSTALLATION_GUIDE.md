# Install Git for Windows

## Option 1: Using Chocolatey (Fastest)
If you have Chocolatey installed:
```powershell
choco install git -y
```

## Option 2: Direct Download
1. Go to https://git-scm.com/download/win
2. Download the installer (64-bit is recommended)
3. Run the installer with default settings
4. Restart PowerShell when done

## Option 3: Using Windows Package Manager (winget)
```powershell
winget install Git.Git
```

## Verify Installation
```powershell
git --version
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

---

# Install Heroku CLI

## Option 1: Using Chocolatey (Fastest)
```powershell
choco install heroku-cli -y
```

## Option 2: Direct Download
1. Go to https://devcenter.heroku.com/articles/heroku-cli
2. Download the Windows installer
3. Run the installer
4. Restart PowerShell when done

## Option 3: Using npm (if Node.js is installed)
```powershell
npm install -g heroku
```

## Verify Installation
```powershell
heroku --version
heroku login
```

---

# Next Steps After Installation

1. **Restart PowerShell** (important!)
2. **Verify both are installed:**
   ```powershell
   git --version
   heroku --version
   ```

3. **Configure Git if needed:**
   ```powershell
   git config --global user.name "Your Name"
   git config --global user.email "your.email@example.com"
   ```

4. **Login to Heroku:**
   ```powershell
   heroku login
   ```

5. **Once verified, run the deployment:**
   ```powershell
   cd c:\cygwin64\home\BTCES\roninaiagent
   heroku login
   # Then we can proceed with deployment
   ```

---

**Recommended Approach:** Use Chocolatey for both (fastest), or use the direct installers.

Please install both tools, then let me know and we'll proceed with deployment!
