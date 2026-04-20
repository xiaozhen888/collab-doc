<template>
  <div class="user-list">
    <h4>在线用户 ({{ users.length }})</h4>
    <div class="users">
      <div v-for="user in users" :key="user.id" class="user-item">
        <span class="user-avatar">{{ user.avatar || '👤' }}</span>
        <span class="user-name">{{ user.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const users = ref([
  { id: 1, name: '当前用户', avatar: '👤' }
])
</script>

<style scoped>
.user-list {
  padding: 16px;
  border-top: 1px solid #e0e0e0;
}
.user-list h4 {
  margin: 0 0 12px 0;
  font-size: 12px;
  color: #666;
}
.users {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.user-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.user-avatar {
  font-size: 16px;
}
.user-name {
  color: #333;
}
</style>