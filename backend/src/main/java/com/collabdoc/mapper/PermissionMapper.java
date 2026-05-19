package com.collabdoc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.collabdoc.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    @Select("SELECT * FROM permission where doc_id = #{docId}")
    List<Permission> findByDocId(@Param("docId") String docId);

    @Select("select * from permission where doc_id = #{docId} and user_id = #{userId}")
    Permission findByDocIdAndUserId(@Param("docId") String docId, @Param("userId") String userId);
}
