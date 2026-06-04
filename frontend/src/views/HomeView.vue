<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Compass, MagicStick, Opportunity, Promotion, TrendCharts } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import CreatorPanel from '../components/CreatorPanel.vue'
import PostCard from '../components/PostCard.vue'
import SectionHeading from '../components/SectionHeading.vue'
import { fetchHome } from '../api/blog'

const route = useRoute()
const loading = ref(true)
const home = ref(null)
const keyword = computed(() => route.query.keyword?.toString().trim() ?? '')
const hasFeaturedPost = computed(() => Boolean(home.value?.featuredPost))

const filteredPosts = computed(() => {
  if (!home.value) {
    return []
  }

  if (!keyword.value) {
    return home.value.posts
  }

  return home.value.posts.filter((post) => {
    const pool = [post.title, post.excerpt, post.category, ...post.tags].join(' ')
    return pool.toLowerCase().includes(keyword.value.toLowerCase())
  })
})

async function loadHome() {
  loading.value = true
  home.value = await fetchHome()
  loading.value = false
}

onMounted(loadHome)
</script>

<template>
  <AppHeader />

  <main class="page-shell home-page">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="surface-card home-page__hero-skeleton"></div>
      </template>

      <template #default>
        <section v-if="hasFeaturedPost" class="home-hero">
          <div
            class="home-hero__main"
            :style="{ background: home.featuredPost.coverStyle }"
          >
            <div class="home-hero__overlay"></div>
            <div class="home-hero__badge">
              <el-icon><Compass /></el-icon>
              <span>{{ home.featuredPost.category }}</span>
            </div>
            <div class="home-hero__copy">
              <p>本周头条</p>
              <h1>{{ home.featuredPost.title }}</h1>
              <span>{{ home.featuredPost.excerpt }}</span>
              <div class="home-hero__stats">
                <strong>{{ home.featuredPost.views }} 浏览</strong>
                <strong>{{ home.featuredPost.comments }} 评论</strong>
              </div>
            </div>
          </div>

          <div class="home-hero__side">
            <article
              v-for="post in home.spotlight"
              :key="post.id"
              class="surface-card home-hero__mini"
            >
              <div class="home-hero__mini-cover" :style="{ background: post.coverStyle }">
                <span>{{ post.category }}</span>
              </div>
              <div class="home-hero__mini-body">
                <h3>{{ post.title }}</h3>
                <p>{{ post.excerpt }}</p>
              </div>
            </article>
          </div>
        </section>

        <section v-else class="surface-card home-empty-hero">
          <el-icon><Opportunity /></el-icon>
          <div>
            <strong>主页还没有发布真实文章</strong>
            <p>演示数据已经移除，等管理员从独立控制台发布首篇主站文章后，这里就会自动显示。</p>
          </div>
        </section>

        <section class="two-column">
          <div>
            <SectionHeading
              title="追更热榜"
              description="把设计稿里的主页节奏转成真实可浏览的内容流。"
            >
              <div class="chip-row">
                <span v-for="category in home.categories" :key="category.slug" class="mini-chip">
                  {{ category.name }}
                </span>
              </div>
            </SectionHeading>

            <div v-if="keyword" class="surface-card search-result">
              <el-icon><MagicStick /></el-icon>
              <span>当前搜索：{{ keyword }}，共找到 {{ filteredPosts.length }} 篇文章。</span>
            </div>

            <div v-if="filteredPosts.length" class="post-list">
              <PostCard
                v-for="(post, index) in filteredPosts"
                :key="post.id"
                :post="post"
                :variant="index === 0 ? 'wide' : 'default'"
              />
            </div>
            <section v-else class="surface-card home-empty-list">
              <el-icon><Opportunity /></el-icon>
              <strong>{{ keyword ? '没有匹配到相关文章' : '主站文章列表暂时为空' }}</strong>
              <p>{{ keyword ? '可以换个关键词再试试。' : '等管理员发布真实文章后，这里会开始滚动更新。' }}</p>
            </section>
          </div>

          <aside class="home-sidebar">
            <CreatorPanel :creator="home.creator" />

            <section class="surface-card sidebar-panel">
              <SectionHeading
                title="站内播报"
                description="适合放活动、征稿、周报。"
              >
                <el-icon class="panel-icon"><Promotion /></el-icon>
              </SectionHeading>

              <div class="notice-list">
                <article v-for="notice in home.announcements" :key="notice.title" class="notice-item">
                  <strong>{{ notice.title }}</strong>
                  <p>{{ notice.content }}</p>
                </article>
              </div>
            </section>

            <section class="surface-card sidebar-panel">
              <SectionHeading
                title="创作趋势"
                description="给博客增加一点 B 站式的数据气质。"
              >
                <el-icon class="panel-icon"><TrendCharts /></el-icon>
              </SectionHeading>

              <div class="trend-list">
                <div v-for="trend in home.trends" :key="trend.label" class="trend-item">
                  <span>{{ trend.label }}</span>
                  <strong>{{ trend.value }}</strong>
                </div>
              </div>
            </section>
          </aside>
        </section>
      </template>
    </el-skeleton>
  </main>
