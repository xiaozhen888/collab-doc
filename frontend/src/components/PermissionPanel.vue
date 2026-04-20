<template>
  <div v-if="show" class="modal-overlay" @click.self="close">
    <div class="modal-content permission-modal">
      <div class="modal-header">
        <h3>👥 协作者管理</h3>
        <button @click="close">✕</button>
      </div>

      <div class="add-collaborator">
        <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索用户..."
            @input="searchUsers"
        />
        <div v-if="searchResults.length > 0" class="search-results">
          <div v-for="user in searchResults" :key="user.id" class="search-result-item">
            <span>{{ user.username }}</span>
            <select v-model="newPermission">
              <option value="read">只读</option>
              <option value="edit">可编辑</option>
            </select>
            <button @click="addCollaborator(user.id, user.username)">添加</button>
          </div>
        </div>
      </div>

      <div class="permission-list">
        <div v-for="perm in list" :key="perm.id" class="permission-item">
          <div class="permission-user">
            <span class="username">{{ perm.username || perm.userId }}</span>
            <span v-if="perm.userId === ownerId" class="owner-badge">所有者</span>
          </div>
          <div class="permission-actions">
            <select
                v-if="perm.userId !== ownerId"
                v-model="perm.permission"
                @change="updatePermission(perm.userId, perm.permission)"
            >
              <option value="read">只读</option>
              <option value="edit">可编辑</option>
            </select>
            <button
                v-if="perm.userId !== ownerId"
                class="btn-remove"
                @click="removeCollaborator(perm.userId)"
            >
              移除
            </button>
            <span v-else class="manage-badge">管理</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  docId: String
})

const emit = defineEmits(['close', 'success', 'error'])

const list = ref([])
const ownerId = ref('')
const searchKeyword = ref('')
const searchResults = ref([])
const newPermission = ref('read')
const loading = ref(false)

const close = () => {
  emit('close')
  searchKeyword.value = ''
  searchResults.value = []
}

const loadPermissions = async () => {
  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch(`http://localhost:8080/api/permission/${props.docId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    list.value = await res.json()
  } catch {
    emit('error', '加载权限列表失败')
  }
}

const fetchOwner = async () => {
  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch(`http://localhost:8080/api/document/${props.docId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await res.json()
    ownerId.value = data.ownerId
  } catch {}
}

const searchUsers = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch(`http://localhost:8080/api/user/search?keyword=${encodeURIComponent(searchKeyword.value)}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const users = await res.json()
    const existingIds = list.value.map(p => p.userId)
    searchResults.value = users.filter(u => !existingIds.includes(u.id) && u.id !== ownerId.value)
  } catch {}
}

const addCollaborator = async (userId, username) => {
  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch(`http://localhost:8080/api/permission/grant`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        docId: props.docId,
        userId,
        permission: newPermission.value
      })
    })
    if (res.ok) {
      emit('success', `已添加 ${username} 为协作者`)
      searchKeyword.value = ''
      searchResults.value = []
      await loadPermissions()
    } else {
      emit('error', '添加失败')
    }
  } catch {
    emit('error', '添加失败')
  }
}

const updatePermission = async (userId, permission) => {
  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch(`http://localhost:8080/api/permission/grant`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        docId: props.docId,
        userId,
        permission
      })
    })
    if (res.ok) {
      emit('success', '权限已更新')
    } else {
      emit('error', '更新失败')
      await loadPermissions()
    }
  } catch {
    emit('error', '更新失败')
    await loadPermissions()
  }
}

const removeCollaborator = async (userId) => {
  if (!confirm('确定要移除此协作者吗？')) return
  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch(`http://localhost:8080/api/permission/revoke`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        docId: props.docId,
        userId
      })
    })
    if (res.ok) {
      emit('success', '已移除协作者')
      await loadPermissions()
    } else {
      emit('error', '移除失败')
    }
  } catch {
    emit('error', '移除失败')
  }
}

// 打开时加载数据
watch(() => props.show, async (newVal) => {
  if (newVal) {
    await fetchOwner()
    await loadPermissions()
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
.permission-modal {
  width: 400px;
  max-width: 90%;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.add-collaborator {
  margin-bottom: 20px;
  position: relative;
}
.add-collaborator input {
  width: 380px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
}
.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 10;
}
.search-result-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid #eee;
}
.search-result-item span {
  flex: 1;
}
.permission-list {
  max-height: 300px;
  overflow-y: auto;
}
.permission-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}
.permission-user .username {
  font-weight: 500;
}
.owner-badge, .manage-badge {
  background: #ff9800;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  margin-left: 8px;
}
.permission-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
.permission-actions select {
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #ddd;
}
.btn-remove {
  background: #ffebee;
  color: #c62828;
  border: none;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
}
</style>