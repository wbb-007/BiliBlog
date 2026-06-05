<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ChatDotRound,
  Clock,
  Compass,
  Headset,
  MagicStick,
  Monitor,
  Opportunity,
  Promotion,
  Refresh,
  Star,
  VideoPlay,
  View,
} from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import { fetchHome } from '../api/blog'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const home = ref(null)
const keyword = computed(() => route.query.keyword?.toString().trim() ?? '')
const hasFeaturedPost = computed(() => Boolean(home.value?.featuredPost))
const creator = computed(() => home.value?.creator ?? {})
const searchablePosts = computed(() => {
  if (!home.value) {
    return []
  }

  const seen = new Set()
  return [home.value.featuredPost, ...(home.value.spotlight ?? []), ...(home.value.posts ?? [])].filter((post) => {
    if (!post?.id || seen.has(post.id)) {
      return false
    }

    seen.add(post.id)
    return true
  })
})
const tagSphereItems = computed(() => {
  const tags = home.value?.tagCloud?.slice(0, 20) ?? []
  const total = Math.max(tags.length, 1)

  return tags.map((tag, index) => {
    const theta = Math.acos(-1 + (2 * index + 1) / total)
    const phi = Math.sqrt(total * Math.PI) * theta
    const x = Math.cos(phi) * Math.sin(theta)
    const y = Math.sin(phi) * Math.sin(theta)
    const z = Math.cos(theta)

    return {
      ...tag,
      x,
      y,
      z,
      color: index % 6,
      scale: 0.72 + (z + 1) * 0.18,
    }
  })
})

const sideNavItems = [
  { label: '主页', icon: Compass, route: 'home' },
  { label: '相册', icon: Monitor, route: 'album' },
  { label: '关于', icon: Headset, route: 'about-me' },
]

const filteredPosts = computed(() => {
  if (!home.value) {
    return []
  }

  if (!keyword.value) {
    return searchablePosts.value
  }

  const lowerKeyword = keyword.value.toLowerCase()

  return searchablePosts.value.filter((post) => {
    const pool = [
      post.title,
      post.excerpt,
      post.category,
      post.board,
      post.author,
      post.coverLabel,
      ...(post.tags ?? []),
    ]
      .filter(Boolean)
      .join(' ')
    return pool.toLowerCase().includes(lowerKeyword)
  })
})

const boardPosts = computed(() => filteredPosts.value.slice(0, 8))

async function loadHome() {
  loading.value = true
  home.value = await fetchHome()
  loading.value = false
}

function openPost(post) {
  if (!post?.id) {
    return
  }

  router.push({ name: 'post-detail', params: { id: post.id } })
}

function openTag(tag) {
  if (!tag?.name) {
    return
  }

  router.push({ name: 'home', query: { keyword: tag.name } })
}

function go(name) {
  router.push({ name })
}

onMounted(loadHome)
</script>

