<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  DataLine,
  Document,
  PictureFilled,
  Plus,
  Promotion,
  Refresh,
  Search,
  Setting,
  SwitchButton,
  User,
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  createAdminAlbumPhoto,
  createAdminAnnouncement,
  deleteAdminAlbumPhoto,
  deleteAdminAnnouncement,
  deleteAdminPost,
  deleteAdminUser,
  fetchAdminAlbumPhotos,
  fetchAdminAnnouncements,
  fetchLive2dSettings,
  fetchProfileSettings,
  fetchAdminOverview,
  fetchAdminPosts,
  fetchAdminUsers,
  fetchCategories,
  fetchPost,
  logoutAuth,
  publishPost,
  uploadImage,
  updateAdminAlbumPhoto,
  updateAdminAnnouncement,
  updateLive2dSettings,
  updateProfileSettings,
  updateAdminPost,
  updateAdminUser,
} from '../api/blog'
import { authState, clearAdminSession } from '../stores/auth'
import MarkdownEditor from '../../src/components/MarkdownEditor.vue'

const router = useRouter()

const coverTones = [
  {
    label: '晨雾蓝',
    value: 'pink-cyan',
    background: 'linear-gradient(135deg, #2563eb 0%, #60a5fa 44%, #c4dafe 100%)',
  },
  {
    label: '深海夜航',
    value: 'neon-night',
    background: 'linear-gradient(135deg, #0f172a 0%, #1e3a8a 42%, #2563eb 100%)',
  },
  {
    label: '青柠云层',
    value: 'mint-wave',
    background: 'linear-gradient(135deg, #0f766e 0%, #34d399 46%, #93c5fd 100%)',
  },
]

const loading = ref(true)
const categories = ref([])
const overview = ref({ metrics: [], latestPosts: [], latestUsers: [], announcements: [] })
const posts = ref([])
const users = ref([])
const announcements = ref([])
const albumPhotos = ref([])
const activePanel = ref('overview')
const editorVisible = ref(false)
const announcementVisible = ref(false)
const albumPhotoVisible = ref(false)
const savingPost = ref(false)
const savingAnnouncement = ref(false)
const savingAlbumPhoto = ref(false)
const savingLive2d = ref(false)
const savingProfile = ref(false)
const uploadingAvatar = ref(false)
const uploadingAlbumPhoto = ref(false)
const workingUserId = ref(null)

const postFilters = reactive({
  keyword: '',
  category: 'all',
  board: 'all',
})

const userFilters = reactive({
  keyword: '',
  role: 'all',
  status: 'all',
})

const postForm = reactive({
  id: null,
  title: '',
  excerpt: '',
  category: '',
  tags: '',
  content: '',
  coverTone: coverTones[0].value,
})

const announcementForm = reactive({
  id: null,
  title: '',
  content: '',
  active: true,
})

const albumPhotoForm = reactive({
  id: null,
  title: '',
  location: '',
  imageUrl: '',
  caption: '',
  color: 'linear-gradient(135deg, #fb7299 0%, #5ac8fa 100%)',
  active: true,
})

const live2dForm = reactive({
  enabled: true,
  modelName: 'Haru',
  modelUrl: '',
  position: 'right',
  width: 280,
  height: 360,
  hOffset: 16,
  vOffset: 0,
  scale: 1,
})

const live2dPresets = ref([])

const profileForm = reactive({
  name: '',
  headline: '',
  bio: '',
  avatarUrl: '',
  avatarLabel: 'AI',
  bannerStyle: '',
  followers: '',
  likes: '',
  tags: '',
})

const animeAvatarIds = [
  43078, 43074, 43077, 43073, 43062, 43067, 43066, 42872, 42968, 42280,
  24695, 27301, 25942, 33959, 32331, 38682, 36855, 34883, 39059, 43068,
  43039, 43032, 42990, 43038, 43037, 43036, 43030, 43079, 43031, 42989,
  42948, 43014, 43013, 43011, 43010, 43009, 43008, 43003, 42999, 42991,
  42987, 42949, 42965, 42967, 42946, 42966, 42962, 42937, 42954, 42944,
  42947, 42945, 42963, 42932, 42929, 42873, 42856, 42855, 42938, 42925,
  42924, 42889, 42833, 42743, 42676, 42903, 42901, 42898, 42897, 42896,
  42894, 42893, 42888, 42955, 42871, 42869, 42864, 42961, 42837, 42836,
  42834, 42831, 42757, 42721, 42707, 42680, 42677, 42668, 42657, 42615,
  42592, 42573, 42784, 42782, 42780, 42779, 42778, 42776, 42775, 42774,
]

const navItems = [
  { key: 'overview', label: '概览', icon: DataLine },
  { key: 'posts', label: '文章管理', icon: Document },
  { key: 'users', label: '账号管理', icon: User },
  { key: 'announcements', label: '公告推送', icon: Bell },
  { key: 'album', label: '相册管理', icon: PictureFilled },
  { key: 'profile', label: '关于我设置', icon: User },
  { key: 'live2d', label: 'Live2D', icon: Setting },
]

const panelTitle = computed(() => navItems.find((item) => item.key === activePanel.value)?.label ?? '概览')
const panelDescription = computed(() => {
  const copy = {
    overview: '查看站点当前运行状态、最新文章和账号动态。',
    posts: '集中管理主站文章内容，支持发布、编辑与删除。',
    users: '管理管理员账号与历史账号状态。',
    announcements: '维护首页公告、活动通知和运营推送。',
    album: '上传相册图片并维护图片墙展示内容。',
    profile: '维护前台“关于我”页面和首页博主卡片，支持头像、简介、标签和展示数据。',
    live2d: '配置博客前台的 Live2D 看板娘，支持预设人物和自定义模型地址。',
  }
  return copy[activePanel.value]
})

const selectedCategoryName = computed(
  () => categories.value.find((item) => item.slug === postForm.category)?.name ?? '未选择分类',
)

const selectedToneStyle = computed(
  () =>
    coverTones.find((tone) => tone.value === postForm.coverTone)?.background ??
    coverTones[0].background,
)

const editorTitle = computed(() => (postForm.id ? '编辑主站文章' : '新建主站文章'))

const filteredPosts = computed(() => {
  const keyword = postFilters.keyword.trim().toLowerCase()
  return posts.value.filter((post) => {
    const matchKeyword =
      !keyword ||
      [post.title, post.excerpt, post.author, post.board, ...(post.tags ?? [])]
        .join(' ')
        .toLowerCase()
        .includes(keyword)

    const matchCategory =
      postFilters.category === 'all' ||
      post.category === categories.value.find((item) => item.slug === postFilters.category)?.name

    const matchBoard = postFilters.board === 'all' || post.board === postFilters.board

    return matchKeyword && matchCategory && matchBoard
  })
})

const filteredUsers = computed(() => {
  const keyword = userFilters.keyword.trim().toLowerCase()
  return users.value.filter((user) => {
    const matchKeyword =
      !keyword ||
      [user.email, user.nickname, user.role, user.status].join(' ').toLowerCase().includes(keyword)
    const matchRole = userFilters.role === 'all' || user.role === userFilters.role
    const matchStatus = userFilters.status === 'all' || user.status === userFilters.status
    return matchKeyword && matchRole && matchStatus
  })
})

const previewBlocks = computed(() => parseEditorContent(postForm.content))

const contentMetrics = computed(() => ({
  chars: postForm.content.trim().length,
  blocks: previewBlocks.value.length,
  tags: normalizedTags().length,
}))

const userMetrics = computed(() => [
  { label: '总账号数', value: users.value.length },
  { label: '管理员', value: users.value.filter((user) => user.role === 'ADMIN').length },
  { label: '已启用', value: users.value.filter((user) => user.status === 'ACTIVE').length },
  { label: '已停用', value: users.value.filter((user) => user.status === 'DISABLED').length },
])

const siteUrl = computed(() => {
  const { protocol, hostname } = window.location
  return `${protocol}//${hostname}:5173`
})

