package com.lolwm.bettertnttag.client.model;

public class UserRequest {
    private String username;
    private String _id;

    public UserRequest() {}

    public UserRequest(String username) {
        this.username = username;
    }

    public UserRequest(String username, String _id) {
        this.username = username;
        this._id = _id;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    @Override
    public String toString() {
        return "UserRequest{" +
                "username='" + username + '\'' +
                ", _id='" + _id + '\'' +
                '}';
    }
}