<template>
  <AppHeader />

  <div class="bili-home">
    <aside class="creator-rail">
      <section class="creator-rail__profile">
        <div class="creator-rail__avatar">
          <img v-if="creator.avatarUrl" :src="creator.avatarUrl" :alt="creator.name" />
          <span v-else>{{ creator.avatarLabel || 'B' }}</span>
        </div>
        <div>
          <h2>{{ creator.name || 'Creator Studio' }}</h2>
          <p>{{ creator.title || 'Level 4 Creator' }}</p>
        </div>
      </section>

      <nav class="creator-rail__nav" aria-label="首页侧边导航">
        <button
          v-for="item in sideNavItems"
          :key="item.label"
          class="rail-link"
          :class="{ 'rail-link--active': route.name === item.route }"
          type="button"
          @click="go(item.route)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <section class="creator-rail__stats">
        <div>
          <strong>{{ creator.followers || 0 }}</strong>
          <span>粉丝</span>
        </div>
        <div>
          <strong>{{ creator.likes || 0 }}</strong>
          <span>获赞</span>
        </div>
        <div>
          <strong>{{ creator.posts || 0 }}</strong>
          <span>文章</span>
        </div>
      </section>

      <section v-if="tagSphereItems.length" class="poetize-left-card tag-cloud-left">
        <div class="poetize-left-card__head">
          <span>标签</span>
          <strong>Tags</strong>
        </div>
        <div class="tag-sphere" aria-label="标签星球">
          <div class="tag-sphere__globe"></div>
          <div class="tag-sphere__orbit">
            <button
              v-for="tag in tagSphereItems"
              :key="tag.name"
              :class="`poetize-tag poetize-tag--${tag.color + 1}`"
              :style="{
                '--x': tag.x,
                '--y': tag.y,
                '--z': tag.z,
                '--scale': tag.scale,
              }"
              type="button"
              @click="openTag(tag)"
            >
              <span>{{ tag.name }}</span>
              <em>{{ tag.count }}</em>
            </button>
          </div>
        </div>
      </section>

      <section class="poetize-left-card left-notice">
        <div class="poetize-left-card__head">
          <span>站内播报</span>
          <strong>Notice</strong>
        </div>
        <div v-if="home?.announcements?.length" class="notice-list">
          <div v-for="notice in home.announcements" :key="notice.title">
            <strong>{{ notice.title }}</strong>
            <p>{{ notice.content }}</p>
          </div>
        </div>
        <p v-else class="panel-empty">暂无站内播报。</p>
      </section>

      <section v-if="home?.latestComments?.length" class="poetize-left-card left-barrage">
        <div class="poetize-left-card__head">
          <span>弹幕墙</span>
          <strong>Barrage</strong>
        </div>
        <div class="left-barrage__stage" aria-label="评论弹幕墙">
          <button
            v-for="(comment, index) in home.latestComments.slice(0, 8)"
            :key="comment.id"
            class="barrage-message"
            :style="{
              '--delay': `${index * -1.85}s`,
              '--row': index % 5,
              '--speed': `${17 + (index % 4) * 2.5}s`,
            }"
            type="button"
            @click="openPost({ id: comment.postId })"
          >
            <span>
              <img v-if="comment.authorAvatarUrl" :src="comment.authorAvatarUrl" :alt="comment.author" />
              <i v-else>{{ comment.authorInitial }}</i>
            </span>
            <strong>{{ comment.author }}</strong>
            <em>{{ comment.content }}</em>
          </button>
        </div>
      </section>
    </aside>

    <main class="bili-main">
      <el-skeleton :loading="loading" animated>
        <template #template>
          <div class="home-skeleton"></div>
        </template>

        <template #default>
          <section v-if="hasFeaturedPost" class="feature-grid">
            <article
              class="feature-card feature-card--large"
              :style="{ background: home.featuredPost.coverStyle }"
              role="button"
              tabindex="0"
              @click="openPost(home.featuredPost)"
              @keyup.enter="openPost(home.featuredPost)"
            >
              <div class="feature-card__shade"></div>
              <div class="feature-card__body">
                <span class="feature-pill">
                  <el-icon><Star /></el-icon>
                  Featured
                </span>
                <h1>{{ home.featuredPost.title }}</h1>
                <p>{{ home.featuredPost.excerpt }}</p>
                <div class="feature-meta">
                  <span><el-icon><View /></el-icon>{{ home.featuredPost.views }} 浏览</span>
                  <span><el-icon><ChatDotRound /></el-icon>{{ home.featuredPost.comments }} 评论</span>
                </div>
              </div>
            </article>

            <div class="feature-side">
              <article
                v-for="post in home.spotlight"
                :key="post.id"
                class="feature-card feature-card--small"
                :style="{ background: post.coverStyle }"
                role="button"
                tabindex="0"
                @click="openPost(post)"
                @keyup.enter="openPost(post)"
              >
                <div class="feature-card__shade"></div>
                <div class="feature-card__body">
                  <span>{{ post.category }}</span>
                  <h2>{{ post.title }}</h2>
                </div>
              </article>
            </div>
          </section>

          <section v-else class="empty-hero">
            <el-icon><Opportunity /></el-icon>
            <div>
              <strong>主页还没有发布真实文章</strong>
              <p>等管理员从独立控制台发布首篇主站文章后，这里就会自动显示。</p>
            </div>
          </section>

          <section v-if="keyword" class="search-result">
            <el-icon><MagicStick /></el-icon>
            <span>当前搜索：{{ keyword }}，共找到 {{ filteredPosts.length }} 篇文章。</span>
          </section>

          <section class="board-header">
            <h2>
              <el-icon><Promotion /></el-icon>
              Trending Now
            </h2>
            <button type="button" @click="loadHome">
              <el-icon><Refresh /></el-icon>
              Refresh
            </button>
          </section>

          <section v-if="boardPosts.length" class="video-grid">
            <article
              v-for="post in boardPosts"
              :key="post.id"
              class="video-card"
              role="button"
              tabindex="0"
              @click="openPost(post)"
              @keyup.enter="openPost(post)"
            >
              <div class="video-card__cover" :style="{ background: post.coverStyle }">
                <span>{{ post.readTime }}</span>
              </div>
              <div class="video-card__body">
                <h3>{{ post.title }}</h3>
                <div class="video-card__author">
                  <span class="author-badge">
                    <img v-if="post.authorAvatarUrl" :src="post.authorAvatarUrl" :alt="post.author" />
                    <span v-else>{{ post.authorInitial }}</span>
                  </span>
                  <span>{{ post.author }}</span>
                </div>
                <div class="video-card__meta">
                  <span><el-icon><VideoPlay /></el-icon>{{ post.views }}</span>
                  <span><el-icon><ChatDotRound /></el-icon>{{ post.comments }}</span>
                  <span><el-icon><Clock /></el-icon>{{ post.publishedAt }}</span>
                </div>
              </div>
            </article>
          </section>

          <section v-else class="empty-list">
            <el-icon><Opportunity /></el-icon>
            <strong>{{ keyword ? '没有匹配到相关文章' : '主站文章列表暂时为空' }}</strong>
            <p>{{ keyword ? '可以换个关键词再试试。' : '等管理员发布真实文章后，这里会开始滚动更新。' }}</p>
          </section>
        </template>
      </el-skeleton>
    </main>
  </div>
