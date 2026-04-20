package com.collabdoc.service;

import com.collabdoc.entity.Document;
import com.collabdoc.entity.Permission;
import com.collabdoc.entity.User;
import com.collabdoc.mapper.DocumentMapper;
import com.collabdoc.mapper.PermissionMapper;
import com.collabdoc.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 权限服务
 *
 * 职责：
 * 1. 授予/撤销文档权限
 * 2. 检查用户对文档的权限
 * 3. 文档所有者自动拥有所有权限
 */
@Service
public class PermissionService {
    private final PermissionMapper permissionMapper;
    private final UserMapper userMapper;
    private final DocumentMapper documentMapper;
//    private final DocumentService documentService;


    private PermissionService (PermissionMapper permissionMapper, UserMapper userMapper, DocumentMapper documentMapper){
        this.permissionMapper = permissionMapper;
        this.userMapper = userMapper;
        this.documentMapper = documentMapper;
//        this.documentService = documentService;
    }

    //授予权限
    public void grantPermission(String docId,String userId,String permission){
        Permission existing = permissionMapper.findByDocIdAndUserId(docId,userId);
        if (existing!=null){
            existing.setPermission(permission);
            permissionMapper.updateById(existing);
        }else {
            Permission p = new Permission();
            p.setId(UUID.randomUUID().toString());
            p.setDocId(docId);
            p.setUserId(userId);
            p.setPermission(permission);
            p.setCreateTime(LocalDateTime.now());
            permissionMapper.insert(p);
        }
    }

    //授予权限
    public void revokePermission(String docId,String userId){
        Permission p = permissionMapper.findByDocIdAndUserId(docId,userId);
        if (p!=null){
            permissionMapper.deleteById(p.getId());
        }
    }

    /**
     * 检查用户是否有指定权限
     *
     * @param docId 文档 ID
     * @param userId 用户 ID
     * @param required 需要的权限（read/edit/manage）
     * @return 是否有权限
     */
    public boolean hasPermission(String docId,String userId,String required){
        //检查是否是文档所有者
        //直接查询数据库，不经过DocumentService
        Document doc = documentMapper.selectById(docId);
        if (doc != null && doc.getOwnerId() != null && doc.getOwnerId().equals(userId)){
            return true;    //所有者有所有权限
        }

        //检查权限表
        Permission p = permissionMapper.findByDocIdAndUserId(docId,userId);
        if (p == null) return false;

        String perm = p.getPermission();
        if ("manage".equals(perm)) return true;
        if ("edit".equals(perm) && "edit".equals(required)) return true;
        if ("read".equals(perm) && "read".equals(required)) return true;

        return false;
    }

    //获取文档的所有权限
    public List<Map<String, Object>> getDocPermissions(String docId){
        List<Permission> permissions = permissionMapper.findByDocId(docId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Permission p : permissions){
            Map<String,Object> item = new HashMap<>();
            item.put("id",p.getId());
            item.put("docId",p.getDocId());
            item.put("userId",p.getUserId());
            item.put("permission",p.getPermission());
            item.put("createTime",p.getCreateTime());

            //获取用户名
            User user = userMapper.selectById(p.getUserId());
            item.put("username",user != null ? user.getUsername() :p.getUserId());

            result.add(item);
        }
        return result;
    }
}
