package com.prodigal.thumb.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.prodigal.thumb.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-thumb
 * @date 2025/4/19 8:12
 * @description: blog查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BlogQueryDto extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 搜索词（同时搜标题、内容）
     */
    private String searchText;
    /**
     * 点赞数
     */
    private Integer thumbcount;

}
