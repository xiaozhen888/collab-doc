<template>
  <div class="toolbar">
    <div class="toolbar-left">
      <h2>📝 实时协作文档</h2>
      <div class="doc-id">文档 ID：{{ docId }}</div>
      <div class="doc-title-display">📄 {{ docTitle }}</div>
    </div>
    <div class="toolbar-right">
      <div class="status" :class="{ connected: isConnected }">
        {{ isConnected ? '● 已连接' : '○ 未连接' }}
      </div>
      <div class="online-info">
        <span class="online-count">👥 {{ connectionCount }} 个连接</span>
        <span class="online-user">👤 {{ userCount }} 个用户</span>

        <!-- 三个点菜单 -->
        <div class="dropdown">
          <button class="btn-menu" @click="showMenu = !showMenu">⋮</button>
          <div v-if="showMenu" class="dropdown-menu" @mouseenter="keepMenuOpen" @mouseleave="showMenu = false">
            <button @click="emit('openHistory')">📜 版本历史</button>
            <button @click="emit('openShare')">🔗 分享</button>
            <button @click="emit('openPermission')">👥 协作者</button>
            <div class="menu-divider"></div>
            <ExportMenu :doc-id="docId" @mouseenter="keepMenuOpen" />
          </div>
        </div>
      </div>

      <div class="save-status" :class="{ saved: isSaved }">
        {{ saveStatus }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ExportMenu from "@/components/ExportMenu.vue";

defineProps({
  docId: String,
  docTitle: String,
  isConnected: Boolean,
  connectionCount: Number,
  userCount: Number,
  isSaved: Boolean,
  saveStatus: String
})

const emit = defineEmits(['openHistory', 'openShare', 'openPermission', 'export'])

const showMenu = ref(false)
const showExportSubmenu = ref(false)
const keepMenuOpen = () => {
  showMenu.value = true
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.toolbar-left h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}
.doc-id {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.doc-title-display {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-top: 6px;
  padding-top: 4px;
  border-top: 1px dashed #eee;
}
.toolbar-right {
  display: flex;
  gap: 24px;
  align-items: center;
}
.status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: #f0f0f0;
  color: #666;
}
.status.connected {
  background: #d4edda;
  color: #155724;
}
.online-info {
  display: flex;
  gap: 12px;
  align-items: center;
}
.online-count, .online-user {
  font-size: 13px;
  color: #666;
  background: #f0f0f0;
  padding: 4px 12px;
  border-radius: 20px;
}
.save-status {
  font-size: 12px;
  color: #999;
  transition: color 0.2s;
  margin-left: 8px;
}
.save-status.saved {
  color: #28a745;
}
.btn-menu {
  padding: 4px 12px;
  background: #f0f0f0;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  line-height: 1;
}
.btn-menu:hover {
  background: #e0e0e0;
}
.dropdown {
  position: relative;
}
/* 添加一个透明桥接层，覆盖按钮和菜单之间的间隙 */
.dropdown::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  height: 8px;
  background: transparent;
}
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  z-index: 100;
  min-width: 150px;
}
.dropdown-menu button {
  display: block;
  width: 100%;
  padding: 10px 16px;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
}
.dropdown-menu button:hover {
  background: #f5f5f5;
}
.menu-divider {
  height: 1px;
  background: #eee;
  margin: 4px 0;
}
.submenu {
  position: relative;
}
.submenu-menu {
  position: absolute;
  left: 100%;
  top: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  min-width: 120px;
}
</style>