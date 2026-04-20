import {defineStore} from "pinia";
import {userApi} from "@/api/user.js";

export const useUserStore = defineStore('user',{
    state: () => ({
        token: sessionStorage.getItem('token') || null,
        userId: sessionStorage.getItem('userId') || null,
        username: null,
        email:null,
        isLoggedIn: !!sessionStorage.getItem('token'),
    }),

    getters:{
        //获取用户头像首字母
        userInitial:(state) => {
            if (!state.username) return 'U'
            return state.username.charAt(0).toUpperCase()
        },
        //是否已登录
        isAuthenticated: (state) => state.isLoggedIn,
    },

    actions:{
        //登录
        async login(username,password){
            try {
                const res = await userApi.login({username,password})
                this.token = res.data.token
                this.userId = res.data.userId
                this.isLoggedIn = true

                //保存到sessionStorage
                sessionStorage.setItem('token',res.data.token)
                sessionStorage.setItem('userId',res.data.userId)

                //获取用户信息
                await this.fetchUserInfo()

                return {success:true}
            }catch (error) {
                console.error('登录失败',error)
                return { success:false,message:error.response?.data?.message || '登录失败'}
            }
        },

        //注册
        async register(username,password,email){
            try {
                const res = await userApi.register({ username,password,email})
                //注册成功后自动登录
                return await this.login(username,password)
            }catch (error){
                console.error('注册失败：',error)
                return { success:false,message:error.response?.data?.message ||'注册失败'}
            }
        },

        //获取用户信息
        async fetchUserInfo(){
            if (!this.userId) return
            try {
                const res = await userApi.getUserInfo(this.userId)
                this.username = res.data.username
                this.email = res.data.email
            }catch (error){
                console.error('获取用户信息失败：',error)
            }
        },

        //登出
        logout(){
            this.token = null;
            this.userId = null;
            this.username = null;
            this.email = null;
            this.isLoggedIn = false;

            sessionStorage.removeItem('token')
            sessionStorage.removeItem('userId')

            //跳转到登录页
            window.location.href = '/login'
        },

        //初始化（从sessionStorage恢复状态）
        init(){
            const token = sessionStorage.getItem('token')
            const userId = sessionStorage.getItem('userId')
            if (token && userId){
                this.token = token
                this.userId = userId
                this.isLoggedIn = true
                this.fetchUserInfo()
            }
        },
    },
})