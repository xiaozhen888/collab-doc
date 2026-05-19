package com.collabdoc.service;

import com.collabdoc.entity.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;

    @Test
    void testSaveAndGetContent(){
        String docId = "test-doc-" + System.currentTimeMillis();
        String content = "这是测试内容\n第二行";

        //1.保存内容
        documentService.saveContent(docId,content);

        //2.获取内容
        String savedContent = documentService.getContent(docId);

        //3.验证
        assertEquals(content,savedContent);
    }

    @Test
    void testCreateAndGetDocument(){
        String userId = "test-user-001";

        //创建文档
        Document doc = new Document();
        doc.setTitle("测试文档");
        Document created = documentService.createDocument(doc,userId);

        //验证 ID 已生成
        assertNotNull(created.getId());

        //查询文档
        Document found = documentService.getDocumentById(created.getId());
        assertNotNull(found);
        assertEquals("测试文档",found.getTitle());

    }
}
