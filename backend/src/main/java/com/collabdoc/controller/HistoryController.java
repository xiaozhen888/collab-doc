package com.collabdoc.controller;

import com.collabdoc.entity.DocHistory;
import com.collabdoc.service.HistoryService;
import com.collabdoc.utils.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    private final HistoryService historyService;
    private final JwtUtil jwtUtil;

    public  HistoryController(HistoryService historyService,JwtUtil jwtUtil){
        this.historyService = historyService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/{docId}")
    public List<DocHistory> getVersionList(@PathVariable String docId){
        return historyService.getVersionList(docId);
    }

    @GetMapping("/{docId}/{version}")
    public DocHistory getVersion(@PathVariable String docId,@PathVariable Integer version){
        return historyService.getVersion(docId,version);
    }

    @PostMapping("/save/{docId}")
    public Map<String,Object> saveVersion(@PathVariable String docId,
                            @RequestBody Map<String,String> body,
                            @RequestHeader("Authorization") String authHeader){     //为什么需要@RequestHeader("Authorization")，因为需要知道【是谁】在保存版本

        Map<String, Object> result = new HashMap<>();
        try {
            String token = authHeader.substring(7);
            String userId = jwtUtil.getUserId(token);

            String content = body.get("content");
            if (content == null){   //如果内容为空，返回400错误
                result.put("code",400);
                result.put("message","content is required");
                return result;
            }

            historyService.saveVersion(docId,content,userId);
            result.put("code",200);
            result.put("message","success");
        }catch (Exception e){
            result.put("code",500);
            result.put("message",e.getMessage());
        }
        return result;
    }
}
