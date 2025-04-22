package com.prodigal.thumb.utils;

import com.prodigal.thumb.constants.ThumbConstant;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-like
 * @date 2025/4/18 17:58
 * @description: Redis Key Util
 */
public class RedisKeyUtil {
    /**
     * 获取用户点赞key
     * @param userID
     * @return
     */
    public static String getUserThumbKey(Long userID){
        return ThumbConstant.USER_THUMB_KEY_PREFIX+userID;
    }

    /**
     * 获取临时点赞key
     * @param time
     * @return
     */
    public static String getTempThumbKey(String time){
        return ThumbConstant.TEMP_THUMB_KEY_PREFIX.formatted(time);
    }
}