</template>

<style scoped>
.home-page__hero-skeleton {
  height: 420px;
}

.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.9fr);
  gap: 24px;
  margin-bottom: 28px;
}

.home-hero__main {
  position: relative;
  min-height: 420px;
  overflow: hidden;
  border-radius: 34px;
  padding: 24px;
  color: white;
  box-shadow: 0 24px 60px rgba(39, 53, 88, 0.18);
}

.home-hero__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(18, 23, 34, 0.1), rgba(18, 23, 34, 0.65));
}

.home-hero__badge,
.home-hero__copy {
  position: relative;
  z-index: 1;
}

.home-hero__badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(6px);
}

.home-hero__copy {
  display: flex;
  flex-direction: column;
  justify-content: end;
  gap: 14px;
  height: calc(100% - 56px);
  padding-top: 80px;
}

.home-hero__copy p,
.home-hero__copy span {
  margin: 0;
}

.home-hero__copy p {
  font-size: 14px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  opacity: 0.88;
}

.home-hero__copy h1 {
  margin: 0;
  max-width: 620px;
  font-family: var(--font-display);
  font-size: 46px;
  line-height: 1.1;
}

.home-hero__copy span {
  max-width: 520px;
  line-height: 1.9;
  opacity: 0.92;
}

.home-hero__stats {
  display: flex;
  gap: 16px;
}

.home-hero__stats strong {
  display: inline-flex;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 14px;
}

.home-hero__side {
  display: grid;
  gap: 18px;
}

.home-empty-hero,
.home-empty-list {
  display: grid;
  gap: 14px;
  align-items: center;
  padding: 24px;
}

.home-empty-hero {
  grid-template-columns: auto 1fr;
  margin-bottom: 28px;
}

.home-empty-hero strong,
.home-empty-list strong {
  display: block;
  font-size: 20px;
}

.home-empty-hero p,
.home-empty-list p {
  margin: 8px 0 0;
  color: var(--text-muted);
  line-height: 1.8;
}

.home-empty-hero :deep(.el-icon),
.home-empty-list :deep(.el-icon) {
  font-size: 30px;
  color: var(--brand-pink);
}

.home-hero__mini {
  padding: 16px;
}

.home-hero__mini-cover {
  display: flex;
  align-items: end;
  min-height: 160px;
  padding: 16px;
  border-radius: 22px;
  color: white;
}

.home-hero__mini-cover span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 12px;
  font-weight: 700;
}

.home-hero__mini-body {
  padding: 16px 6px 4px;
}

.home-hero__mini-body h3 {
  margin: 0;
  font-size: 21px;
  line-height: 1.35;
}

.home-hero__mini-body p {
  margin: 10px 0 0;
  color: var(--text-muted);
  line-height: 1.75;
}

.search-result {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 18px;
  margin-bottom: 18px;
}

.post-list {
  display: grid;
  gap: 18px;
}

.home-sidebar {
  display: grid;
  align-content: start;
  gap: 18px;
}

.sidebar-panel {
  padding: 20px;
}

.panel-icon {
  color: var(--brand-pink);
}

.notice-list,
.trend-list {
  display: grid;
  gap: 14px;
}

.notice-item {
  padding: 16px;
  border-radius: 20px;
  background: var(--surface-soft);
}

.notice-item strong {
  display: block;
  margin-bottom: 8px;
}

.notice-item p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.7;
}

.trend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(251, 114, 153, 0.08), rgba(90, 200, 250, 0.1));
}

.trend-item span {
  color: var(--text-muted);
}

.trend-item strong {
  font-family: var(--font-display);
  font-size: 22px;
}

@media (max-width: 1080px) {
  .home-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .home-hero__main {
    min-height: 360px;
  }

  .home-hero__copy h1 {
    font-size: 34px;
  }

  .home-hero__stats {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
