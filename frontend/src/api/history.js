import axios from "axios";

const api = axios.create({
    // 基础URL，后面请求只需写路径，会自动拼接
    baseURL: import.meta.env.VITE_API_BASE_URL,
})

// 请求拦截器，在每个请求发送之前，自动添加token到请求头
api.interceptors.request.use(
    (config) => {
        const token = sessionStorage.getItem('token')   //从浏览器sessionStorage中取出token
        if (token) config.headers.Authorization = `Bearer ${token.trim()}`     //如果token存在，添加到请求头的Authorization字段  //格式：Bearer  + token
        return config
    },
    (error) => {
        return Promise.reject(error)
    })

//导出Api方法
export const historyApi = {
    getList(docId) {
        return api.get(`/history/${docId}`);
    },
    getVersion(docId,version){
        return api.get(`/history/${docId}/${version}`)
    },
}