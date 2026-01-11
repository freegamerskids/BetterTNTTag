package com.lolwm.bettertnttag.client.model;

public class StatusRequest {
    private String _id;

    public StatusRequest() {}

    public StatusRequest(String _id) {
        this._id = _id;
    }

    // Getters and setters
    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    @Override
    public String toString() {
        return "StatusRequest{" +
                "_id='" + _id + '\'' +
                '}';
    }
}
