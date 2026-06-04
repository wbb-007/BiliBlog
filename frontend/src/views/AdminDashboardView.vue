<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '../components/AppHeader.vue'
import SectionHeading from '../components/SectionHeading.vue'
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
  publishPost,
  updateAdminAnnouncement,
  updateAdminPost,
  updateAdminUser,
} from '../api/blog'
import { authState, isAdminUser } from '../stores/auth'

const coverTones = [
  {
    label: '糖霜粉蓝',
    value: 'pink-cyan',
    background: 'linear-gradient(135deg, #fb7299 0%, #ffb7cc 38%, #5ac8fa 100%)',
  },
  {
    label: '机甲夜航',
    value: 'neon-night',
    background: 'linear-gradient(135deg, #1f274f 0%, #6d3ecb 38%, #fb7299 100%)',
  },
  {
    label: '夏日薄荷',
    value: 'mint-wave',
    background: 'linear-gradient(135deg, #2fc89f 0%, #78e4be 45%, #5ac8fa 100%)',
  },
]

const loading = ref(true)
const categories = ref([])
const overview = ref({ metrics: [], latestPosts: [], latestUsers: [], announcements: [] })
const posts = ref([])
const users = ref([])
const announcements = ref([])
const activeTab = ref('posts')
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

const isAdmin = computed(() => isAdminUser())

const selectedCategoryName = computed(
  () => categories.value.find((item) => item.slug === postForm.category)?.name ?? '未选择分类',
)

const selectedToneStyle = computed(
  () =>
    coverTones.find((tone) => tone.value === postForm.coverTone)?.background ??
    coverTones[0].background,
)

const editorTitle = computed(() => (postForm.id ? '编辑文章' : '新建文章'))

