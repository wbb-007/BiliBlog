<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, MagicStick, Opportunity } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import SectionHeading from '../components/SectionHeading.vue'
import { fetchGarden } from '../api/blog'

const router = useRouter()
const loading = ref(true)
const garden = ref(null)

const barrage = computed(() => garden.value?.barrage ?? [])

async function loadBarrage() {
  loading.value = true
  garden.value = await fetchGarden()
  loading.value = false
}

function openComment(comment) {
  if (!comment?.postId) {
    return
  }

  router.push({ name: 'post-detail', params: { id: comment.postId } })
}

onMounted(loadBarrage)
</script>

<template>
  <AppHeader />

  <main class="page-shell barrage-page">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="surface-card barrage-skeleton"></div>
      </template>

      <template #default>
        <section class="barrage-hero">
          <div class="barrage-hero__copy">
            <span class="barrage-eyebrow">Danmaku Wall</span>
            <h1>弹幕墙</h1>
            <p>把读者评论做成独立弹幕页，保留 Poetize 的流动氛围，外观继续走 BiliBlog 的粉蓝胶囊风。</p>
          </div>
          <div class="barrage-hero__badge">
            <el-icon><ChatDotRound /></el-icon>
            <strong>{{ barrage.length }}</strong>
            <span>条正在漂浮的评论</span>
          </div>
        </section>

        <section class="surface-card barrage-panel">
          <SectionHeading
            title="全屏弹幕"
            description="点击一条弹幕可以回到对应文章。"
          >
            <el-icon class="barrage-icon"><MagicStick /></el-icon>
          </SectionHeading>

          <div v-if="barrage.length" class="barrage-stage">
            <button
              v-for="(comment, index) in barrage"
              :key="comment.id"
              class="barrage-item"
              :style="{
                '--delay': `${index * -1.4}s`,
                '--row': index % 8,
                '--speed': `${22 + (index % 5) * 4}s`,
              }"
              type="button"
              @click="openComment(comment)"
            >
              <span class="barrage-avatar">
                <img
                  v-if="comment.authorAvatarUrl"
                  :src="comment.authorAvatarUrl"
                  :alt="comment.author"
                />
                <span v-else>{{ comment.authorInitial }}</span>
              </span>
              <strong>{{ comment.author }}</strong>
              <em>{{ comment.content }}</em>
            </button>
          </div>

          <section v-else class="barrage-empty">
            <el-icon><Opportunity /></el-icon>
            <strong>弹幕墙还没有评论</strong>
            <p>文章评论出现后，这里会自动生成滚动弹幕。</p>
          </section>
        </section>
      </template>
    </el-skeleton>
  </main>
</template>

<style scoped>
.barrage-skeleton {
  height: 680px;
}

.barrage-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 22px;
  align-items: center;
  margin-bottom: 24px;
  padding: 34px;
  border-radius: 30px;
  background:
    linear-gradient(135deg, rgba(90, 200, 250, 0.16), rgba(251, 114, 153, 0.16)),
    #ffffff;
  box-shadow: var(--soft-shadow);
}

.barrage-eyebrow {
  display: inline-flex;
  width: fit-content;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(90, 200, 250, 0.14);
  color: var(--brand-cyan-deep);
  font-size: 12px;
  font-weight: 800;
}

.barrage-hero h1 {
  margin: 14px 0 10px;
  font-family: var(--font-display);
  font-size: 48px;
  line-height: 1.1;
}

.barrage-hero p {
  max-width: 640px;
  margin: 0;
  color: var(--text-muted);
  line-height: 1.8;
}

.barrage-hero__badge {
  display: grid;
  min-width: 190px;
  gap: 8px;
  justify-items: center;
  padding: 24px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.8);
}

.barrage-hero__badge :deep(.el-icon) {
  color: var(--brand-pink);
  font-size: 32px;
}

.barrage-hero__badge strong {
  font-family: var(--font-display);
  font-size: 34px;
  color: var(--brand-pink);
}

.barrage-hero__badge span {
  color: var(--text-muted);
  font-size: 13px;
}

.barrage-panel {
  padding: 24px;
}

.barrage-icon {
  color: var(--brand-pink);
}

.barrage-stage {
  position: relative;
  height: 470px;
  overflow: hidden;
  border-radius: 26px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.72), rgba(255, 245, 249, 0.86)),
    repeating-linear-gradient(0deg, transparent 0 52px, rgba(90, 200, 250, 0.08) 52px 53px);
}

.barrage-stage::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background:
    radial-gradient(circle at 16% 24%, rgba(251, 114, 153, 0.14), transparent 18%),
    radial-gradient(circle at 78% 68%, rgba(90, 200, 250, 0.15), transparent 20%);
}

.barrage-item {
  position: absolute;
  top: calc(18px + var(--row) * 52px);
  left: 100%;
  display: inline-flex;
  align-items: center;
  gap: 9px;
  max-width: min(560px, 78vw);
  padding: 8px 16px 8px 8px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-primary);
  box-shadow: 0 14px 30px rgba(39, 53, 88, 0.1);
  cursor: pointer;
  animation: barrage-wall-fly var(--speed) linear infinite;
  animation-delay: var(--delay);
}

.barrage-item:hover {
  animation-play-state: paused;
  box-shadow: 0 18px 38px rgba(251, 114, 153, 0.16);
}

.barrage-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 32px;
  height: 32px;
  overflow: hidden;
  border-radius: 50%;
  background: linear-gradient(135deg, #fb7299, #5ac8fa);
  color: white;
  font-size: 12px;
  font-weight: 800;
}

.barrage-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.barrage-item strong {
  flex: 0 0 auto;
  color: var(--brand-pink-deep);
}

.barrage-item em {
  overflow: hidden;
  color: var(--text-muted);
  font-style: normal;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.barrage-empty {
  display: grid;
  gap: 10px;
  padding: 34px;
  border-radius: 24px;
  background: var(--surface-soft);
}

.barrage-empty :deep(.el-icon) {
  color: var(--brand-pink);
  font-size: 30px;
}

.barrage-empty p {
  margin: 0;
  color: var(--text-muted);
}

@keyframes barrage-wall-fly {
  from {
    transform: translateX(0);
  }

  to {
    transform: translateX(-1180px);
  }
}

@media (max-width: 820px) {
  .barrage-hero {
    grid-template-columns: 1fr;
  }

  .barrage-stage {
    height: 420px;
  }
}

@media (max-width: 720px) {
  .barrage-hero,
  .barrage-panel {
    padding: 22px;
  }

  .barrage-hero h1 {
    font-size: 38px;
  }
}
</style>
