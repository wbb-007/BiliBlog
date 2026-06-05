<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  Camera,
  ChatDotRound,
  Collection,
  Link,
  MagicStick,
  Menu,
  MessageBox,
  Suitcase,
} from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import SectionHeading from '../components/SectionHeading.vue'
import { fetchGarden } from '../api/blog'

const loading = ref(true)
const garden = ref(null)

const toolGroups = computed(() => {
  if (!garden.value?.tools) {
    return []
  }

  const groups = new Map()
  garden.value.tools.forEach((tool) => {
    const list = groups.get(tool.category) ?? []
    list.push(tool)
    groups.set(tool.category, list)
  })
  return Array.from(groups.entries()).map(([category, tools]) => ({ category, tools }))
})

async function loadGarden() {
  loading.value = true
  garden.value = await fetchGarden()
  loading.value = false
}

onMounted(loadGarden)
</script>

<template>
  <AppHeader />

  <main class="page-shell garden-page">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="surface-card garden-skeleton"></div>
      </template>

      <template #default>
        <section class="garden-hero">
          <div class="garden-hero__copy">
            <span class="garden-eyebrow">Poetize Fusion</span>
            <h1>诗意花园</h1>
            <p>
              把博客文章、AI 教程、微言碎片、友链和灵感工具收进同一个空间。
            </p>
            <div class="garden-metrics">
              <article v-for="metric in garden.metrics" :key="metric.label">
                <strong>{{ metric.value }}</strong>
                <span>{{ metric.label }}</span>
              </article>
            </div>
          </div>

          <div class="garden-hero__panel">
            <div class="garden-orbit garden-orbit--one"></div>
            <div class="garden-orbit garden-orbit--two"></div>
            <div class="garden-hero__card">
              <el-icon><MagicStick /></el-icon>
              <strong>博客结合版</strong>
              <span>文章负责深度，花园负责温度。</span>
            </div>
          </div>
        </section>

        <section class="garden-layout">
          <div class="garden-main">
            <section class="surface-card garden-panel">
              <SectionHeading
                title="微言"
                description="像 Poetize 的随笔与树洞，把短念头留在文章之外。"
              >
                <el-icon class="garden-panel__icon"><MessageBox /></el-icon>
              </SectionHeading>

              <div class="note-list">
                <article
                  v-for="note in garden.notes"
                  :key="note.title"
                  class="note-item"
                  :class="`note-item--${note.tone}`"
                >
                  <div>
                    <strong>{{ note.title }}</strong>
                    <span>{{ note.mood }} · {{ note.time }}</span>
                  </div>
                  <p>{{ note.content }}</p>
                </article>
              </div>
            </section>

            <section class="surface-card garden-panel barrage-card">
              <SectionHeading
                title="弹幕墙"
                description="复用文章评论，让读者声音在花园里流动起来。"
              >
                <el-icon class="garden-panel__icon"><ChatDotRound /></el-icon>
              </SectionHeading>

              <div v-if="garden.barrage?.length" class="garden-barrage">
                <button
                  v-for="(comment, index) in garden.barrage"
                  :key="comment.id"
                  class="garden-barrage__item"
                  :style="{
                    '--delay': `${index * -1.6}s`,
                    '--row': index % 4,
                    '--speed': `${20 + (index % 3) * 4}s`,
                  }"
                  type="button"
                >
                  <img
                    v-if="comment.authorAvatarUrl"
                    :src="comment.authorAvatarUrl"
                    :alt="comment.author"
                  />
                  <span v-else>{{ comment.authorInitial }}</span>
                  <strong>{{ comment.author }}</strong>
                  <em>{{ comment.content }}</em>
                </button>
              </div>
              <p v-else class="garden-empty">还没有评论弹幕，文章区出现互动后这里会自动亮起来。</p>
            </section>

            <section class="surface-card garden-panel">
              <SectionHeading
                title="旅拍与图片墙"
                description="保留 Poetize 的旅拍感，但用你的封面语言重新表达。"
              >
                <el-icon class="garden-panel__icon"><Camera /></el-icon>
              </SectionHeading>

              <div class="photo-grid">
                <article v-for="photo in garden.photos" :key="photo.title" class="photo-card">
                  <div class="photo-card__visual" :style="{ background: photo.color }">
                    <img v-if="photo.imageUrl" :src="photo.imageUrl" :alt="photo.title" />
                    <span v-else>{{ photo.location }}</span>
                  </div>
                  <div class="photo-card__copy">
                    <strong>{{ photo.title }}</strong>
                    <p>{{ photo.caption }}</p>
                  </div>
                </article>
              </div>
            </section>
          </div>

          <aside class="garden-side">
            <section class="surface-card garden-panel">
              <SectionHeading
                title="友人帐"
                description="给朋友、项目和灵感来源一个固定入口。"
              >
                <el-icon class="garden-panel__icon"><Link /></el-icon>
              </SectionHeading>

              <div class="friend-list">
                <a
                  v-for="friend in garden.friends"
                  :key="friend.name"
                  class="friend-card"
                  :href="friend.url"
                  target="_blank"
                  rel="noreferrer"
                >
                  <span class="friend-card__avatar">
                    <img v-if="friend.avatarUrl" :src="friend.avatarUrl" :alt="friend.name" />
                    <span v-else>{{ friend.name.slice(0, 1) }}</span>
                  </span>
                  <span class="friend-card__copy">
                    <strong>{{ friend.name }}</strong>
                    <em>{{ friend.description }}</em>
                    <small>{{ friend.tag }}</small>
                  </span>
                </a>
              </div>
            </section>

            <section class="surface-card garden-panel">
              <SectionHeading
                title="百宝箱"
                description="把 AI 教程里的方法沉淀成可复用的小工具卡。"
              >
                <el-icon class="garden-panel__icon"><Suitcase /></el-icon>
              </SectionHeading>

              <div class="tool-group-list">
                <article v-for="group in toolGroups" :key="group.category" class="tool-group">
                  <div class="tool-group__head">
                    <el-icon><Menu /></el-icon>
                    <strong>{{ group.category }}</strong>
                  </div>
                  <div class="tool-list">
                    <div v-for="tool in group.tools" :key="tool.name" class="tool-card">
                      <span class="tool-card__accent" :style="{ background: tool.accent }"></span>
                      <strong>{{ tool.name }}</strong>
                      <p>{{ tool.description }}</p>
                      <button type="button">{{ tool.actionLabel }}</button>
                    </div>
                  </div>
                </article>
              </div>
            </section>

            <section class="surface-card garden-panel garden-archive">
              <el-icon><Collection /></el-icon>
              <strong>结合方向</strong>
              <p>
                当前版本先融合 Poetize 的生活化栏目。后续可以把微言、友链、旅拍和百宝箱都接入后台管理。
              </p>
            </section>
          </aside>
        </section>
      </template>
    </el-skeleton>
  </main>
