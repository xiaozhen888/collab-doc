<template>
  <div v-if="show" class="history-panel">
    <div class="history-header">
      <h3>版本历史</h3>
      <button @click="close">✕</button>
    </div>
    <div class="history-list">
      <div v-if="loading" class="history-loading">加载中...</div>
      <div v-else-if="list.length > 0">
        <div v-for="item in list" :key="item.id" class="history-item">
          <div class="history-version">版本 {{ item.version }}</div>
          <div class="history-time">{{ formatDate(item.createTime) }}</div>
          <button @click="restore(item)" class="btn-restore">恢复</button>
        </div>
      </div>
      <div v-else class="history-empty">暂无历史版本</div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  docId: String
})

const emit = defineEmits(['close', 'restore', 'error'])

const list = ref([])
const loading = ref(false)

const close = () => {
  emit('close')
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

const loadList = async () => {
  loading.value = true
  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch(`http://localhost:8080/api/history/${props.docId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    list.value = await res.json() || []
  } catch {
    emit('error', '加载版本历史失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

const restore = async (item) => {
  if (!confirm(`确定要恢复到版本 ${item.version} 吗？当前内容将被覆盖。`)) return

  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch(`http://localhost:8080/api/history/${props.docId}/${item.version}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await res.json()
    emit('restore', data.content)
    emit('success', `已恢复到版本 ${item.version}`)
  } catch {
    emit('error', '恢复版本失败')
  }
}

// 打开时加载
watch(() => props.show, (newVal) => {
  if (newVal) loadList()
})
</script>

<style scoped>
.history-panel {
  position: fixed;
  right: 0;
  top: 0;
  width: 300px;
  height: 100vh;
  background: white;
  box-shadow: -2px 0 10px rgba(0,0,0,0.1);
  z-index: 1000;
  display: flex;
  flex-direction: column;
}
.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}
.history-header button {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}
.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
.history-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 8px;
}
.history-version {
  font-weight: 500;
  margin-bottom: 4px;
}
.history-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
.btn-restore {
  padding: 4px 12px;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}
.history-empty, .history-loading {
  text-align: center;
  color: #999;
  padding: 40px;
}
</style>