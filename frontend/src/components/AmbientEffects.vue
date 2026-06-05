<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

const petalCanvas = ref(null)
const burstCanvas = ref(null)

let petalFrame = 0
let burstFrame = 0
let petals = []
let particles = []
let reduceMotion = false

function resizeCanvas(canvas, context) {
  const ratio = window.devicePixelRatio || 1
  canvas.width = Math.floor(window.innerWidth * ratio)
  canvas.height = Math.floor(window.innerHeight * ratio)
  canvas.style.width = `${window.innerWidth}px`
  canvas.style.height = `${window.innerHeight}px`
  context.setTransform(ratio, 0, 0, ratio, 0, 0)
}

function createPetal(initial = false) {
  return {
    x: Math.random() * window.innerWidth,
    y: initial ? Math.random() * window.innerHeight : -30,
    size: 8 + Math.random() * 12,
    speed: 0.35 + Math.random() * 0.75,
    drift: -0.45 + Math.random() * 0.9,
    sway: Math.random() * Math.PI * 2,
    rotation: Math.random() * Math.PI * 2,
    spin: -0.012 + Math.random() * 0.024,
    alpha: 0.3 + Math.random() * 0.42,
    hue: Math.random() > 0.35 ? '#fb7299' : '#5ac8fa',
  }
}

function drawPetal(ctx, petal) {
  ctx.save()
  ctx.translate(petal.x, petal.y)
  ctx.rotate(petal.rotation)
  ctx.globalAlpha = petal.alpha
  ctx.fillStyle = petal.hue
  ctx.beginPath()
  ctx.moveTo(0, -petal.size)
  ctx.bezierCurveTo(petal.size * 0.9, -petal.size * 0.5, petal.size * 0.75, petal.size * 0.7, 0, petal.size)
  ctx.bezierCurveTo(-petal.size * 0.75, petal.size * 0.7, -petal.size * 0.9, -petal.size * 0.5, 0, -petal.size)
  ctx.fill()
  ctx.restore()
}

function animatePetals() {
  const canvas = petalCanvas.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, window.innerWidth, window.innerHeight)

  petals.forEach((petal) => {
    petal.sway += 0.012
    petal.x += petal.drift + Math.sin(petal.sway) * 0.28
    petal.y += petal.speed
    petal.rotation += petal.spin

    if (petal.y > window.innerHeight + 40 || petal.x < -60 || petal.x > window.innerWidth + 60) {
      Object.assign(petal, createPetal())
    }

    drawPetal(ctx, petal)
  })

  petalFrame = requestAnimationFrame(animatePetals)
}

function createBurstParticle(x, y) {
  const angle = Math.random() * Math.PI * 2
  const distance = 34 + Math.random() * 96
  return {
    x,
    y,
    startX: x,
    startY: y,
    endX: x + Math.cos(angle) * distance,
    endY: y + Math.sin(angle) * distance,
    radius: 3 + Math.random() * 8,
    age: 0,
    life: 520 + Math.random() * 420,
    color: ['#fb7299', '#5ac8fa', '#2fc89f', '#ffd166'][Math.floor(Math.random() * 4)],
  }
}

function drawBurst() {
  const canvas = burstCanvas.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  const now = performance.now()
  ctx.clearRect(0, 0, window.innerWidth, window.innerHeight)

  particles = particles.filter((particle) => {
    if (!particle.lastTime) particle.lastTime = now
    particle.age += now - particle.lastTime
    particle.lastTime = now
    const progress = Math.min(particle.age / particle.life, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    const x = particle.startX + (particle.endX - particle.startX) * eased
    const y = particle.startY + (particle.endY - particle.startY) * eased
    const alpha = 1 - progress

    ctx.save()
    ctx.globalAlpha = alpha
    ctx.fillStyle = particle.color
    ctx.beginPath()
    ctx.arc(x, y, particle.radius * (1 - progress * 0.75), 0, Math.PI * 2)
    ctx.fill()

    ctx.strokeStyle = particle.color
    ctx.lineWidth = 2 * alpha
    ctx.beginPath()
    ctx.arc(particle.startX, particle.startY, progress * 58, 0, Math.PI * 2)
    ctx.stroke()
    ctx.restore()

    return progress < 1
  })

  burstFrame = requestAnimationFrame(drawBurst)
}

function handlePointerDown(event) {
  if (reduceMotion || event.target.closest('input, textarea, select, a, button')) {
    return
  }

  const x = event.clientX
  const y = event.clientY
  particles.push(...Array.from({ length: 18 }, () => createBurstParticle(x, y)))
}

function handleResize() {
  const petalContext = petalCanvas.value?.getContext('2d')
  const burstContext = burstCanvas.value?.getContext('2d')
  if (petalCanvas.value && petalContext) resizeCanvas(petalCanvas.value, petalContext)
  if (burstCanvas.value && burstContext) resizeCanvas(burstCanvas.value, burstContext)
}

onMounted(() => {
  reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  handleResize()

  if (!reduceMotion) {
    petals = Array.from({ length: 26 }, () => createPetal(true))
    animatePetals()
    drawBurst()
    window.addEventListener('pointerdown', handlePointerDown)
    window.addEventListener('resize', handleResize)
  }
})

onBeforeUnmount(() => {
  cancelAnimationFrame(petalFrame)
  cancelAnimationFrame(burstFrame)
  window.removeEventListener('pointerdown', handlePointerDown)
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <div class="ambient-effects" aria-hidden="true">
    <canvas ref="petalCanvas" class="ambient-effects__canvas ambient-effects__canvas--petals"></canvas>
    <canvas ref="burstCanvas" class="ambient-effects__canvas ambient-effects__canvas--burst"></canvas>
  </div>
</template>

<style scoped>
.ambient-effects {
  pointer-events: none;
}

.ambient-effects__canvas {
  position: fixed;
  inset: 0;
  pointer-events: none;
}

.ambient-effects__canvas--petals {
  z-index: 4;
}

.ambient-effects__canvas--burst {
  z-index: 120;
}
</style>
