<!--全局加载组件-->
<!--在执行耗时操作时显示，防止用户重复点击-->
<template>
  <div v-if="visible" class="loading-overlay">
    <div class="loading-spinner">
      <div class="spinner"></div>
      <p>{{ text }}</p>
    </div>
  </div>
</template>

<script setup>
import {ref} from "vue";

const visible = ref(false)
const text = ref('加载中...')

let timer = null

const show = (msg = '加载中...',duration = 0) => {/*duration：自动关闭时间（毫秒）*/
  text.value =msg
  visible.value = true

  if (duration > 0){
    if (timer) clearTimeout(timer)/*清楚之前的定时器*/
    timer = setTimeout(() => {/*设置新定时器*/
      visible.value = false
    },duration)
  }
}

const hide = () =>
{
  visible.value = false
  if (timer) clearTimeout(timer)
}

//暴露方法，使得父组件额可以通过ref调用
defineExpose({show,hide})
</script>

<style scoped>
.loading-overlay{
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}
.loading-spinner{
  background: white;
  padding: 24px 32px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}
.spinner{
  width: 40px;
  height: 40px;
  margin: 0 auto 16px;
  border: 3px solid #f0f0f0;
  border-top-color: #4caf50;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg)}
}
.loading-spinner p {
  margin: 0;
  color: #333;
  font-size: 14px;
}
</style>