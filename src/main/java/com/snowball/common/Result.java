package com.snowball.common;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // 私有化构造方法，强制通过下面的静态方法创建对象
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 成功时调用（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    // 成功时调用（不带数据，比如删除成功）
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    // 失败时调用
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
    // 失败时调用（带具体错误数据，比如校验错误提示哪些字段错了）
    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

}
