<template>
    <div class="home">
      <div class="header">
        <h1>📄 我的文档</h1>
        <div class="header-actions">
          <div class="search-box">
            <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索文档..."
                @input="handleSearch"
                class="search-input"
            />
            <span v-if="searchKeyword" class="search-clear" @click="clearSearch">✕</span>
          </div>
          <button class="btn-new" @click="openCreateModal">+ 新建文档</button>
        </div>
      </div>

      <!--骨架屏-->
      <Skeleton v-if="loadingDocs" :count="5"/>

      <!--新建文档对话框-->
      <div v-if="showCreateModal" class="modal">
        <div class="modal-content">
          <h3>新建文档</h3>
          <input v-model="newDocTitle" type="text" placeholder="文档名称" autofocus>

          <div class="modal-actions">
            <button
                @click="createNewDoc"
                :disabled="isCreating"
                :class="{ 'btn-disabled':isCreating}"
            >
              {{ isCreating ? '创建中...' : '创建'}}
            </button>
            <button @click="showCreateModal = false">取消</button>
          </div>
        </div>
      </div>

      <!--文档列表-->
      <div v-else class="doc-list">
        <div v-if="filteredDocuments.length === 0" class="empty">
          {{ searchKeyword ? '没有找到相关文档' : '暂无文档，点击上方按钮创建'}}
        </div>
        <div v-for="doc in filteredDocuments" :key="doc.id" class="doc-item">
          <div class="doc-info" @click="openDoc(doc.id)">
            <div class="doc-title" v-html="highlightTitle(doc.title)"></div>
            <div class="doc-id">ID: {{ doc.id }}</div>
            <div class="doc-meta">
              更新于 {{ formatDate(doc.updateTime)}}
            </div>
          </div>
          <div class="doc-actions">
            <button class="btn-rename" @click="renameDoc(doc)">✏️</button>
            <button class="btn-delete" @click="deleteDoc(doc.id)">🗑️</button>
          </div>
        </div>
      </div>
    </div>

    <!--重命名对话框-->
    <div v-if="showRenameModal" class="modal">
      <div class="modal-content">
        <h3>重命名文档</h3>
        <input v-model="renameTitle" type="text" placeholder="新名称" />
        <div class="modal-actions">
          <button @click="confirmRename">确定</button>
          <button @click="showRenameModal = false">取消</button>
        </div>
      </div>
    </div>

    <!--删除确认对话框-->
    <div v-if="showDeleteModal" class="modal">
      <div class="modal-content">
        <h3>确认删除</h3>
        <p>确认要删除这个文档吗？删除后无法恢复。</p>
        <div class="modal-actions">
          <button 
              @click="confirmDelete"
              :disabled="isDeleting"
              :class="{ 'btn-disabled':isDeleting}"
          >
            {{ isDeleting ? '删除中...':'确定删除' }}
          </button>
          <button @click="showDeleteModal = false">取消</button>
        </div>
      </div>
    </div>
</template>

<script setup>
import { useDocumentStore, useUserStore } from "@/store/index.js";
// import { storeToRefs } from 'pinia'
import { ref, computed, onMounted } from "vue";
import { useRouter } from 'vue-router';
import { documentApi } from "@/api/document.js";
import { inject } from "vue";
import Skeleton from "@/components/Skeleton.vue";

// Store
const userStore = useUserStore()
const documentStore = useDocumentStore()

// 使用 storeToRefs 保持响应式
// const { documents, loading: storeLoading } = storeToRefs(documentStore)

// 全局 Loading
const globalLoading = inject('loading')
const toast = inject('toast')

const router = useRouter();

// 本地状态
const showRenameModal = ref(false)
const renameTitle = ref('')
const renameId = ref('')
const showCreateModal = ref(false)
const newDocTitle = ref('')
const showDeleteModal = ref(false)
const deleteId = ref('')

const isCreating = ref(false)
const isDeleting = ref(false)

const loadingDocs = ref(true)
const searchKeyword = ref('')
const allDocuments = ref([])
const documents = ref([])
// 用户名
// const username = userStore.username

// 过滤后的文档列表
const filteredDocuments = computed(() => {
  if (!searchKeyword.value.trim()) {
    return allDocuments.value
  }
  const keyword = searchKeyword.value.toLowerCase().trim()
  return allDocuments.value.filter(doc =>
      doc.title.toLowerCase().includes(keyword) ||
      doc.id.toLowerCase().includes(keyword)
  )
})

// 高亮标题
const highlightTitle = (title) => {
  if (!searchKeyword.value.trim()) return title
  const keyword = searchKeyword.value.trim()
  const regex = new RegExp(`(${keyword})`, 'gi')
  return title.replace(regex, '<mark class="highlight">$1</mark>')
}

// 处理搜索
const handleSearch = () => {}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

