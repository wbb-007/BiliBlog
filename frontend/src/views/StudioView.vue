<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Promotion } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import MarkdownEditor from '../components/MarkdownEditor.vue'
import SectionHeading from '../components/SectionHeading.vue'
import { fetchCategories, publishPost, uploadImage } from '../api/blog'
import { authState, isAdminUser } from '../stores/auth'

const router = useRouter()
const formRef = ref()
const categories = ref([])
const publishing = ref(false)
const isAdmin = computed(() => isAdminUser())

const tones = [
  {
    label: '糖霜粉蓝',
    value: 'pink-cyan',
    background:
      'linear-gradient(135deg, #fb7299 0%, #ffb7cc 38%, #5ac8fa 100%)',
  },
  {
    label: '机甲夜航',
    value: 'neon-night',
    background:
      'linear-gradient(135deg, #1f274f 0%, #6d3ecb 38%, #fb7299 100%)',
  },
  {
    label: '夏日薄荷',
    value: 'mint-wave',
    background:
      'linear-gradient(135deg, #2fc89f 0%, #78e4be 45%, #5ac8fa 100%)',
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
  summary: [{ required: true, message: '写一句摘要更容易提升点击率', trigger: 'blur' }],
  content: [{ required: true, message: '正文还没有填写', trigger: 'blur' }],
}

const previewStyle = computed(
  () => tones.find((tone) => tone.value === form.coverTone)?.background ?? tones[0].background,
)

async function loadCategories() {
  categories.value = await fetchCategories()
}

function saveDraft() {
  ElMessage.success('草稿已暂存，后续可接真实草稿接口。')
}

async function submit() {
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
      tags: form.tags
        .split(/[，,]/)
        .map((tag) => tag.trim())
        .filter(Boolean),
    })

    ElMessage.success(result.message)
    router.push({ name: 'post-detail', params: { id: result.id } })
  } finally {
    publishing.value = false
  }
}

onMounted(loadCategories)
</script>

<template>
  <AppHeader />

  <main class="page-shell">
    <section v-if="!isAdmin" class="surface-card studio-guard">
      <SectionHeading
        title="创作中心"
        description="当前只对管理员开放，用于发布和维护站点文章。"
      />
      <p>请先使用管理员邮箱登录，再进入创作中心发布文章。</p>
    </section>

    <section v-else class="two-column studio-layout">
      <div class="surface-card studio-form-card">
        <SectionHeading
          title="发布新文章"
          description="把静态设计稿转换成真正可提交的创作后台。"
        >
          <span class="mini-chip">Vue + Element Plus 表单</span>
        </SectionHeading>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="studio-grid">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="写一个让人想点开的标题" size="large" />
            </el-form-item>

            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="选择文章分类" size="large">
                <el-option
                  v-for="category in categories"
                  :key="category.slug"
                  :label="category.name"
                  :value="category.slug"
                />
              </el-select>
            </el-form-item>

            <el-form-item class="studio-grid__full" label="标签">
              <el-input
                v-model="form.tags"
                placeholder="例如：Vue，动画杂谈，项目复盘"
                size="large"
              />
            </el-form-item>

            <el-form-item class="studio-grid__full" label="摘要" prop="summary">
              <el-input
                v-model="form.summary"
                :rows="3"
                placeholder="一句话总结这篇文章为什么值得看"
                type="textarea"
              />
            </el-form-item>

            <el-form-item class="studio-grid__full" label="封面氛围">
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

            <el-form-item class="studio-grid__full" label="正文" prop="content">
              <MarkdownEditor
                v-model="form.content"
                :upload-image="uploadImage"
                height="520px"
                placeholder="支持 Markdown：## 小标题、> 引用、- 列表。可以直接粘贴图片，上传后会自动插入图片链接。"
              />
            </el-form-item>
          </div>

          <div class="studio-actions">
            <el-button round size="large" @click="saveDraft">保存草稿</el-button>
            <el-button
              :loading="publishing"
              round
              size="large"
              type="primary"
              @click="submit"
            >
              立即发布
            </el-button>
          </div>
        </el-form>
      </div>

      <aside class="studio-side">
        <section class="surface-card preview-panel">
          <SectionHeading title="实时预览" description="延续 bilibili 风格的圆角大卡片。" />
          <div class="preview-cover" :style="{ background: previewStyle }">
            <div class="preview-cover__chip">
              <el-icon><Picture /></el-icon>
              <span>{{ form.category || '未选择分类' }}</span>
            </div>
            <div class="preview-cover__copy">
              <strong>{{ form.title || '你的标题会显示在这里' }}</strong>
              <p>{{ form.summary || '摘要会帮助读者快速理解内容亮点。' }}</p>
            </div>
          </div>
        </section>

        <section class="surface-card tips-panel">
          <SectionHeading title="发布建议" description="先把后台体验跑通，后面再接富文本。">
            <el-icon class="tips-panel__icon"><Promotion /></el-icon>
          </SectionHeading>
          <div class="tips-list">
            <article>
              <strong>标题建议 18 到 28 个字</strong>
              <p>更适合首页瀑布流与详情页首屏展示。</p>
            </article>
            <article>
              <strong>摘要优先写“为什么值得看”</strong>
              <p>和 B 站动态一样，第一屏决定读者会不会继续停留。</p>
            </article>
            <article>
              <strong>内容结构尽量一屏一重点</strong>
              <p>当前后端已支持普通段落、`##` 小标题和 `-` 列表。</p>
            </article>
          </div>
        </section>
      </aside>
    </section>
  </main>
</template>

<style scoped>
.studio-guard {
  padding: 24px;
}

.studio-guard p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.75;
}

.studio-form-card,
.preview-panel,
.tips-panel {
  padding: 24px;
}

.studio-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.studio-grid__full {
  grid-column: 1 / -1;
}

.studio-actions {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  margin-top: 22px;
}

.studio-side {
  display: grid;
  align-content: start;
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
  background: rgba(255, 255, 255, 0.82);
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

.tips-panel__icon {
  color: var(--brand-pink);
}

.tips-list {
  display: grid;
  gap: 14px;
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
  .studio-grid {
    grid-template-columns: 1fr;
  }

  .tone-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .studio-form-card,
  .preview-panel,
  .tips-panel {
    padding: 18px;
  }

  .studio-actions {
    justify-content: stretch;
    flex-direction: column;
  }
}
</style>
