<template>
  <router-view />
  <Toast ref="toastRef"/>
  <Loading ref="loadingRef" />
</template>

<script setup>
import {ref,provide} from 'vue'
import Toast from "@/components/Toast.vue";
import Loading from './components/Loading.vue'

const toastRef = ref(null)
const loadingRef = ref(null)

//提供全局Toast方法
const toast = {
  info:(msg,duration) => toastRef.value?.show(msg,'info',duration),
  success:(msg,duration) => toastRef.value?.show(msg,'success',duration),
  error:(msg,duration) => toastRef.value?.show(msg,'error',duration),
  warning:(msg,duration) => toastRef.value?.show(msg,'warning',duration),
}

const loading = {
  show:(msg,duration) => loadingRef.value?.show(msg,duration),
  hide:() => loadingRef.value?.hide(),
}

provide('toast',toast)
provide('loading',loading)
</script>
<style scoped>
*{
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body{
  font-family: system-ui, -apple-system,  'Segoe UI', Roboto, Roboto, sans-serif;
}
</style>


<!--<template>
  <div class="app">
    <Header v-if="showHeader" />
    <div class="app-body">
      <Sidebar v-if="showSidebar" />
      <main class="app-main">
        <router-view />
      </main>
    </div>
  </div>
  <Toast ref="toastRef" />
  <Loading ref="loadingRef" />
</template>

<script setup>
import { computed, ref, provide } from 'vue'
import { useRoute } from 'vue-router'
import Toast from "@/components/Toast.vue";
import Loading from './components/Loading.vue'

const route = useRoute()
const showHeader = computed(() => route.path !== '/login')
const showSidebar = computed(() => route.path !== '/login')

const toastRef = ref(null)
const loadingRef = ref(null)

const toast = {
  info: (msg, duration) => toastRef.value?.show(msg, 'info', duration),
  success: (msg, duration) => toastRef.value?.show(msg, 'success', duration),
  error: (msg, duration) => toastRef.value?.show(msg, 'error', duration),
  warning: (msg, duration) => toastRef.value?.show(msg, 'warning', duration),
}

const loading = {
  show: (msg, duration) => loadingRef.value?.show(msg, duration),
  hide: () => loadingRef.value?.hide(),
}

provide('toast', toast)
provide('loading', loading)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
}
.app {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.app-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.app-main {
  flex: 1;
  overflow: auto;
}
</style>-->
