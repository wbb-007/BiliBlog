<script setup>
import { onMounted } from 'vue'
import { refreshCurrentAdmin } from './stores/auth'

onMounted(() => {
  refreshCurrentAdmin()
})
</script>

<template>
  <router-view />
</template>
