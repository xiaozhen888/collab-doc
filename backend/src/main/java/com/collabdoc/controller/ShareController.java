package com.collabdoc.controller;

import com.collabdoc.entity.ShareLink;
import com.collabdoc.service.ShareService;
import com.collabdoc.utils.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/share")
public class ShareController {
    private final ShareService shareService;
    private final JwtUtil jwtUtil;

    public ShareController(ShareService shareService,JwtUtil jwtUtil){
        this.shareService=shareService;
        this.jwtUtil=jwtUtil;
    }

    //创建分享链接
    @PostMapping("/create")
    public Map<String,String> createShareLink(@RequestBody Map<String,String> body, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7); //去掉"Bearer "（7个字符），只取token
        String userId = jwtUtil.getUserId(token);

        String docId = body.get("docId");
        String permission = body.getOrDefault("permission","read");     //getOrDefault:如果请求体中有permission就用它，没有就用read
        int expireHours = Integer.parseInt(body.getOrDefault("expireHours","168")); //默认七天

        //把从POST请求中取出来的信息传给shareService.createShareLink，调用分享链接
        ShareLink share = shareService.createShareLink(docId,userId,permission,expireHours);

        //把业务层创建好的分享码和分享链接放进Map，返回给前端
        Map<String,String> result = new HashMap<>();
        result.put("shareCode", share.getShareCode());
        result.put("shareUrl","http://localhost:5173/share/" + share.getShareCode());
        return result;
    }

    //获取分享信息
    @GetMapping("/{shareCode}")
    public ShareLink getShareInfo(@PathVariable String shareCode){
        return shareService.getByShareCode(shareCode);
    }
}
