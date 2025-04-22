package com.prodigal.thumb.model.enums;

import lombok.Getter;

/**
 * @author Lang
 * @version 1.0
 * @program: prodigal-thumb
 * @date 2025/4/18 21:22
 * @description: Lua脚本执行状态枚举
 */
@Getter
public enum LuaStatusEnum {
    SUCCESS(0L, "执行成功"),
    FAIL(-1L, "执行失败"),
    EXCEPTION(2L, "执行异常");

    private final Long code;
    private final String message;

    LuaStatusEnum(Long code, String message) {
        this.code = code;
        this.message = message;
    }
}
