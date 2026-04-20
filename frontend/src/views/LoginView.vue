<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>{{ isLogin ? '登录' : '注册'}}</h2>

      <input v-model="username" type="text" placeholder="用户名" />
      <input v-model="password" type="password" placeholder="密码" />
      <input v-if="!isLogin" v-model="email" type="email" placeholder="邮箱（选填）" />

      <button @click="submit">{{ isLogin ? '登录' : '注册'}}</button>

      <p @click="isLogin = !isLogin" class="switch">
        {{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}
      </p>
    </div>
  </div>
</template>

<script setup>
import {ref} from "vue";
import {useRouter} from "vue-router";
import {userApi} from "@/api/user.js";
import { inject } from "vue";

const router = useRouter()
const isLogin = ref(true)
const username = ref('')
const password = ref('')
const email = ref('')

const toast = inject('toast')
const loading = inject('loading')

const submit = async () => {
  try {
    let res
    if (isLogin.value) {
      res = await userApi.login({ username: username.value, password: password.value })
      console.log('完整响应:', res)
      console.log('res.data:', res.data)
      console.log('token原始值:', JSON.stringify(res.data.token))
      console.log('userId原始值:', JSON.stringify(res.data.userId))
      //登录成功后保存token和userId
      sessionStorage.setItem('token',res.data.token.trim())
      sessionStorage.setItem('userId',res.data.userId.trim())
      toast.success('登录成功')
    } else {
      await userApi.register({ username: username.value, password: password.value, email: email.value })
      res = await userApi.login({ username: username.value, password: password.value })
      //注册并登录成功后保存token和userId
      sessionStorage.setItem('token',res.data.trim())
      sessionStorage.setItem('userId',res.data.userId.trim())
      toast.success('注册成功，已自动登录')
    }
    router.push('/')
  } catch (error) {
    console.error('错误:', error)

    // 获取错误信息
    let errorMsg = '操作失败'

    if (error.response) {
      // 尝试从不同位置获取错误信息
      const data = error.response.data

      if (typeof data === 'string') {
        errorMsg = data
      } else if (data && typeof data === 'object') {
        errorMsg = data.message || data.error || JSON.stringify(data)
      }

      // 根据关键词提取友好提示
      if (errorMsg.includes('用户不存在')) {
        toast.error('用户不存在')
      } else if (errorMsg.includes('用户名或密码错误')) {
        toast.error('用户名或密码错误')
      } else if (errorMsg.includes('用户名已存在')) {
        toast.error('用户名已存在，请换一个')
      } else if (error.response.status === 500) {
        toast.error('服务器错误，请稍后重试')
      } else {
        toast.error(errorMsg)
      }
    } else {
      toast.error('网络错误，请检查连接')
    }
  }
}
</script>

<style scoped>
.auth-container{
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}
.auth-card{
  background: white;
  padding: 40px;
  border-radius: 12px;
  width: 320px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}
.auth-card h2{
  margin: 0 0 20px 0;
  text-align: center;
}
.auth-card input{
  width: 100%;
  padding: 10px;
  margin-bottom: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
}
.auth-card button{
  width: 100%;
  padding: 10px;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
}
.switch{
  text-align: center;
  margin-top: 16px;
  color: #4caf50;
  cursor: pointer;
}
</style>