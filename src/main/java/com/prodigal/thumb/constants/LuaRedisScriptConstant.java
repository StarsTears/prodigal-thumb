package com.prodigal.thumb.constants;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-thumb
 * @date 2025/4/18 21:23
 * @description: Lua脚本
 */
public class LuaRedisScriptConstant {
    /**
     * 点赞 Lua 脚本
     * @author Lang
     * KEYS[1] --临时计数键
     * KEYS[2] --用户点赞状态键
     * ARGV[1] --用户 ID
     * ARGV[2] --博客 ID
     */
    public static final RedisScript<Long> THUMB_SCRIPT = new DefaultRedisScript<>("""
                        local tempThumbKey = KEYS[1]                --临时计数键（如：thumb:temp:{timestamp}）
                        local userThumbKey = KEYS[2]                --用户点赞状态键（如：thumb:{userID}）
                        local userID = ARGV[1]                      --用户 ID
                        local blogID = ARGV[2]                      --博客 ID
           
                       --1、检查是否已被点赞（避免重复操作）
                       if redis.call('hexists',userThumbKey,blogID) == 1 then
                            return -1  --已点赞，返回 -1 标识 失败
                        end
            
                        --2、获取旧值（不存在默认值 0）
                        local hashKey = userID..':'..blogID
                        local oldValue = tonumber(redis.call('hget', tempThumbKey, hashKey) or 0)
            
                        -- 3、计算新值
                        local newValue = oldValue + 1
                        
                        -- 4、原子性更新：写入临时计数+用户已标记点赞
                        redis.call('hset',tempThumbKey, hashKey, newValue)
                        redis.call('hset',userThumbKey,blogID,1)
                        return 0 --返回 0 表示成功
            """,Long.class);

    /**
     * 取消点赞 Lua 脚本
     * @author Lang
     * KEYS[1] --临时计数键
     * KEYS[2] --用户点赞状态键
     * ARGV[1] --用户 ID
     * ARGV[2] --博客 ID
     */
    public static final RedisScript<Long> UNTHUMB_SCRIPT = new DefaultRedisScript<>("""
                        local tempThumbKey = KEYS[1]                --临时计数键（如：thumb:temp:{timestamp}）
                        local userThumbKey = KEYS[2]                --用户点赞状态键（如：thumb:{userID}）
                        local userID = ARGV[1]                      --用户 ID
                        local blogID = ARGV[2]                      --博客 ID
           
                       --1、检查是否已被点赞（避免重复操作）
                       if redis.call('hexists',userThumbKey,blogID) ~= 1 then
                            return -1  --未点赞，返回 -1 标识 失败
                        end
            
                        --2、获取旧值（不存在默认值 0）
                        local hashKey = userID..':'..blogID
                        local oldValue = tonumber(redis.call('hget', tempThumbKey, hashKey) or 0)
            
                        -- 3、计算新值
                        local newValue = oldValue - 1
                        
                        -- 4、原子性更新：更新临时计数、删除用户点赞标记
                        redis.call('hset',tempThumbKey, hashKey, newValue)
                        redis.call('hdel',userThumbKey,blogID,1)
                        return 0 --返回 0 表示成功
            """,Long.class);

}
