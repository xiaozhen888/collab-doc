<template>
  <div class="editor-container">
    <ToolbarPanel
        :doc-id="docId"
        :doc-title="docTitle"
        :is-connected="isConnected"
        :connection-count="connectionCount"
        :user-count="userCount"
        :is-saved="isSaved"
        :save-status="saveStatus"
        @open-history="showHistory = true"
        @open-share="showShare = true"
        @open-permission="showPermission = true"
        @export="handleExport"
    />

    <HistoryPanel
        :show="showHistory"
        :doc-id="docId"
        @close="showHistory = false"
        @restore="handleRestore"
        @success="toast.success($event)"
        @error="toast.error($event)"
    />

    <PermissionPanel
        :show="showPermission"
        :doc-id="docId"
        @close="showPermission = false"
        @success="toast.success($event)"
        @error="toast.error($event)"
    />

    <SharePanel
        :show="showShare"
        :doc-id="docId"
        @close="showShare = false"
        @success="toast.success($event)"
        @error="toast.error($event)"
    />

    <EditorPanel
        ref="editorPanelRef"
        :content="content"
        :can-edit="canEdit"
        :cursor-line="cursorLine"
        :cursor-column="cursorColumn"
        :selected-word-count="selectedWordCount"
        :word-count="wordCount"
        @update:content="content = $event"
        @input="handleInput"
        @keydown="handleKeydown"
        @select="updateSelectedCount"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { inject } from 'vue'
import ToolbarPanel from '@/components/ToolbarPanel.vue'
import HistoryPanel from '@/components/HistoryPanel.vue'
import PermissionPanel from '@/components/PermissionPanel.vue'
import SharePanel from '@/components/SharePanel.vue'
import EditorPanel from '@/components/EditorPanel.vue'

const toast = inject('toast')

// 文档ID
const getDocIdFromUrl = () => {
  const path = window.location.pathname
  const match = path.match(/\/editor\/(.+)/)
  return match ? match[1] : 'demo-doc-001'
}
const docId = getDocIdFromUrl()

// 状态
const docTitle = ref('未命名文档')
const content = ref('')
const isConnected = ref(false)
const isSaved = ref(true)
const saveStatus = ref('已保存')
const connectionCount = ref(0)
const userCount = ref(0)
const showHistory = ref(false)
const showPermission = ref(false)
const showShare = ref(false)

// 编辑器状态
const cursorLine = ref(1)
const cursorColumn = ref(1)
const selectedWordCount = ref(0)
const editorPanelRef = ref(null)

// 权限
const userPermission = ref('read')
const canEdit = computed(() => userPermission.value === 'edit' || userPermission.value === 'manage')

// WebSocket
let ws = null
let isManualClose = false
let saveTimer = null

// ========== 核心方法 ==========

const connect = () => {
  if (isManualClose) return

  const userId = sessionStorage.getItem('userId') || 'anonymous'
  ws = new WebSocket(`ws://localhost:8080/collab?userId=${userId}`)

  let heartbeatInterval = null

  ws.onopen = () => {
    isConnected.value = true
    toast.success('已连接到服务器')
    ws.send(JSON.stringify({ type: 'join', docId }))

    // 启动心跳：每 25 秒发送一次 ping
    heartbeatInterval = setInterval(() => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send('ping')
        console.log('发送心跳 ping')
      }
    }, 25000)
  }

  ws.onmessage = (event) => {
    if (event.data === 'pong') {
      console.log('收到心跳 pong')
      return
    }
    const msg = JSON.parse(event.data)

    if (msg.type === 'init') {
      content.value = msg.content || ''
    } else if (msg.type === 'update') {
      content.value = msg.content || ''
    } else if (msg.type === 'presence') {
      connectionCount.value = msg.connectionCount || 0
      userCount.value = msg.userCount || 0
    }
  }

  ws.onerror = () => {
    isConnected.value = false
    toast.error('连接失败，正在重连...')
  }

  ws.onclose = () => {
    isConnected.value = false

    // 清除心跳定时器
    if (heartbeatInterval) clearInterval(heartbeatInterval)

    if (!isManualClose) {
      toast.warning('连接已断开，正在重连...')
      setTimeout(connect, 3000)
    }
  }
}
const handleInput = () => {
  // 标记未保存
  isSaved.value = false
  saveStatus.value = '未保存'
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    isSaved.value = true
    saveStatus.value = '已保存'
    toast.success('文档已自动保存')
  }, 2000)

  // 发送更新
  if (ws?.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({
      type: 'update',
      docId,
      content: content.value
    }))
  }

  updateCursorPosition()
}

const handleKeydown = () => {
  setTimeout(updateCursorPosition, 0)
}

const updateCursorPosition = () => {
  const textarea = editorPanelRef.value?.editorRef
  if (textarea) {
    const textBeforeCursor = textarea.value.substring(0, textarea.selectionStart)
    const lines = textBeforeCursor.split('\n')
    cursorLine.value = lines.length
    cursorColumn.value = lines[lines.length - 1].length + 1
  }
}

const updateSelectedCount = () => {
  const textarea = editorPanelRef.value?.editorRef
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const selectedText = content.value.substring(start, end)
    selectedWordCount.value = selectedText.replace(/\s/g, '').length
  }
}

const handleRestore = (restoredContent) => {
  content.value = restoredContent
  handleInput()
}

const wordCount = computed(() => content.value.replace(/\s/g, '').length)

// 加载文档信息
const loadDocInfo = async () => {
  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch(`http://localhost:8080/api/document/${docId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (res.ok) {
      const data = await res.json()
      docTitle.value = data.title || '未命名文档'
    }
  } catch {}
}

// 加载用户权限
const loadUserPermission = async () => {
  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch(`http://localhost:8080/api/permission/${docId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const permissions = await res.json()
    const userId = sessionStorage.getItem('userId')
    const myPerm = permissions.find(p => p.userId === userId)
    userPermission.value = myPerm?.permission || 'read'
  } catch {
    userPermission.value = 'read'
  }
}

// 生命周期
onMounted(async () => {
  await loadDocInfo()
  await loadUserPermission()
  connect()
  setInterval(updateCursorPosition, 100)
})

onUnmounted(() => {
  isManualClose = true
  ws?.close()
  if (saveTimer) clearTimeout(saveTimer)
})
</script>

<style scoped>
.editor-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}
</style>