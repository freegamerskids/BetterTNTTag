package com.lolwm.bettertnttag.client.model;

public class NamesRequest {
    private String _id;

    public NamesRequest() {}

    public NamesRequest(String _id) {
        this._id = _id;
    }

    // Getters and setters
    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    @Override
    public String toString() {
        return "NamesRequest{" +
                "_id='" + _id + '\'' +
                '}';
    }
}
