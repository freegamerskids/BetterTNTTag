package com.lolwm.bettertnttag.client.model;

public class ApiResponse<T> {
    private boolean success;
    private String error;
    private Integer code;
    private T data;

    public ApiResponse() {}

    public ApiResponse(boolean success, String error, Integer code, T data) {
        this.success = success;
        this.error = error;
        this.code = code;
        this.data = data;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", error='" + error + '\'' +
                ", code=" + code +
                ", data=" + data +
                '}';
    }
}
