<template>
  <div class="export-wrapper" @mouseenter="keepOpen" @mouseleave="closeMenu">
    <button class="btn-export" @click="toggleMenu">
      📥 导出 ▸
    </button>
    <div v-if="showMenu" class="export-menu">
      <button @click="handleExport('txt')">TXT</button>
      <button @click="handleExport('md')">Markdown</button>
      <button @click="handleExport('html')">HTML</button>
      <button @click="handleExport('json')">JSON</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { inject } from 'vue'

const props = defineProps({
  docId: { type: String, required: true }
})

const emit = defineEmits(['mouseenter'])

const loading = inject('loading')
const toast = inject('toast')
const showMenu = ref(false)

const toggleMenu = () => {
  showMenu.value = !showMenu.value
  if (showMenu.value){
    emit('mouseenter')
  }
}

const keepOpen = () => {
  emit('mouseenter')
}

const closeMenu = () => {
  showMenu.value = false
}

const handleExport = async (format) => {
  showMenu.value = false
  loading.show('导出中...')
  try {
    const token = sessionStorage.getItem('token')
    const response = await fetch(`http://localhost:8080/api/export/${format}/${props.docId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (!response.ok) throw new Error('导出失败')

    const contentDisposition = response.headers.get('Content-Disposition')
    let filename = `文档.${format}`
    if (contentDisposition) {
      const match = contentDisposition.match(/filename\*=UTF-8''(.+)/)
      if (match) filename = decodeURIComponent(match[1])
    }

    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    toast.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    toast.error('导出失败')
  } finally {
    loading.hide()
  }
}
</script>

<style scoped>
.export-wrapper {
  position: relative;
}
.btn-export {
  display: block;
  width: 100%;
  padding: 10px 16px;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
}
.btn-export:hover {
  background: #f5f5f5;
}
.export-menu {
  position: absolute;
  left: 100%;
  top: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  min-width: 100px;
  z-index: 101;
}
.export-menu button {
  display: block;
  width: 100%;
  padding: 8px 16px;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
}
.export-menu button:hover {
  background: #f5f5f5;
}
</style>