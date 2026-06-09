# Ronin VS Code Extension

This package contains the VS Code extension for Ronin, the AI Agent Master Controller.

## Getting Started

1. Install dependencies:
   ```bash
   cd vscode-extension
   npm install
   ```
2. Compile the extension:
   ```bash
   npm run compile
   ```
3. Launch the extension development host from VS Code.

## Usage

- Open the command palette and run `Ronin: Open Agent View`.
- Use the Ronin tree view to sign in, refresh the view, and open project details.
- Set the backend URL in settings under `ronin.backendUrl`.

## Configuration

- `ronin.backendUrl`: Primary Ronin backend URL. Defaults to the Heroku deployment.
- `ronin.fallbackBackendUrl`: Fallback local backend URL for development or offline use.
