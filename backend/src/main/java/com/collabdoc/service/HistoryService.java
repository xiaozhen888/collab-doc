package com.collabdoc.service;

import com.collabdoc.entity.DocHistory;
import com.collabdoc.mapper.HistoryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class HistoryService {

    //注入HistoryMapper类，spring自动创建实例对象
    private final HistoryMapper historyMapper;

    public HistoryService(HistoryMapper historyMapper) {
        this.historyMapper = historyMapper;
    }

    //保存版本
    public void saveVersion(String docId, String content, String userId) {
        Integer maxVersion = historyMapper.getMaxVersion(docId);
        int newVersion = (maxVersion == null) ? 1 : maxVersion + 1;

        DocHistory history = new DocHistory();
        history.setId(UUID.randomUUID().toString());
        history.setDocId(docId);
        history.setContent(content);
        history.setVersion(newVersion);
        history.setCreateBy(userId);
        history.setCreateTime(LocalDateTime.now());

        historyMapper.insert(history);
    }

    //获取版本列表
    public List<DocHistory> getVersionList(String docId) {
        return historyMapper.findByDocId(docId);
    }

    //获取指定版本
    public DocHistory getVersion(String docId, Integer version) {
        return historyMapper.selectList(null).stream()
                .filter(h -> h.getDocId().equals(docId) && h.getVersion().equals(version))
                .findFirst()
                .orElse(null);
    }
}
