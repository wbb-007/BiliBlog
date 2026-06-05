<script setup>
import { computed } from 'vue'
import { ChatDotRound, Clock, View } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  post: {
    type: Object,
    required: true,
  },
  variant: {
    type: String,
    default: 'default',
  },
})

const router = useRouter()

const cardClass = computed(() => `post-card post-card--${props.variant}`)

function openPost() {
  router.push({ name: 'post-detail', params: { id: props.post.id } })
}
</script>

<template>
  <article :class="cardClass" @click="openPost">
    <div class="post-card__cover" :style="{ background: post.coverStyle }">
      <div class="post-card__glow"></div>
      <div class="post-card__chips">
        <span class="post-card__category">{{ post.category }}</span>
        <span v-if="post.board" class="post-card__board">{{ post.board }}</span>
      </div>
      <div class="post-card__cover-copy">
        <span>{{ post.readTime }}</span>
        <strong>{{ post.coverLabel }}</strong>
      </div>
    </div>

    <div class="post-card__content">
      <div class="post-card__tags">
        <span v-for="tag in post.tags" :key="tag">{{ tag }}</span>
      </div>

      <h3>{{ post.title }}</h3>
      <p>{{ post.excerpt }}</p>

      <div class="post-card__meta">
        <div class="post-card__author">
          <span class="post-card__author-badge">
            <img v-if="post.authorAvatarUrl" :src="post.authorAvatarUrl" :alt="post.author" />
            <span v-else>{{ post.authorInitial }}</span>
          </span>
          <div>
            <strong>{{ post.author }}</strong>
            <span>{{ post.publishedAt }}</span>
          </div>
        </div>

        <div class="post-card__stats">
          <span><el-icon><View /></el-icon>{{ post.views }}</span>
          <span><el-icon><ChatDotRound /></el-icon>{{ post.comments }}</span>
          <span><el-icon><Clock /></el-icon>{{ post.readTime }}</span>
        </div>
      </div>
    </div>
  </article>
</template>

<style scoped>
.post-card {
  display: grid;
  gap: 14px;
  padding: 12px;
  border-radius: 20px;
  background: var(--surface-container-lowest);
  box-shadow: var(--soft-shadow);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.post-card--wide {
  grid-template-columns: minmax(220px, 320px) 1fr;
}

.post-card__cover {
  position: relative;
  min-height: 190px;
  overflow: hidden;
  border-radius: 12px;
  padding: 18px;
  color: white;
}

.post-card__glow {
  position: absolute;
  inset: auto -18px -48px auto;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
}

.post-card__category {
  display: inline-flex;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 12px;
  font-weight: 700;
}

.post-card__chips {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.post-card__board {
  display: inline-flex;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(18, 23, 34, 0.2);
  font-size: 12px;
  font-weight: 700;
  backdrop-filter: blur(6px);
}

.post-card__cover-copy {
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 18px;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.post-card__cover-copy span {
  font-size: 12px;
  opacity: 0.88;
}

.post-card__cover-copy strong {
  max-width: 220px;
  font-family: var(--font-display);
  font-size: 20px;
  line-height: 1.25;
}

.post-card__content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.post-card__tags span {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(90, 200, 250, 0.12);
  color: var(--brand-cyan-deep);
  font-size: 12px;
  font-weight: 700;
}

h3 {
  margin: 0;
  font-family: var(--font-display);
  font-size: 20px;
  line-height: 1.3;
  transition: color 0.18s ease;
}

.post-card:hover h3 {
  color: var(--brand-pink-deep);
}

p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.75;
}

.post-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: auto;
  padding-top: 14px;
  border-top: 1px solid var(--surface-variant);
}

.post-card__author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-card__author-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  overflow: hidden;
  border-radius: 50%;
  background: linear-gradient(135deg, #fb7299, #5ac8fa);
  color: white;
  font-weight: 800;
}

.post-card__author-badge img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-card__author strong,
.post-card__author span {
  display: block;
}

.post-card__author span {
  color: var(--text-muted);
  font-size: 12px;
}

.post-card__stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: var(--text-muted);
  font-size: 13px;
}

.post-card__stats span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 900px) {
  .post-card--wide {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .post-card__meta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
