package com.prodigal.thumb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.thumb.constants.ThumbConstant;
import com.prodigal.thumb.exception.BusinessException;
import com.prodigal.thumb.exception.ErrorCode;
import com.prodigal.thumb.exception.ThrowUtils;
import com.prodigal.thumb.manager.cache.CacheManager;
import com.prodigal.thumb.model.dto.DoThumbDto;
import com.prodigal.thumb.model.entity.Blog;
import com.prodigal.thumb.model.entity.Thumb;
import com.prodigal.thumb.model.entity.User;
import com.prodigal.thumb.service.BlogService;
import com.prodigal.thumb.service.ThumbService;
import com.prodigal.thumb.mapper.ThumbMapper;
import com.prodigal.thumb.service.UserService;
import com.prodigal.thumb.utils.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author Lang
 * @description 针对表【thumb】的数据库操作Service实现
 * @createDate 2025-04-18 16:23:12
 */
@Slf4j
@Service("thumbServiceLocalCache")
@RequiredArgsConstructor
//@Deprecated
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;
    private final BlogService blogService;
    private final TransactionTemplate transactionTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;

    @Override
    public Boolean doThumb(DoThumbDto doThumbDto, HttpServletRequest request) {
        ThrowUtils.throwIf(doThumbDto == null || doThumbDto.getBlogID() == null, new RuntimeException("参数错误"));
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.USER_NOT_LOGIN);
        //加锁
        synchronized (doThumbDto.getBlogID().toString().intern()) {
            //编程式事务
            return transactionTemplate.execute(status -> {
                Long blogID = doThumbDto.getBlogID();
                //判断blog 是否存在
//                boolean exists = lambdaQuery()
//                        .eq(Thumb::getUserid, loginUser.getId())
//                        .eq(Thumb::getBlogid, blogID)
//                        .exists();
                //从Redis中获取
                Boolean hassedThumb = this.hasThumb(blogID, loginUser.getId());
                ThrowUtils.throwIf(hassedThumb, new BusinessException(60011, "用户已经点赞"));
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogID)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();
                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogID);
                //更新成功后执行，需将其存入缓存redis
                boolean isOk = update && this.save(thumb);
                //点赞记录存入redis
                if (isOk) {
                    String key = RedisKeyUtil.getUserThumbKey(loginUser.getId());
                    String fieldKey = blogID.toString();
                    redisTemplate.opsForHash().put(key,fieldKey, thumb.getId());
                    //设置过期时间
                    redisTemplate.expire(key, 7, TimeUnit.DAYS);
                    cacheManager.putIfPresent(key, fieldKey, thumb.getId());
                }
                return isOk;
            });
        }
    }

    @Override
    public Boolean undoThumb(DoThumbDto doThumbDto, HttpServletRequest request) {
        ThrowUtils.throwIf(doThumbDto == null || doThumbDto.getBlogID() == null, new RuntimeException("参数错误"));
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.USER_NOT_LOGIN);
        //加锁
        synchronized (doThumbDto.getBlogID().toString().intern()) {
            //编程式事务
            return transactionTemplate.execute(status -> {
                Long blogID = doThumbDto.getBlogID();
                //判断blog 是否存在
                String key = RedisKeyUtil.getUserThumbKey(loginUser.getId());
//                Long thumbID = (Long) redisTemplate.opsForHash().get(key, blogID.toString());
                //后续使用定期从数据库同步至redis中
//                if (thumbID == null){
//                      Thumb thumb = lambdaQuery()
//                                .eq(Thumb::getUserid, loginUser.getId())
//                                .eq(Thumb::getBlogid, blogID)
//                                .one();
//                      if (thumb != null){
//                          thumbID = thumb.getId();
//                      }
//                }
                Object thumbObjId = cacheManager.get(key, blogID.toString());
                ThrowUtils.throwIf(thumbObjId == null||thumbObjId.equals(ThumbConstant.UN_THUMB_CONSTANT), new BusinessException(60010, "用户未点赞"));

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogID)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();
                //更新成功后执行
                boolean isOK = update && this.removeById(Long.valueOf(thumbObjId.toString()));
                //从 reids 中移除
                if (isOK) {
                    redisTemplate.opsForHash().delete(key, blogID.toString());
                    cacheManager.putIfPresent(key, blogID.toString(), ThumbConstant.UN_THUMB_CONSTANT);
                }
                return isOK;
            });
        }
    }

    @Override
    public Boolean hasThumb(Long blogID, Long userID) {
        String key = RedisKeyUtil.getUserThumbKey(userID);
//        return redisTemplate.opsForHash().hasKey(key, blogID.toString());
        //改为从缓存中获取
        Object thumbObj = cacheManager.get(key, blogID.toString());
        if(thumbObj == null){
            return false;
        }
        Long thumbId = Long.valueOf(thumbObj.toString());
        return !thumbId.equals(ThumbConstant.UN_THUMB_CONSTANT);
    }
}




