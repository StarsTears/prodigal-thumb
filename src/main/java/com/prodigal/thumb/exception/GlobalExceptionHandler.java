package com.prodigal.thumb.exception;

import com.prodigal.thumb.common.BaseResult;
import com.prodigal.thumb.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description:
 **/
@Slf4j
// 在接口文档中隐藏
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResult<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException:{}", e);
        return ResultUtils.error(e.getCode(),e.getMessage());
    }
    @ExceptionHandler(RuntimeException.class)
    public BaseResult<?> RuntimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException:{}", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}
