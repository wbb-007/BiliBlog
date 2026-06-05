import axios from 'axios'
import { clearAdminSession, getAuthToken } from '../stores/auth'

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
      clearAdminSession()
    }
    return Promise.reject(error)
  },
)

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

export async function logoutAuth() {
  await client.post('/auth/logout')
}

export async function fetchCategories() {
  const { data } = await client.get('/categories')
  return data
}

export async function fetchLive2dSettings() {
  const { data } = await client.get('/admin/live2d')
  return data
}

export async function fetchProfileSettings() {
  const { data } = await client.get('/admin/profile')
  return data
}

export async function fetchPost(id) {
  const { data } = await client.get(`/posts/${id}`)
  return data
}

export async function publishPost(payload) {
  const { data } = await client.post('/posts', payload)
  return data
}

export async function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await client.post('/uploads/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
  return data
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

export async function fetchAdminAlbumPhotos() {
  const { data } = await client.get('/admin/album/photos')
  return data
}

export async function createAdminAlbumPhoto(payload) {
  const { data } = await client.post('/admin/album/photos', payload)
  return data
}

export async function updateAdminAlbumPhoto(id, payload) {
  const { data } = await client.put(`/admin/album/photos/${id}`, payload)
  return data
}

export async function deleteAdminAlbumPhoto(id) {
  await client.delete(`/admin/album/photos/${id}`)
}

export async function updateLive2dSettings(payload) {
  const { data } = await client.put('/admin/live2d', payload)
  return data
}

export async function updateProfileSettings(payload) {
  const { data } = await client.put('/admin/profile', payload)
  return data
}
