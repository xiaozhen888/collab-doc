import axios from "axios";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
})

//添加请求拦截器，自动携带token
api.interceptors.request.use((config) => {
    let token = sessionStorage.getItem('token')
    if (token){
        token = token.trim()
        config.headers.Authorization = `Bearer ${token}`
    }
    console.log('请求头：',config.headers)
    return config
},(error) => {
    return Promise.reject(error)
})
export const documentApi = {
    //获取所有文档
    getList(){
        return api.get('/document/list')
    },

    //获取单个文档
    getById(id){
        return api.get(`/document/${id}`)
    },

    //创建文档
    create(title){
        return api.post('/document/create', { title })
    },

    //更新文档（重命名）
    update(id,title){
        return api.put(`/document/${id}`,{ title })
    },

    //删除文档
    delete(id){
        return api.delete(`/document/${id}`)
    },
}