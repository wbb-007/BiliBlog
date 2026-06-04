import { reactive } from 'vue'

const STORAGE_KEY = 'biliblog-auth-session'

export const authState = reactive({
  token: '',
  user: null,
  loaded: false,
})

function loadStoredSession() {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    authState.loaded = true
    return
  }

  try {
    const parsed = JSON.parse(raw)
    authState.token = parsed.token ?? ''
    authState.user = parsed.user ?? null
  } catch {
    localStorage.removeItem(STORAGE_KEY)
  } finally {
    authState.loaded = true
  }
}

loadStoredSession()

export function persistAuthSession(token, user) {
  authState.token = token
  authState.user = user
  authState.loaded = true
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ token, user }))
}

export function clearAuthSession() {
  authState.token = ''
  authState.user = null
  authState.loaded = true
  localStorage.removeItem(STORAGE_KEY)
}

export function getAuthToken() {
  return authState.token
}

export function isAdminUser() {
  return authState.user?.role === 'ADMIN'
}

export async function refreshCurrentUser() {
  if (!authState.token) {
    authState.loaded = true
    return null
  }

  try {
    const response = await fetch('/api/auth/me', {
      headers: {
        Authorization: `Bearer ${authState.token}`,
      },
    })

    if (!response.ok) {
      throw new Error('Unauthorized')
    }

    const payload = await response.json()
    persistAuthSession(authState.token, payload.user)
    return payload.user
  } catch {
    clearAuthSession()
    return null
  }
}
