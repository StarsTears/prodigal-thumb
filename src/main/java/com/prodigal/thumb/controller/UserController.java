package com.prodigal.thumb.controller;

import com.prodigal.thumb.common.BaseResult;
import com.prodigal.thumb.common.ResultUtils;
import com.prodigal.thumb.constants.UserConstant;
import com.prodigal.thumb.exception.ErrorCode;
import com.prodigal.thumb.exception.ThrowUtils;
import com.prodigal.thumb.model.dto.user.LoginUserDto;
import com.prodigal.thumb.model.dto.user.RegisterUserDto;
import com.prodigal.thumb.model.entity.User;
import com.prodigal.thumb.model.vo.UserVO;
import com.prodigal.thumb.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-like
 * @date 2025/4/18 16:25
 * @description: TODO
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @PostMapping("/register")
    public BaseResult<String> register(@RequestBody RegisterUserDto registerUserDto, HttpServletRequest request) {
        ThrowUtils.throwIf(registerUserDto == null, ErrorCode.PARAMS_ERROR);
        long register = userService.register(registerUserDto);
        return ResultUtils.success(String.valueOf(register));
    }
    @PostMapping("/login")
    public BaseResult<UserVO> login(@RequestBody LoginUserDto loginUserDto,HttpServletRequest request) {
        ThrowUtils.throwIf(loginUserDto == null,  ErrorCode.PARAMS_ERROR);
        UserVO userVO = userService.login(loginUserDto, request);
        return ResultUtils.success(userVO);
    }
    @PostMapping("/logout")
    public BaseResult<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.LOGIN_USER);
        return ResultUtils.success("退出登录");
    }
    @GetMapping("/get/login")
    public BaseResult<User> getLoginUser(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
        return ResultUtils.success(loginUser);
    }
}
