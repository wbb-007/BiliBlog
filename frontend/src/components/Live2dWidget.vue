<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { fetchLive2dSettings } from '../api/blog'

const SCRIPT_ID = 'live2d-widget-script'
const SCRIPT_URL = 'https://unpkg.com/live2d-widget@3.1.4/lib/L2Dwidget.min.js'

const ready = ref(false)
let disposed = false

function loadScript() {
  return new Promise((resolve, reject) => {
    if (window.L2Dwidget) {
      resolve()
      return
    }

    const existing = document.getElementById(SCRIPT_ID)
    if (existing) {
      existing.addEventListener('load', resolve, { once: true })
      existing.addEventListener('error', reject, { once: true })
      return
    }

    const script = document.createElement('script')
    script.id = SCRIPT_ID
    script.src = SCRIPT_URL
    script.async = true
    script.onload = resolve
    script.onerror = reject
    document.head.appendChild(script)
  })
}

function removeWidget() {
  document.querySelectorAll('#live2d-widget, #live2dcanvas').forEach((item) => item.remove())
}

function initWidget(settings) {
  removeWidget()
  window.L2Dwidget.init({
    model: {
      jsonPath: settings.modelUrl,
      scale: settings.scale || 1,
    },
    display: {
      position: settings.position || 'right',
      width: settings.width || 280,
      height: settings.height || 360,
      hOffset: settings.hOffset ?? 16,
      vOffset: settings.vOffset ?? 0,
    },
    mobile: {
      show: true,
      scale: 0.72,
    },
    react: {
      opacityDefault: 0.92,
      opacityOnHover: 1,
    },
  })
}

async function boot() {
  try {
    const settings = await fetchLive2dSettings()
    if (disposed || !settings.enabled || !settings.modelUrl) {
      removeWidget()
      return
    }

    await loadScript()
    await nextTick()

    if (!disposed) {
      initWidget(settings)
      ready.value = true
    }
  } catch {
    removeWidget()
  }
}

onMounted(boot)

onBeforeUnmount(() => {
  disposed = true
  removeWidget()
})
</script>

<template>
  <div v-if="ready" class="live2d-hit-area" aria-hidden="true"></div>
</template>

<style scoped>
.live2d-hit-area {
  pointer-events: none;
}

:global(#live2d-widget),
:global(#live2dcanvas) {
  z-index: 45 !important;
}

@media (max-width: 720px) {
  :global(#live2d-widget),
  :global(#live2dcanvas) {
    transform-origin: bottom right;
  }
}
</style>
