import axios from 'axios'
import { clearAuthSession, getAuthToken } from '../stores/auth'

const client = axios.create({
  baseURL: '/api',
  timeout: 8000,
})

client.interceptors.request.use((config) => {
  const token = getAuthToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      clearAuthSession()
    }
    return Promise.reject(error)
  },
)

export async function fetchHome() {
  const { data } = await client.get('/home')
  return data
}

export async function fetchCommunity() {
  const { data } = await client.get('/community')
  return data
}

export async function fetchPost(id) {
  const { data } = await client.get(`/posts/${id}`)
  return data
}

export async function fetchPostComments(id) {
  const { data } = await client.get(`/posts/${id}/comments`)
  return data
}

export async function createComment(id, payload) {
  const { data } = await client.post(`/posts/${id}/comments`, payload)
  return data
}

export async function fetchProfile() {
  const { data } = await client.get('/profile')
  return data
}

export async function fetchCategories() {
  const { data } = await client.get('/categories')
  return data
}

export async function publishPost(payload) {
  const { data } = await client.post('/posts', payload)
  return data
}

export async function sendAuthCode(email, purpose) {
  const { data } = await client.post('/auth/send-code', { email, purpose })
  return data
}

export async function loginAuth(payload) {
  const { data } = await client.post('/auth/login', payload)
  return data
}

export async function registerAuth(payload) {
  const { data } = await client.post('/auth/register', payload)
  return data
}

export async function resetPasswordAuth(payload) {
  const { data } = await client.post('/auth/reset-password', payload)
  return data
}

export async function fetchCurrentUser() {
  const { data } = await client.get('/auth/me')
  return data
}

export async function logoutAuth() {
  await client.post('/auth/logout')
}

export async function fetchAdminOverview() {
  const { data } = await client.get('/admin/overview')
  return data
}

export async function fetchAdminUsers() {
  const { data } = await client.get('/admin/users')
  return data
}

export async function updateAdminUser(id, payload) {
  const { data } = await client.put(`/admin/users/${id}`, payload)
  return data
}

export async function deleteAdminUser(id) {
  await client.delete(`/admin/users/${id}`)
}

export async function fetchAdminPosts() {
  const { data } = await client.get('/admin/posts')
  return data
}

export async function updateAdminPost(id, payload) {
  const { data } = await client.put(`/admin/posts/${id}`, payload)
  return data
}

export async function deleteAdminPost(id) {
  await client.delete(`/admin/posts/${id}`)
}

export async function fetchAdminAnnouncements() {
  const { data } = await client.get('/admin/announcements')
  return data
}

export async function createAdminAnnouncement(payload) {
  const { data } = await client.post('/admin/announcements', payload)
  return data
}

export async function updateAdminAnnouncement(id, payload) {
  const { data } = await client.put(`/admin/announcements/${id}`, payload)
  return data
}

export async function deleteAdminAnnouncement(id) {
  await client.delete(`/admin/announcements/${id}`)
}
