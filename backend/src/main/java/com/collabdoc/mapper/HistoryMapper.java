package com.collabdoc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.collabdoc.entity.DocHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HistoryMapper extends BaseMapper<DocHistory> {

    //对版本号进行操作的方法baseMapper中没有要自己写
    //baseMapper中只提供了基础CRUD,

    //@Select（...）告诉MyBatis这是一个查询操作
    //@Param("docId")：把方法参数docId的值绑定到SQL语句中的#{docId}，将来传值进来会替换
    //该方法的作用是根据文档ID查询该文档的所有版本历史，按版本号从大到小排序
    @Select("select * from doc_history where doc_id = #{docId} order by version DESC")
    List<DocHistory> findByDocId(@Param("docId") String docId);

    //获取某个文档当前最大的版本号，用于生成下一个版本号
    @Select("select max(version) from doc_history where doc_id = #{docId}")
    Integer getMaxVersion(@Param("docId") String docId);
}
