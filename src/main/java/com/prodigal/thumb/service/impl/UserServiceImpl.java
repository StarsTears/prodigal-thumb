package com.prodigal.thumb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.thumb.constants.UserConstant;
import com.prodigal.thumb.exception.BusinessException;
import com.prodigal.thumb.exception.ErrorCode;
import com.prodigal.thumb.model.dto.user.LoginUserDto;
import com.prodigal.thumb.model.dto.user.RegisterUserDto;
import com.prodigal.thumb.model.entity.User;
import com.prodigal.thumb.model.vo.UserVO;
import com.prodigal.thumb.service.UserService;
import com.prodigal.thumb.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
* @author Lang
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-04-18 16:19:34
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
    }

    @Override
    public UserVO getUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public long register(RegisterUserDto registerUserDto) {
        //参数校验
        if (registerUserDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (registerUserDto.getUserName().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度过短!");
        }
        if (registerUserDto.getUserPassword().length() < 6 || registerUserDto.getCheckPassword().length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短!");
        }
        if (!registerUserDto.getUserPassword().equals(registerUserDto.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致!");
        }
        //查询账户是否重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.eq(User::getUserName, registerUserDto.getUserName());
        Long count = this.baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "账户已存在!");
        }
        //密码加密存储
        String encryptPassword = this.getEncryptPassword(registerUserDto.getUserPassword());
        //插入数据
        User user = new User();
        user.setUserName(registerUserDto.getUserName());
        user.setUserName(encryptPassword);
        String userName = registerUserDto.getUserName();
        user.setUserName(StrUtil.isNotBlank(userName)?userName:user.getUserName());
        boolean isOk = this.save(user);
        if (!isOk) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败!");
        }
        return user.getId();
    }

    @Override
    public UserVO login(LoginUserDto loginUserDto, HttpServletRequest request) {
        //参数校验
        if (loginUserDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUserDto.getUserName().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户错误!");
        }
        if (loginUserDto.getPassword().length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误!");
        }
        //密码加密
        String encryptPassword = getEncryptPassword(loginUserDto.getPassword());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, loginUserDto.getUserName())
                .eq(User::getPassword, encryptPassword);
        User user = this.baseMapper.selectOne(wrapper);
        if (user == null) {
            log.error("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在或密码错误!");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.LOGIN_USER, user);
        // 4. 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的信息过期时间一致
//        StpKit.SPACE.login(user.getId());
//        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        return this.getUserVO(user);
    }

    /**
     * 密码加密
     *
     * @param password 密码
     * @return 密文
     */
    @Override
    public String getEncryptPassword(String password) {
        final String salt = "prodigal";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }
}




