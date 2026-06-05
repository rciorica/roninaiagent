import '@testing-library/jest-dom'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useState } from 'react'
import Dashboard from './Dashboard'
import * as api from '../api'

vi.mock('../api', () => ({
  login: vi.fn(),
  fetchProjects: vi.fn(),
  createProject: vi.fn(),
  chatWithLLM: vi.fn(),
}))

function TestApp() {
  const [token, setToken] = useState<string | null>(null)

  return <Dashboard token={token} currentUser={null} onLogin={(newToken) => setToken(newToken)} onLogout={() => setToken(null)} />
}

describe('Dashboard end-to-end flow', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('logs in and creates a project', async () => {
    const loginMock = vi.mocked(api.login)
    const createProjectMock = vi.mocked(api.createProject)
    const fetchProjectsMock = vi.mocked(api.fetchProjects)

    loginMock.mockResolvedValue({ email: 'ronin@example.com', token: 'abc123' })
    fetchProjectsMock.mockResolvedValue([])
    createProjectMock.mockResolvedValue({
      id: 1,
      name: 'My New Project',
      description: 'End-to-end project creation test',
      phase: 'BACKEND',
      status: 'IN_PROGRESS',
    })

    render(<TestApp />)

    await userEvent.type(screen.getByLabelText(/email/i), 'ronin@example.com')
    await userEvent.type(screen.getByLabelText(/password/i), 'ronin123')
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }))

    await waitFor(() => {
      expect(loginMock).toHaveBeenCalledWith({ email: 'ronin@example.com', password: 'ronin123' })
    })

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create project/i })).toBeInTheDocument()
    })

    await userEvent.type(screen.getByLabelText(/project name/i), 'My New Project')
    await userEvent.type(screen.getByLabelText(/description/i), 'End-to-end project creation test')
    await userEvent.selectOptions(screen.getByLabelText(/phase/i), 'BACKEND')
    await userEvent.click(screen.getByRole('button', { name: /create project/i }))

    await waitFor(() => {
      expect(createProjectMock).toHaveBeenCalledWith('abc123', {
        name: 'My New Project',
        description: 'End-to-end project creation test',
        phase: 'BACKEND',
      })
    })

    expect(await screen.findByText(/My New Project/i)).toBeInTheDocument()
  })

  it('allows sending an LLM chat message and shows a model switch warning', async () => {
    const loginMock = vi.mocked(api.login)
    const createProjectMock = vi.mocked(api.createProject)
    const fetchProjectsMock = vi.mocked(api.fetchProjects)
    const chatWithLLMMock = vi.mocked(api.chatWithLLM)

    loginMock.mockResolvedValue({ email: 'ronin@example.com', token: 'abc123' })
    fetchProjectsMock.mockResolvedValue([])
    createProjectMock.mockResolvedValue({
      id: 1,
      name: 'LLM Project',
      description: 'Chat test',
      phase: 'BACKEND',
      status: 'IN_PROGRESS',
    })
    chatWithLLMMock.mockResolvedValue({
      response: 'I recommend using the backend model.',
      modelUsed: 'CodeGemma-7B',
      modelSwitched: true,
      previousModel: 'Llama-3-70B-Code',
      editsApplied: false,
    })

    render(<TestApp />)

    await userEvent.type(screen.getByLabelText(/email/i), 'ronin@example.com')
    await userEvent.type(screen.getByLabelText(/password/i), 'ronin123')
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }))

    await waitFor(() => {
      expect(loginMock).toHaveBeenCalledWith({ email: 'ronin@example.com', password: 'ronin123' })
    })

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create project/i })).toBeInTheDocument()
    })

    await userEvent.type(screen.getByLabelText(/project name/i), 'LLM Project')
    await userEvent.type(screen.getByLabelText(/description/i), 'Chat test')
    await userEvent.selectOptions(screen.getByLabelText(/phase/i), 'BACKEND')
    await userEvent.click(screen.getByRole('button', { name: /create project/i }))

    await waitFor(() => {
      expect(createProjectMock).toHaveBeenCalledWith('abc123', {
        name: 'LLM Project',
        description: 'Chat test',
        phase: 'BACKEND',
      })
    })

    expect(await screen.findByText(/LLM Project/i)).toBeInTheDocument()

    await userEvent.type(screen.getByLabelText(/message/i), 'How should I build this backend?')
    await userEvent.click(screen.getByRole('button', { name: /send to Ronin/i }))

    await waitFor(() => {
      expect(chatWithLLMMock).toHaveBeenCalledWith('abc123', {
        projectId: 1,
        message: 'How should I build this backend?',
      })
    })

    expect(await screen.findByText(/I recommend using the backend model./i)).toBeInTheDocument()
    expect(screen.getByText(/Ronin switched providers automatically because the preferred provider reached its free token limit./i)).toBeInTheDocument()
  })
})
