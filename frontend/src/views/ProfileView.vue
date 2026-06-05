<script setup>
import { onMounted, ref } from 'vue'
import { Collection, Connection, Location, TrendCharts } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import PostCard from '../components/PostCard.vue'
import SectionHeading from '../components/SectionHeading.vue'
import { fetchProfile } from '../api/blog'

const loading = ref(true)
const profileData = ref(null)
const activeTab = ref('posts')

async function loadProfile() {
  loading.value = true
  profileData.value = await fetchProfile()
  loading.value = false
}

onMounted(loadProfile)
</script>

<template>
  <AppHeader />

  <main class="page-shell">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="surface-card profile-skeleton"></div>
      </template>

      <template #default>
        <section class="surface-card profile-banner">
          <div class="profile-banner__cover" :style="{ background: profileData.profile.bannerStyle }">
            <div class="profile-banner__veil"></div>
          </div>

          <div class="profile-banner__content">
            <div class="profile-banner__identity">
              <div class="profile-banner__avatar">
                <img
                  v-if="profileData.profile.avatarUrl"
                  :src="profileData.profile.avatarUrl"
                  :alt="profileData.profile.name"
                />
                <span v-else>{{ profileData.profile.avatarLabel }}</span>
              </div>
              <div>
                <h1>{{ profileData.profile.name }}</h1>
                <p>{{ profileData.profile.headline }}</p>
              </div>
            </div>

            <div class="profile-banner__stats">
              <div v-for="item in profileData.profile.stats" :key="item.label">
                <strong>{{ item.value }}</strong>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </div>
        </section>

        <section class="two-column profile-layout">
          <div>
            <section class="surface-card profile-tabs">
              <el-tabs v-model="activeTab">
                <el-tab-pane label="作品总览" name="posts">
                  <SectionHeading title="精选文章" description="对应设计稿里的空间主内容区。">
                    <span class="mini-chip">最近更新 {{ profileData.profile.lastUpdated }}</span>
                  </SectionHeading>
                  <div class="post-list">
                    <PostCard
                      v-for="(post, index) in profileData.pinnedPosts"
                      :key="post.id"
                      :post="post"
                      :variant="index === 0 ? 'wide' : 'default'"
                    />
                  </div>
                </el-tab-pane>

                <el-tab-pane label="最新发布" name="latest">
                  <div class="post-list">
                    <PostCard
                      v-for="post in profileData.recentPosts"
                      :key="post.id"
                      :post="post"
                    />
                  </div>
                </el-tab-pane>
              </el-tabs>
            </section>
          </div>

          <aside class="profile-side">
            <section class="surface-card side-panel">
              <SectionHeading title="关于我" description="作为博客作者卡片非常实用。">
                <el-icon class="side-panel__icon"><Location /></el-icon>
              </SectionHeading>
              <p class="profile-bio">{{ profileData.profile.bio }}</p>
              <div class="chip-row">
                <span v-for="tag in profileData.profile.tags" :key="tag" class="mini-chip">
                  {{ tag }}
                </span>
              </div>
            </section>

            <section class="surface-card side-panel">
              <SectionHeading title="创作节点" description="可以继续扩成时间轴模块。">
                <el-icon class="side-panel__icon"><Connection /></el-icon>
              </SectionHeading>
              <div class="timeline-list">
                <article v-for="item in profileData.timeline" :key="item.title" class="timeline-item">
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.content }}</span>
                </article>
              </div>
            </section>

            <section class="surface-card side-panel">
              <SectionHeading title="收藏方向" description="更像 B 站空间里的兴趣分区。">
                <el-icon class="side-panel__icon"><Collection /></el-icon>
              </SectionHeading>
              <div class="trend-list">
                <div v-for="item in profileData.favoriteBoards" :key="item.label" class="trend-item">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </div>
              </div>
            </section>

            <section class="surface-card side-panel">
              <SectionHeading title="成长数据" description="给个人空间一些运营看板感。">
                <el-icon class="side-panel__icon"><TrendCharts /></el-icon>
              </SectionHeading>
              <div class="trend-list">
                <div v-for="item in profileData.growthStats" :key="item.label" class="trend-item">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
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
.profile-skeleton {
  height: 900px;
}

.profile-banner {
  overflow: hidden;
  margin-bottom: 24px;
}

.profile-banner__cover {
  position: relative;
  height: 280px;
}

.profile-banner__veil {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.15), rgba(15, 23, 42, 0.52));
}

.profile-banner__content {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 0 28px 28px;
  margin-top: -68px;
  position: relative;
}

.profile-banner__identity {
  display: flex;
  align-items: end;
  gap: 18px;
}

.profile-banner__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 112px;
  height: 112px;
  overflow: hidden;
  border: 6px solid white;
  border-radius: 34px;
  background: linear-gradient(135deg, #fb7299, #5ac8fa);
  color: white;
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 800;
  box-shadow: var(--soft-shadow);
}

.profile-banner__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-banner__identity h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: 36px;
}

.profile-banner__identity p {
  margin: 8px 0 0;
  color: var(--text-muted);
}

.profile-banner__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(80px, 1fr));
  gap: 12px;
  align-self: end;
}

.profile-banner__stats div {
  padding: 18px 16px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  text-align: center;
}

.profile-banner__stats strong,
.profile-banner__stats span {
  display: block;
}

.profile-banner__stats strong {
  font-size: 24px;
}

.profile-banner__stats span {
  margin-top: 6px;
  color: var(--text-muted);
  font-size: 12px;
}

.profile-tabs {
  padding: 24px;
}

.post-list {
  display: grid;
  gap: 18px;
}

.profile-side {
  display: grid;
  align-content: start;
  gap: 18px;
}

.side-panel {
  padding: 20px;
}

.side-panel__icon {
  color: var(--brand-pink);
}

.profile-bio {
  margin: 0 0 18px;
  color: var(--text-muted);
  line-height: 1.85;
}

.timeline-list,
.trend-list {
  display: grid;
  gap: 14px;
}

.timeline-item {
  padding: 16px;
  border-radius: 20px;
  background: var(--surface-soft);
}

.timeline-item strong,
.timeline-item span {
  display: block;
}

.timeline-item span {
  margin-top: 8px;
  color: var(--text-muted);
  line-height: 1.7;
}

.trend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(90, 200, 250, 0.08), rgba(251, 114, 153, 0.1));
}

.trend-item span {
  color: var(--text-muted);
}

.trend-item strong {
  font-family: var(--font-display);
  font-size: 22px;
}

@media (max-width: 1080px) {
  .profile-banner__content {
    flex-direction: column;
  }

  .profile-banner__stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 720px) {
  .profile-banner__identity {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-banner__avatar {
    width: 88px;
    height: 88px;
    border-radius: 28px;
    font-size: 32px;
  }

  .profile-banner__stats {
    grid-template-columns: 1fr 1fr;
  }

  .profile-tabs {
    padding: 18px;
  }
}
</style>
