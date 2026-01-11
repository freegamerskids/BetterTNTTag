package com.lolwm.bettertnttag.client.model;

import java.util.List;

public class MultipleResponse {
    private boolean success;
    private String error;
    private Integer code;
    private List<UserWins> users;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public List<UserWins> getUsers() { return users; }
    public void setUsers(List<UserWins> data) { this.users = data; }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", error='" + error + '\'' +
                ", code=" + code +
                ", users=" + users +
                '}';
    }
}
