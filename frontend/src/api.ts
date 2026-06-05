// Get API base URL from environment variable (Heroku) or use relative path (local)
// In production: VITE_API_URL is set to the backend URL
// In development: Use relative paths (Vite proxy handles routing to backend)
const API_BASE_URL = import.meta.env.VITE_API_URL || '';

type LoginRequest = {
  email: string;
  password: string;
};

type LoginResponse = {
  email: string;
  token: string;
};

type CreateProjectRequest = {
  name: string;
  description: string;
  phase: string;
};

type LLMChatRequest = {
  projectId: number;
  message: string;
};

type LLMChatResponse = {
  response: string;
  modelUsed: string;
  modelSwitched: boolean;
  previousModel: string;
  editsApplied: boolean;
};

type SignupRequest = {
  email: string;
  password: string;
  username: string;
};

type UserRank = {
  id: number;
  name: string;
  level: number;
  minProjects: number;
  maxProjects: number;
  beltColor: string;
  meaning: string;
};

type CurrentUser = {
  id: number;
  email: string;
  completedProjects: number;
  rank: UserRank;
  projectsToNextRank?: number;
};

const apiFetch = async (path: string, options: RequestInit = {}) => {
  const headers = new Headers(options.headers ?? {});
  if (!(options.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const url = API_BASE_URL ? `${API_BASE_URL}${path}` : path;

  const response = await fetch(url, {
    ...options,
    headers,
    credentials: "include",
  });

  if (!response.ok) {
    const text = await response.text();
    const error = new Error(text || response.statusText) as Error & { status?: number };
    error.status = response.status;
    throw error;
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
};

export async function login(body: LoginRequest): Promise<LoginResponse> {
  return apiFetch("/auth/login", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export async function fetchProjects(token: string) {
  return apiFetch("/projects", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

export async function createProject(token: string, body: CreateProjectRequest) {
  return apiFetch("/projects", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(body),
  });
}

export async function chatWithLLM(
  token: string,
  body: LLMChatRequest
): Promise<LLMChatResponse> {
  return apiFetch("/llm/chat", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(body),
  });
}

export async function signup(body: SignupRequest): Promise<{ email: string; username: string; message: string }> {
  return apiFetch("/auth/signup", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export async function verifyEmail(body: { email: string }) {
  return apiFetch("/auth/verify-email", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export async function verifyPassword(body: { sessionId: string; password: string }) {
  return apiFetch("/auth/verify-password", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

type ProjectArtifactFile = {
  id: number;
  filePath: string;
  content: string;
};

type ProjectArtifactResponse = {
  projectId: number;
  artifactUrl: string | null;
  description: string;
  files: ProjectArtifactFile[];
};

export async function fetchProjectMessages(token: string, projectId: number) {
  return apiFetch(`/projects/${projectId}/messages`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

type ProjectFile = {
  id: number;
  filePath: string;
  content: string;
};

const encodeProjectFilePath = (filePath: string) =>
  filePath
    .split("/")
    .map((segment) => encodeURIComponent(segment))
    .join("/");

export async function fetchProjectFiles(token: string, projectId: number): Promise<ProjectFile[]> {
  return apiFetch(`/projects/${projectId}/files`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

export async function readProjectFile(token: string, projectId: number, filePath: string): Promise<ProjectFile> {
  return apiFetch(`/projects/${projectId}/files/${encodeProjectFilePath(filePath)}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

export async function saveProjectFile(
  token: string,
  projectId: number,
  filePath: string,
  content: string,
  dryRun = false
): Promise<{ filePath: string; dryRun: boolean; oldContent: string; newContent: string }> {
  return apiFetch(`/projects/${projectId}/files/${encodeProjectFilePath(filePath)}?dryRun=${dryRun}`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "text/plain",
    },
    body: content,
  });
}

export async function deleteProjectFile(token: string, projectId: number, filePath: string) {
  return apiFetch(`/projects/${projectId}/files/${encodeProjectFilePath(filePath)}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

export async function fetchProjectArtifact(token: string, projectId: number): Promise<ProjectArtifactResponse> {
  return apiFetch(`/projects/${projectId}/artifact`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}

export async function uploadProjectFiles(token: string, projectId: number, files: FileList | File[]) {
  const form = new FormData();
  Array.from(files).forEach((file) => form.append("files", file));

  return apiFetch(`/projects/${projectId}/messages/attachments`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: form,
  });
}

export async function fetchCurrentUser(token: string): Promise<CurrentUser> {
  const response = await apiFetch("/users/me", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  // Ensure projectsToNextRank is always present (backend may take time to update)
  return {
    ...response,
    projectsToNextRank: response.projectsToNextRank ?? 0,
  };
}