function normalizedTags() {
  return postForm.tags
    .split(/[，,]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function isCurrentUser(user) {
  return user.id === authState.user?.id
}

function toneFromStyle(style) {
  if (style?.includes('#1f274f') || style?.includes('#0f172a')) {
    return 'neon-night'
  }
  if (style?.includes('#2fc89f') || style?.includes('#0f766e')) {
    return 'mint-wave'
  }
  return 'pink-cyan'
}

function resolveCategorySlug(categoryValue) {
  return (
    categories.value.find(
      (item) => item.slug === categoryValue || item.name === categoryValue,
    )?.slug ??
    categories.value[0]?.slug ??
    'tech'
  )
}

function blocksToEditorText(blocks) {
  return blocks
    .map((block) => {
      if (block.type === 'heading') return `## ${block.content}`
      if (block.type === 'image') return `![${block.content ?? ''}](${block.items?.[0] ?? ''})`
      if (block.type === 'quote') return `> ${block.content}`
      if (block.type === 'list') return (block.items ?? []).map((item) => `- ${item}`).join('\n')
      return block.content
    })
    .join('\n\n')
}

function parseEditorContent(content) {
  const blocks = []
  const listBuffer = []

  for (const rawLine of content.split(/\r?\n/)) {
    const line = rawLine.trim()
    if (!line) {
      flushList(blocks, listBuffer)
      continue
    }

    if (line.startsWith('##')) {
      flushList(blocks, listBuffer)
      blocks.push({ type: 'heading', content: line.slice(2).trim() })
      continue
    }

    const imageMatch = line.match(/^!\[([^\]]*)]\(([^)]+)\)$/)
    if (imageMatch) {
      flushList(blocks, listBuffer)
      blocks.push({ type: 'image', content: imageMatch[1].trim(), items: [imageMatch[2].trim()] })
      continue
    }

    if (line.startsWith('>')) {
      flushList(blocks, listBuffer)
      blocks.push({ type: 'quote', content: line.slice(1).trim() })
      continue
    }

    if (line.startsWith('- ')) {
      listBuffer.push(line.slice(2).trim())
      continue
    }

    flushList(blocks, listBuffer)
    blocks.push({ type: 'paragraph', content: line })
  }

  flushList(blocks, listBuffer)

  if (!blocks.length && content.trim()) {
    blocks.push({ type: 'paragraph', content: content.trim() })
  }

  return blocks
}

function flushList(blocks, listBuffer) {
  if (listBuffer.length) {
    blocks.push({ type: 'list', items: [...listBuffer] })
    listBuffer.length = 0
  }
}

function resetPostForm() {
  postForm.id = null
  postForm.title = ''
  postForm.excerpt = ''
  postForm.category = categories.value[0]?.slug ?? 'tech'
  postForm.tags = ''
  postForm.content = ''
  postForm.coverTone = coverTones[0].value
}

function fillPostForm(post, content) {
  postForm.id = post.id
  postForm.title = post.title
  postForm.excerpt = post.excerpt
  postForm.category = resolveCategorySlug(post.category)
  postForm.tags = (post.tags ?? []).join(', ')
  postForm.content = content
  postForm.coverTone = toneFromStyle(post.coverStyle)
}

function resetAnnouncementForm() {
  announcementForm.id = null
  announcementForm.title = ''
  announcementForm.content = ''
  announcementForm.active = true
}

function resetAlbumPhotoForm() {
  albumPhotoForm.id = null
  albumPhotoForm.title = ''
  albumPhotoForm.location = ''
  albumPhotoForm.imageUrl = ''
  albumPhotoForm.caption = ''
  albumPhotoForm.color = 'linear-gradient(135deg, #fb7299 0%, #5ac8fa 100%)'
  albumPhotoForm.active = true
}

function fillAlbumPhotoForm(photo) {
  albumPhotoForm.id = photo.id
  albumPhotoForm.title = photo.title
  albumPhotoForm.location = photo.location
  albumPhotoForm.imageUrl = photo.imageUrl
  albumPhotoForm.caption = photo.caption
  albumPhotoForm.color = photo.color || 'linear-gradient(135deg, #fb7299 0%, #5ac8fa 100%)'
  albumPhotoForm.active = photo.active
}

function fillLive2dForm(settings) {
  live2dForm.enabled = settings.enabled
  live2dForm.modelName = settings.modelName || 'Custom'
  live2dForm.modelUrl = settings.modelUrl || ''
  live2dForm.position = settings.position || 'right'
  live2dForm.width = settings.width || 280
  live2dForm.height = settings.height || 360
  live2dForm.hOffset = settings.hOffset ?? 16
  live2dForm.vOffset = settings.vOffset ?? 0
  live2dForm.scale = settings.scale || 1
  live2dPresets.value = settings.presets ?? []
}

function fillProfileForm(settings) {
  profileForm.name = settings.name || 'Kimi Chan'
  profileForm.headline = settings.headline || '插画师 / ACG 内容创作者'
  profileForm.bio = settings.bio || ''
  profileForm.avatarUrl = settings.avatarUrl || ''
  profileForm.avatarLabel = settings.avatarLabel || 'AI'
  profileForm.bannerStyle =
    settings.bannerStyle || 'linear-gradient(135deg, #1f274f 0%, #6d3ecb 35%, #fb7299 100%)'
  profileForm.followers = settings.followers || '12.4w'
  profileForm.likes = settings.likes || '138.9w'
  profileForm.tags = (settings.tags ?? []).join('、')
}

function applyLive2dPreset(modelUrl) {
  const preset = live2dPresets.value.find((item) => item.modelUrl === modelUrl)
  live2dForm.modelUrl = modelUrl
  live2dForm.modelName = preset?.name ?? 'Custom'
}

function openCreatePost() {
  resetPostForm()
  editorVisible.value = true
}

function previewPost(post) {
  window.open(`${siteUrl.value}/post/${post.id}`, '_blank', 'noopener')
}

async function loadAdminData() {
  loading.value = true
  try {
    const [
      overviewData,
      postData,
      userData,
      announcementData,
      albumPhotoData,
      categoryData,
      live2dData,
      profileData,
    ] = await Promise.all([
      fetchAdminOverview(),
      fetchAdminPosts(),
      fetchAdminUsers(),
      fetchAdminAnnouncements(),
      fetchAdminAlbumPhotos(),
      fetchCategories(),
      fetchLive2dSettings(),
      fetchProfileSettings(),
    ])

    overview.value = overviewData
    posts.value = postData
    users.value = userData
    announcements.value = announcementData
    albumPhotos.value = albumPhotoData
    categories.value = categoryData
    fillLive2dForm(live2dData)
    fillProfileForm(profileData)

    if (!postForm.category && categoryData.length) {
      postForm.category = categoryData[0].slug
    }
  } catch (error) {
    ElMessage.error(readErrorMessage(error, '加载控制台数据失败'))
  } finally {
    loading.value = false
  }
}

async function editPost(post) {
  try {
    const detail = await fetchPost(post.id)
    fillPostForm(post, blocksToEditorText(detail.post.blocks))
    editorVisible.value = true
  } catch (error) {
    ElMessage.error(readErrorMessage(error, '读取文章详情失败'))
  }
}

async function removePost(post) {
  try {
    await ElMessageBox.confirm(`确认删除文章《${post.title}》吗？`, '删除文章', {
      type: 'warning',
    })
    await deleteAdminPost(post.id)
    ElMessage.success('文章已删除')
    await loadAdminData()
  } catch (error) {
    if (!isCancelAction(error)) {
      ElMessage.error(readErrorMessage(error, '删除文章失败'))
    }
  }
}

async function savePost() {
  if (!postForm.title.trim() || !postForm.excerpt.trim() || !postForm.category || !postForm.content.trim()) {
    ElMessage.warning('请先填写完整的标题、摘要、分类和正文')
    return
  }

  savingPost.value = true
  try {
    const payload = {
      title: postForm.title.trim(),
      category: postForm.category,
      tags: normalizedTags(),
      content: postForm.content.trim(),
      coverTone: postForm.coverTone,
    }

    if (postForm.id) {
      await updateAdminPost(postForm.id, {
        ...payload,
        excerpt: postForm.excerpt.trim(),
      })
      ElMessage.success('文章已更新')
    } else {
      await publishPost({
        ...payload,
        summary: postForm.excerpt.trim(),
      })
      ElMessage.success('主站文章已创建')
    }

    editorVisible.value = false
    await loadAdminData()
  } catch (error) {
    ElMessage.error(readErrorMessage(error, '保存文章失败'))
  } finally {
    savingPost.value = false
  }
}

