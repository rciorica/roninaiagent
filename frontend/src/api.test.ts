import { beforeEach, describe, expect, it, vi } from 'vitest'
import { login, signup, fetchProjects, createProject } from './api'

const jsonResponse = (body: unknown, status = 200) =>
  Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
    text: async () => (typeof body === 'string' ? body : JSON.stringify(body)),
    headers: {
      get: (k: string) => (k.toLowerCase() === 'content-type' ? 'application/json' : null),
    },
  } as unknown as Response)

describe('api wrapper', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('sends login request and returns the auth payload', async () => {
    vi.stubGlobal('fetch', vi.fn(() => jsonResponse({ email: 'ronin@example.com', token: 'abc123' })))

    const result = await login({ email: 'ronin@example.com', password: 'ronin123' })

    expect(fetch).toHaveBeenCalledWith(
      '/auth/login',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ email: 'ronin@example.com', password: 'ronin123' }),
      }),
    )
    expect(result).toEqual({ email: 'ronin@example.com', token: 'abc123' })
  })

  it('sends signup and returns the created account payload', async () => {
    vi.stubGlobal('fetch', vi.fn(() => jsonResponse({ email: 'ronin@example.com', username: 'ronin', message: 'Account created successfully. Welcome to Ronin.' }, 201)))

    const result = await signup({ email: 'ronin@example.com', password: 'ronin123', username: 'ronin' })

    expect(fetch).toHaveBeenCalledWith(
      '/auth/signup',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ email: 'ronin@example.com', password: 'ronin123', username: 'ronin' }),
      }),
    )
    expect(result).toEqual({ email: 'ronin@example.com', username: 'ronin', message: 'Account created successfully. Welcome to Ronin.' })
  })

  it('applies authorization header for fetchProjects', async () => {
    vi.stubGlobal('fetch', vi.fn(() => jsonResponse([{ id: 1, name: 'A', description: 'desc', phase: 'FRONTEND', status: 'IN_PROGRESS' }])))

    await fetchProjects('my-token')

    expect(fetch).toHaveBeenCalledWith(
      '/projects',
      expect.objectContaining({
        credentials: 'include',
      }),
    )

    const fetchMock = fetch as unknown as { mock: { calls: Array<[string, RequestInit?]> } }
    const callOptions = fetchMock.mock.calls[0][1]
    const headers = new Headers(callOptions?.headers)
    expect(headers.get('Authorization')).toBe('Bearer my-token')
  })

  it('throws an error when the response is not ok', async () => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.resolve(new Response('Forbidden', { status: 403 }))))

    await expect(createProject('my-token', { name: 'x', description: 'y', phase: 'DB' })).rejects.toThrow('Forbidden')
  })
})
