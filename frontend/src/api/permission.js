import axios from "axios";
import {config} from "@vue/test-utils";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
})

//请求拦截器：自动添加token
api.interceptors.request.use((config) => {
    const token = sessionStorage.getItem('token')
    if (token){
        config.headers.Authorization = `Bearer ${token.trim()}`
    }
    return config
},(error) => {
    return Promise.reject(error)
})

export const permissionApi = {
    //授予权限
    grant(docId,userId,permission){
        return api.post('/permission/grant',{docId,userId,permission})
    },

    //撤销权限
    revoke(docId,userId){
        return api.post('/permission/revoke',docId,userId)
    },

    //获取文档的所有权限
    getDocPermissions(docId){
        return api.get(`/permission/${docId}`)
    },

    //检查用户是否有权限
    checkPermission(docId,userId,required){
        return api.get(`/permission/check/${docId}/${userId}/${required}`)
    }
}