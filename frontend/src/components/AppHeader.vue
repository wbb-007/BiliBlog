<script setup>
import { computed, ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const keyword = ref(route.query.keyword?.toString() ?? '')

const activeName = computed(() => route.name)
const navItems = computed(() => [
  { label: '首页', name: 'home' },
  { label: '相册', name: 'album' },
  { label: '关于我', name: 'about-me' },
])

function go(name) {
  router.push({ name })
}

function search() {
  const nextKeyword = keyword.value.trim()

  if (!nextKeyword) {
    router.push({ name: 'home' })
    return
  }

  router.push({ name: 'home', query: { keyword: nextKeyword } })
}

watch(
  () => route.query.keyword,
  (value) => {
    keyword.value = value?.toString() ?? ''
  },
)
</script>

<template>
  <header class="app-header">
    <div class="app-header__inner">
      <div class="app-header__left">
        <button class="app-header__brand" type="button" @click="go('home')">BiliBlog</button>

        <nav class="app-header__nav" aria-label="主导航">
          <button
            v-for="item in navItems"
            :key="item.name"
            class="nav-link"
            :class="{ 'nav-link--active': activeName === item.name }"
            type="button"
            @click="go(item.name)"
          >
            {{ item.label }}
          </button>
        </nav>
      </div>

      <form class="app-header__search" @submit.prevent="search">
        <el-input v-model="keyword" clearable placeholder="搜索文章、标签、分类..." size="large">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #suffix>
            <button class="search-submit" type="submit" title="搜索">
              <el-icon><Search /></el-icon>
            </button>
          </template>
        </el-input>
      </form>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 50;
  width: 100%;
  background: var(--surface-container-lowest);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05);
}

.app-header__inner {
  display: grid;
  grid-template-columns: minmax(0, auto) minmax(220px, 420px);
  align-items: center;
  gap: 24px;
  max-width: 1320px;
  margin: 0 auto;
  padding: 12px 20px;
}

.app-header__left {
  display: flex;
  align-items: center;
  gap: 24px;
  min-width: 0;
}

.app-header__brand {
  border: 0;
  padding: 0;
  background: transparent;
  color: var(--brand-pink-deep);
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 900;
  line-height: 1;
  cursor: pointer;
}

.app-header__nav {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-link {
  position: relative;
  border: 0;
  border-radius: 8px;
  padding: 8px 2px;
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.18s ease, background 0.18s ease, transform 0.18s ease;
}

.nav-link:hover {
  color: var(--brand-pink-deep);
}

.nav-link--active {
  color: var(--brand-pink-deep);
}

.nav-link--active::after {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 1px;
  height: 2px;
  border-radius: 999px;
  content: '';
  background: var(--brand-pink-deep);
}

.app-header__search {
  min-width: 0;
}

.app-header__search :deep(.el-input__wrapper) {
  height: 40px;
  border-radius: 999px;
  background: var(--surface-container-low);
  box-shadow: none;
}

.app-header__search :deep(.el-input__wrapper.is-focus) {
  background: var(--surface-container-lowest);
  box-shadow: 0 0 0 2px rgba(251, 114, 153, 0.35);
}

.search-submit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 50%;
  background: var(--brand-pink);
  color: #fff;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
}

.search-submit:hover {
  background: var(--brand-pink-deep);
  transform: translateY(-1px);
}

@media (max-width: 1100px) {
  .app-header__inner {
    grid-template-columns: 1fr;
  }

  .app-header__search {
    grid-column: 1 / -1;
    grid-row: 2;
  }

  .app-header__left {
    gap: 16px;
  }
}

@media (max-width: 760px) {
  .app-header__inner {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 12px;
  }

  .app-header__left,
  .app-header__nav {
    flex-wrap: wrap;
  }
}
</style>