</template>

<style scoped>
.bili-home {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 24px;
  max-width: 1440px;
  margin: 0 auto;
  padding: 20px 24px 64px;
}

.creator-rail {
  position: sticky;
  top: 82px;
  display: flex;
  flex-direction: column;
  align-self: start;
  gap: 16px;
  height: calc(100vh - 104px);
  overflow-y: auto;
  padding: 16px;
}

.creator-rail__profile {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.creator-rail__avatar,
.author-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--brand-pink), var(--brand-cyan));
  color: white;
  font-weight: 900;
}

.creator-rail__avatar {
  flex: 0 0 auto;
  width: 48px;
  height: 48px;
  border: 2px solid rgba(251, 114, 153, 0.32);
  font-family: var(--font-display);
}

.creator-rail__avatar img,
.author-badge img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.creator-rail__profile h2,
.creator-rail__profile p {
  margin: 0;
}

.creator-rail__profile h2 {
  overflow: hidden;
  color: var(--brand-pink-deep);
  font-family: var(--font-display);
  font-size: 20px;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.creator-rail__profile p {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 12px;
}

.creator-rail__nav {
  display: grid;
  gap: 8px;
}

.rail-link,
.poetize-tag,
.board-header button,
.barrage-message {
  border: 0;
  cursor: pointer;
}

.rail-link {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  border-radius: 12px;
  padding: 0 14px;
  background: transparent;
  color: var(--text-muted);
  font-weight: 800;
  text-align: left;
  transition: background 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.rail-link:hover,
.rail-link--active {
  background: rgba(251, 114, 153, 0.18);
  color: var(--brand-pink-deep);
}

.rail-link:hover {
  transform: translateX(2px);
}

.creator-rail__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.creator-rail__stats div {
  border-radius: 12px;
  padding: 12px 8px;
  background: var(--surface-container-lowest);
  box-shadow: var(--soft-shadow);
  text-align: center;
}

.creator-rail__stats strong,
.creator-rail__stats span {
  display: block;
}

.creator-rail__stats strong {
  font-size: 16px;
}

.creator-rail__stats span {
  margin-top: 3px;
  color: var(--text-muted);
  font-size: 12px;
}

.bili-main {
  min-width: 0;
}

.poetize-left-card {
  position: relative;
  overflow: hidden;
  border-radius: 18px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 8px 22px rgba(39, 53, 88, 0.08);
}

.poetize-left-card::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background:
    radial-gradient(circle at 14% 16%, rgba(251, 114, 153, 0.11), transparent 24%),
    radial-gradient(circle at 82% 74%, rgba(90, 200, 250, 0.12), transparent 26%);
}

.poetize-left-card__head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.poetize-left-card__head span {
  color: var(--text-primary);
  font-weight: 900;
}

.poetize-left-card__head strong {
  color: var(--text-muted);
  font-size: 11px;
  letter-spacing: 0;
}

.tag-sphere {
  position: relative;
  z-index: 1;
  height: 236px;
  overflow: hidden;
  border-radius: 16px;
  perspective: 680px;
  background:
    radial-gradient(circle at 50% 50%, rgba(57, 197, 187, 0.12), transparent 45%),
    linear-gradient(180deg, rgba(247, 251, 255, 0.74), rgba(255, 246, 250, 0.82));
}

.tag-sphere__globe {
  position: absolute;
  inset: 32px;
  border: 1px dashed rgba(57, 197, 187, 0.28);
  border-radius: 50%;
  background:
    linear-gradient(90deg, transparent 49%, rgba(57, 197, 187, 0.2) 50%, transparent 51%),
    linear-gradient(0deg, transparent 49%, rgba(251, 114, 153, 0.18) 50%, transparent 51%);
  box-shadow:
    inset 0 0 30px rgba(90, 200, 250, 0.12),
    0 12px 28px rgba(39, 53, 88, 0.08);
}

.tag-sphere__globe::before,
.tag-sphere__globe::after {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(57, 197, 187, 0.14);
  border-radius: 50%;
  content: '';
}

.tag-sphere__globe::after {
  inset: 42px 18px;
}

.tag-sphere__orbit {
  position: absolute;
  inset: 0;
  transform-style: preserve-3d;
  animation: tag-sphere-spin 22s linear infinite;
}

.poetize-tag {
  position: absolute;
  top: 50%;
  left: 50%;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 112px;
  border-radius: 999px;
  padding: 7px 10px;
  color: #fff;
  font-size: 12px;
  font-weight: 900;
  white-space: nowrap;
  box-shadow: 0 8px 18px rgba(39, 53, 88, 0.08);
  transform:
    translate3d(
      calc(var(--x) * 82px - 50%),
      calc(var(--y) * 78px - 50%),
      calc(var(--z) * 74px)
    )
    scale(var(--scale));
  transform-style: preserve-3d;
  transition: filter 0.18s ease, opacity 0.18s ease;
}

.poetize-tag:hover {
  filter: saturate(1.08);
}

.tag-sphere:hover .tag-sphere__orbit {
  animation-play-state: paused;
}

.poetize-tag span {
  overflow: hidden;
  text-overflow: ellipsis;
}

.poetize-tag em {
  font-style: normal;
  opacity: 0.78;
}

.poetize-tag--1 {
  background: #ff6b9a;
}

.poetize-tag--2 {
  background: #39c5bb;
}

.poetize-tag--3 {
  background: #7c83fd;
}

.poetize-tag--4 {
  background: #ffb347;
}

.poetize-tag--5 {
  background: #60a5fa;
}

.poetize-tag--6 {
  background: #2fc89f;
}

.left-notice .notice-list {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 10px;
  margin-top: 0;
}

.left-notice .notice-list div {
  border-radius: 12px;
  padding: 12px;
  background: rgba(247, 249, 254, 0.82);
}

.left-notice .notice-list strong {
  display: block;
  margin-bottom: 5px;
  color: var(--text-primary);
  font-size: 13px;
}

.left-notice .notice-list p,
.panel-empty {
  position: relative;
  z-index: 1;
  margin: 0;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.65;
}

.left-barrage__stage {
  position: relative;
  z-index: 1;
  height: 178px;
  overflow: hidden;
  border-radius: 14px;
  background:
    linear-gradient(180deg, rgba(247, 251, 255, 0.72), rgba(255, 246, 250, 0.82)),
    repeating-linear-gradient(0deg, transparent 0 34px, rgba(57, 197, 187, 0.08) 34px 35px);
}

.left-barrage__stage::before,
.left-barrage__stage::after {
  position: absolute;
  top: 0;
  bottom: 0;
  z-index: 2;
  width: 32px;
  pointer-events: none;
  content: '';
}

.left-barrage__stage::before {
  left: 0;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.92), transparent);
}

