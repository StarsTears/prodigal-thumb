package com.prodigal.thumb.service;

import com.prodigal.thumb.model.dto.DoThumbDto;
import com.prodigal.thumb.model.entity.Thumb;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author Lang
* @description 针对表【thumb】的数据库操作Service
* @createDate 2025-04-18 16:23:12
*/
public interface ThumbService extends IService<Thumb> {
    /**
     * 点赞
     * @param doThumbDto
     * @param request
     * @return
     */
    Boolean doThumb(DoThumbDto doThumbDto, HttpServletRequest request);

    /**
     * 取消点赞
     * @param doThumbDto
     * @param request
     * @return
     */
    Boolean undoThumb(DoThumbDto doThumbDto, HttpServletRequest request);

    /**
     * 判断用户是否点赞
     * @param blogID
     * @param userID
     * @return
     */
    Boolean hasThumb(Long blogID,Long userID);
}
