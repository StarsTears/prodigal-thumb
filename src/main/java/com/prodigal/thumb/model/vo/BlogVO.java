package com.prodigal.thumb.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.json.JSONPropertyIgnore;

import java.util.Date;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-like
 * @date 2025/4/18 16:41
 * @description: 博客信息脱敏实体
 */
@Data
public class BlogVO {
    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 封面
     */
    private String coverimg;

    /**
     * 内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer thumbCount;
    /**
     * 用户id
     */
    private Long userid;
    /**
     * 用户脱敏信息
     */
    private UserVO user;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否点赞
     */
    private Boolean hasThumb;

}