</template>

<style scoped>
.garden-skeleton {
  height: 860px;
}

.garden-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
  gap: 24px;
  min-height: 360px;
  margin-bottom: 24px;
  overflow: hidden;
  border-radius: 34px;
  background:
    linear-gradient(135deg, rgba(20, 184, 166, 0.1), rgba(251, 114, 153, 0.1)),
    #ffffff;
  box-shadow: var(--soft-shadow);
}

.garden-hero__copy {
  display: grid;
  align-content: center;
  gap: 18px;
  padding: 36px;
}

.garden-eyebrow {
  display: inline-flex;
  width: fit-content;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(47, 200, 159, 0.12);
  color: #0f766e;
  font-size: 12px;
  font-weight: 800;
}

.garden-hero h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: 54px;
  line-height: 1.05;
}

.garden-hero p {
  max-width: 560px;
  margin: 0;
  color: var(--text-muted);
  line-height: 1.9;
}

.garden-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.garden-metrics article {
  padding: 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.86);
}

.garden-metrics strong,
.garden-metrics span {
  display: block;
}

.garden-metrics strong {
  font-family: var(--font-display);
  font-size: 26px;
}

.garden-metrics span {
  margin-top: 6px;
  color: var(--text-muted);
  font-size: 13px;
}

.garden-hero__panel {
  position: relative;
  min-height: 360px;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(31, 39, 79, 0.94), rgba(109, 62, 203, 0.82)),
    repeating-linear-gradient(45deg, rgba(255, 255, 255, 0.08) 0 1px, transparent 1px 20px);
}

.garden-orbit {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 50%;
}

.garden-orbit--one {
  inset: 44px 76px;
}

.garden-orbit--two {
  inset: 88px 34px 28px 126px;
}

.garden-hero__card {
  position: absolute;
  right: 36px;
  bottom: 36px;
  left: 36px;
  display: grid;
  gap: 10px;
  padding: 24px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.13);
  color: white;
  backdrop-filter: blur(12px);
}

.garden-hero__card :deep(.el-icon) {
  font-size: 34px;
}

.garden-hero__card strong {
  font-family: var(--font-display);
  font-size: 26px;
}

.garden-hero__card span {
  opacity: 0.84;
}

.garden-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 24px;
  align-items: start;
}

.garden-main,
.garden-side,
.note-list,
.friend-list,
.tool-group-list,
.tool-list {
  display: grid;
  gap: 18px;
}

.garden-side {
  position: sticky;
  top: 92px;
}

.garden-panel {
  padding: 22px;
}

.garden-panel__icon {
  color: var(--brand-pink);
}

.note-item {
  display: grid;
  gap: 12px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid transparent;
}

.note-item--pink {
  background: rgba(251, 114, 153, 0.09);
  border-color: rgba(251, 114, 153, 0.18);
}

