package com.prodigal.thumb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.thumb.model.dto.BlogQueryDto;
import com.prodigal.thumb.model.entity.Blog;
import com.prodigal.thumb.model.entity.Thumb;
import com.prodigal.thumb.model.entity.User;
import com.prodigal.thumb.model.vo.BlogVO;
import com.prodigal.thumb.model.vo.UserVO;
import com.prodigal.thumb.service.BlogService;
import com.prodigal.thumb.mapper.BlogMapper;
import com.prodigal.thumb.service.ThumbService;
import com.prodigal.thumb.service.UserService;
import com.prodigal.thumb.utils.RedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lang
 * @description 针对表【blog】的数据库操作Service实现
 * @createDate 2025-04-18 16:23:19
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    @Resource
    private UserService userService;

    @Resource(name="thumbServiceLocalCache")
    @Lazy
    private ThumbService thumbService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public BlogVO getBlogVOByID(long blogID, HttpServletRequest request) {
        Blog blog = this.getById(blogID);
        User loginUser = userService.getLoginUser(request);
        return this.getBlogVO(blog, loginUser);
    }

    @Override
    public BlogVO getBlogVO(Blog blog, User loginUser) {
        BlogVO blogVO = new BlogVO();
        BeanUtil.copyProperties(blog, blogVO);
        if (loginUser == null) {
            return blogVO;
        }
//        Thumb thumb = thumbService.lambdaQuery()
//                .eq(Thumb::getUserid, loginUser.getId())
//                .eq(Thumb::getBlogid, blog.getId()).one();
//        blogVO.setHasThumb(thumb != null);
        //将上述代码改为从redis缓存中获取
        Boolean hassedThumb = thumbService.hasThumb(blog.getId(), loginUser.getId());
        blogVO.setHasThumb(hassedThumb);
        blogVO.setUser(userService.getUserVO(loginUser));
        return blogVO;
    }

    @Override
    public List<BlogVO> getBlogVOlist(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<Long, Boolean> blogIdHasThumb = new HashMap<>();//存放每个博客是否被当前用户点赞
        if (ObjectUtil.isNotEmpty(loginUser)) {
            Set<Object> blogIdSet = blogList.stream().map(blog -> blog.getId().toString()).collect(Collectors.toSet());
//            List<Thumb> thumbs = thumbService.lambdaQuery()
//                    .eq(Thumb::getUserid, loginUser.getId())
//                    .in(Thumb::getBlogid, blogIdSet)
//                    .list();
//            thumbs.forEach(thumb -> blogIdHasThumb.put(thumb.getBlogid(), true));
            String key = RedisKeyUtil.getUserThumbKey(loginUser.getId());
            List<Object> thumbList = redisTemplate.opsForHash().multiGet(key, blogIdSet);
            for (Object thumb : thumbList) {
                if (ObjectUtil.isEmpty(thumb)) {
                    continue;
                }
                blogIdHasThumb.put(Long.parseLong(thumb.toString()), true);
            }
        }

        return blogList.stream().map(blog -> {
            BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
            blogVO.setHasThumb(!ObjectUtil.isNull(blogIdHasThumb.get(blog.getId())) && blogIdHasThumb.get(blog.getId()));
            blogVO.setUser(userService.getUserVO(loginUser));
            return blogVO;
        }).toList();
    }

    @Override
    public Wrapper<Blog> getQueryWrapper(BlogQueryDto blogQueryDto) {
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        String sortOrder = blogQueryDto.getSortOrder();
        String sortField = blogQueryDto.getSortField() == null ? "" : blogQueryDto.getSortField().trim();
        //关键字查询(标题，内容)
        if (StrUtil.isNotBlank(blogQueryDto.getSearchText())) {
            queryWrapper.and(e -> e.like(Blog::getTitle, blogQueryDto.getSearchText())
                    .or().like(Blog::getContent, blogQueryDto.getSearchText()));
        }
        queryWrapper.eq(ObjectUtil.isNotEmpty(blogQueryDto.getId()), Blog::getId, blogQueryDto.getId())
                .like(StrUtil.isNotBlank(blogQueryDto.getTitle()), Blog::getUserid, blogQueryDto.getTitle())
                .like(StrUtil.isNotBlank(blogQueryDto.getContent()), Blog::getContent, blogQueryDto.getContent());
        switch (sortField) {
            case "title":
                queryWrapper.orderBy(StrUtil.isNotBlank(blogQueryDto.getTitle()), StrUtil.equals(sortOrder, "ascend"), Blog::getTitle);
                break;
            case "content":
                queryWrapper.orderBy(StrUtil.isNotBlank(blogQueryDto.getContent()), StrUtil.equals(sortOrder, "ascend"), Blog::getContent);
                break;
            case "thumbCount":
                queryWrapper.orderBy(ObjectUtil.isNotEmpty(blogQueryDto.getThumbcount()), StrUtil.equals(sortOrder, "ascend"), Blog::getThumbCount);
                break;
            case "createTime":
                queryWrapper.orderBy(StrUtil.isNotEmpty(blogQueryDto.getSortField()), sortOrder.equals("ascend"), Blog::getCreateTime);
            case "updateTime":
                queryWrapper.orderBy(StrUtil.isNotEmpty(blogQueryDto.getSortField()), sortOrder.equals("ascend"), Blog::getUpdateTime);
                break;
            default:
                break;
        }
        return queryWrapper;
    }

    @Override
    public List<BlogVO> getBlogVOList(List<Blog> blogList) {
        if (CollUtil.isEmpty(blogList)) {
            return null;
        }
        //获取所有用户id
        Set<Long> userIdSet = blogList.stream().map(Blog::getUserid).collect(Collectors.toSet());
        Map<Long, List<User>> userIdMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        //遍历博客列表
//        LambdaQueryWrapper<Thumb> queryWrapper = new LambdaQueryWrapper<>();
//        for (Blog blog : blogList) {
//            queryWrapper.or().eq(Thumb::getBlogId, blog.getId()).eq(Thumb::getUserId, blog.getUserid());
//        }
//        List<Thumb> thumbList = thumbService.list(queryWrapper);


        //转换VO
        List<BlogVO> blogVOList = blogList.stream().map(blog -> this.getBlogVO(blog, null)).collect(Collectors.toList());
        blogVOList.forEach(blogVO -> {
            Long userid = blogVO.getUserid();
            if (userIdMap.containsKey(userid)) {
                //博客创建者信息
                UserVO userVO = userService.getUserVO(userIdMap.get(userid).get(0));
                blogVO.setUser(userVO);
                //若未登录，则点赞状态为false
                //登录。则根据点赞记录设置点赞状态
            }
        });

        return blogVOList;
    }

}




