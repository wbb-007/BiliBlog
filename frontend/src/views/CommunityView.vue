<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Opportunity, User } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import AppHeader from '../components/AppHeader.vue'
import PostCard from '../components/PostCard.vue'
import SectionHeading from '../components/SectionHeading.vue'
import { fetchCategories, fetchCommunity } from '../api/blog'

const route = useRoute()
const loading = ref(true)
const community = ref({ posts: [], metrics: [] })
const categories = ref([])

const filters = reactive({
  category: 'all',
})

const keyword = computed(() => route.query.keyword?.toString().trim() ?? '')

const filteredPosts = computed(() => {
  return community.value.posts.filter((post) => {
    const matchKeyword =
      !keyword.value ||
      [post.title, post.excerpt, post.category, post.author, ...(post.tags ?? [])]
        .join(' ')
        .toLowerCase()
        .includes(keyword.value.toLowerCase())

    const selectedCategory = categories.value.find((item) => item.slug === filters.category)?.name
    const matchCategory = filters.category === 'all' || post.category === selectedCategory

    return matchKeyword && matchCategory
  })
})

async function loadCommunity() {
  loading.value = true
  const [communityData, categoryData] = await Promise.all([
    fetchCommunity(),
    fetchCategories(),
  ])
  community.value = communityData
  categories.value = categoryData
  loading.value = false
}

onMounted(loadCommunity)
</script>

<template>
  <AppHeader />

  <main class="page-shell">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="surface-card community-skeleton"></div>
      </template>

      <template #default>
        <section class="surface-card community-hero">
          <SectionHeading
            title="投稿广场"
            description="这里保留历史社区内容展示。公网版本不开放前台注册和投稿，站点内容统一由管理员后台维护。"
          />

          <div class="community-metrics">
            <article v-for="metric in community.metrics" :key="metric.label">
              <strong>{{ metric.value }}</strong>
              <span>{{ metric.label }}</span>
            </article>
          </div>
        </section>

        <section class="two-column community-layout">
          <div>
            <SectionHeading
              title="最新投稿"
              description="社区用户的内容不会打乱主页节奏，而是在这里形成独立分区。"
            />

            <div class="community-toolbar">
              <el-select v-model="filters.category">
                <el-option label="全部分类" value="all" />
                <el-option
                  v-for="category in categories"
                  :key="category.slug"
                  :label="category.name"
                  :value="category.slug"
                />
              </el-select>
              <div v-if="keyword" class="surface-card keyword-tip">
                正在搜索“{{ keyword }}”，当前匹配 {{ filteredPosts.length }} 篇投稿
              </div>
            </div>

            <div v-if="filteredPosts.length" class="post-list">
              <PostCard
                v-for="(post, index) in filteredPosts"
                :key="post.id"
                :post="post"
                :variant="index === 0 ? 'wide' : 'default'"
              />
            </div>
            <section v-else class="surface-card empty-state">
              <el-icon><Opportunity /></el-icon>
              <strong>投稿广场还没有匹配内容</strong>
              <p>可以换个分类筛选，或者先发布第一篇社区文章。</p>
            </section>
          </div>

          <aside class="community-side">
            <section class="surface-card side-panel">
              <SectionHeading title="投稿规则" description="先把社区创作流程跑通，后面再接审核流。">
                <el-icon class="side-icon"><User /></el-icon>
              </SectionHeading>
              <div class="rule-list">
                <article>
                  <strong>前台只读展示</strong>
                  <p>线上访客可以浏览文章和评论，不需要注册登录。</p>
                </article>
                <article>
                  <strong>评论和详情页仍然共享</strong>
                  <p>无论是主站文章还是投稿文章，都能正常评论和阅读。</p>
                </article>
                <article>
                  <strong>管理员改用独立控制台</strong>
                  <p>文章发布、相册上传和站点设置都在后台完成。</p>
                </article>
              </div>
            </section>
          </aside>
        </section>
      </template>
    </el-skeleton>
  </main>
</template>

<style scoped>
.community-skeleton {
  height: 720px;
}

.community-hero,
.side-panel,
.empty-state {
  padding: 24px;
}

.community-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.community-metrics article {
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(90, 200, 250, 0.1), rgba(251, 114, 153, 0.1));
}

.community-metrics strong,
.community-metrics span {
  display: block;
}

.community-metrics strong {
  font-family: var(--font-display);
  font-size: 28px;
}

.community-metrics span {
  margin-top: 8px;
  color: var(--text-muted);
}

.community-layout {
  align-items: start;
}

.community-toolbar {
  display: grid;
  gap: 14px;
  margin-bottom: 18px;
}

.keyword-tip {
  padding: 14px 16px;
  color: var(--text-secondary);
}

.post-list,
.rule-list {
  display: grid;
  gap: 18px;
}

.community-side {
  display: grid;
  align-content: start;
  gap: 18px;
}

.side-icon {
  color: var(--brand-pink);
}

.rule-list article {
  padding: 16px;
  border-radius: 20px;
  background: var(--surface-soft);
}

.rule-list strong {
  display: block;
}

.rule-list p,
.empty-state p {
  margin: 8px 0 0;
  color: var(--text-muted);
  line-height: 1.75;
}

.empty-state {
  display: grid;
  justify-items: start;
  gap: 12px;
}

.empty-state :deep(.el-icon) {
  font-size: 28px;
  color: var(--brand-pink);
}

@media (max-width: 900px) {
  .community-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
