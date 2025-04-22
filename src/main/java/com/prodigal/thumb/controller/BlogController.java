package com.prodigal.thumb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prodigal.thumb.common.BaseResult;
import com.prodigal.thumb.common.PageRequest;
import com.prodigal.thumb.common.ResultUtils;
import com.prodigal.thumb.model.dto.BlogQueryDto;
import com.prodigal.thumb.model.entity.Blog;
import com.prodigal.thumb.model.vo.BlogVO;
import com.prodigal.thumb.service.BlogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-like
 * @date 2025/4/18 16:50
 * @description: TODO
 */
@RestController
@RequestMapping("/blog")
public class BlogController {
    @Resource
    private BlogService blogService;

    @GetMapping("/get")
    public BaseResult<BlogVO> get(long blogID, HttpServletRequest request){
        return ResultUtils.success(blogService.getBlogVOByID(blogID, request));
    }
    @GetMapping("/list")
    public BaseResult<List<BlogVO>> list(HttpServletRequest request){
        List<Blog> blogList = blogService.list();
        List<BlogVO> blogVOlist = blogService.getBlogVOlist(blogList, request);
        return ResultUtils.success(blogVOlist);
    }

    /**
     * 分页查询
     * @param blogQueryDto 查询条件
     * @param request 请求
     * @return 分页结果
     */
    @PostMapping("/page")
    public BaseResult<Page<BlogVO>> page(@RequestBody BlogQueryDto blogQueryDto, HttpServletRequest request){
        long current = blogQueryDto.getCurrent();
        long size = blogQueryDto.getPageSize()== 0 ? 20 : blogQueryDto.getPageSize();
        Page<Blog> blogPage = blogService.page(new Page<>(current, size), blogService.getQueryWrapper(blogQueryDto));
        List<BlogVO> blogVOList = blogService.getBlogVOList(blogPage.getRecords());
        //需根据用户是否登录，来判断是否查询点赞的状态
        Page<BlogVO> blogVOPage = new Page<>(blogPage.getCurrent(), blogPage.getSize(), blogPage.getTotal());
        blogVOPage.setRecords(blogVOList);
        return ResultUtils.success(blogVOPage);
    }
}
