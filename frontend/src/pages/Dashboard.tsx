import { useCallback, useEffect, useRef, useState, type ChangeEvent, type FormEvent } from "react";
import {
  chatWithLLM,
  login,
  fetchProjects,
  createProject,
  fetchProjectMessages,
  fetchProjectArtifact,
  fetchProjectFiles,
  readProjectFile,
  uploadProjectFiles,
} from "../api";
import ProjectCard from "../components/ProjectCard";
import ImageGenerator from "../components/ImageGenerator";
import { type CurrentUser } from "../App";

type DashboardProps = {
  token: string | null;
  currentUser: CurrentUser | null;
  onLogin: (token: string, email: string) => void;
  onLogout: () => void;
};

type LLMChatEditInstruction = {
  path: string;
  action: string;
  content: string;
};

type LLMChatResponse = {
  response: string;
  modelUsed: string;
  modelSwitched: boolean;
  previousModel: string;
  editsApplied: boolean;
  editPayload?: string;
  edits?: LLMChatEditInstruction[];
};

type Project = {
  id: number;
  name: string;
  description: string;
  phase: string;
  status: string;
  repoUrl?: string;
};

type ProjectMessageAttachment = {
  id: number;
  fileName: string;
  contentType: string | null;
};

type ProjectMessage = {
  id: number;
  sender: string;
  modelUsed: string | null;
  message: string;
  createdAt: string;
  attachments?: ProjectMessageAttachment[];
};

type ProjectFile = {
  id: number;
  filePath: string;
  content: string;
};

type ProjectArtifact = {
  projectId: number;
  artifactUrl: string | null;
  description: string;
  files: {
    id: number;
    filePath: string;
    content: string;
  }[];
};

const initialProjectForm = {
  name: "",
  description: "",
  phase: "FRONTEND",
};

const pageGradient = "bg-gradient-to-br from-slate-80 via-slate-90 to-white";
const cardGradient = "bg-gradient-to-br from-slate-50 via-slate-100 to-white";

