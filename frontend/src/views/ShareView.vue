<template>
  <div class="share-container">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">
      <h2>❌ {{ error }}</h2>
      <button @click="goHome">返回首页</button>
    </div>
    <div v-else class="share-content">
      <div class="header">
        <h1>📄 {{ docTitle }}</h1>
        <div class="permission-badge">
          {{ permission === 'edit' ? '✏️ 可编辑' : '👀 只读'}}
        </div>
      </div>
      <div class="content">
        <pre>{{ content }}</pre>
      </div>
      <div class="footer">
        <button @click="goHome">返回首页</button>
        <button v-if="permission === 'edit'" @click="openInEditor">在编辑器中打开</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref,onMounted} from "vue";
import {useRoute,useRouter} from "vue-router";

const route = useRoute()
const router = useRouter()
const shareCode = route.params.shareCode

const loading = ref(true)
const error =ref('')
const docTitle = ref('')
const content = ref('')
const permission = ref('read')
const docId = ref('')

onMounted(async () => {
  try {
    //获取分享信息
    const res = await fetch(`http://localhost:8080/api/share/${shareCode}`)
    if (!res.ok){
      throw new Error('分享链接无效或已过期')
    }
    const shareInfo =await res.json()

    //获取文档内容
    const docRes = await fetch(`http://localhost:8080/api/document/public/${shareInfo.docId}`)
    if (!docRes.ok){
      throw new Error('文档不存在')
    }
    const doc = await docRes.json()

    docId.value = doc.id
    docTitle.value = doc.title
    content.value = doc.content || ''
    permission.value = shareInfo.permission
  }catch (err){
    error.value = err.message
  }finally {
    loading.value = false
  }
})

const goHome = () => {
  router.push('/')
}

const openInEditor = () => {
  router.push(`/editor/${docId.value}`)
}
</script>

<style scoped>
.share-container{
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
}
.header{
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}
.permission-badge{
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  background: #f0f0f0;
}
.content pre{
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.6;
}
.footer{
  margin-top: 30px;
  display: flex;
  gap: 16px;
  justify-content: center;
}
.footer button{
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.footer button:first-child{
  background: #f0f0f0;
}
.footer button:last-child{
  background: #4caf50;
  color: white;
}
.loading, .error{
  text-align: center;
  padding: 100px 20px;
}
.error button{
  margin-top: 20px;
  padding: 8px 24px;
  cursor: pointer;
}
</style>