package com.prodigal.thumb.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-thumb
 * @date 2025/4/19 8:43
 * @description: 用户注册请求参数
 */
@Data
public class RegisterUserDto implements Serializable {
    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 密码
     */
    private String userPassword;
    /**
     * 确认密码
     */
    private String checkPassword;
}
