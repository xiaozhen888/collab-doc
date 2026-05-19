import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from "@/views/LoginView.vue";

//路由守卫:检查登录状态
//to:要跳转到的目标路由
//from:当前离开的路由
//next():放行函数
const requireAuth = (to,from,next) => {
  const token = sessionStorage.getItem('token')
  if (!token) next('/login')
  else next()
}

const router = createRouter({
  history: createWebHistory(),/*createWebHistory:使用干净的URL格式*/
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/',
      name: 'home',
      component: HomeView,
      beforeEnter: requireAuth,/*进入前执行路由守卫。需要登录*/
    },
    {
      path: '/editor/:docId',
      name: 'editor',
      component: () => import('../views/EditorView.vue'),/*懒加载，只有访问时才加载组件*/
      beforeEnter: requireAuth,
    },
    {
      path:'/share/:shareCode',/*分享码作为动态参数*/
      name: 'share',
      component: () => import('../views/ShareView.vue'),
    }
  ],
})

export default router
