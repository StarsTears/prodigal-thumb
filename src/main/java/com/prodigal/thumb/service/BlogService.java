package com.prodigal.thumb.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.thumb.model.dto.BlogQueryDto;
import com.prodigal.thumb.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.thumb.model.entity.User;
import com.prodigal.thumb.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author Lang
* @description 针对表【blog】的数据库操作Service
* @createDate 2025-04-18 16:23:19
*/
public interface BlogService extends IService<Blog> {
    BlogVO getBlogVO(Blog blog, User loginUser);
    BlogVO getBlogVOByID(long blogID, HttpServletRequest request);
    List<BlogVO> getBlogVOlist(List<Blog> blogs, HttpServletRequest request);

    Wrapper<Blog> getQueryWrapper(BlogQueryDto blogQueryDto);

    List<BlogVO> getBlogVOList(List<Blog> blogList);
}