function createAnnouncement() {
  resetAnnouncementForm()
  announcementVisible.value = true
}

function editAnnouncement(item) {
  announcementForm.id = item.id
  announcementForm.title = item.title
  announcementForm.content = item.content
  announcementForm.active = item.active
  announcementVisible.value = true
}

function createAlbumPhoto() {
  resetAlbumPhotoForm()
  albumPhotoVisible.value = true
}

function editAlbumPhoto(item) {
  fillAlbumPhotoForm(item)
  albumPhotoVisible.value = true
}

async function saveAnnouncement() {
  if (!announcementForm.title.trim() || !announcementForm.content.trim()) {
    ElMessage.warning('请先填写完整的公告标题和内容')
    return
  }

  savingAnnouncement.value = true
  try {
    const payload = {
      title: announcementForm.title.trim(),
      content: announcementForm.content.trim(),
      active: announcementForm.active,
    }

    if (announcementForm.id) {
      await updateAdminAnnouncement(announcementForm.id, payload)
    } else {
      await createAdminAnnouncement(payload)
    }

    announcementVisible.value = false
    ElMessage.success('公告已保存')
    await loadAdminData()
  } catch (error) {
    ElMessage.error(readErrorMessage(error, '保存公告失败'))
  } finally {
    savingAnnouncement.value = false
  }
}

async function removeAnnouncement(item) {
  try {
    await ElMessageBox.confirm(`确认删除公告《${item.title}》吗？`, '删除公告', {
      type: 'warning',
    })
    await deleteAdminAnnouncement(item.id)
    ElMessage.success('公告已删除')
    await loadAdminData()
  } catch (error) {
    if (!isCancelAction(error)) {
      ElMessage.error(readErrorMessage(error, '删除公告失败'))
    }
  }
}

async function saveAlbumPhoto() {
  if (!albumPhotoForm.title.trim() || !albumPhotoForm.imageUrl.trim() || !albumPhotoForm.caption.trim()) {
    ElMessage.warning('请填写标题、图片和描述')
    return
  }

  savingAlbumPhoto.value = true
  try {
    const payload = {
      title: albumPhotoForm.title.trim(),
      location: albumPhotoForm.location.trim() || '生活片段',
      imageUrl: albumPhotoForm.imageUrl.trim(),
      caption: albumPhotoForm.caption.trim(),
      color: albumPhotoForm.color.trim(),
      active: albumPhotoForm.active,
    }

    if (albumPhotoForm.id) {
      await updateAdminAlbumPhoto(albumPhotoForm.id, payload)
      ElMessage.success('相册图片已更新')
    } else {
      await createAdminAlbumPhoto(payload)
      ElMessage.success('相册图片已添加')
    }
    albumPhotoVisible.value = false
    await loadAdminData()
  } catch (error) {
    ElMessage.error(readErrorMessage(error, '保存相册图片失败'))
  } finally {
    savingAlbumPhoto.value = false
  }
}

async function uploadAlbumPhoto(file) {
  uploadingAlbumPhoto.value = true
  try {
    const result = await uploadImage(file.raw ?? file)
    albumPhotoForm.imageUrl = result.url
    if (!albumPhotoForm.title.trim()) {
      albumPhotoForm.title = '新的相册照片'
    }
    ElMessage.success('图片已上传，补充信息后保存')
  } catch (error) {
    ElMessage.error(readErrorMessage(error, '上传相册图片失败'))
  } finally {
    uploadingAlbumPhoto.value = false
  }
  return false
}

