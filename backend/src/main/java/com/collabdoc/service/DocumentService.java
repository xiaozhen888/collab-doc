package com.collabdoc.service;

import com.collabdoc.entity.Document;
import com.collabdoc.mapper.DocumentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    //final:表示不可变，只能赋值一次
    private final DocumentMapper documentMapper;
    private final HistoryService historyService;
    private final PermissionService permissionService;

    //构造器注入：当Spring创建DocumentService实例时，会把DocumentMapper传进来
    public DocumentService(DocumentMapper documentMapper, HistoryService historyService, PermissionService permissionService){
        this.documentMapper = documentMapper;
        this.historyService = historyService;
        this.permissionService = permissionService;
    }

    //获取所有文档
    public List<Document> getAllDocuments(){
        return documentMapper.selectList(null);
    }

    //根据id获取文档
    public Document getDocumentById(String id){
        return documentMapper.selectById(id);
    }

    //创建新文档
    //创建文档时自动给所有者添加manage权限
    public Document createDocument(Document document,String userId){
        document.setId(java.util.UUID.randomUUID().toString());
        document.setOwnerId(userId);
        document.setCreateTime(LocalDateTime.now());
        document.setUpdateTime(LocalDateTime.now());
/*        if (document.getTitle() == null || document.getTitle().isEmpty()){
            document.setTitle("未命名文档");
        }*/
        documentMapper.insert(document);

        //给所有者添加manage权限
        permissionService.grantPermission(document.getId(), userId,"manage");

        return document;
    }

    //更新文档
    public Document updateDocument(Document document){
        document.setUpdateTime(LocalDateTime.now());
        documentMapper.updateById(document);
        return document;
    }

    //删除文档
    public void deleteDocument(String id){
        documentMapper.deleteById(id);
    }

    //保存文档内容
    public void saveContent(String docId,String content){
        Document doc = documentMapper.selectById(docId);    //根据docId从数据库查询文档，只查一次
        boolean isNew = (doc == null);

        if (isNew){
            doc = new Document();       //文档不存在则创建一个新的Document对象
            doc.setId(docId);
            doc.setTitle("新文档");
            doc.setCreateTime(LocalDateTime.now());
        }
        doc.setContent(content);        //更新内容为传入的content
        doc.setUpdateTime(LocalDateTime.now());         //设置更新时间

        //判断文档是否已存在数据库，不存在则插入，存在则更新
        if (isNew) documentMapper.insert(doc);
        else documentMapper.updateById(doc);

    }

    //获取文档内容
    public String getContent(String docId){
        Document doc = documentMapper.selectById(docId);
        return doc == null ? "":doc.getContent();
    }
}
