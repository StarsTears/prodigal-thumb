package com.prodigal.thumb.controller;

import com.prodigal.thumb.common.BaseResult;
import com.prodigal.thumb.common.ResultUtils;
import com.prodigal.thumb.model.dto.DoThumbDto;
import com.prodigal.thumb.service.ThumbService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-like
 * @date 2025/4/18 17:19
 * @description: TODO
 */
@RestController
@RequestMapping("/thumb")
public class ThumbController {
    @Resource(name="thumbServiceLocalCache")
    private ThumbService thumbService;

    @PostMapping("/do")
    public BaseResult<Boolean> doThumb(@RequestBody DoThumbDto doThumbDto, HttpServletRequest request) {
        Boolean isOK = thumbService.doThumb(doThumbDto, request);
        return ResultUtils.success(isOK);
    }
    @PostMapping("/undo")
    public BaseResult<Boolean> undoThumb(@RequestBody DoThumbDto doThumbDto, HttpServletRequest request) {
        Boolean isOK = thumbService.undoThumb(doThumbDto, request);
        return ResultUtils.success(isOK);
    }
}