async function removeAlbumPhoto(item) {
  try {
    await ElMessageBox.confirm(`确定删除相册图片「${item.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteAdminAlbumPhoto(item.id)
    ElMessage.success('相册图片已删除')
    await loadAdminData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(readErrorMessage(error, '删除相册图片失败'))
    }
  }
}

async function saveLive2dSettings() {
  if (!live2dForm.modelUrl.trim()) {
    ElMessage.warning('请先选择人物预设或填写模型地址')
    return
  }

  savingLive2d.value = true
  try {
    const settings = await updateLive2dSettings({
      enabled: live2dForm.enabled,
      modelName: live2dForm.modelName,
      modelUrl: live2dForm.modelUrl.trim(),
      position: live2dForm.position,
      width: Number(live2dForm.width),
      height: Number(live2dForm.height),
      hOffset: Number(live2dForm.hOffset),
      vOffset: Number(live2dForm.vOffset),
      scale: Number(live2dForm.scale),
    })
    fillLive2dForm(settings)
    ElMessage.success('Live2D 看板娘设置已保存')
  } catch (error) {
    ElMessage.error(readErrorMessage(error, '保存 Live2D 设置失败'))
  } finally {
    savingLive2d.value = false
  }
}

async function saveProfileSettings() {
  savingProfile.value = true
  try {
    const settings = await updateProfileSettings({
      name: profileForm.name.trim(),
      headline: profileForm.headline.trim(),
      bio: profileForm.bio.trim(),
      avatarUrl: profileForm.avatarUrl.trim(),
      avatarLabel: profileForm.avatarLabel.trim(),
      bannerStyle: profileForm.bannerStyle.trim(),
      followers: profileForm.followers.trim(),
      likes: profileForm.likes.trim(),
      tags: profileForm.tags
        .split(/[、,，\n]/)
        .map((tag) => tag.trim())
        .filter(Boolean),
    })
    fillProfileForm(settings)
    ElMessage.success('关于我设置已保存')
    await loadAdminData()
  } catch (error) {
    ElMessage.error(readErrorMessage(error, '保存关于我设置失败'))
  } finally {
    savingProfile.value = false
  }
}

function useRandomAnimeAvatar() {
  const index = Math.floor(Math.random() * animeAvatarIds.length)
  profileForm.avatarUrl = `/backup-images/anime/anime-${String(index + 1).padStart(3, '0')}-${animeAvatarIds[index]}.jpg`
  ElMessage.success('已随机选择一张本地动漫头像，记得保存设置')
}

async function uploadProfileAvatar(file) {
  uploadingAvatar.value = true
  try {
    const result = await uploadImage(file.raw ?? file)
    profileForm.avatarUrl = result.url
    ElMessage.success('头像已上传，记得保存设置')
  } catch (error) {
    ElMessage.error(readErrorMessage(error, '上传头像失败'))
  } finally {
    uploadingAvatar.value = false
  }
  return false
}

async function changeUserRole(user) {
  const nextRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
  const actionText = nextRole === 'ADMIN' ? '提升为管理员' : '调整为历史用户'

  try {
    await ElMessageBox.confirm(`确认将 ${user.nickname} ${actionText}吗？`, '修改角色', {
      type: 'warning',
    })
    workingUserId.value = user.id
    await updateAdminUser(user.id, {
      role: nextRole,
      status: user.status,
    })
    ElMessage.success('账号角色已更新')
    await loadAdminData()
  } catch (error) {
    if (!isCancelAction(error)) {
      ElMessage.error(readErrorMessage(error, '更新账号角色失败'))
    }
  } finally {
    workingUserId.value = null
  }
}

async function toggleUserStatus(user) {
  const nextStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const actionText = nextStatus === 'DISABLED' ? '停用' : '启用'

  try {
    await ElMessageBox.confirm(`确认${actionText}账号 ${user.nickname} 吗？`, '账号状态', {
      type: 'warning',
    })
    workingUserId.value = user.id
    await updateAdminUser(user.id, {
      role: user.role,
      status: nextStatus,
    })
    ElMessage.success(`账号已${actionText}`)
    await loadAdminData()
  } catch (error) {
    if (!isCancelAction(error)) {
      ElMessage.error(readErrorMessage(error, '更新账号状态失败'))
    }
  } finally {
    workingUserId.value = null
  }
}

async function removeUser(user) {
  try {
    await ElMessageBox.confirm(`确认删除账号 ${user.nickname}（${user.email}）吗？`, '删除账号', {
      type: 'warning',
    })
    workingUserId.value = user.id
    await deleteAdminUser(user.id)
    ElMessage.success('账号及其关联文章已删除')
    await loadAdminData()
  } catch (error) {
    if (!isCancelAction(error)) {
      ElMessage.error(readErrorMessage(error, '删除账号失败'))
    }
  } finally {
    workingUserId.value = null
  }
}

async function handleLogout() {
  try {
    await logoutAuth()
  } catch {
    // ignore logout network errors
  }

  clearAdminSession()
  ElMessage.success('已退出控制台')
  router.push({ name: 'admin-login' })
}

function openSite() {
  window.open(siteUrl.value, '_blank', 'noopener')
}

function readErrorMessage(error, fallback) {
  return error?.response?.data?.message ?? fallback
}

function isCancelAction(error) {
  return error === 'cancel' || error === 'close'
}

onMounted(loadAdminData)
</script>

<template>
  <div class="console-shell dashboard-shell">
    <aside class="dashboard-sidebar">
      <div class="sidebar-brand">
        <span class="sidebar-brand__logo">BiliBlog</span>
        <span class="sidebar-brand__text">Console</span>
      </div>

      <div class="sidebar-group">
        <button
          v-for="item in navItems"
          :key="item.key"
          class="sidebar-link"
          :class="{ 'sidebar-link--active': activePanel === item.key }"
          type="button"
          @click="activePanel = item.key"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </div>

      <div class="sidebar-foot">
        <div class="sidebar-admin-card">
          <strong>{{ authState.user?.nickname }}</strong>
          <span>{{ authState.user?.email }}</span>
        </div>
        <button class="sidebar-link sidebar-link--ghost" type="button" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </button>
      </div>
    </aside>

    <section class="dashboard-main">
      <header class="dashboard-topbar">
        <div>
          <p class="topbar-eyebrow">独立管理控制台</p>
          <h1>{{ panelTitle }}</h1>
          <span>{{ panelDescription }}</span>
        </div>

        <div class="topbar-actions">
          <el-button round @click="openSite">
            打开博客前台
          </el-button>
          <el-button round type="primary" @click="loadAdminData">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </header>

      <el-skeleton :loading="loading" animated>
        <template #template>
          <div class="console-card skeleton-panel"></div>
        </template>

        <template #default>
          <section v-if="activePanel === 'overview'" class="dashboard-grid">
            <section class="console-card hero-panel">
              <div class="hero-panel__head">
                <div>
                  <span class="section-eyebrow">站点概览</span>
                  <h2>后台已经与博客前台彻底分离</h2>
                </div>
                <el-button round type="primary" @click="activePanel = 'posts'">
                  <el-icon><Plus /></el-icon>
                  进入文章管理
                </el-button>
              </div>

              <div class="metric-grid">
                <article v-for="item in overview.metrics" :key="item.label">
                  <strong>{{ item.value }}</strong>
                  <span>{{ item.label }}</span>
                </article>
              </div>
            </section>

            <section class="overview-triple">
              <article class="console-card overview-card">
                <div class="card-head">
                  <div>
                    <span class="section-eyebrow">最近文章</span>
                    <h3>最新内容</h3>
                  </div>
                  <button class="text-link" type="button" @click="activePanel = 'posts'">查看全部</button>
                </div>
                <div class="simple-list">
                  <article v-for="post in overview.latestPosts" :key="post.id" class="simple-list__item">
                    <strong>{{ post.title }}</strong>
                    <span>{{ post.board }} · {{ post.publishedAt }}</span>
                  </article>
                </div>
              </article>

              <article class="console-card overview-card">
                <div class="card-head">
                  <div>
                    <span class="section-eyebrow">最近账号</span>
                    <h3>账号动态</h3>
                  </div>
                  <button class="text-link" type="button" @click="activePanel = 'users'">查看全部</button>
                </div>
                <div class="simple-list">
                  <article v-for="user in overview.latestUsers" :key="user.id" class="simple-list__item">
                    <strong>{{ user.nickname }}</strong>
                    <span>{{ user.email }} · {{ user.role }}</span>
                  </article>
                </div>
              </article>

              <article class="console-card overview-card">
                <div class="card-head">
                  <div>
                    <span class="section-eyebrow">公告流</span>
                    <h3>推送状态</h3>
                  </div>
                  <button class="text-link" type="button" @click="activePanel = 'announcements'">查看全部</button>
                </div>
                <div class="simple-list">
                  <article
                    v-for="notice in overview.announcements.slice(0, 5)"
                    :key="notice.id"
                    class="simple-list__item"
                  >
                    <strong>{{ notice.title }}</strong>
                    <span>{{ notice.active ? '启用中' : '未启用' }}</span>
                  </article>
                </div>
              </article>
            </section>
          </section>

          <section v-else-if="activePanel === 'posts'" class="console-card content-panel">
            <div class="panel-head">
              <div>
                <span class="section-eyebrow">文章工作台</span>
                <h2>主站文章集中管理</h2>
              </div>
              <el-button round type="primary" @click="openCreatePost">
                <el-icon><Plus /></el-icon>
                新建主站文章
              </el-button>
            </div>

            <div class="toolbar-grid">
              <el-input
                v-model="postFilters.keyword"
                clearable
                placeholder="搜索标题、摘要、作者、标签或分区"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select v-model="postFilters.category">
                <el-option label="全部分类" value="all" />
                <el-option
                  v-for="item in categories"
                  :key="item.slug"
                  :label="item.name"
                  :value="item.slug"
                />
              </el-select>
              <el-select v-model="postFilters.board">
                <el-option label="全部分区" value="all" />
                <el-option label="主站专栏" value="主站专栏" />
                <el-option label="历史投稿" value="投稿广场" />
              </el-select>
            </div>

            <div class="metric-grid metric-grid--compact">
              <article>
                <strong>{{ filteredPosts.length }}</strong>
                <span>筛选结果</span>
              </article>
              <article>
                <strong>{{ posts.filter((post) => post.board === '主站专栏').length }}</strong>
                <span>主站文章</span>
              </article>
              <article>
                <strong>{{ posts.filter((post) => post.board === '投稿广场').length }}</strong>
                <span>历史投稿</span>
              </article>
            </div>

            <el-table :data="filteredPosts" stripe>
              <el-table-column label="文章" min-width="360">
                <template #default="{ row }">
                  <div class="title-cell">
                    <strong>{{ row.title }}</strong>
                    <p>{{ row.excerpt }}</p>
                    <div class="tag-row">
                      <el-tag v-for="tag in row.tags" :key="tag" effect="plain" round size="small">
                        {{ tag }}
                      </el-tag>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="分区" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.board === '主站专栏' ? 'primary' : 'success'" round>
                    {{ row.board }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="分类" min-width="120" prop="category" />
              <el-table-column label="作者" min-width="120" prop="author" />
              <el-table-column label="发布时间" min-width="130" prop="publishedAt" />
              <el-table-column label="操作" width="210">
                <template #default="{ row }">
                  <el-button link type="primary" @click="editPost(row)">编辑</el-button>
                  <el-button link @click="previewPost(row)">查看</el-button>
                  <el-button link type="danger" @click="removePost(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </section>

          <section v-else-if="activePanel === 'users'" class="console-card content-panel">
            <div class="panel-head">
              <div>
                <span class="section-eyebrow">账号管理</span>
                <h2>管理员账号与历史账号状态</h2>
              </div>
            </div>

            <div class="toolbar-grid toolbar-grid--users">
              <el-input
                v-model="userFilters.keyword"
                clearable
                placeholder="搜索邮箱、昵称、角色或状态"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select v-model="userFilters.role">
                <el-option label="全部角色" value="all" />
                <el-option label="管理员" value="ADMIN" />
                <el-option label="历史用户" value="USER" />
              </el-select>
              <el-select v-model="userFilters.status">
                <el-option label="全部状态" value="all" />
                <el-option label="启用中" value="ACTIVE" />
                <el-option label="已停用" value="DISABLED" />
              </el-select>
            </div>

            <div class="metric-grid metric-grid--compact">
              <article v-for="item in userMetrics" :key="item.label">
                <strong>{{ item.value }}</strong>
                <span>{{ item.label }}</span>
              </article>
            </div>

            <el-table :data="filteredUsers" stripe>
              <el-table-column label="用户" min-width="250">
                <template #default="{ row }">
                  <div class="title-cell">
                    <strong>{{ row.nickname }}</strong>
                    <p>{{ row.email }}</p>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="角色" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" round>
                    {{ row.role === 'ADMIN' ? '管理员' : '历史用户' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'warning'" round>
                    {{ row.status === 'ACTIVE' ? '启用中' : '已停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" min-width="150" prop="createdAt" />
              <el-table-column label="最近登录" min-width="150">
                <template #default="{ row }">
                  {{ row.lastLoginAt || '从未登录' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="260">
                <template #default="{ row }">
                  <el-button
                    :disabled="isCurrentUser(row)"
                    :loading="workingUserId === row.id"
                    link
                    type="primary"
                    @click="changeUserRole(row)"
                  >
                    {{ row.role === 'ADMIN' ? '调整为历史用户' : '设为管理员' }}
                  </el-button>
                  <el-button
                    :disabled="isCurrentUser(row)"
                    :loading="workingUserId === row.id"
                    link
                    type="warning"
                    @click="toggleUserStatus(row)"
                  >
                    {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
                  </el-button>
                  <el-button
                    :disabled="isCurrentUser(row)"
                    :loading="workingUserId === row.id"
                    link
                    type="danger"
                    @click="removeUser(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </section>

          <section v-else-if="activePanel === 'announcements'" class="console-card content-panel">
            <div class="panel-head">
              <div>
                <span class="section-eyebrow">公告推送</span>
                <h2>首页播报与站点活动通知</h2>
              </div>
              <el-button round type="primary" @click="createAnnouncement">
                <el-icon><Promotion /></el-icon>
                新建公告
              </el-button>
            </div>

            <el-table :data="announcements" stripe>
              <el-table-column label="标题" min-width="220" prop="title" />
              <el-table-column label="内容" min-width="360" prop="content" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.active ? 'success' : 'info'" round>
                    {{ row.active ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" min-width="150" prop="createdAt" />
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button link type="primary" @click="editAnnouncement(row)">编辑</el-button>
                  <el-button link type="danger" @click="removeAnnouncement(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </section>

          <section v-else-if="activePanel === 'album'" class="console-card content-panel">
            <div class="panel-head">
              <div>
                <span class="section-eyebrow">Album Upload</span>
                <h2>相册图片墙管理</h2>
              </div>
              <el-button round type="primary" @click="createAlbumPhoto">
                <el-icon><PictureFilled /></el-icon>
                上传相册
              </el-button>
            </div>

            <div class="album-admin-grid">
              <article
                v-for="photo in albumPhotos"
                :key="photo.id"
                class="album-admin-card"
              >
                <div class="album-admin-card__image" :style="{ background: photo.color }">
                  <img v-if="photo.imageUrl" :src="photo.imageUrl" :alt="photo.title" />
                </div>
                <div class="album-admin-card__body">
                  <div>
                    <strong>{{ photo.title }}</strong>
                    <p>{{ photo.caption }}</p>
                  </div>
                  <div class="album-admin-card__meta">
                    <el-tag :type="photo.active ? 'success' : 'info'" round>
                      {{ photo.active ? '展示中' : '已隐藏' }}
                    </el-tag>
                    <span>{{ photo.location }}</span>
                  </div>
                  <div class="album-admin-card__actions">
                    <el-button link type="primary" @click="editAlbumPhoto(photo)">编辑</el-button>
                    <el-button link type="danger" @click="removeAlbumPhoto(photo)">删除</el-button>
                  </div>
                </div>
              </article>

              <button class="album-admin-empty" type="button" @click="createAlbumPhoto">
                <el-icon><Plus /></el-icon>
                <strong>添加一张照片</strong>
                <span>上传后会出现在前台相册和花园图片墙。</span>
              </button>
            </div>
          </section>

          <section v-else-if="activePanel === 'profile'" class="console-card content-panel">
            <div class="panel-head">
              <div>
                <span class="section-eyebrow">About Profile</span>
                <h2>关于我设置</h2>
              </div>
              <div class="panel-actions">
                <el-button round @click="useRandomAnimeAvatar">
                  <el-icon><Refresh /></el-icon>
                  随机头像
                </el-button>
                <el-button :loading="savingProfile" round type="primary" @click="saveProfileSettings">
                  <el-icon><User /></el-icon>
                  保存资料
                </el-button>
              </div>
            </div>

            <div class="profile-settings-grid">
              <section class="profile-form-panel">
                <div class="field-block">
                  <span class="field-label">昵称</span>
                  <el-input v-model="profileForm.name" placeholder="例如 Kimi Chan" size="large" />
                </div>

                <div class="field-block">
                  <span class="field-label">身份签名</span>
                  <el-input v-model="profileForm.headline" placeholder="例如 插画师 / ACG 内容创作者" size="large" />
                </div>

                <div class="field-block">
                  <span class="field-label">关于我简介</span>
                  <el-input
                    v-model="profileForm.bio"
                    :rows="4"
                    maxlength="500"
                    placeholder="写一段会展示在关于我页面侧栏的介绍"
                    show-word-limit
                    type="textarea"
                  />
                </div>

                <div class="field-block">
                  <span class="field-label">标签</span>
                  <el-input
                    v-model="profileForm.tags"
                    placeholder="用顿号或逗号分隔，例如 个人博客、视觉设计、前端实验"
                    size="large"
                  />
                </div>

                <div class="field-block">
                  <span class="field-label">头像图片地址</span>
                  <el-input
                    v-model="profileForm.avatarUrl"
                    placeholder="上传图片后会自动填入，也可以粘贴 https://... 图片地址"
                    size="large"
                  />
                </div>

                <div class="field-block">
                  <span class="field-label">无图片时显示的文字</span>
                  <el-input
                    v-model="profileForm.avatarLabel"
                    maxlength="4"
                    placeholder="例如 AI"
                    size="large"
                    show-word-limit
                  />
                </div>

                <div class="field-block">
                  <span class="field-label">头图背景</span>
                  <el-input
                    v-model="profileForm.bannerStyle"
                    placeholder="CSS 背景，例如 linear-gradient(...) 或图片 url(...)"
                    size="large"
                  />
                </div>

                <div class="field-grid">
                  <div class="field-block">
                    <span class="field-label">粉丝数</span>
                    <el-input v-model="profileForm.followers" placeholder="12.4w" size="large" />
                  </div>
                  <div class="field-block">
                    <span class="field-label">获赞数</span>
                    <el-input v-model="profileForm.likes" placeholder="138.9w" size="large" />
                  </div>
                </div>

                <el-upload
                  :before-upload="uploadProfileAvatar"
                  :show-file-list="false"
                  accept="image/*"
                  drag
                >
                  <div class="avatar-upload-drop">
                    <el-icon><Plus /></el-icon>
                    <strong>{{ uploadingAvatar ? '上传中...' : '上传一张新头像' }}</strong>
                    <span>建议使用正方形二次元动漫头像，保存后前台立即生效。</span>
                  </div>
                </el-upload>
              </section>

              <aside class="profile-preview-panel">
                <div class="profile-card-preview">
                  <div class="profile-card-preview__banner" :style="{ background: profileForm.bannerStyle }"></div>
                  <div class="profile-card-preview__body">
                    <div class="profile-avatar-preview">
                      <img
                        v-if="profileForm.avatarUrl"
                        :src="profileForm.avatarUrl"
                        alt="博主头像预览"
                      />
                      <span v-else>{{ profileForm.avatarLabel || 'AI' }}</span>
                    </div>
                    <h3>{{ profileForm.name || 'Kimi Chan' }}</h3>
                    <p>{{ profileForm.headline || '插画师 / ACG 内容创作者' }}</p>
                    <p class="profile-card-preview__bio">{{ profileForm.bio || '关于我简介会显示在这里。' }}</p>
                    <div class="profile-card-preview__chips">
                      <span
                        v-for="tag in profileForm.tags.split(/[、,，\n]/).map((item) => item.trim()).filter(Boolean)"
                        :key="tag"
                      >
                        {{ tag }}
                      </span>
                    </div>
                    <div class="profile-card-preview__stats">
                      <strong>{{ profileForm.followers || '12.4w' }}<span>粉丝</span></strong>
                      <strong>{{ profileForm.likes || '138.9w' }}<span>获赞</span></strong>
                    </div>
                  </div>
                </div>
                <div class="editor-help">
                  <p>这些内容会显示在首页作者卡、关于我页面、文章卡片和文章详情作者信息里。</p>
                  <p>头像可以上传自己的图片，也可以从本地 100 张动漫素材里随机挑一张。</p>
                  <p>评论头像不用手动设置，读者发表评论时会自动生成随机二次元头像。</p>
                </div>
              </aside>
            </div>
          </section>

          <section v-else-if="activePanel === 'live2d'" class="console-card content-panel">
            <div class="panel-head">
              <div>
                <span class="section-eyebrow">Live2D Widget</span>
                <h2>前台看板娘设置</h2>
              </div>
              <el-button :loading="savingLive2d" round type="primary" @click="saveLive2dSettings">
                <el-icon><Setting /></el-icon>
                保存设置
              </el-button>
            </div>

            <div class="live2d-settings-grid">
              <section class="live2d-form-panel">
                <el-switch
                  v-model="live2dForm.enabled"
                  active-text="启用看板娘"
                  inactive-text="暂不显示"
                />

                <div class="field-block">
                  <span class="field-label">人物预设</span>
                  <el-select
                    :model-value="live2dForm.modelUrl"
                    placeholder="选择一个预设人物"
                    size="large"
                    @change="applyLive2dPreset"
                  >
                    <el-option
                      v-for="preset in live2dPresets"
                      :key="preset.modelUrl"
                      :label="preset.name"
                      :value="preset.modelUrl"
                    />
                  </el-select>
                </div>

                <div class="field-block">
                  <span class="field-label">自定义人物名称</span>
                  <el-input v-model="live2dForm.modelName" placeholder="例如 My Waifu" size="large" />
                </div>

                <div class="field-block">
                  <span class="field-label">模型 JSON 地址</span>
                  <el-input
                    v-model="live2dForm.modelUrl"
                    placeholder="https://.../model.json"
                    size="large"
                  />
                </div>

                <div class="live2d-control-grid">
                  <div class="field-block">
                    <span class="field-label">显示位置</span>
                    <el-segmented
                      v-model="live2dForm.position"
                      :options="[
                        { label: '左下', value: 'left' },
                        { label: '右下', value: 'right' },
                      ]"
                    />
                  </div>
                  <div class="field-block">
                    <span class="field-label">缩放</span>
                    <el-input-number v-model="live2dForm.scale" :min="1" :max="3" :step="0.1" />
                  </div>
                  <div class="field-block">
                    <span class="field-label">宽度</span>
                    <el-input-number v-model="live2dForm.width" :min="120" :max="600" :step="10" />
                  </div>
                  <div class="field-block">
                    <span class="field-label">高度</span>
                    <el-input-number v-model="live2dForm.height" :min="120" :max="800" :step="10" />
                  </div>
                  <div class="field-block">
                    <span class="field-label">水平偏移</span>
                    <el-input-number v-model="live2dForm.hOffset" :min="-200" :max="200" :step="4" />
                  </div>
                  <div class="field-block">
                    <span class="field-label">垂直偏移</span>
                    <el-input-number v-model="live2dForm.vOffset" :min="-200" :max="200" :step="4" />
                  </div>
                </div>
              </section>

              <aside class="live2d-preview-panel">
                <div class="live2d-preview-stage">
                  <div
                    class="live2d-preview-card"
                    :class="`live2d-preview-card--${live2dForm.position}`"
                  >
                    <strong>{{ live2dForm.modelName || 'Custom' }}</strong>
                    <span>{{ live2dForm.enabled ? '前台显示中' : '当前已隐藏' }}</span>
                  </div>
                </div>
                <div class="editor-help">
                  <p>预设模型来自公共 CDN，保存后刷新博客前台即可看到新人物。</p>
                  <p>自定义模型请填写可公开访问的 Live2D model.json 地址。</p>
                  <p>如果人物加载不出来，通常是模型地址不支持跨域或资源路径不完整。</p>
                </div>
              </aside>
            </div>
          </section>
        </template>
      </el-skeleton>
    </section>

    <el-drawer
      v-model="editorVisible"
      :title="editorTitle"
      class="admin-editor-drawer"
      size="90%"
    >
      <div class="editor-shell">
        <section class="console-card editor-form-panel">
          <div class="panel-head panel-head--simple">
            <div>
              <span class="section-eyebrow">文章编辑器</span>
              <h2>{{ postForm.id ? '编辑主站文章' : '发布新的主站文章' }}</h2>
            </div>
          </div>

          <div class="editor-grid">
            <el-input v-model="postForm.title" placeholder="标题，建议 18 到 28 个字" size="large" />
            <el-select v-model="postForm.category" placeholder="分类" size="large">
              <el-option
                v-for="item in categories"
                :key="item.slug"
                :label="item.name"
                :value="item.slug"
              />
            </el-select>
            <el-input
              v-model="postForm.tags"
              class="editor-grid__full"
              placeholder="标签，使用逗号分隔"
              size="large"
            />
            <el-input
              v-model="postForm.excerpt"
              :rows="3"
              class="editor-grid__full"
              placeholder="摘要会同时用于首页卡片和详情页导语"
              type="textarea"
            />

            <div class="editor-grid__full">
              <span class="field-label">封面氛围</span>
              <div class="tone-grid">
                <button
                  v-for="tone in coverTones"
                  :key="tone.value"
                  :class="{ 'tone-card--active': tone.value === postForm.coverTone }"
                  class="tone-card"
                  type="button"
                  @click="postForm.coverTone = tone.value"
                >
                  <div class="tone-card__preview" :style="{ background: tone.background }"></div>
                  <span>{{ tone.label }}</span>
                </button>
              </div>
            </div>

            <MarkdownEditor
              v-model="postForm.content"
              class="editor-grid__full"
              :upload-image="uploadImage"
              height="560px"
              placeholder="支持 Markdown：## 小标题、> 引用、- 列表。可以直接粘贴图片，上传后会自动插入图片链接。"
            />
          </div>

          <div class="editor-actions">
            <el-button round @click="editorVisible = false">取消</el-button>
            <el-button :loading="savingPost" round type="primary" @click="savePost">
              {{ postForm.id ? '保存修改' : '发布文章' }}
            </el-button>
          </div>
        </section>

        <aside class="editor-preview-side">
          <section class="console-card preview-panel">
            <div class="panel-head panel-head--simple">
              <div>
                <span class="section-eyebrow">实时预览</span>
                <h2>主站文章上线效果</h2>
              </div>
            </div>

            <div class="preview-cover" :style="{ background: selectedToneStyle }">
              <div class="preview-cover__meta">
                <span class="preview-chip">{{ selectedCategoryName }}</span>
                <span class="preview-chip">主站专栏</span>
              </div>
              <div class="preview-cover__copy">
                <strong>{{ postForm.title || '你的标题会显示在这里' }}</strong>
                <p>{{ postForm.excerpt || '摘要会帮助读者快速理解这篇文章为什么值得看。' }}</p>
              </div>
            </div>

            <article class="preview-article">
              <div class="tag-row">
                <el-tag v-for="tag in normalizedTags()" :key="tag" effect="plain" round>
                  {{ tag }}
                </el-tag>
              </div>

              <template v-if="previewBlocks.length">
                <div
                  v-for="(block, index) in previewBlocks"
                  :key="`${block.type}-${index}`"
                  class="preview-block"
                >
                  <h3 v-if="block.type === 'heading'">{{ block.content }}</h3>
                  <blockquote v-else-if="block.type === 'quote'">{{ block.content }}</blockquote>
                  <ul v-else-if="block.type === 'list'">
                    <li v-for="item in block.items" :key="item">{{ item }}</li>
                  </ul>
                  <figure v-else-if="block.type === 'image'" class="preview-image">
                    <img :src="block.items?.[0]" :alt="block.content || '文章图片'" />
                    <figcaption v-if="block.content">{{ block.content }}</figcaption>
                  </figure>
                  <p v-else>{{ block.content }}</p>
                </div>
              </template>
              <p v-else class="preview-empty">正文预览会在你开始输入后显示在这里。</p>
            </article>
          </section>

          <section class="console-card insight-panel">
            <div class="panel-head panel-head--simple">
              <div>
                <span class="section-eyebrow">编辑概览</span>
                <h2>发布前检查</h2>
              </div>
            </div>
            <div class="metric-grid metric-grid--compact">
              <article>
                <strong>{{ contentMetrics.chars }}</strong>
                <span>正文字符</span>
              </article>
              <article>
                <strong>{{ contentMetrics.blocks }}</strong>
                <span>内容块</span>
              </article>
              <article>
                <strong>{{ contentMetrics.tags }}</strong>
                <span>标签数</span>
              </article>
            </div>
            <div class="editor-help">
              <p>`## 小标题` 会转成章节标题。</p>
              <p>`> 引用` 适合强调观点。</p>
              <p>`- 列表项` 会自动组合成项目列表。</p>
            </div>
          </section>
        </aside>
      </div>
    </el-drawer>

    <el-dialog v-model="announcementVisible" title="公告编辑" width="620px">
      <div class="editor-grid">
        <el-input v-model="announcementForm.title" placeholder="公告标题" />
        <el-input
          v-model="announcementForm.content"
          :rows="5"
          placeholder="公告内容"
          type="textarea"
        />
        <el-switch
          v-model="announcementForm.active"
          active-text="启用展示"
          inactive-text="暂不展示"
        />
      </div>
      <template #footer>
        <el-button round @click="announcementVisible = false">取消</el-button>
        <el-button :loading="savingAnnouncement" round type="primary" @click="saveAnnouncement">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="albumPhotoVisible" title="相册图片编辑" width="760px">
      <div class="album-photo-editor">
        <section class="album-photo-editor__upload">
          <el-upload
            :before-upload="uploadAlbumPhoto"
            :show-file-list="false"
            accept="image/*"
            drag
          >
            <div v-if="albumPhotoForm.imageUrl" class="album-upload-preview">
              <img :src="albumPhotoForm.imageUrl" alt="相册预览" />
            </div>
            <div v-else class="avatar-upload-drop">
              <el-icon><Plus /></el-icon>
              <strong>{{ uploadingAlbumPhoto ? '上传中...' : '上传相册图片' }}</strong>
              <span>上传后会自动填入图片地址。</span>
            </div>
          </el-upload>
        </section>

        <section class="editor-grid">
          <el-input v-model="albumPhotoForm.title" placeholder="图片标题" />
          <el-input v-model="albumPhotoForm.location" placeholder="地点 / 分组，例如 Home Studio" />
          <el-input v-model="albumPhotoForm.imageUrl" placeholder="图片地址，也可以先上传" />
          <el-input v-model="albumPhotoForm.color" placeholder="占位背景，如 linear-gradient(...)" />
          <el-input
            v-model="albumPhotoForm.caption"
            :rows="4"
            placeholder="图片描述"
            type="textarea"
          />
          <el-switch
            v-model="albumPhotoForm.active"
            active-text="前台展示"
            inactive-text="暂时隐藏"
          />
        </section>
      </div>

      <template #footer>
        <el-button round @click="albumPhotoVisible = false">取消</el-button>
        <el-button :loading="savingAlbumPhoto" round type="primary" @click="saveAlbumPhoto">
          保存相册
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dashboard-shell {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
}

.dashboard-sidebar {
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: 28px;
  min-height: 100vh;
  padding: 22px 18px;
  background: linear-gradient(180deg, var(--console-sidebar), var(--console-sidebar-soft));
  color: rgba(255, 255, 255, 0.92);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.06);
}

.sidebar-brand__logo,
.sidebar-brand__text {
  font-family: var(--console-display);
  font-weight: 700;
}

.sidebar-brand__logo {
  color: #93c5fd;
}

.sidebar-group,
.sidebar-foot {
  display: grid;
  gap: 10px;
}

.sidebar-link {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  border: 0;
  border-radius: 18px;
  background: transparent;
  color: rgba(255, 255, 255, 0.78);
  cursor: pointer;
  text-align: left;
  transition: background 0.18s ease, transform 0.18s ease;
}

.sidebar-link:hover,
.sidebar-link--active {
  background: rgba(147, 197, 253, 0.16);
  color: white;
}

.sidebar-link:hover {
  transform: translateX(2px);
}

.sidebar-link--ghost {
  color: rgba(255, 255, 255, 0.72);
}

.sidebar-admin-card {
  padding: 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.08);
}

.sidebar-admin-card strong,
.sidebar-admin-card span {
  display: block;
}

.sidebar-admin-card span {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.68);
  font-size: 13px;
  line-height: 1.6;
}

