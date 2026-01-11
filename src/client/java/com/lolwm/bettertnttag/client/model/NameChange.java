package com.lolwm.bettertnttag.client.model;

import java.util.List;

public class NameChange {
    private String name;
    private long changedToAt;

    public NameChange() {}

    public NameChange(String name, long changedToAt) {
        this.name = name;
        this.changedToAt = changedToAt;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getChangedToAt() { return changedToAt; }
    public void setChangedToAt(long changedToAt) { this.changedToAt = changedToAt; }

    @Override
    public String toString() {
        return "NameChange{" +
                "name='" + name + '\'' +
                ", changedToAt=" + changedToAt +
                '}';
    }
}
