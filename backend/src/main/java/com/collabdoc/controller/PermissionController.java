package com.collabdoc.controller;

import com.collabdoc.entity.Document;
import com.collabdoc.service.PermissionService;
import com.collabdoc.utils.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {
    private final PermissionService permissionService;
    private final JwtUtil jwtUtil;

    public PermissionController(PermissionService permissionService,JwtUtil jwtUtil){
        this.permissionService = permissionService;
        this.jwtUtil = jwtUtil;
    }

    //授予权限
    @PostMapping("/grant")
    public Map<String,String> grant(@RequestBody Map<String,String> body, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String userId = jwtUtil.getUserId(token);

        String docId = body.get("docId");
        String targetUserId = body.get("userId");
        String permission = body.get("permission");

        permissionService.grantPermission(docId,targetUserId,permission);

        Map<String,String> result = new HashMap<>();
        result.put("message","success");
        return result;
    }

    //撤销权限
    @PostMapping("/revoke")
    public Map<String,String> revoke(@RequestBody Map<String,String> body){
        String docId = body.get("docId");
        String userId = body.get("userId");

        permissionService.revokePermission(docId,userId);

        Map<String,String> result = new HashMap<>();
        result.put("message","success");
        return result;
    }

    //获取文档权限列表
    @GetMapping("/{docId}")
    public List<?> getDocPermissions(@PathVariable String docId){
        return permissionService.getDocPermissions(docId);
    }
}
