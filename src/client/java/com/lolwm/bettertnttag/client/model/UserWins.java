package com.lolwm.bettertnttag.client.model;

public class UserWins {
    private String uuid;
    private int wins;

    public UserWins() {}

    public UserWins(String uuid, int wins) {
        this.uuid = uuid;
        this.wins = wins;
    }

    // Getters and setters
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    @Override
    public String toString() {
        return "UserWins{" +
                "uuid='" + uuid + '\'' +
                ", wins=" + wins +
                '}';
    }
}
