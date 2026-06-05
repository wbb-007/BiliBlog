<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  height: {
    type: [String, Number],
    default: 460,
  },
  placeholder: {
    type: String,
    default: 'Write Markdown here. Paste an image to upload it automatically.',
  },
  uploadImage: {
    type: Function,
    required: true,
  },
})

const emit = defineEmits(['update:modelValue'])

const content = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

async function handleUploadImg(files, callback) {
  try {
    const urls = await Promise.all(files.map((file) => props.uploadImage(file).then((item) => item.url)))
    callback(urls)
  } catch (error) {
    ElMessage.error(error?.response?.data?.message ?? '图片上传失败')
  }
}
</script>

<template>
  <MdEditor
    v-model="content"
    language="zh-CN"
    :height="height"
    :placeholder="placeholder"
    @on-upload-img="handleUploadImg"
  />
</template>

<style scoped>
:deep(.md-editor) {
  border-radius: 16px;
  overflow: hidden;
  border-color: var(--line-soft, #e5e7eb);
}

:deep(.md-editor-toolbar) {
  flex-wrap: wrap;
}
</style>
