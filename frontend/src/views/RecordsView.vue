<script setup>
import { computed, onMounted, ref } from 'vue'
import { Calendar, CollectionTag, Opportunity, TrendCharts } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import PostCard from '../components/PostCard.vue'
import SectionHeading from '../components/SectionHeading.vue'
import { fetchHome } from '../api/blog'

const loading = ref(true)
const home = ref(null)

const posts = computed(() => home.value?.posts ?? [])
const categories = computed(() => home.value?.categories ?? [])
const trends = computed(() => home.value?.trends ?? [])

async function loadRecords() {
  loading.value = true
  home.value = await fetchHome()
  loading.value = false
}

onMounted(loadRecords)
</script>

<template>
  <AppHeader />

  <main class="page-shell records-page">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="surface-card records-skeleton"></div>
      </template>

      <template #default>
        <section class="records-hero">
          <div>
            <span class="records-eyebrow">BiliBlog Records</span>
            <h1>记录</h1>
            <p>把文章、教程和日常更新按时间摊开，保留 Poetize 的生活感，但仍然是 B 站风的内容流。</p>
          </div>

          <div class="records-hero__stats">
            <article>
              <strong>{{ posts.length }}</strong>
              <span>篇记录</span>
            </article>
            <article>
              <strong>{{ categories.length }}</strong>
              <span>个分区</span>
            </article>
            <article>
              <strong>{{ home?.latestComments?.length ?? 0 }}</strong>
              <span>条互动</span>
            </article>
          </div>
        </section>

        <section class="two-column records-layout">
          <div>
            <SectionHeading
              title="时间线"
              description="最新内容优先展示，适合检查文章卡片和封面效果。"
            >
              <el-icon class="records-icon"><Calendar /></el-icon>
            </SectionHeading>

            <div v-if="posts.length" class="records-list">
              <PostCard
                v-for="(post, index) in posts"
                :key="post.id"
                :post="post"
                :variant="index === 0 ? 'wide' : 'default'"
              />
            </div>

            <section v-else class="surface-card records-empty">
              <el-icon><Opportunity /></el-icon>
              <strong>记录暂时为空</strong>
              <p>后台发布文章后，这里会自动成为记录页的内容流。</p>
            </section>
          </div>

          <aside class="records-side">
            <section class="surface-card records-panel">
              <SectionHeading title="内容分区" description="沿用 B 站式分区标签。">
                <el-icon class="records-icon"><CollectionTag /></el-icon>
              </SectionHeading>
              <div class="chip-row">
                <span v-for="category in categories" :key="category.slug" class="mini-chip">
                  {{ category.name }}
                </span>
              </div>
            </section>

            <section class="surface-card records-panel">
              <SectionHeading title="创作趋势" description="让记录页也有一点数据看板感。">
                <el-icon class="records-icon"><TrendCharts /></el-icon>
              </SectionHeading>
              <div class="records-trends">
                <div v-for="trend in trends" :key="trend.label" class="records-trend">
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
.records-skeleton {
  height: 760px;
}

.records-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 420px);
  gap: 22px;
  align-items: end;
  margin-bottom: 24px;
  padding: 34px;
  overflow: hidden;
  border-radius: 30px;
  background:
    linear-gradient(135deg, rgba(251, 114, 153, 0.14), rgba(90, 200, 250, 0.16)),
    #ffffff;
  box-shadow: var(--soft-shadow);
}

.records-eyebrow {
  display: inline-flex;
  width: fit-content;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(251, 114, 153, 0.12);
  color: var(--brand-pink-deep);
  font-size: 12px;
  font-weight: 800;
}

.records-hero h1 {
  margin: 14px 0 10px;
  font-family: var(--font-display);
  font-size: 48px;
  line-height: 1.1;
}

.records-hero p {
  max-width: 620px;
  margin: 0;
  color: var(--text-muted);
  line-height: 1.8;
}

.records-hero__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.records-hero__stats article,
.records-panel,
.records-empty {
  padding: 20px;
}

.records-hero__stats article {
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.78);
}

.records-hero__stats strong {
  display: block;
  font-family: var(--font-display);
  font-size: 28px;
  color: var(--brand-pink);
}

.records-hero__stats span,
.records-trend span {
  color: var(--text-muted);
  font-size: 13px;
}

.records-list,
.records-side,
.records-trends {
  display: grid;
  gap: 18px;
}

.records-side {
  align-content: start;
}

.records-icon {
  color: var(--brand-pink);
}

.records-trend {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px;
  border-radius: 18px;
  background: var(--surface-soft);
}

.records-trend strong {
  color: var(--brand-cyan-deep);
}

.records-empty {
  display: grid;
  gap: 10px;
}

.records-empty :deep(.el-icon) {
  color: var(--brand-pink);
  font-size: 30px;
}

.records-empty p {
  margin: 0;
  color: var(--text-muted);
}

@media (max-width: 900px) {
  .records-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .records-hero {
    padding: 24px;
  }

  .records-hero h1 {
    font-size: 38px;
  }

  .records-hero__stats {
    grid-template-columns: 1fr;
  }
}
</style>