.left-barrage__stage::after {
  right: 0;
  background: linear-gradient(270deg, rgba(255, 255, 255, 0.92), transparent);
}

.barrage-message {
  position: absolute;
  top: calc(9px + var(--row) * 32px);
  left: 100%;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  max-width: 250px;
  border-radius: 999px;
  padding: 6px 11px 6px 6px;
  background: rgba(255, 255, 255, 0.93);
  color: var(--text-primary);
  box-shadow: 0 8px 18px rgba(39, 53, 88, 0.09);
  text-align: left;
  animation: left-barrage-fly var(--speed) linear infinite;
  animation-delay: var(--delay);
}

.barrage-message:hover {
  background: rgba(251, 114, 153, 0.12);
  animation-play-state: paused;
}

.barrage-message span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 24px;
  height: 24px;
  overflow: hidden;
  border-radius: 50%;
  background: linear-gradient(135deg, #fb7299, #5ac8fa);
  color: white;
  font-size: 11px;
  font-weight: 900;
}

.barrage-message img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.barrage-message i {
  font-style: normal;
}

.barrage-message strong {
  flex: 0 0 auto;
  max-width: 54px;
  overflow: hidden;
  color: var(--brand-pink-deep);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.barrage-message em {
  overflow: hidden;
  max-width: 120px;
  color: var(--text-muted);
  font-size: 12px;
  font-style: normal;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@keyframes left-barrage-fly {
  from {
    transform: translateX(0);
  }

  to {
    transform: translateX(-530px);
  }
}

@keyframes tag-sphere-spin {
  from {
    transform: rotateY(0deg) rotateX(8deg);
  }

  to {
    transform: rotateY(360deg) rotateX(8deg);
  }
}

.home-skeleton {
  height: 430px;
  border-radius: 20px;
  background: var(--surface-container-lowest);
  box-shadow: var(--soft-shadow);
}

.feature-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.feature-side {
  display: grid;
  gap: 20px;
}

.feature-card {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  background-size: 180% 180%;
  color: white;
  cursor: pointer;
  box-shadow: var(--soft-shadow);
  transition: transform 0.28s ease, box-shadow 0.28s ease;
  animation: bili-gradient-flow 14s ease-in-out infinite;
}

.feature-card:hover,
.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 34px rgba(0, 0, 0, 0.1);
}

.feature-card--large {
  min-height: 430px;
}

.feature-card--small {
  min-height: 205px;
}

.feature-card__shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(18, 23, 34, 0.04), rgba(18, 23, 34, 0.78));
}

