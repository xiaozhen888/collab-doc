package com.collabdoc.controller;

import com.collabdoc.dto.DocumentDTO;
import com.collabdoc.entity.Document;
import com.collabdoc.request.CreateDocRequest;
import com.collabdoc.service.DocumentService;
import com.collabdoc.utils.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final DocumentService documentService;
    private final JwtUtil jwtUtil;

    public DocumentController(DocumentService documentService, JwtUtil jwtUtil){
        this.documentService = documentService;
        this.jwtUtil = jwtUtil;
    }

    //获取所有文档列表
    @GetMapping("/list")
    public List<DocumentDTO> list(@RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String userId = jwtUtil.getUserId(token);
        List<Document> docs = documentService.getAllDocuments();

        return docs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //获取单个文档
    @GetMapping("/{id}")
    public Document getById(@PathVariable String id){
        return documentService.getDocumentById(id);
    }

    //创建新文档
    @PostMapping("/create")
    public DocumentDTO create(@RequestBody CreateDocRequest request,@RequestHeader("Authorization") String authHeader){
        String userId = jwtUtil.getUserId(authHeader);
        Document doc = new Document();
        doc.setTitle(request.getTitle());
        Document created = documentService.createDocument(doc,userId);
        return convertToDTO(created);
    }

    private DocumentDTO convertToDTO(Document doc){
        DocumentDTO dto = new DocumentDTO();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setContent(doc.getContent());
        dto.setOwnerId(doc.getOwnerId());
        dto.setCreateTime(doc.getCreateTime());
        dto.setUpdateTime(doc.getUpdateTime());
        return dto;
    }

    private String getUserId(String authHeader){
        String token = authHeader.substring(7);
        return jwtUtil.getUserId(token);
    }

    //更新文档（重命名或修改内容）
    @PutMapping("/{id}")
    public Document update(@PathVariable String id,@RequestBody Document document){
        document.setId(id);
        return documentService.updateDocument(document);
    }

    //删除文档
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id){
        documentService.deleteDocument(id);
    }

    @GetMapping("/public/{docId}")
    public Document getPublicDocument(@PathVariable String docId){
        return documentService.getDocumentById(docId);
    }
}
