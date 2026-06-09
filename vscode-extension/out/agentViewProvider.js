"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.RoninAgentProvider = void 0;
const path = __importStar(require("path"));
const vscode = __importStar(require("vscode"));
const DEFAULT_BACKEND_URL = 'https://ronin-backend.herokuapp.com';
const SECONDARY_BACKEND_URL = 'https://ronin-backend-d8bbbbb0386c.herokuapp.com';
const FALLBACK_BACKEND_URL = 'http://localhost:8080';
function getConfiguredBackendUrl() {
    const configuration = vscode.workspace.getConfiguration('ronin');
    return configuration.get('backendUrl')?.trim() || DEFAULT_BACKEND_URL;
}
function getFallbackBackendUrl() {
    const configuration = vscode.workspace.getConfiguration('ronin');
    return configuration.get('fallbackBackendUrl')?.trim() || FALLBACK_BACKEND_URL;
}
function getBackendUrls(path) {
    const configured = getConfiguredBackendUrl().replace(/\/+$|\s+/g, '');
    const primary = configured ? `${configured}${path.startsWith('/') ? path : `/${path}`}` : undefined;
    const secondary = SECONDARY_BACKEND_URL ? `${SECONDARY_BACKEND_URL}${path.startsWith('/') ? path : `/${path}`}` : undefined;
    const urls = [primary, secondary].filter(Boolean);
    return Array.from(new Set(urls));
}
class RoninAgentProvider {
    constructor(context) {
        this.context = context;
        this._onDidChangeTreeData = new vscode.EventEmitter();
        this.onDidChangeTreeData = this._onDidChangeTreeData.event;
    }
    getTreeItem(element) {
        return element;
    }
    async getChildren(element) {
        if (!element) {
            return [
                this.createCollapsibleItem('Projects'),
                this.createCollapsibleItem('Rank & Status'),
                this.createCollapsibleItem('Actions')
            ];
        }
        switch (element.label) {
            case 'Projects':
                return this.getProjectNodes();
            case 'Rank & Status':
                return [
                    this.createCommandItem('Show My Rank', 'ronin.showRank'),
                    this.createCommandItem('Refresh User Info', 'ronin.refresh')
                ];
            case 'Actions':
                return [
                    this.createCommandItem('Sign in to Ronin', 'ronin.login'),
                    this.createCommandItem('Open Dashboard Panel', 'ronin.openAgentWebview'),
                    this.createCommandItem('Ask About Selection', 'ronin.askSelection'),
                    this.createActionCommandItem('Explain selected code', 'explain', 'Explain the selected code or the current file in detail.'),
                    this.createActionCommandItem('Fix selected code', 'fix', 'Review and fix the selected code or current file, correcting bugs and improving quality.'),
                    this.createActionCommandItem('Refactor selection', 'refactor', 'Refactor the selected code or current file for readability and maintainability.'),
                    this.createActionCommandItem('Generate tests for selection', 'generate_tests', 'Generate test code for the selected code or current file.'),
                    this.createActionCommandItem('Create a new file from prompt', 'create_file', 'Create a new file according to the following prompt.'),
                    this.createActionCommandItem('Insert snippet into active editor', 'insert_snippet', 'Provide a code snippet suitable for insertion into the active editor.'),
                    this.createActionCommandItem('Replace active editor content', 'replace_content', 'Replace the active editor content with an improved AI-generated version.'),
                    this.createActionCommandItem('Document selected code', 'document', 'Add comments and documentation to the selected code or active file.'),
                    this.createActionCommandItem('Summarize current file', 'summarize', 'Summarize the current file and explain its purpose.'),
                    this.createActionCommandItem('Search workspace and apply changes', 'search_and_apply', 'Find the relevant workspace files and suggest edits across the project.'),
                    this.createActionCommandItem('Open file with Ronin suggestion', 'open_file', 'Identify a workspace file that should be changed and describe the update needed.'),
                    this.createCommandItem('Refresh Agent View', 'ronin.refresh')
                ];
            default:
                return [];
        }
    }
    createCollapsibleItem(label) {
        const item = new vscode.TreeItem(label, vscode.TreeItemCollapsibleState.Collapsed);
        item.contextValue = label.toLowerCase().replace(/\s+/g, '-');
        return item;
    }
    createCommandItem(label, commandId) {
        const item = new vscode.TreeItem(label, vscode.TreeItemCollapsibleState.None);
        item.command = { command: commandId, title: label };
        return item;
    }
    createActionCommandItem(label, actionKey, prompt) {
        const item = new vscode.TreeItem(label, vscode.TreeItemCollapsibleState.None);
        item.command = {
            command: 'ronin.performEditorAction',
            title: label,
            arguments: [{ actionKey, prompt }]
        };
        return item;
    }
    async getProjectNodes() {
        const token = await this.getToken();
        if (!token) {
            return [new vscode.TreeItem('Please sign in to load projects', vscode.TreeItemCollapsibleState.None)];
        }
        try {
            const projects = await this.request('/projects');
            if (!Array.isArray(projects) || projects.length === 0) {
                return [new vscode.TreeItem('No projects available', vscode.TreeItemCollapsibleState.None)];
            }
            return projects.map((project) => {
                const item = new vscode.TreeItem(project.name || `Project #${project.id}`, vscode.TreeItemCollapsibleState.None);
                item.description = project.status || '';
                item.command = {
                    command: 'ronin.openProject',
                    title: 'Open project details',
                    arguments: [project]
                };
                return item;
            });
        }
        catch (error) {
            const message = error instanceof Error ? error.message : 'Unable to load projects';
            return [new vscode.TreeItem(`Error: ${message}`, vscode.TreeItemCollapsibleState.None)];
        }
    }
    async getProjectMessages(projectId) {
        try {
            const messages = await this.request(`/projects/${projectId}/messages`);
            return Array.isArray(messages) ? messages : [];
        }
        catch (error) {
            return [];
        }
    }
    async askSelection() {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showWarningMessage('Open a file and select text before asking Ronin.');
            return;
        }
        const project = await this.pickProjectForSelection();
        if (!project) {
            return;
        }
        const selectedText = editor.document.getText(editor.selection).trim();
        const filePath = vscode.workspace.asRelativePath(editor.document.uri, false);
        const prompt = selectedText
            ? `Review and improve the selected code in ${filePath}:\n\n${selectedText}`
            : `Review the active file ${filePath} and provide guidance:\n\n${editor.document.getText()}`;
        try {
            const response = await this.request('/llm/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    projectId: project.id,
                    message: prompt,
                    filePath,
                    activeEditorText: selectedText || editor.document.getText()
                })
            });
            const output = vscode.window.createOutputChannel('Ronin AI');
            output.clear();
            output.appendLine(`Ronin response for ${filePath}`);
            output.appendLine('---');
            output.appendLine(response?.response || 'No response received from Ronin.');
            output.show(true);
        }
        catch (error) {
            const message = error instanceof Error ? error.message : 'Unable to ask Ronin about selection.';
            vscode.window.showErrorMessage(`Ronin selection request failed: ${message}`);
        }
    }
    async pickProjectForSelection() {
        const token = await this.getToken();
        if (!token) {
            vscode.window.showWarningMessage('Please sign in to Ronin before using selection-based assistance.');
            return undefined;
        }
        const projects = await this.request('/projects');
        if (!Array.isArray(projects) || projects.length === 0) {
            vscode.window.showWarningMessage('No Ronin projects are available for workspace assistance.');
            return undefined;
        }
        if (projects.length === 1) {
            return projects[0];
        }
        const selection = await vscode.window.showQuickPick(projects.map((project) => ({
            label: project.name || `Project #${project.id}`,
            description: project.status || '',
            project
        })), { placeHolder: 'Select a Ronin project to use for this editor selection' });
        return selection?.project;
    }
    async performEditorAction(args) {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showWarningMessage('Open a file in the editor before running a Ronin editor action.');
            return;
        }
        const project = await this.pickProjectForSelection();
        if (!project) {
            return;
        }
        const selectedText = editor.document.getText(editor.selection).trim();
        const filePath = vscode.workspace.asRelativePath(editor.document.uri, false);
        const basePrompt = String(args?.prompt || '').trim();
        const derivedPrompt = basePrompt || `Perform the editor action ${String(args?.actionKey || 'action')} on ${filePath}.`;
        const message = selectedText
            ? `${derivedPrompt}\n\nSelected text:\n${selectedText}`
            : `${derivedPrompt}\n\nActive file: ${filePath}`;
        try {
            const response = await this.request('/llm/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    projectId: project.id,
                    message,
                    filePath,
                    activeEditorText: selectedText || editor.document.getText()
                })
            });
            const output = vscode.window.createOutputChannel('Ronin AI');
            output.clear();
            output.appendLine(`Ronin editor action result for ${filePath}`);
            output.appendLine('---');
            output.appendLine(response?.response || 'No response received from Ronin.');
            output.show(true);
        }
        catch (error) {
            const message = error instanceof Error ? error.message : 'Unable to perform the editor action.';
            vscode.window.showErrorMessage(`Ronin editor action failed: ${message}`);
        }
    }
    async runTerminalCommand(command) {
        if (!command?.trim()) {
            throw new Error('Command cannot be empty.');
        }
        const terminal = vscode.window.activeTerminal || vscode.window.createTerminal('Ronin AI');
        terminal.show(true);
        terminal.sendText(command, true);
        return `Sent command to Ronin terminal: ${command}`;
    }
    async applyWorkspaceEdit(filePath, content) {
        if (!content?.trim()) {
            throw new Error('Edit content cannot be empty.');
        }
        if (filePath?.trim()) {
            const workspaceFolder = vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;
            if (!workspaceFolder) {
                throw new Error('No workspace folder is open.');
            }
            const resolvedPath = path.isAbsolute(filePath)
                ? filePath
                : path.join(workspaceFolder, filePath);
            const uri = vscode.Uri.file(resolvedPath);
            await vscode.workspace.fs.writeFile(uri, Buffer.from(content, 'utf8'));
            const document = await vscode.workspace.openTextDocument(uri);
            await vscode.window.showTextDocument(document, { preview: false });
            return `Saved file: ${resolvedPath}`;
        }
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            throw new Error('No active editor to modify.');
        }
        const fullRange = new vscode.Range(editor.document.positionAt(0), editor.document.positionAt(editor.document.getText().length));
        await editor.edit((editBuilder) => {
            editBuilder.replace(fullRange, content);
        });
        await editor.document.save();
        return `Updated active editor: ${editor.document.fileName}`;
    }
    async openProject(project) {
        if (!project) {
            vscode.window.showWarningMessage('No project selected.');
            return;
        }
        const messages = await this.getProjectMessages(project.id);
        const panel = vscode.window.createWebviewPanel('roninProjectDetails', `Ronin Project: ${project.name || project.id}`, vscode.ViewColumn.One, {
            enableScripts: true,
            retainContextWhenHidden: true
        });
        panel.webview.html = this.getProjectHtml(project, messages);
        panel.webview.onDidReceiveMessage(async (event) => {
            if (!event?.command) {
                return;
            }
            try {
                if (event.command === 'sendChat') {
                    const text = String(event.text || '').trim();
                    if (!text) {
                        return;
                    }
                    const actionType = String(event.actionType || 'General').trim();
                    const editor = vscode.window.activeTextEditor;
                    let filePath;
                    let activeEditorText;
                    if (editor) {
                        filePath = vscode.workspace.asRelativePath(editor.document.uri, false);
                        activeEditorText = editor.document.getText(editor.selection).trim() || editor.document.getText();
                    }
                    const response = await this.request('/llm/chat', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            projectId: project.id,
                            message: text,
                            actionType,
                            filePath,
                            activeEditorText
                        })
                    });
                    panel.webview.postMessage({
                        command: 'chatResult',
                        text,
                        response: response?.response || 'No response received from Ronin.',
                        modelUsed: response?.modelUsed,
                        modelSwitched: response?.modelSwitched,
                        previousModel: response?.previousModel
                    });
                }
                if (event.command === 'runCommand') {
                    const commandText = String(event.commandText || '').trim();
                    const result = await this.runTerminalCommand(commandText);
                    panel.webview.postMessage({ command: 'actionResult', result });
                }
                if (event.command === 'applyActiveEditor') {
                    const content = String(event.content || '');
                    const result = await this.applyWorkspaceEdit(undefined, content);
                    panel.webview.postMessage({ command: 'actionResult', result });
                }
                if (event.command === 'applyFile') {
                    const filePath = String(event.filePath || '').trim();
                    const content = String(event.content || '');
                    const result = await this.applyWorkspaceEdit(filePath, content);
                    panel.webview.postMessage({ command: 'actionResult', result });
                }
            }
            catch (error) {
                const message = error instanceof Error ? error.message : 'Action failed.';
                panel.webview.postMessage({ command: 'actionError', message });
            }
        });
    }
    escapeHtml(value) {
        if (value === undefined || value === null) {
            return '';
        }
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }
    getProjectHtml(project, messages = []) {
        const description = this.escapeHtml(project.description || 'No description available.');
        const projectName = this.escapeHtml(project.name || `Project #${project.id}`);
        const messageItems = messages.map((message) => {
            const sender = this.escapeHtml(message.sender || 'system');
            const body = this.escapeHtml(message.message || '');
            const meta = message.modelUsed ? this.escapeHtml(message.modelUsed) : '';
            return `<div class="message ${sender}"><div class="sender">${sender}</div><div class="body">${body}</div>${meta ? `<div class="meta">Model: ${meta}</div>` : ''}</div>`;
        }).join('');
        return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Ronin Project</title>
  <style>
    body { font-family: var(--vscode-font-family); color: var(--vscode-editor-foreground); background: var(--vscode-editor-background); padding: 24px; }
    h1 { margin-bottom: 0.5em; }
    p { line-height: 1.6; margin: 0.5em 0; }
    .meta { margin-top: 0.75em; color: var(--vscode-descriptionForeground); }
    .panel { margin-top: 1.5em; border: 1px solid var(--vscode-editorWidget-border); border-radius: 8px; padding: 16px; background: var(--vscode-editor-inactiveSelectionBackground); }
    .messages { max-height: 320px; overflow-y: auto; margin-bottom: 1rem; }
    .message { margin-bottom: 0.75rem; padding: 0.75rem; border-radius: 8px; background: var(--vscode-editor-background); border: 1px solid var(--vscode-editorHoverWidget-border); }
    .message.user { background: rgba(79, 103, 255, 0.08); }
    .message.ronin { background: rgba(60, 179, 113, 0.1); }
    .message .sender { font-size: 0.85rem; font-weight: 600; margin-bottom: 0.35rem; }
    .message .body { white-space: pre-wrap; }
    .message .meta { margin-top: 0.4rem; font-size: 0.78rem; color: var(--vscode-descriptionForeground); }
    .form-row { display: grid; gap: 0.5rem; margin-bottom: 1rem; }
    input, textarea { width: 100%; border-radius: 6px; border: 1px solid var(--vscode-inputBorder); background: var(--vscode-input-background); color: var(--vscode-input-foreground); padding: 0.75rem; }
    textarea { min-height: 90px; resize: vertical; font-family: var(--vscode-font-family); }
    button { width: auto; padding: 0.75rem 1rem; border: none; border-radius: 6px; background: var(--vscode-button-background); color: var(--vscode-button-foreground); cursor: pointer; }
    button:hover { background: var(--vscode-button-hoverBackground); }
    .action-group { margin-top: 1.5rem; }
    .action-group h3 { margin-bottom: 0.5rem; }
    .status { margin-top: 0.75rem; color: var(--vscode-inputValidation-infoBorder); }
    .label { font-weight: 600; margin-bottom: 0.25rem; display: block; }
  </style>
</head>
<body>
  <h1>${projectName}</h1>
  <p>${description}</p>
  <div class="meta">
    <p><strong>ID:</strong> ${project.id}</p>
    <p><strong>Status:</strong> ${this.escapeHtml(project.status || 'Unknown')}</p>
    <p><strong>Created:</strong> ${this.escapeHtml(project.createdAt || 'Unknown')}</p>
  </div>

  <div class="panel">
    <h2>Chat with Ronin</h2>
    <div class="messages" id="messageList">${messageItems || '<p>No conversation yet. Send a message to begin.</p>'}</div>
    <div class="form-row">
        <label class="label" for="actionType">Action type</label>
        <select id="actionType" class="action-select" aria-label="Ronin action type">
          <option value="General">General</option>
          <option value="Explain selected code">Explain selected code</option>
          <option value="Fix selected code">Fix selected code</option>
          <option value="Refactor selection">Refactor selection</option>
          <option value="Generate tests for selection">Generate tests for selection</option>
          <option value="Create a new file from prompt">Create a new file from prompt</option>
          <option value="Insert snippet into active editor">Insert snippet into active editor</option>
          <option value="Replace active editor content">Replace active editor content</option>
          <option value="Document selected code">Document selected code</option>
          <option value="Summarize current file">Summarize current file</option>
          <option value="Search workspace and apply changes">Search workspace and apply changes</option>
          <option value="Open file with Ronin suggestion">Open file with Ronin suggestion</option>
        </select>
      </div>
      <div class="form-row">
    <div class="status" id="chatStatus"></div>
  </div>

  <div class="panel">
    <h3>Workspace Actions</h3>
    <div class="action-group">
      <label class="label" for="commandInput">Run terminal command</label>
      <input id="commandInput" placeholder="e.g. npm test or git status" aria-label="Terminal command" />
      <button id="runCommandButton" type="button">Run command</button>
    </div>
    <div class="action-group">
      <label class="label" for="fileContentInput">Apply content to active open file</label>
      <textarea id="fileContentInput" placeholder="Paste file content to overwrite the current active editor."></textarea>
      <button id="applyActiveButton" type="button">Apply to active editor</button>
    </div>
    <div class="action-group">
      <label class="label" for="filePathInput">Save content to a specific workspace file</label>
      <input id="filePathInput" placeholder="src/example.txt" aria-label="Workspace file path" />
      <textarea id="filePathContent" placeholder="File content to write to the file."></textarea>
      <button id="applyFileButton" type="button">Save file</button>
    </div>
    <div class="status" id="actionStatus"></div>
  </div>

  <script>
    const vscode = acquireVsCodeApi();
    const chatForm = document.getElementById('chatForm');
    const messageInput = document.getElementById('messageInput');
    const messageList = document.getElementById('messageList');
    const chatStatus = document.getElementById('chatStatus');
    const actionStatus = document.getElementById('actionStatus');
    const sendChatButton = document.getElementById('sendChatButton');
    const runCommandButton = document.getElementById('runCommandButton');
    const applyActiveButton = document.getElementById('applyActiveButton');
    const applyFileButton = document.getElementById('applyFileButton');
    const commandInput = document.getElementById('commandInput');
    const fileContentInput = document.getElementById('fileContentInput');
    const filePathInput = document.getElementById('filePathInput');
    const filePathContent = document.getElementById('filePathContent');
    const actionTypeSelect = document.getElementById('actionType');

    function appendMessage(sender, text, meta = '') {
      const container = document.createElement('div');
      container.className = 'message ' + sender;
      const senderNode = document.createElement('div');
      senderNode.className = 'sender';
      senderNode.textContent = sender === 'user' ? 'You' : 'Ronin';
      const bodyNode = document.createElement('div');
      bodyNode.className = 'body';
      bodyNode.textContent = text;
      container.appendChild(senderNode);
      container.appendChild(bodyNode);

      if (meta) {
        const metaNode = document.createElement('div');
        metaNode.className = 'meta';
        metaNode.textContent = meta;
        container.appendChild(metaNode);
      }

      messageList.appendChild(container);
      messageList.scrollTop = messageList.scrollHeight;
    }

    function sendChat() {
      const text = messageInput.value.trim();
      if (!text) {
        return;
      }
      const actionType = actionTypeSelect?.value || 'General';
      const sendText = actionType !== 'General' ? `;
        $;
        {
            actionType;
        }
        n;
        n$;
        {
            text;
        }
        ` : text;
      appendMessage('user', `;
        $;
        {
            actionType;
        }
        $;
        {
            text;
        }
        `);
      messageInput.value = '';
      chatStatus.textContent = 'Sending...';
      vscode.postMessage({ command: 'sendChat', text: sendText, actionType });
    }

    sendChatButton.addEventListener('click', sendChat);
    messageInput.addEventListener('keydown', (event) => {
      if (event.key === 'Enter' && event.shiftKey) {
        sendChat();
      }
    });

    runCommandButton.addEventListener('click', () => {
      const commandText = commandInput.value.trim();
      if (!commandText) {
        actionStatus.textContent = 'Please enter a command.';
        return;
      }
      actionStatus.textContent = 'Sending command to terminal...';
      vscode.postMessage({ command: 'runCommand', commandText });
    });

    applyActiveButton.addEventListener('click', () => {
      const content = fileContentInput.value;
      if (!content.trim()) {
        actionStatus.textContent = 'Please enter content to apply.';
        return;
      }
      actionStatus.textContent = 'Applying content to active editor...';
      vscode.postMessage({ command: 'applyActiveEditor', content });
    });

    applyFileButton.addEventListener('click', () => {
      const filePath = filePathInput.value.trim();
      const content = filePathContent.value;
      if (!filePath) {
        actionStatus.textContent = 'Please enter a file path.';
        return;
      }
      if (!content.trim()) {
        actionStatus.textContent = 'Please enter content for the file.';
        return;
      }
      actionStatus.textContent = 'Saving ' + filePath + '...';
      vscode.postMessage({ command: 'applyFile', filePath, content });
    });

    window.addEventListener('message', (event) => {
      const message = event.data;
      if (message.command === 'chatResult') {
        appendMessage('ronin', message.response, message.modelUsed ? 'Model: ' + message.modelUsed : '');
        chatStatus.textContent = 'Ronin has responded.';
      }
      if (message.command === 'chatError') {
        chatStatus.textContent = 'Unable to send message. Please try again.';
        alert(message.message);
      }
      if (message.command === 'actionResult') {
        actionStatus.textContent = message.result;
      }
      if (message.command === 'actionError') {
        actionStatus.textContent = 'Action failed.';
        alert(message.message);
      }
    });
  </script>
</body>
</html>`;
    }
    async login() {
        const email = await vscode.window.showInputBox({ prompt: 'Ronin email', ignoreFocusOut: true });
        if (!email) {
            return;
        }
        const password = await vscode.window.showInputBox({ prompt: 'Ronin password', password: true, ignoreFocusOut: true });
        if (!password) {
            return;
        }
        try {
            const response = await this.request('/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            }, false);
            const token = response?.token || response?.accessToken || response?.jwt;
            if (!token) {
                throw new Error('Login response did not contain a token.');
            }
            await this.context.secrets.store('ronin.token', token);
            vscode.window.showInformationMessage('Ronin login successful. Refreshing view.');
            this.refresh();
        }
        catch (error) {
            const message = error instanceof Error ? error.message : 'Login failed';
            vscode.window.showErrorMessage(`Ronin login failed: ${message}`);
        }
    }
    async showRank() {
        try {
            const profile = await this.request('/users/me');
            if (!profile) {
                throw new Error('Unable to load authenticated user.');
            }
            let rank = profile?.rank;
            if (!rank) {
                rank = await this.request('/users/me/rank');
            }
            const label = rank?.name || rank?.title || 'Unknown rank';
            const completed = profile?.completedProjects != null ? `${profile.completedProjects} completed project${profile.completedProjects === 1 ? '' : 's'}` : null;
            const details = [completed, rank?.meaning].filter(Boolean).join(' — ');
            vscode.window.showInformationMessage(`Ronin rank: ${label}${details ? ` — ${details}` : ''}`);
        }
        catch (error) {
            const message = error instanceof Error ? error.message : 'Unable to load rank.';
            vscode.window.showErrorMessage(`Ronin rank lookup failed: ${message}`);
        }
    }
    async openAgentWebview() {
        const panel = vscode.window.createWebviewPanel('roninDashboard', 'Ronin Dashboard', vscode.ViewColumn.One, {
            enableScripts: false,
            retainContextWhenHidden: true
        });
        const backendUrl = getConfiguredBackendUrl();
        const token = await this.getToken();
        const status = token ? 'Signed in' : 'Not signed in';
        panel.webview.html = this.getDashboardHtml(status, backendUrl);
    }
    getDashboardHtml(status, backendUrl) {
        return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Ronin Dashboard</title>
  <style>
    body { font-family: var(--vscode-font-family); color: var(--vscode-editor-foreground); background: var(--vscode-editor-background); padding: 24px; }
    h1 { margin-bottom: 0.5em; }
    p { margin: 0.5em 0; }
    .status { margin-top: 1rem; padding: 1rem; border: 1px solid var(--vscode-editorHoverWidget-border); border-radius: 6px; }
    .url { color: var(--vscode-textLink-foreground); }
  </style>
</head>
<body>
  <h1>Ronin Dashboard</h1>
  <p>Backend URL: <span class="url">${backendUrl}</span></p>
  <div class="status">
    <p><strong>Status:</strong> ${status}</p>
    <p>Use the Ronin tree view to browse projects, sign in, and refresh the agent data.</p>
  </div>
</body>
</html>`;
    }
    async refresh() {
        this._onDidChangeTreeData.fire(null);
    }
    async request(path, init, useAuth = true) {
        const urlsToTry = getBackendUrls(path);
        const fallbackBase = getFallbackBackendUrl();
        const fallbackUrl = fallbackBase ? `${fallbackBase.replace(/\/+$/, '')}${path.startsWith('/') ? path : `/${path}`}` : undefined;
        const headersBase = {
            ...(init?.headers || {})
        };
        if (useAuth) {
            const token = await this.getToken();
            if (token) {
                headersBase['Authorization'] = `Bearer ${token}`;
            }
        }
        const executeFetch = async (url) => {
            return fetch(url, {
                ...init,
                headers: headersBase
            });
        };
        let lastError = null;
        let response;
        for (const url of urlsToTry) {
            try {
                response = await executeFetch(url);
                if (response.ok || response.status === 401) {
                    break;
                }
            }
            catch (error) {
                lastError = error instanceof Error ? error : new Error(String(error));
            }
        }
        if ((!response || !response.ok) && fallbackUrl && !urlsToTry.includes(fallbackUrl)) {
            try {
                response = await executeFetch(fallbackUrl);
            }
            catch (error) {
                lastError = error instanceof Error ? error : new Error(String(error));
            }
        }
        if (!response) {
            throw new Error(lastError?.message || 'No backend endpoints responded.');
        }
        if (response.status === 401) {
            throw new Error('Unauthorized. Please sign in again.');
        }
        if (!response.ok) {
            const body = await response.text();
            throw new Error(`Request failed: ${response.status} ${response.statusText} ${body}`);
        }
        if (response.status === 401) {
            throw new Error('Unauthorized. Please sign in again.');
        }
        if (!response.ok) {
            const body = await response.text();
            throw new Error(`Request failed: ${response.status} ${response.statusText} ${body}`);
        }
        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
            return response.json();
        }
        return response.text();
    }
    resolveUrl(path) {
        const base = getConfiguredBackendUrl().replace(/\/+$/, '');
        const candidate = `${base}${path.startsWith('/') ? path : `/${path}`}`;
        return candidate;
    }
    async getToken() {
        return this.context.secrets.get('ronin.token');
    }
}
exports.RoninAgentProvider = RoninAgentProvider;
//# sourceMappingURL=agentViewProvider.js.map