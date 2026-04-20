package com.collabdoc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.collabdoc.entity.Document;
import org.apache.ibatis.annotations.Mapper;

@Mapper //该注解可不写
public interface DocumentMapper extends BaseMapper<Document> {
    //接口虽为空，但是继承了父接口BaseMapper的所有方法
}
