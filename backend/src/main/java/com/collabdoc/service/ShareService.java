package com.collabdoc.service;

import com.collabdoc.entity.ShareLink;
import com.collabdoc.mapper.ShareMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShareService {
    private final ShareMapper shareMapper;

    public ShareService(ShareMapper shareMapper) {
        this.shareMapper = shareMapper;
    }

    //生成分享链接
    public ShareLink createShareLink(String docId, String userId, String permission, int expireHours) {
        ShareLink share = new ShareLink();
        share.setId(UUID.randomUUID().toString());
        share.setDocId(docId);

        //查重保护逻辑
        String code = generateShareCode();
        while (shareMapper.findByShareCode(code) != null) {
            code = generateShareCode();     //如果数据库里有了，就再生成一个新的
        }

        share.setShareCode(generateShareCode());    //调用generateShareCode()方法生成8位分享码
        share.setPermission(permission);
        share.setCreatedBy(userId);
        share.setCreateTime(LocalDateTime.now());
        share.setExpireTime(LocalDateTime.now().plusHours(expireHours));
        shareMapper.insert(share);
        return share;
    }

    //根据分享码获取分享信息
    public ShareLink getByShareCode(String shareCode) {
        return shareMapper.findByShareCode(shareCode);
    }

    //生成 8 位分享码
    private String generateShareCode() {
        //定义字符集：小写字母 + 大写字母 + 数字
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(); //创建StringBuilder，用于高校拼接字符串
        for (int i = 0; i < 8; i++) {    //循环8次，生成8位码
            //Math.random():生成0~1之间的随机小数
            //* chars.length()：得到0~62之间的随机数
            //最后转成整数得到随机索引
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));   //从字符集中取出对应位置的字符，追加到StringBuilder
        }
        return sb.toString();
    }
}
