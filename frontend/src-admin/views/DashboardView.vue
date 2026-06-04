<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  DataLine,
  Document,
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
  createAdminAnnouncement,
  deleteAdminAnnouncement,
  deleteAdminPost,
  deleteAdminUser,
  fetchAdminAnnouncements,
  fetchAdminOverview,
  fetchAdminPosts,
  fetchAdminUsers,
  fetchCategories,
  fetchPost,
  logoutAuth,
  publishPost,
  updateAdminAnnouncement,
  updateAdminPost,
  updateAdminUser,
} from '../api/blog'
import { authState, clearAdminSession } from '../stores/auth'

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
const activePanel = ref('overview')
const editorVisible = ref(false)
const announcementVisible = ref(false)
const savingPost = ref(false)
const savingAnnouncement = ref(false)
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

const navItems = [
  { key: 'overview', label: '概览', icon: DataLine },
  { key: 'posts', label: '文章管理', icon: Document },
  { key: 'users', label: '账号管理', icon: User },
  { key: 'announcements', label: '公告推送', icon: Bell },
]

const panelTitle = computed(() => navItems.find((item) => item.key === activePanel.value)?.label ?? '概览')
const panelDescription = computed(() => {
  const copy = {
    overview: '查看站点当前运行状态、最新文章和账号动态。',
    posts: '集中管理主站文章内容，支持发布、编辑与删除。',
    users: '管理管理员账号与历史账号状态。',
    announcements: '维护首页公告、活动通知和运营推送。',
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
    const [overviewData, postData, userData, announcementData, categoryData] = await Promise.all([
      fetchAdminOverview(),
      fetchAdminPosts(),
      fetchAdminUsers(),
      fetchAdminAnnouncements(),
      fetchCategories(),
    ])

    overview.value = overviewData
    posts.value = postData
    users.value = userData
    announcements.value = announcementData
    categories.value = categoryData

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

          <section v-else class="console-card content-panel">
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

            <el-input
              v-model="postForm.content"
              :rows="18"
              class="editor-grid__full"
              placeholder="支持普通段落、## 小标题、> 引用和 - 列表。"
              type="textarea"
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
</style>
