package com.lolwm.bettertnttag.client.model;

import java.util.List;

public class MultipleRequest {
    private List<String> _id;

    public MultipleRequest() {}

    public MultipleRequest(List<String> _id) {
        this._id = _id;
    }

    // Getters and setters
    public List<String> getId() { return _id; }
    public void setId(List<String> _id) { this._id = _id; }

    @Override
    public String toString() {
        return "MultipleRequest{" +
                "_id=" + _id +
                '}';
    }
}
