<script setup>
import { computed, ref } from 'vue'
import { Bell, Clock, Search, Setting, Star } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { openAdminConsole } from '../utils/adminConsole'

const route = useRoute()
const router = useRouter()
const keyword = ref('')

const activeName = computed(() => route.name)
const navItems = computed(() => [
  { label: '首页', name: 'home' },
  { label: '博主空间', name: 'space' },
])

function go(name) {
  router.push({ name })
}

function search() {
  if (!keyword.value.trim()) {
    return
  }

  router.push({ name: 'home', query: { keyword: keyword.value.trim() } })
}
</script>

<template>
  <header class="app-header">
    <div class="app-header__inner">
      <div class="app-header__brand" @click="go('home')">
        <span class="app-header__logo">BiliBlog</span>
        <span class="app-header__subtitle">轻内容博客与独立后台</span>
      </div>

      <nav class="app-header__nav">
        <button
          v-for="item in navItems"
          :key="item.name"
          class="nav-pill"
          :class="{ 'nav-pill--active': activeName === item.name }"
          @click="go(item.name)"
        >
          {{ item.label }}
        </button>
      </nav>

      <div class="app-header__search">
        <el-input
          v-model="keyword"
          placeholder="搜索文章、分类、灵感"
          size="large"
          @keyup.enter="search"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="app-header__actions">
        <button class="icon-badge" type="button">
          <el-icon><Bell /></el-icon>
        </button>
        <button class="icon-badge" type="button">
          <el-icon><Clock /></el-icon>
        </button>
        <button class="icon-badge" type="button">
          <el-icon><Star /></el-icon>
        </button>
        <el-button class="manage-btn" plain round @click="openAdminConsole">
          <el-icon><Setting /></el-icon>
          进入后台
        </el-button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 50;
  border-bottom: 1px solid rgba(255, 255, 255, 0.7);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 245, 249, 0.92));
  backdrop-filter: blur(18px);
}

.app-header__inner {
  display: grid;
  grid-template-columns: auto auto minmax(220px, 1fr) auto;
  align-items: center;
  gap: 18px;
  max-width: 1320px;
  margin: 0 auto;
  padding: 14px 20px;
}

.app-header__brand {
  display: flex;
  flex-direction: column;
  gap: 2px;
  cursor: pointer;
}

.app-header__logo {
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 800;
  color: var(--brand-pink);
  line-height: 1;
}

.app-header__subtitle {
  color: var(--text-muted);
  font-size: 12px;
}

.app-header__nav {
  display: flex;
  gap: 10px;
}

.nav-pill {
  border: none;
  border-radius: 999px;
  padding: 10px 16px;
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-pill:hover,
.nav-pill--active {
  background: rgba(251, 114, 153, 0.14);
  color: var(--brand-pink-deep);
}

.app-header__search {
  min-width: 0;
}

.app-header__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.85);
  color: var(--text-primary);
  box-shadow: 0 10px 30px rgba(251, 114, 153, 0.12);
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease;
}

.icon-badge:hover {
  transform: translateY(-1px);
  background: rgba(251, 114, 153, 0.14);
}

.manage-btn :deep(.el-icon) {
  margin-right: 4px;
}

@media (max-width: 1100px) {
  .app-header__inner {
    grid-template-columns: auto 1fr auto;
  }

  .app-header__nav {
    display: none;
  }
}

@media (max-width: 780px) {
  .app-header__inner {
    grid-template-columns: 1fr;
  }

  .app-header__actions {
    justify-content: space-between;
  }
}
</style>
