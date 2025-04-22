package com.prodigal.thumb.model.enums;

import lombok.Getter;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-thumb
 * @date 2025/4/18 21:18
 * @description: 点赞类型枚举
 */
@Getter
public enum ThumbTypeEnum {
    INCR(1),
    DECR(-1),
    NON(0);

    private final int value;

    ThumbTypeEnum(int value) {
        this.value = value;
    }
}
