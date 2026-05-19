package com.collabdoc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.collabdoc.entity.ShareLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShareMapper extends BaseMapper<ShareLink> {

    @Select("select * from share_link where share_code = #{shareCode}")
    ShareLink findByShareCode(@Param("shareCode") String shareCode);
}
