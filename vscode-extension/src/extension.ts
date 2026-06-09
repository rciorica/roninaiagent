import * as vscode from 'vscode';
import { RoninAgentProvider } from './agentViewProvider';

export function activate(context: vscode.ExtensionContext) {
  const provider = new RoninAgentProvider(context);

  context.subscriptions.push(
    vscode.window.registerTreeDataProvider('roninAgentView', provider),
    vscode.commands.registerCommand('ronin.openAgentView', async () => {
      await vscode.commands.executeCommand('workbench.view.explorer');
      provider.refresh();
    }),
    vscode.commands.registerCommand('ronin.login', () => provider.login()),
    vscode.commands.registerCommand('ronin.openAgentWebview', () => provider.openAgentWebview()),
    vscode.commands.registerCommand('ronin.askSelection', () => provider.askSelection()),
    vscode.commands.registerCommand('ronin.performEditorAction', (args) => provider.performEditorAction(args)),
    vscode.commands.registerCommand('ronin.refresh', () => provider.refresh()),
    vscode.commands.registerCommand('ronin.showRank', () => provider.showRank()),
    vscode.commands.registerCommand('ronin.openProject', (project) => provider.openProject(project))
  );
}

export function deactivate() {
  // Cleanup is handled by VS Code subscriptions.
}
