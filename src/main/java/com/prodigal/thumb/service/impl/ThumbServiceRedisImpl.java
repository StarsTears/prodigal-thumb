package com.prodigal.thumb.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.thumb.constants.LuaRedisScriptConstant;
import com.prodigal.thumb.exception.BusinessException;
import com.prodigal.thumb.exception.ErrorCode;
import com.prodigal.thumb.exception.ThrowUtils;
import com.prodigal.thumb.mapper.ThumbMapper;
import com.prodigal.thumb.model.dto.DoThumbDto;
import com.prodigal.thumb.model.entity.Thumb;
import com.prodigal.thumb.model.entity.User;
import com.prodigal.thumb.model.enums.LuaStatusEnum;
import com.prodigal.thumb.service.ThumbService;
import com.prodigal.thumb.service.UserService;
import com.prodigal.thumb.utils.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-thumb
 * @date 2025/4/19 8:56
 * @description: 使用 reids 执行 Lua脚本
 */
@Slf4j
@Service("thumbServiceRedis")
@RequiredArgsConstructor
public class ThumbServiceRedisImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {
    private final UserService userService;
    private final RedisTemplate<String,Object> redisTemplate;
    @Override
    public Boolean doThumb(DoThumbDto doThumbDto, HttpServletRequest request) {
        ThrowUtils.throwIf(doThumbDto == null || doThumbDto.getBlogID() == null, new RuntimeException("参数错误"));
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.USER_NOT_LOGIN );
        Long blogID = doThumbDto.getBlogID();
        String timestamp = this.getTimestamp();
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timestamp);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());
        Long result = redisTemplate.execute(LuaRedisScriptConstant.THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogID);
        if (Objects.equals(LuaStatusEnum.FAIL.getCode(), result)){
            throw new BusinessException(60011,"用户已点赞");
        }
        return LuaStatusEnum.SUCCESS.getCode().equals(result);
    }

    @Override
    public Boolean undoThumb(DoThumbDto doThumbDto, HttpServletRequest request) {
        ThrowUtils.throwIf(doThumbDto == null || doThumbDto.getBlogID() == null, new RuntimeException("参数错误"));
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.USER_NOT_LOGIN );
        Long blogID = doThumbDto.getBlogID();
        String timestamp = getTimestamp();
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timestamp);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());
        Long result = redisTemplate.execute(LuaRedisScriptConstant.UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogID);
        if (Objects.equals(LuaStatusEnum.FAIL.getCode(), result)){
            throw new BusinessException(60010,"用户未点赞");
        }
        return LuaStatusEnum.SUCCESS.getCode().equals(result);
    }

    @Override
    public Boolean hasThumb(Long blogID, Long userID) {
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userID), blogID.toString());
    }

    private String getTimestamp(){
        DateTime nowDate = DateUtil.date();
        //获取当前时间最近的整数秒
        return DateUtil.format(nowDate, "HH:mm:")+(DateUtil.second(nowDate)/10)*10;
    }
}
