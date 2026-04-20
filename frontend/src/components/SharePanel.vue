<template>
  <div v-if="show" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <h3>🔗 分享文档</h3>

      <div class="share-permission">
        <label>权限设置: </label>
        <select v-model="permission">
          <option value="read">只读</option>
          <option value="edit">可编辑</option>
        </select>
      </div>

      <div class="share-expire">
        <label>有效期: </label>
        <select v-model="expireHours">
          <option value="24">1 天</option>
          <option value="168">7 天</option>
          <option value="720">30 天</option>
        </select>
      </div>

      <button class="btn-generate" @click="generateShareLink" :disabled="generating">
        {{ generating ? '生成中...' : '生成分享链接' }}
      </button>

      <div v-if="shareUrl" class="share-result">
        <input type="text" :value="shareUrl" readonly ref="urlInput">
        <button @click="copyLink">复制链接</button>
      </div>

      <button class="btn-close" @click="close">关闭</button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, inject } from 'vue'

const props = defineProps({
  show: Boolean,
  docId: String
})

const emit = defineEmits(['close', 'success', 'error'])

// 注入 loading 和 toast
const loading = inject('loading')
const toast = inject('toast')

const shareUrl = ref('')
const permission = ref('read')
const expireHours = ref(168)
const generating = ref(false)
const urlInput = ref(null)

const close = () => {
  emit('close')
  setTimeout(() => {
    shareUrl.value = ''
  }, 300)
}

const generateShareLink = async () => {
  generating.value = true
  if (loading) loading.show('生成分享链接...')

  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch('http://localhost:8080/api/share/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        docId: props.docId,
        permission: permission.value,
        expireHours: expireHours.value
      })
    })

    if (res.ok) {
      const data = await res.json()
      shareUrl.value = data.shareUrl
      if (toast) toast.success('分享链接已生成')
      emit('success', '分享链接已生成')
    } else {
      const error = await res.text()
      console.error('生成失败:', error)
      if (toast) toast.error('生成失败')
      emit('error', '生成失败')
    }
  } catch (err) {
    console.error('网络错误:', err)
    if (toast) toast.error('网络错误')
    emit('error', '网络错误')
  } finally {
    generating.value = false
    if (loading) loading.hide()
  }
}

const copyLink = async () => {
  if (!shareUrl.value) return;

  // 1. 优先尝试现代 API (需要 HTTPS 环境)
  if (navigator.clipboard && window.isSecureContext) {
    try {
      await navigator.clipboard.writeText(shareUrl.value);
      if (toast) toast.success('链接已复制到剪贴板');
      emit('success', '链接已复制到剪贴板');
      return; // 成功则直接返回
    } catch (err) {
      console.error('Clipboard API 失败，尝试降级方案', err);
    }
  }

  // 2. 降级方案：使用传统 select + copy (即便在 HTTP 环境也能跑)
  if (urlInput.value) {
    urlInput.value.select();
    const success = document.execCommand('copy');
    if (success) {
      if (toast) toast.success('链接已复制（兼容模式）');
      emit('success', '链接已复制');
    } else {
      if (toast) toast.error('复制失败，请手动选择复制');
    }
  }
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    shareUrl.value = ''
    permission.value = 'read'
    expireHours.value = 168
  }
})
</script>
<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.modal-content {
  background: white;
  padding: 24px;
  border-radius: 12px;
  width: 400px;
  max-width: 90%;
}
.share-permission, .share-expire {
  margin-bottom: 16px;
}
.share-permission select, .share-expire select {
  margin-left: 12px;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #ddd;
}
.btn-generate {
  width: 100%;
  padding: 10px;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 16px;
}
.btn-generate:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.share-result {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.share-result input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 12px;
}
.share-result button {
  padding: 8px 16px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.btn-close {
  width: 100%;
  padding: 10px;
  background: #f0f0f0;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
</style>