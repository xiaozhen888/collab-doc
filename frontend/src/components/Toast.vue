<template>
  <Teleport to="body">
    <div v-if="visible" class="toast-container">
      <div class="toast" :class="type">
        {{ message }}
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import {ref} from "vue";

const visible =ref(false)
const message = ref('')
const type = ref('info')  //info,success,error,warning

let timer = null

const show = (text,toastType = 'info',duration = 3000) => {
  //清楚之前的定时器
  if (timer) clearTimeout(timer)

  message.value = text
  type.value = toastType
  visible.value=true

  timer = setTimeout(() => {
    visible.value = false
  },duration)
}

//暴露方法给全局使用
defineExpose({show})
</script>

<style scoped>
.toast-container{
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
}
.toast{
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  color: white;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.2);
  animation: fadeIn 0.3s ease;
}
.toast.info{
  background: #333;
}
.toast.success{
  background: #4caf50;
}
.toast.error{
  background: #f44336;
}
.toast.warning{
  background: #ff9800;
}
@keyframes fadeIn {
  from {opacity: 0;transform: translateY(-20px);}
  to{ opacity: 1;transform: translateY(0);}
}
</style>