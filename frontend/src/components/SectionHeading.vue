<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <div class="section-heading">
    <div>
      <h2>{{ title }}</h2>
      <p v-if="description">{{ description }}</p>
    </div>
    <slot />
  </div>
</template>

<style scoped>
.section-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

h2 {
  margin: 0 0 4px;
  font-family: var(--font-display);
  font-size: 28px;
  line-height: 1.2;
}

p {
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
}

@media (max-width: 780px) {
  .section-heading {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