.dashboard-main {
  padding: 24px;
}

.dashboard-topbar {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.topbar-eyebrow,
.section-eyebrow {
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.1);
  color: var(--console-primary-deep);
  font-size: 12px;
  font-weight: 700;
}

.dashboard-topbar h1,
.hero-panel h2,
.content-panel h2,
.overview-card h3,
.preview-panel h2,
.insight-panel h2,
.editor-form-panel h2 {
  margin: 14px 0 0;
  font-family: var(--console-display);
}

.dashboard-topbar h1 {
  font-size: 34px;
}

.dashboard-topbar span {
  display: block;
  margin-top: 10px;
  color: var(--console-text-muted);
}

.topbar-actions {
  display: flex;
  gap: 12px;
}

.dashboard-grid,
.overview-triple,
.metric-grid,
.simple-list,
.editor-preview-side,
.editor-help {
  display: grid;
  gap: 18px;
}

.hero-panel,
.content-panel,
.overview-card,
.preview-panel,
.insight-panel,
.editor-form-panel {
  padding: 24px;
}

.hero-panel__head,
.panel-head,
.card-head {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 18px;
}

.panel-head--simple {
  margin-bottom: 18px;
}

.overview-triple {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.metric-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-top: 20px;
}

.metric-grid--compact {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.metric-grid article {
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(180deg, #f9fbff, #f4f7fb);
}

.metric-grid strong,
.metric-grid span {
  display: block;
}

.metric-grid strong {
  font-family: var(--console-display);
  font-size: 28px;
}

.metric-grid span {
  margin-top: 8px;
  color: var(--console-text-muted);
}

.simple-list__item {
  padding: 16px;
  border-radius: 20px;
  background: var(--console-surface-soft);
}

.simple-list__item strong,
.simple-list__item span {
  display: block;
}

.simple-list__item span,
.title-cell p {
  margin-top: 8px;
  color: var(--console-text-muted);
  line-height: 1.7;
}

.text-link {
  border: 0;
  background: transparent;
  color: var(--console-primary-deep);
  cursor: pointer;
  font-weight: 700;
}

.toolbar-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(180px, 220px) minmax(180px, 220px);
  gap: 14px;
  margin: 18px 0;
}

.toolbar-grid--users {
  grid-template-columns: minmax(0, 2fr) minmax(160px, 200px) minmax(160px, 200px);
}

.title-cell strong {
  display: block;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.skeleton-panel {
  height: 520px;
}

.editor-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(360px, 0.85fr);
  gap: 18px;
  min-height: 100%;
}

.editor-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.editor-grid__full {
  grid-column: 1 / -1;
}

.field-label {
  display: inline-block;
  margin-bottom: 10px;
  color: var(--console-text-muted);
  font-size: 14px;
}

.field-block {
  display: grid;
  gap: 8px;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.tone-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.tone-card {
  padding: 10px;
  border: 1px solid transparent;
  border-radius: 20px;
  background: #f7f9fc;
  cursor: pointer;
}

.tone-card--active {
  border-color: rgba(59, 130, 246, 0.5);
  box-shadow: 0 14px 28px rgba(59, 130, 246, 0.12);
}

.tone-card__preview {
  height: 84px;
  margin-bottom: 10px;
  border-radius: 14px;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 22px;
}

.live2d-settings-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(340px, 0.8fr);
  gap: 20px;
  margin-top: 22px;
}

.profile-settings-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 0.65fr);
  gap: 20px;
  margin-top: 22px;
}

