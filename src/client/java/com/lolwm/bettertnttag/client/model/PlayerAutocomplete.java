package com.lolwm.bettertnttag.client.model;

public class PlayerAutocomplete {
    private String _id;
    private String username;

    public PlayerAutocomplete() {}

    public PlayerAutocomplete(String _id, String username) {
        this._id = _id;
        this.username = username;
    }

    // Getters and setters
    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Override
    public String toString() {
        return "PlayerAutocomplete{" +
                "_id='" + _id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
