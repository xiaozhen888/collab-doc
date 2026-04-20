import axios from "axios";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
})

//注册和登录时，用户还没有token，不需要添加
//加了拦截器：不会报错，但会发送多余的Authorization: Bearer 头

export const userApi = {
    register(data){
        return api.post(`/user/register`,data)
    },
    login(data){
        console.log('发送登录请求：',data)
        return api.post('/user/login',data)
    },
}