.feature-card__body {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  min-height: inherit;
  padding: 24px;
}

.feature-pill,
.feature-meta,
.video-card__meta,
.video-card__author {
  display: flex;
  align-items: center;
}

.feature-pill {
  align-self: flex-start;
  gap: 6px;
  border-radius: 999px;
  padding: 7px 12px;
  background: rgba(251, 114, 153, 0.88);
  font-size: 12px;
  font-weight: 900;
}

.feature-card h1,
.feature-card h2,
.board-header h2,
.video-card h3,
.mini-panel h3 {
  margin: 0;
  font-family: var(--font-display);
}

.feature-card h1 {
  max-width: 760px;
  margin-top: 14px;
  font-size: 36px;
  line-height: 1.18;
}

.feature-card h2 {
  display: -webkit-box;
  margin-top: 8px;
  overflow: hidden;
  font-size: 22px;
  line-height: 1.28;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.feature-card p {
  display: -webkit-box;
  max-width: 620px;
  margin: 12px 0 0;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.84);
  line-height: 1.8;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.feature-card--small .feature-card__body > span {
  align-self: flex-start;
  border-radius: 999px;
  padding: 6px 10px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 12px;
  font-weight: 900;
}

.feature-meta {
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 16px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
  font-weight: 800;
}

.feature-meta span,
.video-card__meta span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.search-result,
.empty-hero,
.empty-list {
  border-radius: 20px;
  background: var(--surface-container-lowest);
  box-shadow: var(--soft-shadow);
}

.search-result {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 16px 18px;
  color: var(--brand-pink-deep);
  font-weight: 800;
}

.board-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.board-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 30px;
}

