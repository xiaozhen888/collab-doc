import {defineStore} from "pinia";
import {documentApi} from "@/api/document.js";

export const useDocumentStore = defineStore('document',{
    state:() => ({
        documents:[],
        currentDoc:null,
        loading:false,
    }),

    getters:{
        //按标题搜索
        searchDocuments:(state) => (keyword) => {
            if (!keyword) return state.documents
            const lowerKeyword = keyword.toLowerCase()
            return state.documents.filter(doc =>
                doc.title.toLowerCase().includes(lowerKeyword))
        },
        //文档总数
        totalCount:(state) => state.documents.length,
    },

    actions:{
        //加载文档列表
        async loadDocuments(){
            this.loading = true
            try {
                const res = await documentApi.getList()
                this.documents = res.data || []
                return {success:true}
            }catch (error){
                console.error('加载文档失败：',error)
                return {success:false,message:'加载文档失败'}
            }finally {
                this.loading = false
            }
        },

        //创建文档
        async createDocument(title){
            try {
                const res = await documentApi.create(title)
                //刷新列表
                await this.loadDocuments()
                return {success:true,docId:res.data.id}
            }catch (error){
                console.error('创建文档失败：',error)
                return {success:false,message:'创建文档失败'}
            }
        },

        //重命名文档
        async renameDocument(id,title){
            try {
                await documentApi.update(id,title)
                //更新本地列表
                const doc = this.documents.find(d => d.id === id)
                if (doc){
                    doc.title = title
                }
                return { success:true}
            }catch (error){
                console.error('重命名文档失败：',error)
                return { success:false,message:'重命名失败'}
            }
        },

        //删除文档
        async deleteDocument(id){
            try {
                await documentApi.delete(id)
                //从本地列表删除
                this.documents = this.documents.filter(d => d.id === id)
                return {success:true}
            }catch (error){
                console.error('删除失败',error)
                return {success:false,message:'删除失败'}
            }
        },

        //设置当前文档
        setCurrentDoc(doc){
            this.currentDoc = doc
        },

        //清空当前文档
        clearCurrentDoc(){
            this.currentDoc = null
        },
    },

})