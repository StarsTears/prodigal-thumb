package com.prodigal.thumb.model.dto.user;

import lombok.Data;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-like
 * @date 2025/4/18 17:37
 * @description: 用户登录请求实体类
 */
@Data
public class LoginUserDto {
    /**
     * 账户
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
}