// 获取新文档名称
const getNewDocTitle = () => {
  const existingTitles = allDocuments.value.map(doc => doc.title)
  if (!existingTitles.includes('新文档')) return '新文档'
  let maxNum = 0
  for (const title of existingTitles) {
    const match = title.match(/^新文档(?:\((\d+)\))?$/)
    if (match) {
      if (match[1]) {
        maxNum = Math.max(maxNum, parseInt(match[1]))
      } else {
        maxNum = Math.max(maxNum, 0)
      }
    }
  }
  return `新文档(${maxNum + 1})`
}

const openCreateModal = () => {
  newDocTitle.value = getNewDocTitle()
  showCreateModal.value = true
}

// 加载文档列表
const loadDocs = async () => {
  loadingDocs.value = true
  try {
    const res = await documentApi.getList()
    if (Array.isArray(res.data)) {
      allDocuments.value = [...res.data]
      documents.value = [...res.data]
    }
  } catch (error) {
    console.error('加载文档失败:', error)
    toast.error('加载文档失败')
  } finally {
    loadingDocs.value = false
  }
}

// 打开文档
const openDoc = (id) => {
  router.push(`/editor/${id}`)
}

// 新建文档
const createNewDoc = async () => {
  if (!newDocTitle.value.trim()) {
    toast.warning('请输入文档名称')
    return
  }
  if (isCreating.value) return
  isCreating.value = true
  globalLoading.show('创建文档中...')
  try {
    const res = await documentApi.create(newDocTitle.value.trim())
    showCreateModal.value = false
    newDocTitle.value = ''
    toast.success('文档创建成功')
    globalLoading.hide()
    router.push(`/editor/${res.data.id}`)
  } catch (error) {
    globalLoading.hide()
    toast.error('创建文档失败')
  } finally {
    isCreating.value = false
  }
}

// 重命名
const renameDoc = (doc) => {
  renameId.value = doc.id
  renameTitle.value = doc.title
  showRenameModal.value = true
}

const confirmRename = async () => {
  if (!renameTitle.value.trim()) return
  try {
    await documentApi.update(renameId.value, renameTitle.value)
    showRenameModal.value = false
    loadDocs()
  } catch (error) {
    console.error('重命名失败:', error)
  }
}

// 删除
const deleteDoc = (id) => {
  deleteId.value = id
  showDeleteModal.value = true
}

const confirmDelete = async () => {
  if (isDeleting.value) return
  isDeleting.value = true
  globalLoading.show('删除中...')
  try {
    await documentApi.delete(deleteId.value)
    showDeleteModal.value = false
    deleteId.value = ''
    toast.success('删除成功')
    loadDocs()
  } catch (error) {
    globalLoading.hide()
    toast.error('删除失败')
  } finally {
    isDeleting.value = false
    globalLoading.hide()

  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

onMounted(() => {
  loadDocs()
})
</script>

<style scoped>
.home{
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
}
.header{
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 16px;
}
.header-actions{
  display: flex;
  gap: 16px;
  align-items: center;
}
.search-box{
  position: relative;
  display: flex;
  align-items: center;
}
.search-input{
  padding: 8px 32px 8px 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  width: 200px;
  outline: none;
  transition: width 0.2s,border-color 0.2s;
}
.search-input:focus{
  width: 260px;
  border-color:#4caf50;
}
.search-clear{
  position: absolute;
  right: 10px;
  cursor: pointer;
  color: #999;
  font-size: 14px;
}
.search-clear:hover{
  color: #666;
}
:deep(.highlight){
  background-color: #ffeb3b;
  padding: 0 2px;
  border-radius: 2px ;
}
.btn-new{
  padding: 8px 16px;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
.btn-new:hover{
  background: #45a049;
}
.empty{
  text-align: center;
  color: #999;
  padding: 60px;
}
.doc-item{
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
  transition: background 0.2s;
}
.doc-item:hover{
  background: #f9f9f9;
}
.doc-info{
  flex: 1;
  cursor: pointer;
}
.doc-title{
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #333;
}
.doc-id{
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
  font-family: monospace;
}
.doc-meta{
  font-size: 12px;
  color: #999;
}
.doc-actions{
  display: flex;
  gap: 8px;
}
.btn-rename, .btn-delete{
  padding: 6px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.btn-rename{
  background: #f0f0f0;
}
.btn-rename:hover{
  background: #e0e0e0;
}
.btn-delete{
  background: #ffebee;
  color: #c62828;
}
.btn-delete:hover{
  background: #ffcdd2;
}
.btn-disabled{
  opacity: 0.6;
  cursor: not-allowed;
}
/*模态框*/
.modal{
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content{
  background: white;
  padding: 24px;
  border-radius: 12px;
  width: 320px;
}
.modal-content h3{
  margin: 0 0 16px 0;
}
.modal-content input{
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  margin-bottom: 16px;
}
.modal-content p {
  margin: 0 0 16px 0;
  line-height: 1.5;
  word-break: normal;
  white-space: normal;
}
.modal-actions{
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.modal-actions button{
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.modal-actions button:first-child{
  background: #4caf50;
  color: white;
}
.modal-actions button:last-child{
  background: #f0f0f0;
}
</style>