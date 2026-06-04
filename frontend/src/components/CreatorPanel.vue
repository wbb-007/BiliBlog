<script setup>
defineProps({
  creator: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <section class="creator-panel">
    <div class="creator-panel__banner" :style="{ background: creator.bannerStyle }"></div>
    <div class="creator-panel__body">
      <div class="creator-panel__avatar">{{ creator.avatarLabel }}</div>
      <div class="creator-panel__headline">
        <h3>{{ creator.name }}</h3>
        <p>{{ creator.title }}</p>
      </div>

      <div class="creator-panel__stats">
        <div>
          <strong>{{ creator.followers }}</strong>
          <span>粉丝</span>
        </div>
        <div>
          <strong>{{ creator.likes }}</strong>
          <span>获赞</span>
        </div>
        <div>
          <strong>{{ creator.posts }}</strong>
          <span>文章</span>
        </div>
      </div>

      <p class="creator-panel__bio">{{ creator.bio }}</p>

      <div class="creator-panel__tags">
        <span v-for="tag in creator.tags" :key="tag">{{ tag }}</span>
      </div>

      <el-button class="creator-panel__button" round type="primary">
        关注这位作者
      </el-button>
    </div>
  </section>
</template>

<style scoped>
.creator-panel {
  overflow: hidden;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: var(--soft-shadow);
}

.creator-panel__banner {
  height: 120px;
}

.creator-panel__body {
  padding: 0 20px 22px;
  margin-top: -28px;
}

.creator-panel__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border: 4px solid white;
  border-radius: 24px;
  background: linear-gradient(135deg, #fb7299, #5ac8fa);
  color: white;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
}

.creator-panel__headline {
  margin-top: 12px;
}

.creator-panel__headline h3 {
  margin: 0;
  font-size: 24px;
}

.creator-panel__headline p {
  margin: 6px 0 0;
  color: var(--text-muted);
}

.creator-panel__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 18px;
}

.creator-panel__stats div {
  padding: 14px 12px;
  border-radius: 18px;
  background: var(--surface-soft);
  text-align: center;
}

.creator-panel__stats strong,
.creator-panel__stats span {
  display: block;
}

.creator-panel__stats strong {
  font-size: 20px;
}

.creator-panel__stats span {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 12px;
}

.creator-panel__bio {
  margin: 18px 0 0;
  color: var(--text-muted);
  line-height: 1.75;
}

.creator-panel__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 18px;
}

.creator-panel__tags span {
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(251, 114, 153, 0.12);
  color: var(--brand-pink-deep);
  font-size: 12px;
  font-weight: 700;
}

.creator-panel__button {
  width: 100%;
  margin-top: 20px;
}
</style>
