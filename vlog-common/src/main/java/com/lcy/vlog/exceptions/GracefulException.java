package com.lcy.vlog.exceptions;

import com.lcy.vlog.graceful.result.ResponseStatusEnum;

/**
 * 优雅的处理异常，统一封装
 */
public class GracefulException {

    public static void display(ResponseStatusEnum responseStatusEnum) {
        throw new MyCustomException(responseStatusEnum);
    }

}