.note-item--cyan {
  background: rgba(90, 200, 250, 0.1);
  border-color: rgba(90, 200, 250, 0.18);
}

.note-item--mint {
  background: rgba(47, 200, 159, 0.1);
  border-color: rgba(47, 200, 159, 0.18);
}

.note-item strong,
.note-item span {
  display: block;
}

.note-item span {
  margin-top: 6px;
  color: var(--text-muted);
  font-size: 13px;
}

.note-item p {
  margin: 0;
  color: #26303d;
  line-height: 1.85;
}

.garden-barrage {
  position: relative;
  height: 170px;
  overflow: hidden;
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.74), rgba(240, 253, 250, 0.82)),
    repeating-linear-gradient(0deg, transparent 0 35px, rgba(47, 200, 159, 0.07) 35px 36px);
}

.garden-barrage__item {
  position: absolute;
  top: calc(16px + var(--row) * 34px);
  left: 100%;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 420px;
  padding: 8px 14px 8px 8px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.94);
  color: var(--text-primary);
  box-shadow: 0 12px 28px rgba(47, 200, 159, 0.12);
  white-space: nowrap;
  animation: garden-barrage var(--speed) linear infinite;
  animation-delay: var(--delay);
}

.garden-barrage__item img,
.garden-barrage__item > span {
  width: 28px;
  height: 28px;
  border-radius: 50%;
}

.garden-barrage__item img {
  object-fit: cover;
}

.garden-barrage__item > span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fb7299, #5ac8fa);
  color: white;
  font-size: 12px;
  font-weight: 800;
}

.garden-barrage__item strong {
  color: #0f766e;
}

.garden-barrage__item em {
  overflow: hidden;
  color: var(--text-muted);
  font-style: normal;
  text-overflow: ellipsis;
}

@keyframes garden-barrage {
  from {
    transform: translateX(0);
  }

  to {
    transform: translateX(-820px);
  }
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.photo-card {
  overflow: hidden;
  border-radius: 24px;
  background: var(--surface-soft);
}

.photo-card__visual {
  display: grid;
  min-height: 180px;
  place-items: end start;
  padding: 16px;
  color: white;
}

.photo-card__visual img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-card__visual span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  font-weight: 800;
}

.photo-card__copy {
  padding: 16px;
}

.photo-card__copy strong,
.photo-card__copy p {
  display: block;
  margin: 0;
}

.photo-card__copy p {
  margin-top: 8px;
  color: var(--text-muted);
  line-height: 1.7;
}

.friend-card {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  align-items: center;
  padding: 14px;
  border-radius: 20px;
  background: var(--surface-soft);
}

.friend-card__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  overflow: hidden;
  border-radius: 18px;
  background: linear-gradient(135deg, #fb7299, #5ac8fa);
  color: white;
  font-weight: 800;
}

.friend-card__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.friend-card__copy strong,
.friend-card__copy em,
.friend-card__copy small {
  display: block;
}

.friend-card__copy em {
  margin-top: 5px;
  color: var(--text-muted);
  font-size: 13px;
  font-style: normal;
  line-height: 1.55;
}

.friend-card__copy small {
  margin-top: 8px;
  color: var(--brand-pink-deep);
  font-weight: 800;
}

.tool-group {
  display: grid;
  gap: 12px;
}

.tool-group__head {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-muted);
}

.tool-card {
  position: relative;
  display: grid;
  gap: 8px;
  padding: 16px;
  overflow: hidden;
  border-radius: 20px;
  background: var(--surface-soft);
}

.tool-card__accent {
  position: absolute;
  inset: 0 auto 0 0;
  width: 5px;
}

.tool-card strong {
  padding-left: 6px;
}

.tool-card p {
  margin: 0;
  padding-left: 6px;
  color: var(--text-muted);
  line-height: 1.7;
}

.tool-card button {
  justify-self: start;
  margin-left: 6px;
  border: 0;
  border-radius: 999px;
  padding: 8px 12px;
  background: white;
  color: var(--text-primary);
  cursor: pointer;
  font-weight: 800;
}

.garden-archive {
  display: grid;
  gap: 12px;
  background: linear-gradient(135deg, rgba(251, 114, 153, 0.1), rgba(47, 200, 159, 0.1));
}

.garden-archive :deep(.el-icon) {
  color: var(--brand-pink);
  font-size: 28px;
}

.garden-archive p,
.garden-empty {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.8;
}

@media (max-width: 1100px) {
  .garden-hero,
  .garden-layout {
    grid-template-columns: 1fr;
  }

  .garden-side {
    position: static;
  }
}

@media (max-width: 720px) {
  .garden-hero h1 {
    font-size: 40px;
  }

  .garden-hero__copy {
    padding: 24px;
  }

  .garden-metrics,
  .photo-grid {
    grid-template-columns: 1fr;
  }
}
</style>
