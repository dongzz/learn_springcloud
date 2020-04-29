package com.dongz.springcloud.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author dong
 * @date 2020/4/24 14:52
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public Result(Integer code, String message) {
        this(code, message, null);
    }
}