export default function Dashboard({ token, currentUser, onLogin, onLogout }: DashboardProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [projects, setProjects] = useState<Project[]>([]);
  const [projectForm, setProjectForm] = useState(initialProjectForm);
  const [projectError, setProjectError] = useState<string | null>(null);
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null);
  const activeProjectId = selectedProjectId ?? projects[0]?.id ?? null;
  const selectedProject = projects.find((project) => project.id === selectedProjectId) ?? projects.find((project) => project.id === activeProjectId) ?? null;
  const completedProjects = projects.filter((project) => project.status === "COMPLETED");
  const activeProjects = projects.filter((project) => project.status !== "COMPLETED");
  const [llmMessage, setLlmMessage] = useState("");
  const [selectedAction, setSelectedAction] = useState('General');
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [uploadLoading, setUploadLoading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const projectDetailRef = useRef<HTMLElement | null>(null);
  const [llmResponse, setLlmResponse] = useState<string | null>(null);
  const [llmLoading, setLlmLoading] = useState(false);
  const [modelSwitched, setModelSwitched] = useState(false);
  const [editInstructions, setEditInstructions] = useState<LLMChatEditInstruction[]>([]);
  const [projectMessages, setProjectMessages] = useState<ProjectMessage[]>([]);
  const [projectArtifact, setProjectArtifact] = useState<ProjectArtifact | null>(null);
  const [projectFiles, setProjectFiles] = useState<ProjectFile[]>([]);
  const [fileBrowserOpen, setFileBrowserOpen] = useState(false);
  const [selectedFilePath, setSelectedFilePath] = useState<string | null>(null);
  const [selectedFileContent, setSelectedFileContent] = useState<string | null>(null);
  const [fileBrowserLoading, setFileBrowserLoading] = useState(false);
  const [fileContentLoading, setFileContentLoading] = useState(false);

  const loadProjects = useCallback(async () => {
    if (!token) {
      return;
    }

    setLoading(true);
    try {
      const data = await fetchProjects(token);
      setProjects(data);
    } catch (err: unknown) {
      console.error(err);
      if (err instanceof Error && (err as Error & { status?: number }).status === 401) {
        setError("Session expired. Please sign in again.");
        onLogout();
      } else {
        setError("Unable to load projects from backend.");
      }
    } finally {
      setLoading(false);
    }
  }, [token]);

  const loadProjectMessages = useCallback(async (projectId: number) => {
    if (!token) {
      return;
    }

    try {
      const messages = await fetchProjectMessages(token, projectId);
      setProjectMessages(messages);
    } catch (err: unknown) {
      console.error(err);
      setProjectMessages([]);
    }
  }, [token]);

  const loadProjectArtifact = useCallback(async (projectId: number) => {
    if (!token) {
      return;
    }

    try {
      const artifact = await fetchProjectArtifact(token, projectId);
      setProjectArtifact(artifact);
    } catch (err: unknown) {
      console.error(err);
      setProjectArtifact(null);
    }
  }, [token]);

  const loadProjectFiles = useCallback(async (projectId: number) => {
    if (!token) {
      return;
    }

    setFileBrowserLoading(true);
    try {
      const files = await fetchProjectFiles(token, projectId);
      setProjectFiles(files);
      if (files.length > 0) {
        setSelectedFilePath(files[0].filePath);
      }
    } catch (err: unknown) {
      console.error(err);
      setProjectFiles([]);
    } finally {
      setFileBrowserLoading(false);
    }
  }, [token]);

  const loadFileContent = useCallback(async (projectId: number, path: string) => {
    if (!token) {
      return;
    }

    setFileContentLoading(true);
    try {
      const file = await readProjectFile(token, projectId, path);
      setSelectedFilePath(file.filePath);
      setSelectedFileContent(file.content);
    } catch (err: unknown) {
      console.error(err);
      setSelectedFileContent(null);
    } finally {
      setFileContentLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (!token) {
      return;
    }

    const run = async () => {
      await loadProjects();
    };

    void run();
  }, [token, loadProjects]);

  useEffect(() => {
    if (projects.length > 0 && selectedProjectId === null) {
      setSelectedProjectId(projects[0].id);
    }
  }, [projects, selectedProjectId]);

  useEffect(() => {
    if (selectedProjectId !== null) {
      void loadProjectMessages(selectedProjectId);
      void loadProjectArtifact(selectedProjectId);
      if (fileBrowserOpen) {
        void loadProjectFiles(selectedProjectId);
      }
    } else {
      setProjectMessages([]);
      setProjectArtifact(null);
      setProjectFiles([]);
      setSelectedFileContent(null);
      setSelectedFilePath(null);
    }
  }, [selectedProjectId, loadProjectMessages, loadProjectArtifact, fileBrowserOpen, loadProjectFiles]);

  async function handleChatSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setUploadError(null);
    setLlmResponse(null);
    setModelSwitched(false);
    setEditInstructions([]);

    if (!token || selectedProjectId === null) {
      setError("Select a project and sign in before sending a message.");
      return;
    }

    if (!llmMessage.trim()) {
      setError("Enter a message for Ronin.");
      return;
    }

    setLlmLoading(true);

    const actionPrefix = selectedAction !== 'General' ? `${selectedAction}:\n\n` : '';
    const requestMessage = `${actionPrefix}${llmMessage}`;

    try {
      const response: LLMChatResponse = await chatWithLLM(token, {
        projectId: selectedProjectId,
        message: requestMessage,
        actionType: selectedAction !== 'General' ? selectedAction : undefined,
      });
      setLlmResponse(response.response);
      setModelSwitched(response.modelSwitched);
      setEditInstructions(response.edits ?? []);
      setLlmMessage("");
      await loadProjectMessages(selectedProjectId);
      if (response.editsApplied) {
        await loadProjectFiles(selectedProjectId);
        setFileBrowserOpen(true);
      }
    } catch (err: unknown) {
      console.error(err);
      if (err instanceof Error && (err as Error & { status?: number }).status === 401) {
        setError("Session expired. Please log in again.");
        onLogout();
      } else {
        setError("Unable to reach the LLM service.");
      }
    } finally {
      setLlmLoading(false);
    }
  }

  async function handleFileUpload(event: ChangeEvent<HTMLInputElement>) {
    const targetProjectId = selectedProjectId ?? activeProjectId;
    if (!token || targetProjectId === null) {
      setUploadError("Select a project before uploading files.");
      return;
    }

    const files = event.target.files;
    if (!files || files.length === 0) {
      return;
    }

    setUploadError(null);
    setUploadLoading(true);

    try {
      await uploadProjectFiles(token, targetProjectId, files);
      await loadProjectMessages(targetProjectId);
      await loadProjectFiles(targetProjectId);
      setFileBrowserOpen(true);
      setLlmResponse("Files uploaded successfully.");
    } catch (err: unknown) {
      console.error(err);
      setUploadError("Unable to upload files. Try again.");
    } finally {
      setUploadLoading(false);
      if (event.target) {
        event.target.value = "";
      }
    }
  }

  function copyFileContent(content: string) {
    if (!navigator.clipboard) {
      console.warn("Clipboard API not available");
      return;
    }
    navigator.clipboard.writeText(content).catch((err) => {
      console.error("Failed to copy content:", err);
    });
  }

  function downloadFileContent(filePath: string, content: string) {
    const fileName = filePath.split("/").pop() ?? "generated-file.txt";
    const blob = new Blob([content], { type: "text/plain;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = fileName;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  function triggerFileUpload() {
    fileInputRef.current?.click();
  }

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await login({ email, password });
      onLogin(response.token, response.email);
      setEmail("");
      setPassword("");
    } catch (err) {
      console.error(err);
      setError("Login failed. Check email and password.");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateProject(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setProjectError(null);
    if (!token) {
      setProjectError("You must sign in first.");
      return;
    }

    try {
      const project = await createProject(token, projectForm);
      setProjects((current) => [project, ...current]);
      setSelectedProjectId(project.id);
      setProjectForm(initialProjectForm);
    } catch (err: unknown) {
      console.error(err);
      if (err instanceof Error && (err as Error & { status?: number }).status === 401) {
        setProjectError("Session expired. Please sign in again.");
        onLogout();
      } else {
        setProjectError("Unable to create a new project.");
      }
    }
  }

  if (!token) {
    return (
      <div className="mx-auto max-w-xl rounded-3xl border border-gray-200 bg-white p-8 shadow-sm">
        <h1 className="text-3xl font-semibold mb-4">Sign in to Ronin</h1>
        <p className="mb-6 text-gray-600">
          Use the default credentials created by the backend if you are running locally.
        </p>
        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">Email</label>
            <input
              id="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3"
              type="email"
              autoComplete="email"
              required
            />
          </div>
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700">Password</label>
            <input
              id="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3"
              type="password"
              autoComplete="current-password"
              required
            />
          </div>
          {error && <div className="text-sm text-red-600">{error}</div>}
          <button
            className="w-full rounded-xl bg-slate-900 px-4 py-3 text-white hover:bg-slate-700"
            type="submit"
            disabled={loading}
          >
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>
        <div className="mt-6 rounded-2xl bg-slate-100 p-4 text-sm text-gray-700">
          Default credentials: <strong>ronin@example.com</strong> / <strong>ronin123</strong>
        </div>
      </div>
    );
  }

  return (
    <div className={`h-full w-full min-w-0 overflow-hidden ${pageGradient} px-2 py-2 lg:px-3 lg:py-3 text-left`}>
      <div className="mx-auto flex min-h-full w-full min-w-0 flex-col gap-2 overflow-hidden pb-6 text-left">
        <div className={`rounded-3xl border border-gray-200 ${cardGradient} p-3 shadow-sm`}>
          <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <h1 className="text-xl font-semibold text-slate-950">Your Projects</h1>
              <p className="text-sm text-gray-600">Create a new project or review existing work.</p>
            </div>
            <button
              onClick={loadProjects}
              className="rounded-xl border border-slate-200 bg-slate-900 px-3 py-1.5 text-white hover:bg-slate-700"
            >
              Refresh
            </button>
          </div>
        </div>

        <div className="space-y-3">
          <section className={`rounded-3xl border border-gray-200 ${cardGradient} p-3 shadow-sm`}>
            <h2 className="text-lg font-semibold text-slate-950 mb-2">Create a project</h2>
            <form className="space-y-3" onSubmit={handleCreateProject}>
              <div>
                <label htmlFor="projectName" className="block text-sm font-medium text-gray-700">Project name</label>
                <input
                  id="projectName"
                  value={projectForm.name}
                  onChange={(event) => setProjectForm({ ...projectForm, name: event.target.value })}
                  className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3"
                  required
                />
              </div>
              <div>
                <label htmlFor="projectDescription" className="block text-sm font-medium text-gray-700">Description</label>
                <textarea
                  id="projectDescription"
                  value={projectForm.description}
                  onChange={(event) => setProjectForm({ ...projectForm, description: event.target.value })}
                  className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3"
                  rows={4}
                  required
                />
              </div>
              <div>
                <label htmlFor="projectPhase" className="block text-sm font-medium text-gray-700">Phase</label>
                <select
                  id="projectPhase"
                  value={projectForm.phase}
                  onChange={(event) => setProjectForm({ ...projectForm, phase: event.target.value })}
                  className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3"
                >
                  <option value="FRONTEND">FRONTEND</option>
                  <option value="BACKEND">BACKEND</option>
                  <option value="DB">DB</option>
                  <option value="CLOUD">CLOUD</option>
                  <option value="TESTING">TESTING</option>
                </select>
              </div>
              {projectError && <div className="text-sm text-red-600">{projectError}</div>}
              <button className="rounded-xl bg-slate-900 px-3 py-2 text-white hover:bg-slate-700" type="submit">
                Create project
              </button>
            </form>
          </section>

          <div className="space-y-3">
            <section className={`rounded-3xl border border-gray-200 ${cardGradient} p-3 shadow-sm`}>
              <h2 className="text-lg font-semibold text-slate-950 mb-2">Ask Ronin</h2>
              {projects.length === 0 ? (
                <div className="text-gray-600">Create a project first to chat with the AI assistant.</div>
              ) : (
                <form className="space-y-3" onSubmit={handleChatSubmit}>
                  <div>
                    <label htmlFor="chatProject" className="block text-sm font-medium text-gray-700">Project</label>
                    <select
                      id="chatProject"
                      className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3"
                      value={activeProjectId ?? undefined}
                      onChange={(event) => setSelectedProjectId(Number(event.target.value))}
                    >
                      {projects.map((project) => (
                        <option key={project.id} value={project.id}>
                          {project.name} ({project.phase})
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label htmlFor="actionType" className="block text-sm font-medium text-gray-700">Action</label>
                    <select
                      id="actionType"
                      value={selectedAction}
                      onChange={(event) => setSelectedAction(event.target.value)}
                      className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3"
                    >
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
                  <div>
                    <label htmlFor="llmMessage" className="block text-sm font-medium text-gray-700">Message</label>
                    <textarea
                      id="llmMessage"
                      value={llmMessage}
                      onChange={(event) => setLlmMessage(event.target.value)}
                      className="mt-1 w-full rounded-xl border-gray-300 bg-gray-50 px-4 py-3"
                      rows={4}
                      placeholder="Ask Ronin for assistance on this project..."
                      required
                    />
                  </div>
                  <input
                    ref={(el) => {
                      fileInputRef.current = el;
                      if (el) {
                        el.setAttribute("webkitdirectory", "");
                        el.setAttribute("directory", "");
                      }
                    }}
                    type="file"
                    hidden
                    multiple
                    onChange={handleFileUpload}
                  />
                  {uploadError && <div className="text-sm text-red-600">{uploadError}</div>}
                  <div className="flex flex-wrap items-center gap-3">
                    <button
                      type="button"
                      onClick={triggerFileUpload}
                      disabled={!token || selectedProjectId === null || uploadLoading}
                      className="rounded-full bg-slate-900 px-3 py-2 text-white hover:bg-slate-700"
                    >
                      Upload folder
                    </button>
                    <span className="text-sm text-gray-600">Import a local folder or file set into this project.</span>
                    <button
                      className="rounded-xl bg-slate-900 px-4 py-3 text-white hover:bg-slate-700"
                      type="submit"
                      disabled={llmLoading}
                    >
                      {llmLoading ? "Sending..." : "Send to Ronin"}
                    </button>
                  </div>
                  <div className="mt-4 flex flex-wrap items-center gap-2">
                    <button
                      type="button"
                      onClick={() => {
                        const projectId = activeProjectId;
                        if (projectId === null) {
                          return;
                        }
                        if (selectedProjectId === null) {
                          setSelectedProjectId(projectId);
                        }
                        triggerFileUpload();
                      }}
                      className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-slate-900 hover:bg-slate-50"
                    >
                      {fileBrowserOpen ? "Hide folder" : "Open folder"}
                    </button>
                    <span className="text-sm text-gray-600">Browse and preview project files before editing.</span>
                  </div>
                </form>
              )}
              {modelSwitched && (
                <div className={`mt-4 rounded-2xl ${cardGradient} border border-amber-200 p-3 text-sm text-amber-900`}>
                  Ronin switched providers automatically because the preferred provider reached its free token limit.
                </div>
              )}
              {llmResponse && (
                <div className={`mt-4 rounded-3xl border border-slate-200 ${cardGradient} p-3 text-sm text-gray-800`}>
                  <div className="mb-2 text-sm text-slate-600">Response from Ronin</div>
                  <p>{llmResponse}</p>
                </div>
              )}
              {editInstructions.length > 0 && (
                <div className={`mt-4 rounded-3xl border border-slate-200 ${cardGradient} p-3 text-sm text-gray-800`}>
                  <div className="mb-2 text-sm font-semibold text-slate-900">Edit instructions detected</div>
                  <div className="space-y-3">
                    {editInstructions.map((edit, index) => (
                      <div key={`${edit.path}-${index}`} className="rounded-2xl bg-slate-50 p-3">
                        <div className="text-slate-700 font-semibold">{edit.action.toUpperCase()} {edit.path}</div>
                        <pre className="mt-2 overflow-x-auto rounded-xl bg-white p-3 text-xs text-slate-700">{edit.content || "(no content provided)"}</pre>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </section>

            {/* Image Generator Section */}
            {selectedProject && token && activeProjectId && (
              <ImageGenerator
                token={token}
                projectId={activeProjectId}
                cardGradient={cardGradient}
                onImageGenerated={(imageUrl) => {
                  setLlmResponse(`✨ Image generated! View it or use it in your project: ${imageUrl}`);
                }}
              />
            )}

            <section ref={projectDetailRef} className={`rounded-3xl border border-gray-200 ${cardGradient} p-3 shadow-sm`}>
              <h2 className="text-lg font-semibold text-slate-950 mb-2">Project details</h2>
              {selectedProject ? (
                <div className="space-y-3">
                  <div className={`rounded-2xl ${cardGradient} p-3 text-sm text-gray-700`}>
                    <div className="text-sm text-gray-500">Selected project</div>
                    <div className="mt-2 text-base font-semibold text-slate-900">{selectedProject.name}</div>
                    <p className="mt-2 text-sm text-gray-600">{selectedProject.description}</p>
                    <div className="mt-3 flex flex-wrap gap-2 text-sm">
                      <span className="rounded-full bg-slate-900 px-3 py-1 text-white">{selectedProject.status}</span>
                      <span className="rounded-full bg-slate-100 px-3 py-1 text-slate-700">{selectedProject.phase}</span>
                    </div>
                    {selectedProject.status === "COMPLETED" ? (
                      projectArtifact ? (
                        <div className={`mt-3 rounded-2xl border border-slate-200 ${cardGradient} p-3 text-sm text-slate-800 text-left`}>
                          <div className="font-semibold text-slate-900">Final product</div>
                          <div className="mt-2 text-gray-600">{projectArtifact.description}</div>
                          {projectArtifact.artifactUrl ? (
                            <>
                              <div className="mt-2 text-xs text-slate-500">
                                Artifact endpoint: <code>{projectArtifact.artifactUrl}</code>
                              </div>
                              <button
                                type="button"
                                onClick={() => {
                                  const productUrl = `${projectArtifact.artifactUrl}${token ? `?token=${encodeURIComponent(token)}` : ""}`;
                                  window.open(productUrl, "_blank");
                                }}
                                className="mt-3 rounded-xl bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-700"
                              >
                                View Product
                              </button>
                            </>
                          ) : (
                            <div className="mt-2 text-xs text-amber-700">The final product endpoint is not yet available.</div>
                          )}
                          {projectArtifact.files.length > 0 ? (
                            <div className="mt-3">
                              <div className="text-sm font-semibold text-slate-900">Generated files</div>
                              <ul className="mt-2 space-y-2 text-sm text-slate-700">
                                {projectArtifact.files.map((file) => (
                                  <li key={file.id} className={`rounded-2xl border border-slate-200 ${cardGradient} p-3`}>
                                    <div className="flex flex-col gap-3">
                                      <div className="flex flex-wrap items-center justify-between gap-3">
                                        <div className="font-medium text-slate-900">{file.filePath}</div>
                                        <div className="flex flex-wrap gap-2">
                                          <button
                                            type="button"
                                            onClick={() => copyFileContent(file.content)}
                                            className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-3 py-2 text-xs font-medium text-slate-900 hover:bg-slate-100"
                                          >
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" className="h-4 w-4 fill-current">
                                              <path d="M16 1H4a2 2 0 0 0-2 2v14h2V3h12V1zm3 4H8a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2zm0 16H8V7h11v14z" />
                                            </svg>
                                            Copy
                                          </button>
                                          <button
                                            type="button"
                                            onClick={() => downloadFileContent(file.filePath, file.content)}
                                            className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-3 py-2 text-xs font-medium text-slate-900 hover:bg-slate-100"
                                          >
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" className="h-4 w-4 fill-current">
                                              <path d="M5 20h14v-2H5v2zm7-18L5.33 9h3.84v4h5.66V9h3.84L12 2z" />
                                            </svg>
                                            Download
                                          </button>
                                        </div>
                                      </div>
                                      <pre className="mt-2 overflow-x-auto rounded-lg bg-slate-900 p-3 text-xs text-slate-100">
                                        {file.content}
                                      </pre>
                                    </div>
                                  </li>
                                ))}
                              </ul>
                            </div>
                          ) : (
                            <div className="mt-2 text-sm text-slate-600">This completed project does not have generated artifact files saved yet.</div>
                          )}
                        </div>
                      ) : (
                        <div className="mt-3 text-sm text-gray-600">Loading final product details...</div>
                      )
                    ) : selectedProject.status === "FAILED" ? (
                      <div className="mt-3 rounded-2xl border border-rose-200 bg-rose-50 p-3 text-sm text-rose-900">
                        This project failed to complete. No final product is available.
                      </div>
                    ) : (
                      <div className="mt-3 rounded-2xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
                        The final product is still generating. Come back once the project is complete to review the final output.
                      </div>
                    )}
                  </div>

                  {fileBrowserOpen && (
                    <div className={`rounded-2xl border border-slate-200 ${cardGradient} p-3 text-sm text-slate-700`}>
                      <div className="flex items-center justify-between gap-3">
                        <div>
                          <div className="text-sm font-semibold text-slate-900">Project files</div>
                          <div className="text-xs text-slate-500">Click a file to preview its content.</div>
                        </div>
                        <button
                          type="button"
                          onClick={() => setFileBrowserOpen(false)}
                          className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-slate-800 hover:bg-slate-100"
                        >
                          Close
                        </button>
                      </div>
                      <div className="mt-4 grid gap-3 lg:grid-cols-[1fr_2fr]">
                        <div className={`rounded-2xl border border-slate-200 ${cardGradient} p-3`}>
                          {fileBrowserLoading ? (
                            <div className="text-sm text-slate-600">Loading files...</div>
                          ) : projectFiles.length === 0 ? (
                            <div className="text-sm text-slate-600">No files available for this project.</div>
                          ) : (
                            <ul className="space-y-2 text-sm">
                              {projectFiles.map((file) => (
                                <li key={file.id}>
                                  <button
                                    type="button"
                                    onClick={() => void loadFileContent(selectedProject.id, file.filePath)}
                                    className={`w-full text-left rounded-2xl px-3 py-2 ${selectedFilePath === file.filePath ? "bg-slate-900 text-white" : "bg-white text-slate-800 hover:bg-slate-100"}`}
                                  >
                                    {file.filePath}
                                  </button>
                                </li>
                              ))}
                            </ul>
                          )}
                        </div>
                        <div className={`rounded-2xl border border-slate-200 ${cardGradient} p-3`}>
                          <div className="mb-3 flex items-center justify-between gap-3 text-sm text-slate-700">
                            <div>
                              <div className="font-semibold">Preview</div>
                              <div className="text-xs text-slate-500">Selected file content appears here.</div>
                            </div>
                            {fileContentLoading && <span className="text-xs text-slate-500">Loading...</span>}
                          </div>
                          {selectedFileContent ? (
                            <pre className="max-h-72 overflow-auto rounded-2xl bg-slate-900 p-3 text-xs text-slate-100">
                              {selectedFileContent}
                            </pre>
                          ) : (
                            <div className="text-sm text-slate-500">Select a file to preview its contents.</div>
                          )}
                        </div>
                      </div>
                    </div>
                  )}

                  <div className={`rounded-2xl ${cardGradient} p-3 text-left`}>
                    <div className="text-sm font-semibold text-slate-800 mb-3">Project messages</div>
                    {projectMessages.length === 0 ? (
                      <div className="text-sm text-gray-600">No messages for this project yet.</div>
                    ) : (
                      <div className="space-y-3">
                        {projectMessages.map((message) => (
                          <div key={message.id} className={`rounded-2xl border border-slate-200 ${cardGradient} p-3`}>
                            <div className="flex items-center justify-between gap-2 text-xs uppercase tracking-wide text-slate-500">
                              <span>{message.sender}</span>
                              <span>{new Date(message.createdAt).toLocaleString()}</span>
                            </div>
                            <div className="mt-2 text-sm text-slate-800">{message.message}</div>
                            {message.attachments && message.attachments.length > 0 && (
                              <div className={`mt-3 rounded-2xl ${cardGradient} p-3`}>
                                <div className="text-xs uppercase tracking-wide text-slate-500">Attachments</div>
                                <ul className="mt-2 space-y-1 text-sm text-slate-700">
                                  {message.attachments.map((attachment) => (
                                    <li key={attachment.id}>
                                      <a
                                        href={`/projects/${selectedProjectId}/messages/${message.id}/attachments/${attachment.id}?token=${encodeURIComponent(token ?? "")}`}
                                        target="_blank"
                                        rel="noreferrer"
                                        className="text-slate-900 underline"
                                      >
                                        {attachment.fileName}
                                      </a>
                                    </li>
                                  ))}
                                </ul>
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              ) : (
                <div className="text-sm text-gray-600">Select a project to review current status, artifacts, and message history.</div>
              )}
            </section>
          </div>
        </div>

        <section className={`rounded-3xl border border-gray-200 ${cardGradient} p-3 shadow-sm`}>
          <h2 className="text-lg font-semibold text-slate-950 mb-2">Project list</h2>
          {loading && <div className="text-gray-500">Loading projects...</div>}
          {!loading && projects.length === 0 && (
            <div className="text-gray-600">No projects yet. Create one to begin your first Ronin project.</div>
          )}

          {!loading && completedProjects.length > 0 && (
            <div className="mb-4">
              <div className="mb-2 text-sm font-semibold text-slate-900">Completed projects</div>
              <div className="space-y-3">
                {completedProjects.map((project) => (
                  <ProjectCard
                    key={project.id}
                    project={project}
                    isCompleted={true}
                    currentUser={currentUser}
                    onSelectProject={(projectId) => {
                      setSelectedProjectId(projectId);
                      projectDetailRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
                    }}
                  />
                ))}
              </div>
            </div>
          )}

          {!loading && activeProjects.length > 0 && (
            <div className="space-y-3">
              {activeProjects.map((project) => (
                <ProjectCard
                  key={project.id}
                  project={project}
                  isCompleted={false}
                  currentUser={currentUser}
                  onSelectProject={(projectId) => {
                    setSelectedProjectId(projectId);
                    projectDetailRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
                  }}
                />
              ))}
            </div>
          )}

          {!loading && projects.length > 0 && completedProjects.length === 0 && activeProjects.length === 0 && (
            <div className="text-gray-600">No projects are available to display at this time.</div>
          )}
        </section>
      </div>
    </div>
  );
}