.board-header h2 :deep(.el-icon) {
  color: var(--brand-pink);
}

.board-header button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: 999px;
  padding: 9px 12px;
  background: transparent;
  color: var(--text-muted);
  font-weight: 800;
}

.board-header button:hover {
  color: var(--brand-pink-deep);
  background: var(--surface-container-low);
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.video-card {
  overflow: hidden;
  border-radius: 20px;
  background: var(--surface-container-lowest);
  box-shadow: var(--soft-shadow);
  cursor: pointer;
  transition: transform 0.24s ease, box-shadow 0.24s ease;
}

.video-card__cover {
  position: relative;
  min-height: 0;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background-size: 180% 180%;
  animation: bili-gradient-flow 14s ease-in-out infinite;
}

.video-card__cover::after {
  position: absolute;
  inset: 0;
  content: '';
  background:
    radial-gradient(circle at 18% 24%, rgba(255, 255, 255, 0.3) 0 12px, transparent 13px),
    linear-gradient(180deg, transparent, rgba(18, 23, 34, 0.28));
}

.video-card__cover span {
  position: absolute;
  right: 10px;
  bottom: 10px;
  z-index: 1;
  border-radius: 6px;
  padding: 4px 7px;
  background: rgba(0, 0, 0, 0.62);
  color: white;
  font-size: 11px;
  font-weight: 800;
}

.video-card__body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 13px;
}

.video-card h3 {
  display: -webkit-box;
  min-height: 48px;
  overflow: hidden;
  font-size: 18px;
  line-height: 1.35;
  transition: color 0.18s ease;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.video-card:hover h3 {
  color: var(--brand-pink-deep);
}

.video-card__author {
  gap: 8px;
  min-width: 0;
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 700;
}

.author-badge {
  flex: 0 0 auto;
  width: 26px;
  height: 26px;
  font-size: 12px;
}

.video-card__author > span:last-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-card__meta {
  flex-wrap: wrap;
  gap: 10px;
  color: var(--text-muted);
  font-size: 12px;
}

.notice-list p,
.empty-hero p,
.empty-list p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.75;
}

.empty-hero,
.empty-list {
  display: grid;
  gap: 12px;
  padding: 24px;
}

.empty-hero {
  grid-template-columns: auto 1fr;
  align-items: center;
  margin-bottom: 28px;
}

.empty-list {
  place-items: center;
  text-align: center;
}

.empty-hero :deep(.el-icon),
.empty-list :deep(.el-icon) {
  color: var(--brand-pink);
  font-size: 30px;
}

@media (max-width: 1200px) {
  .bili-home {
    grid-template-columns: 1fr;
  }

  .creator-rail {
    position: static;
    height: auto;
    border-radius: 20px;
    background: var(--surface-container-lowest);
    box-shadow: var(--soft-shadow);
  }

  .creator-rail__nav {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .rail-link {
    justify-content: center;
  }

  .creator-rail__stats,
  .poetize-left-card {
    display: none;
  }
}

@media (max-width: 980px) {
  .feature-grid {
    grid-template-columns: 1fr;
  }

  .feature-side {
    grid-template-columns: 1fr 1fr;
  }

  .video-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .bili-home {
    padding: 14px 12px 48px;
  }

  .creator-rail__nav {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .rail-link {
    padding: 0 10px;
  }

  .feature-side,
  .video-grid {
    grid-template-columns: 1fr;
  }

  .feature-card--large {
    min-height: 360px;
  }

  .feature-card h1 {
    font-size: 28px;
  }

  .board-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .board-header h2 {
    font-size: 25px;
  }
}
</style>
