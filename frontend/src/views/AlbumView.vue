<script setup>
import { computed, onMounted, ref } from 'vue'
import { Camera, PictureFilled, Star } from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import SectionHeading from '../components/SectionHeading.vue'
import { fetchGarden } from '../api/blog'

const loading = ref(true)
const garden = ref(null)

const photos = computed(() => garden.value?.photos ?? [])

async function loadAlbum() {
  loading.value = true
  garden.value = await fetchGarden()
  loading.value = false
}

onMounted(loadAlbum)
</script>

<template>
  <AppHeader />

  <main class="page-shell album-page">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="surface-card album-skeleton"></div>
      </template>

      <template #default>
        <section class="album-hero">
          <div>
            <span class="album-eyebrow">BiliBlog Album</span>
            <h1>相册</h1>
            <p>把 Poetize 的图片墙拆成独立相册入口，用粉蓝渐变、封面卡和轻动效来贴合你的 BiliBlog UI。</p>
          </div>
          <div class="album-hero__chip">
            <el-icon><Camera /></el-icon>
            <strong>{{ photos.length }}</strong>
            <span>张展示图</span>
          </div>
        </section>

        <section class="surface-card album-panel">
          <SectionHeading
            title="图片墙"
            description="旅拍、封面图和生活片段都可以在这里展示。"
          >
            <el-icon class="album-icon"><PictureFilled /></el-icon>
          </SectionHeading>

          <div v-if="photos.length" class="album-grid">
            <article
              v-for="(photo, index) in photos"
              :key="photo.title"
              class="album-card"
              :class="{ 'album-card--wide': index % 5 === 0 }"
            >
              <div class="album-card__visual" :style="{ background: photo.color }">
                <img v-if="photo.imageUrl" :src="photo.imageUrl" :alt="photo.title" />
                <span v-else>{{ photo.location }}</span>
              </div>
              <div class="album-card__copy">
                <strong>{{ photo.title }}</strong>
                <p>{{ photo.caption }}</p>
                <small>
                  <el-icon><Star /></el-icon>
                  {{ photo.location }}
                </small>
              </div>
            </article>
          </div>

          <section v-else class="album-empty">
            <el-icon><PictureFilled /></el-icon>
            <strong>相册暂时为空</strong>
            <p>接入图片数据后，这里会自动铺成 B 站风图片墙。</p>
          </section>
        </section>
      </template>
    </el-skeleton>
  </main>
</template>

<style scoped>
.album-skeleton {
  height: 720px;
}

.album-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 22px;
  align-items: center;
  margin-bottom: 24px;
  padding: 34px;
  border-radius: 30px;
  background:
    linear-gradient(135deg, rgba(251, 114, 153, 0.15), rgba(90, 200, 250, 0.18)),
    #ffffff;
  box-shadow: var(--soft-shadow);
}

.album-eyebrow {
  display: inline-flex;
  width: fit-content;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(251, 114, 153, 0.12);
  color: var(--brand-pink-deep);
  font-size: 12px;
  font-weight: 800;
}

.album-hero h1 {
  margin: 14px 0 10px;
  font-family: var(--font-display);
  font-size: 48px;
  line-height: 1.1;
}

.album-hero p {
  max-width: 640px;
  margin: 0;
  color: var(--text-muted);
  line-height: 1.8;
}

.album-hero__chip {
  display: grid;
  min-width: 170px;
  gap: 8px;
  justify-items: center;
  padding: 24px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.8);
}

.album-hero__chip :deep(.el-icon),
.album-icon {
  color: var(--brand-pink);
}

.album-hero__chip :deep(.el-icon) {
  font-size: 32px;
}

.album-hero__chip strong {
  font-family: var(--font-display);
  font-size: 34px;
  color: var(--brand-pink);
}

.album-hero__chip span {
  color: var(--text-muted);
  font-size: 13px;
}

.album-panel {
  padding: 24px;
}

.album-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.album-card {
  overflow: hidden;
  border-radius: 24px;
  background: var(--surface-soft);
  transition: transform 0.24s ease, box-shadow 0.24s ease;
}

.album-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 22px 42px rgba(251, 114, 153, 0.16);
}

.album-card--wide {
  grid-column: span 2;
}

.album-card__visual {
  display: grid;
  min-height: 230px;
  place-items: end start;
  overflow: hidden;
  padding: 16px;
  color: white;
}

.album-card__visual img {
  width: 100%;
  height: 100%;
  min-height: 230px;
  object-fit: cover;
}

.album-card__visual span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  font-weight: 800;
  backdrop-filter: blur(8px);
}

.album-card__copy {
  display: grid;
  gap: 8px;
  padding: 16px;
}

.album-card__copy strong {
  font-size: 18px;
}

.album-card__copy p,
.album-card__copy small {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.7;
}

.album-card__copy small {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--brand-cyan-deep);
  font-weight: 800;
}

.album-empty {
  display: grid;
  gap: 10px;
  padding: 34px;
  border-radius: 24px;
  background: var(--surface-soft);
}

.album-empty :deep(.el-icon) {
  color: var(--brand-pink);
  font-size: 30px;
}

.album-empty p {
  margin: 0;
  color: var(--text-muted);
}

@media (max-width: 980px) {
  .album-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .album-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .album-hero,
  .album-panel {
    padding: 22px;
  }

  .album-hero h1 {
    font-size: 38px;
  }

  .album-grid,
  .album-card--wide {
    grid-template-columns: 1fr;
    grid-column: auto;
  }
}
</style>
