package com.app.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private int status;
    private String message;
    private boolean success;
    private Object params;
    private T data;

    // 成功返回结果
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, null, true);
    }

    // 成功返回结果（带自定义消息）
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data, null, true);
    }

    // 失败返回结果
    public static <T> Result<T> error(int status, String msg) {
        return new Result<>(status, msg, null, null, false);
    }

    // 构造方法
    public Result(int code, String msg, T data, Object params, boolean success) {
        this.status = code;
        this.message = msg;
        this.data = data;
        this.params = params;
        this.success = success;
    }

    // Getters and Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
