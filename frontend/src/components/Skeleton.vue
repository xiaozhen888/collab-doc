<!--骨架屏组件-->
<!--用于在数据加载时显示占位效果-->
<template>
  <div class="skeleton-list">
    <div v-for="i in count" :key="i" class="skeleton-item">
      <div class="skeleton-title"></div>
      <div class="skeleton-meta"></div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  count:{type:Number,default:5}
})
</script>

<style scoped>
.skeleton-list{
  padding: 20px;
}
.skeleton-item{
  padding: 16px;/*容器内边距*/
  border-bottom: 1px solid #eee;
}
.skeleton-title{
  width: 60%;
  height: 20px;
  background: #e0e0e0;
  border-radius: 4px;
  margin-bottom: 8px;
  animation: pulse 1.5s ease-in-out infinite;
}
.skeleton-meta{
  width: 40%;
  height: 14px;
  background: #e0e0e0;
  border-radius: 4px;
  animation: pulse 1.5s ease-in-out infinite;
}
@keyframes pulse {
  0%,100% {opacity: 1;}
  50% {opacity: 0.5}
}
</style>