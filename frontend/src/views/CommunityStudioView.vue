<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { EditPen, Picture } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import AuthDialog from '../components/AuthDialog.vue'
import MarkdownEditor from '../components/MarkdownEditor.vue'
import SectionHeading from '../components/SectionHeading.vue'
import { fetchCategories, publishPost, uploadImage } from '../api/blog'
import { authState, isAdminUser } from '../stores/auth'
import { openAdminConsole } from '../utils/adminConsole'

const router = useRouter()
const formRef = ref()
const categories = ref([])
const publishing = ref(false)
const showAuthDialog = ref(false)

const tones = [
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

const form = reactive({
  title: '',
  category: '',
  tags: '',
  summary: '',
  content: '',
  coverTone: tones[0].value,
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  summary: [{ required: true, message: '请补一句摘要', trigger: 'blur' }],
  content: [{ required: true, message: '正文还没有填写', trigger: 'blur' }],
}

const currentUser = computed(() => authState.user)
const isAdmin = computed(() => isAdminUser())
const previewStyle = computed(
  () => tones.find((tone) => tone.value === form.coverTone)?.background ?? tones[0].background,
)

async function loadCategories() {
  categories.value = await fetchCategories()
}

async function submit() {
  if (!currentUser.value) {
    showAuthDialog.value = true
    return
  }

  if (isAdmin.value) {
    router.push({ name: 'studio' })
    return
  }

  let valid = false
  await formRef.value
    .validate()
    .then(() => {
      valid = true
    })
    .catch(() => {
      valid = false
    })

  if (!valid) {
    return
  }

  publishing.value = true
  try {
    const result = await publishPost({
      ...form,
      tags: form.tags.split(/[，,]/).map((tag) => tag.trim()).filter(Boolean),
    })
    ElMessage.success(result.message)
    router.push({ name: 'post-detail', params: { id: result.id } })
  } catch (error) {
    ElMessage.error(error?.response?.data?.message ?? '投稿发布失败')
  } finally {
    publishing.value = false
  }
}

onMounted(loadCategories)
</script>

<template>
  <AppHeader />

  <main class="page-shell">
    <section v-if="!currentUser" class="surface-card submit-guard">
      <SectionHeading title="用户投稿" description="登录后就可以把文章发布到投稿广场。" />
      <p>普通用户注册登录后即可发文，投稿会进入社区广场，不会直接出现在首页。</p>
      <el-button round type="primary" @click="showAuthDialog = true">登录 / 注册</el-button>
    </section>

    <section v-else-if="isAdmin" class="surface-card submit-guard">
      <SectionHeading title="用户投稿" description="当前账号是管理员，请前往独立控制台发布主站文章。" />
      <p>现在后台已经从博客前台剥离，管理员内容请在单独的控制台中管理和发布。</p>
      <el-button round type="primary" @click="openAdminConsole()">打开管理控制台</el-button>
    </section>

    <section v-else class="two-column submit-layout">
      <div class="surface-card submit-form-card">
        <SectionHeading
          title="投稿发布"
          description="这里是普通用户的独立发文区，文章会沉淀到投稿广场。"
        >
          <span class="mini-chip">当前投稿人：{{ currentUser.nickname }}</span>
        </SectionHeading>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="submit-grid">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="给这篇投稿起一个标题" size="large" />
            </el-form-item>

            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="选择分类" size="large">
                <el-option
                  v-for="category in categories"
                  :key="category.slug"
                  :label="category.name"
                  :value="category.slug"
                />
              </el-select>
            </el-form-item>

            <el-form-item class="submit-grid__full" label="标签">
              <el-input v-model="form.tags" placeholder="例如：观后感，设计灵感，学习笔记" size="large" />
            </el-form-item>

            <el-form-item class="submit-grid__full" label="摘要" prop="summary">
              <el-input v-model="form.summary" :rows="3" type="textarea" placeholder="一句话介绍你的投稿内容" />
            </el-form-item>

            <el-form-item class="submit-grid__full" label="封面氛围">
              <div class="tone-grid">
                <button
                  v-for="tone in tones"
                  :key="tone.value"
                  class="tone-card"
                  :class="{ 'tone-card--active': tone.value === form.coverTone }"
                  type="button"
                  @click="form.coverTone = tone.value"
                >
                  <div class="tone-card__preview" :style="{ background: tone.background }"></div>
                  <span>{{ tone.label }}</span>
                </button>
              </div>
            </el-form-item>

            <el-form-item class="submit-grid__full" label="正文" prop="content">
              <MarkdownEditor
                v-model="form.content"
                :upload-image="uploadImage"
                height="520px"
                placeholder="支持 Markdown：## 小标题、> 引用、- 列表。可以直接粘贴图片，上传后会自动插入图片链接。"
              />
            </el-form-item>
          </div>

          <div class="submit-actions">
            <el-button :loading="publishing" round size="large" type="primary" @click="submit">
              发布到投稿广场
            </el-button>
          </div>
        </el-form>
      </div>

      <aside class="submit-side">
        <section class="surface-card preview-panel">
          <SectionHeading title="投稿预览" description="发布后会以卡片形式出现在投稿广场。" />
          <div class="preview-cover" :style="{ background: previewStyle }">
            <div class="preview-cover__chip">
              <el-icon><Picture /></el-icon>
              <span>{{ form.category || '未选择分类' }}</span>
            </div>
            <div class="preview-cover__copy">
              <strong>{{ form.title || '你的投稿标题会显示在这里' }}</strong>
              <p>{{ form.summary || '摘要会帮助其他读者快速理解你的内容。' }}</p>
            </div>
          </div>
        </section>

        <section class="surface-card tips-panel">
          <SectionHeading title="投稿建议" description="社区文章和主站文章分区展示，不用担心打乱主页。" >
            <el-icon class="tips-icon"><EditPen /></el-icon>
          </SectionHeading>
          <div class="tips-list">
            <article>
              <strong>写清楚你的观点和切入点</strong>
              <p>投稿广场更适合表达个人经验、学习记录和兴趣观察。</p>
            </article>
            <article>
              <strong>结构清楚会更容易被阅读</strong>
              <p>可以多用 `##` 和列表，把内容分成几个小段落。</p>
            </article>
            <article>
              <strong>主页不会混入用户投稿</strong>
              <p>所以你可以更自由地分享，不会影响主站专栏节奏。</p>
            </article>
          </div>
        </section>
      </aside>
    </section>
    <AuthDialog v-model="showAuthDialog" />
  </main>
</template>

<style scoped>
.submit-guard,
.submit-form-card,
.preview-panel,
.tips-panel {
  padding: 24px;
}

.submit-guard p {
  margin: 0 0 16px;
  color: var(--text-muted);
  line-height: 1.8;
}

.submit-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.submit-grid__full {
  grid-column: 1 / -1;
}

.submit-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 22px;
}

.submit-side,
.tips-list {
  display: grid;
  gap: 18px;
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
}

.tone-card--active {
  border-color: rgba(251, 114, 153, 0.6);
  box-shadow: 0 12px 28px rgba(251, 114, 153, 0.12);
}

.tone-card__preview {
  height: 88px;
  border-radius: 16px;
  margin-bottom: 10px;
}

.preview-cover {
  position: relative;
  min-height: 280px;
  overflow: hidden;
  border-radius: 28px;
  padding: 22px;
  color: white;
}

.preview-cover__chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
}

.preview-cover__copy {
  position: absolute;
  left: 22px;
  right: 22px;
  bottom: 22px;
}

.preview-cover__copy strong {
  display: block;
  font-family: var(--font-display);
  font-size: 28px;
  line-height: 1.2;
}

.preview-cover__copy p {
  margin: 12px 0 0;
  line-height: 1.8;
  opacity: 0.92;
}

.tips-icon {
  color: var(--brand-pink);
}

.tips-list article {
  padding: 16px;
  border-radius: 20px;
  background: var(--surface-soft);
}

.tips-list strong {
  display: block;
}

.tips-list p {
  margin: 8px 0 0;
  color: var(--text-muted);
  line-height: 1.75;
}

@media (max-width: 900px) {
  .submit-grid,
  .tone-grid {
    grid-template-columns: 1fr;
  }
}
</style>
