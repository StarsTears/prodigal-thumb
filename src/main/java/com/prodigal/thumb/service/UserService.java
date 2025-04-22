package com.prodigal.thumb.service;

import com.prodigal.thumb.model.dto.user.LoginUserDto;
import com.prodigal.thumb.model.dto.user.RegisterUserDto;
import com.prodigal.thumb.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prodigal.thumb.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author Lang
* @description 针对表【user】的数据库操作Service
* @createDate 2025-04-18 16:19:34
*/
public interface UserService extends IService<User> {
    User getLoginUser(HttpServletRequest request);
    UserVO getUserVO(User user);

    long register(RegisterUserDto registerUserDto);

    UserVO login(LoginUserDto loginUserDto, HttpServletRequest request);

    String getEncryptPassword(String password);
}
