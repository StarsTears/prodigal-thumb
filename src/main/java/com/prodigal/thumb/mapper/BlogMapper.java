package com.prodigal.thumb.mapper;

import com.prodigal.thumb.model.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author Lang
 * @description 针对表【blog】的数据库操作Mapper
 * @createDate 2025-04-18 16:23:19
 * @Entity com.prodigal.like.model.entity.Blog
 */
public interface BlogMapper extends BaseMapper<Blog> {
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);
}




