package com.lolwm.bettertnttag.client.model;

public class Status {
    private boolean online;
    private String playing;
    private String mode;
    private String map;

    public Status() {}

    public Status(boolean online, String playing, String mode, String map) {
        this.online = online;
        this.playing = playing;
        this.mode = mode;
        this.map = map;
    }

    // Getters and setters
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getPlaying() { return playing; }
    public void setPlaying(String playing) { this.playing = playing; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getMap() { return map; }
    public void setMap(String map) { this.map = map; }

    @Override
    public String toString() {
        return "Status{" +
                "online=" + online +
                ", playing='" + playing + '\'' +
                ", mode='" + mode + '\'' +
                ", map='" + map + '\'' +
                '}';
    }
}