const filteredPosts = computed(() => {
  const keyword = postFilters.keyword.trim().toLowerCase()
  return posts.value.filter((post) => {
    const matchKeyword =
      !keyword ||
      [post.title, post.excerpt, post.author, ...(post.tags ?? [])]
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

const userMetrics = computed(() => [
  { label: '总用户数', value: users.value.length },
  { label: '管理员', value: users.value.filter((user) => user.role === 'ADMIN').length },
  { label: '已启用', value: users.value.filter((user) => user.status === 'ACTIVE').length },
  { label: '已停用', value: users.value.filter((user) => user.status === 'DISABLED').length },
])

const contentMetrics = computed(() => ({
  chars: postForm.content.trim().length,
  paragraphs: previewBlocks.value.length,
  tags: normalizedTags().length,
}))

const previewBlocks = computed(() => parseEditorContent(postForm.content))

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
  if (style?.includes('#1f274f')) {
    return 'neon-night'
  }
  if (style?.includes('#2fc89f')) {
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
  window.open(`/post/${post.id}`, '_blank')
}

async function loadAdminData() {
  if (!isAdmin.value) {
    loading.value = false
    return
  }

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
    ElMessage.error(readErrorMessage(error, '加载后台数据失败'))
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
      ElMessage.success('文章已创建')
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
  const actionText = nextRole === 'ADMIN' ? '提升为管理员' : '降级为普通用户'

  try {
    await ElMessageBox.confirm(`确认将 ${user.nickname} ${actionText}吗？`, '修改角色', {
      type: 'warning',
    })
    workingUserId.value = user.id
    await updateAdminUser(user.id, {
      role: nextRole,
      status: user.status,
    })
    ElMessage.success('用户角色已更新')
    await loadAdminData()
  } catch (error) {
    if (!isCancelAction(error)) {
      ElMessage.error(readErrorMessage(error, '更新用户角色失败'))
    }
  } finally {
    workingUserId.value = null
  }
}

async function toggleUserStatus(user) {
  const nextStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const actionText = nextStatus === 'DISABLED' ? '停用' : '启用'

  try {
    await ElMessageBox.confirm(`确认${actionText}用户 ${user.nickname} 吗？`, '用户状态', {
      type: 'warning',
    })
    workingUserId.value = user.id
    await updateAdminUser(user.id, {
      role: user.role,
      status: nextStatus,
    })
    ElMessage.success(`用户已${actionText}`)
    await loadAdminData()
  } catch (error) {
    if (!isCancelAction(error)) {
      ElMessage.error(readErrorMessage(error, '更新用户状态失败'))
    }
  } finally {
    workingUserId.value = null
  }
}

async function removeUser(user) {
  try {
    await ElMessageBox.confirm(`确认删除用户 ${user.nickname}（${user.email}）吗？`, '删除用户', {
      type: 'warning',
    })
    workingUserId.value = user.id
    await deleteAdminUser(user.id)
    ElMessage.success('用户已删除')
    await loadAdminData()
  } catch (error) {
    if (!isCancelAction(error)) {
      ElMessage.error(readErrorMessage(error, '删除用户失败'))
    }
  } finally {
    workingUserId.value = null
  }
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
  <AppHeader />

  <main class="page-shell">
    <section v-if="!isAdmin" class="surface-card admin-empty">
      <SectionHeading title="管理后台" description="当前账号没有管理员权限。" />
      <p>请使用管理员邮箱登录。默认管理员邮箱可在后端配置 `blog.admin.email` 中修改。</p>
    </section>

    <template v-else>
      <section class="surface-card admin-hero">
        <SectionHeading
          title="站点控制后台"
          description="这里已经接上真实数据流，后面部署到服务器后可以直接管理文章、公告和注册用户。"
        >
          <span class="mini-chip">当前管理员：{{ authState.user?.nickname }}</span>
        </SectionHeading>

        <div class="admin-metrics">
          <article v-for="item in overview.metrics" :key="item.label">
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </article>
        </div>
      </section>

      <section class="surface-card admin-panel">
        <el-skeleton :loading="loading" animated>
          <template #template>
            <div class="admin-skeleton"></div>
          </template>

          <template #default>
            <el-tabs v-model="activeTab">
              <el-tab-pane label="文章管理" name="posts">
                <SectionHeading
                  title="文章管理"
                  description="支持新建、编辑、预览、搜索和删除，让后台直接承担内容工作台。"
                >
                  <el-button round type="primary" @click="openCreatePost">新建文章</el-button>
                </SectionHeading>

                <div class="toolbar-grid">
                  <el-input
                    v-model="postFilters.keyword"
                    clearable
                    placeholder="搜索标题、摘要、作者或标签"
                  />
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
                    <el-option label="投稿广场" value="投稿广场" />
                  </el-select>
                </div>

                <div class="quick-metrics">
                  <article>
                    <strong>{{ filteredPosts.length }}</strong>
                    <span>筛选结果</span>
                  </article>
                  <article>
                    <strong>{{ posts.length }}</strong>
                    <span>文章总数</span>
                  </article>
                  <article>
                    <strong>{{ posts.filter((post) => post.board === '主站专栏').length }}</strong>
                    <span>主站文章</span>
                  </article>
                  <article>
                    <strong>{{ posts.filter((post) => post.board === '投稿广场').length }}</strong>
                    <span>用户投稿</span>
                  </article>
                </div>

                <el-table :data="filteredPosts" stripe>
                  <el-table-column label="文章" min-width="340">
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
                  <el-table-column label="分类" min-width="120" prop="category" />
                  <el-table-column label="分区" width="120">
                    <template #default="{ row }">
                      <el-tag :type="row.board === '主站专栏' ? 'danger' : 'success'" round>
                        {{ row.board }}
                      </el-tag>
                    </template>
                  </el-table-column>
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
              </el-tab-pane>

              <el-tab-pane label="用户管理" name="users">
                <SectionHeading
                  title="用户管理"
                  description="支持搜索用户、切换角色、启停用账号和清理异常注册。"
                />

                <div class="toolbar-grid toolbar-grid--users">
                  <el-input
                    v-model="userFilters.keyword"
                    clearable
                    placeholder="搜索邮箱、昵称、角色或状态"
                  />
                  <el-select v-model="userFilters.role">
                    <el-option label="全部角色" value="all" />
                    <el-option label="管理员" value="ADMIN" />
                    <el-option label="普通用户" value="USER" />
                  </el-select>
                  <el-select v-model="userFilters.status">
                    <el-option label="全部状态" value="all" />
                    <el-option label="启用中" value="ACTIVE" />
                    <el-option label="已停用" value="DISABLED" />
                  </el-select>
                </div>

                <div class="quick-metrics">
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
                        {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
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
                  <el-table-column label="注册时间" min-width="150" prop="createdAt" />
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
                        {{ row.role === 'ADMIN' ? '降为用户' : '设为管理员' }}
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
              </el-tab-pane>

              <el-tab-pane label="公告推送" name="announcements">
                <SectionHeading title="公告推送" description="用于首页播报和站点活动通知。">
                  <el-button round type="primary" @click="createAnnouncement">新建公告</el-button>
                </SectionHeading>
                <el-table :data="announcements" stripe>
                  <el-table-column label="标题" min-width="220" prop="title" />
                  <el-table-column label="内容" min-width="320" prop="content" />
                  <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="row.active ? 'success' : 'info'">
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
              </el-tab-pane>
            </el-tabs>
          </template>
        </el-skeleton>
      </section>
    </template>

    <el-drawer
      v-model="editorVisible"
      :title="editorTitle"
      class="admin-editor-drawer"
      size="92%"
    >
      <div class="admin-editor-shell">
        <section class="editor-form-panel">
          <SectionHeading
            :title="postForm.id ? '文章编辑器' : '发布新文章'"
            description="左侧配置正文与元信息，右侧实时预览上线后的呈现效果。"
          />

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
          <section class="preview-panel">
            <SectionHeading title="实时预览" description="封面和正文块会按博客前台风格即时渲染。" />

            <div class="preview-cover" :style="{ background: selectedToneStyle }">
              <div class="preview-cover__meta">
                <span class="preview-chip">{{ selectedCategoryName }}</span>
                <span class="preview-chip">{{ postForm.id ? '后台编辑' : '新稿件' }}</span>
              </div>
              <div class="preview-cover__copy">
                <strong>{{ postForm.title || '你的标题会显示在这里' }}</strong>
                <p>{{ postForm.excerpt || '摘要会帮助读者快速理解这篇文章为什么值得看。' }}</p>
              </div>
            </div>

            <article class="preview-article">
              <div class="preview-tag-row">
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

          <section class="insight-panel">
            <SectionHeading title="编辑概览" description="发文前快速检查结构和信息完整度。" />
            <div class="insight-grid">
              <article>
                <strong>{{ contentMetrics.chars }}</strong>
                <span>正文字符</span>
              </article>
              <article>
                <strong>{{ contentMetrics.paragraphs }}</strong>
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
  </main>
</template>

<style scoped>
.admin-empty,
.admin-hero,
.admin-panel {
  padding: 24px;
}

.admin-empty p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.75;
}

.admin-metrics,
.quick-metrics,
.insight-grid {
  display: grid;
  gap: 16px;
}

.admin-metrics {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.quick-metrics {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-bottom: 18px;
}

.insight-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.admin-metrics article,
.quick-metrics article,
.insight-grid article {
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(251, 114, 153, 0.1), rgba(90, 200, 250, 0.1));
}

.admin-metrics strong,
.admin-metrics span,
.quick-metrics strong,
.quick-metrics span,
.insight-grid strong,
.insight-grid span {
  display: block;
}

.admin-metrics strong,
.quick-metrics strong,
.insight-grid strong {
  font-family: var(--font-display);
  font-size: 28px;
}

.admin-metrics span,
.quick-metrics span,
.insight-grid span {
  margin-top: 8px;
  color: var(--text-muted);
}

.admin-skeleton {
  height: 420px;
}

.toolbar-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(180px, 220px) minmax(180px, 220px);
  gap: 14px;
  margin-bottom: 18px;
}

.toolbar-grid--users {
  grid-template-columns: minmax(0, 2fr) minmax(160px, 200px) minmax(160px, 200px);
}

.title-cell strong {
  display: block;
  margin-bottom: 8px;
}

.title-cell p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.7;
}

.tag-row,
.preview-tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-row {
  margin-top: 12px;
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
  color: var(--text-secondary);
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
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.85);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.tone-card:hover {
  transform: translateY(-2px);
}

.tone-card--active {
  border-color: rgba(251, 114, 153, 0.6);
  box-shadow: 0 12px 28px rgba(251, 114, 153, 0.12);
}

.tone-card__preview {
  height: 88px;
  margin-bottom: 10px;
  border-radius: 16px;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  margin-top: 22px;
}

.admin-editor-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(340px, 0.9fr);
  gap: 20px;
  min-height: 100%;
}

.editor-form-panel,
.preview-panel,
.insight-panel {
  padding: 24px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.88);
}

.editor-preview-side {
  display: grid;
  align-content: start;
  gap: 18px;
}

.preview-cover {
  position: relative;
  min-height: 260px;
  padding: 24px;
  border-radius: 28px;
  overflow: hidden;
  color: white;
}

.preview-cover__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preview-chip {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(10px);
}

.preview-cover__copy {
  position: absolute;
  right: 24px;
  bottom: 24px;
  left: 24px;
}

.preview-cover__copy strong {
  display: block;
  font-family: var(--font-display);
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
  border-radius: 24px;
  background: var(--surface-soft);
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
  color: var(--text-secondary);
}

.preview-block blockquote {
  padding: 12px 16px;
  border-left: 4px solid rgba(251, 114, 153, 0.48);
  border-radius: 16px;
  background: rgba(251, 114, 153, 0.08);
  color: var(--text-primary);
  line-height: 1.8;
}

.preview-block ul {
  padding-left: 18px;
}

.preview-empty {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.8;
}

.editor-help {
  display: grid;
  gap: 10px;
  margin-top: 18px;
}

.editor-help p {
  margin: 0;
  padding: 14px 16px;
  border-radius: 18px;
  background: var(--surface-soft);
  color: var(--text-secondary);
  line-height: 1.7;
}

:deep(.admin-editor-drawer .el-drawer__body) {
  padding: 16px;
  background: linear-gradient(180deg, rgba(255, 248, 251, 0.96), rgba(244, 251, 255, 0.96));
}

@media (max-width: 1180px) {
  .admin-editor-shell {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .admin-metrics,
  .quick-metrics,
  .insight-grid,
  .toolbar-grid,
  .toolbar-grid--users,
  .editor-grid,
  .tone-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .admin-metrics {
    grid-template-columns: 1fr;
  }

  .admin-empty,
  .admin-hero,
  .admin-panel,
  .editor-form-panel,
  .preview-panel,
  .insight-panel {
    padding: 18px;
  }

  .editor-actions {
    flex-direction: column;
  }
}
</style>