.live2d-form-panel,
.live2d-preview-panel,
.profile-form-panel,
.profile-preview-panel {
  display: grid;
  align-content: start;
  gap: 18px;
  padding: 22px;
  border-radius: 22px;
  background: var(--console-surface-soft);
}

.avatar-upload-drop {
  display: grid;
  gap: 8px;
  justify-items: center;
  padding: 18px;
  color: var(--console-text-muted);
}

.avatar-upload-drop strong {
  color: var(--console-text);
}

.album-admin-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-top: 20px;
}

.album-admin-card,
.album-admin-empty {
  overflow: hidden;
  border-radius: 22px;
  background: var(--console-surface-soft);
}

.album-admin-card__image {
  height: 170px;
  overflow: hidden;
}

.album-admin-card__image img,
.album-upload-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.album-admin-card__body {
  display: grid;
  gap: 12px;
  padding: 16px;
}

.album-admin-card__body strong,
.album-admin-card__body p {
  display: block;
  margin: 0;
}

.album-admin-card__body p {
  display: -webkit-box;
  overflow: hidden;
  color: var(--console-text-muted);
  line-height: 1.65;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.album-admin-card__meta,
.album-admin-card__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.album-admin-card__meta span {
  overflow: hidden;
  color: var(--console-text-muted);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-admin-empty {
  display: grid;
  min-height: 310px;
  place-items: center;
  align-content: center;
  gap: 10px;
  border: 1px dashed rgba(59, 130, 246, 0.28);
  color: var(--console-text-muted);
  cursor: pointer;
}

.album-admin-empty :deep(.el-icon) {
  color: #60a5fa;
  font-size: 30px;
}

.album-admin-empty strong {
  color: var(--console-text);
}

.album-photo-editor {
  display: grid;
  grid-template-columns: minmax(260px, 0.8fr) minmax(0, 1fr);
  gap: 18px;
}

.album-upload-preview {
  height: 230px;
  overflow: hidden;
  border-radius: 12px;
}

.profile-card-preview {
  overflow: hidden;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
}

.profile-card-preview__banner {
  height: 120px;
}

.profile-card-preview__body {
  display: grid;
  justify-items: center;
  gap: 10px;
  padding: 0 18px 20px;
  margin-top: -54px;
  text-align: center;
}

.profile-card-preview h3,
.profile-card-preview p {
  margin: 0;
}

.profile-card-preview h3 {
  color: var(--console-text);
  font-family: var(--console-display);
  font-size: 24px;
}

.profile-card-preview p {
  color: var(--console-text-muted);
  line-height: 1.6;
}

.profile-card-preview__bio {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.profile-card-preview__chips,
.profile-card-preview__stats {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.profile-card-preview__chips span {
  border-radius: 999px;
  padding: 6px 10px;
  background: rgba(59, 130, 246, 0.1);
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
}

.profile-card-preview__stats strong {
  display: grid;
  min-width: 82px;
  border-radius: 14px;
  padding: 10px;
  background: var(--console-surface-soft);
  color: var(--console-text);
  font-size: 18px;
}

.profile-card-preview__stats span {
  margin-top: 4px;
  color: var(--console-text-muted);
  font-size: 12px;
  font-weight: 600;
}

.profile-avatar-preview {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 160px;
  height: 160px;
  overflow: hidden;
  border: 6px solid white;
  border-radius: 42px;
  background: linear-gradient(135deg, #fb7299, #5ac8fa);
  color: white;
  font-family: var(--console-display);
  font-size: 44px;
  font-weight: 800;
  box-shadow: 0 22px 42px rgba(59, 130, 246, 0.18);
}

.profile-avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.live2d-control-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.live2d-preview-stage {
  position: relative;
  min-height: 360px;
  overflow: hidden;
  border-radius: 22px;
  background:
    linear-gradient(180deg, rgba(59, 130, 246, 0.08), rgba(15, 23, 42, 0.06)),
    repeating-linear-gradient(45deg, rgba(59, 130, 246, 0.08) 0 1px, transparent 1px 18px);
}

.live2d-preview-card {
  position: absolute;
  bottom: 24px;
  display: grid;
  gap: 8px;
  width: min(220px, 58%);
  aspect-ratio: 3 / 4;
  place-content: end center;
  padding: 18px;
  border-radius: 24px;
  color: white;
  text-align: center;
  background: linear-gradient(160deg, #fb7299, #60a5fa 58%, #1e3a8a);
  box-shadow: 0 22px 42px rgba(59, 130, 246, 0.22);
}

.live2d-preview-card--left {
  left: 24px;
}

.live2d-preview-card--right {
  right: 24px;
}

.live2d-preview-card strong,
.live2d-preview-card span {
  display: block;
}

.live2d-preview-card strong {
  font-family: var(--console-display);
  font-size: 24px;
}

.live2d-preview-card span {
  opacity: 0.88;
}

.preview-cover {
  position: relative;
  min-height: 260px;
  overflow: hidden;
  padding: 24px;
  border-radius: 24px;
  color: white;
}

.preview-cover__meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.preview-chip {
  display: inline-flex;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
}

.preview-cover__copy {
  position: absolute;
  right: 24px;
  bottom: 24px;
  left: 24px;
}

.preview-cover__copy strong {
  display: block;
  font-family: var(--console-display);
  font-size: 30px;
  line-height: 1.2;
}

.preview-cover__copy p {
  margin: 12px 0 0;
  line-height: 1.8;
  opacity: 0.92;
}

.preview-article {
  margin-top: 18px;
  padding: 22px;
  border-radius: 22px;
  background: var(--console-surface-soft);
}

.preview-block + .preview-block {
  margin-top: 18px;
}

.preview-block h3,
.preview-block p,
.preview-block blockquote,
.preview-block ul {
  margin: 0;
}

.preview-block h3 {
  font-size: 22px;
}

.preview-block p,
.preview-block li {
  line-height: 1.85;
  color: var(--console-text-muted);
}

.preview-block blockquote {
  padding: 12px 16px;
  border-left: 4px solid rgba(59, 130, 246, 0.48);
  border-radius: 16px;
  background: rgba(59, 130, 246, 0.08);
  color: var(--console-text);
  line-height: 1.8;
}

.preview-block ul {
  padding-left: 18px;
}

.preview-image {
  margin: 0;
}

.preview-image img {
  display: block;
  width: 100%;
  max-height: 420px;
  object-fit: contain;
  border-radius: 18px;
  background: #eef3f9;
}

.preview-image figcaption {
  margin-top: 8px;
  color: var(--console-text-muted);
  font-size: 13px;
  text-align: center;
}

.preview-empty,
.editor-help p {
  margin: 0;
  color: var(--console-text-muted);
  line-height: 1.8;
}

.editor-help p {
  padding: 14px 16px;
  border-radius: 18px;
  background: var(--console-surface-soft);
}

:deep(.admin-editor-drawer .el-drawer__body) {
  padding: 16px;
  background: linear-gradient(180deg, #f8fbff, #eef3f9);
}

@media (max-width: 1120px) {
  .album-admin-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .album-photo-editor {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .album-admin-grid {
    grid-template-columns: 1fr;
  }
}
</style>
