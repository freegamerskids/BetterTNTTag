package com.lolwm.bettertnttag.client.model;

import java.util.List;

public class Leaderboard {
    private List<User> winsLeaderboard;
    private List<User> killsLeaderboard;
    private List<User> deathsLeaderboard;
    private List<User> powerupsLeaderboard;
    private List<User> tagsLeaderboard;

    public Leaderboard() {}

    public Leaderboard(List<User> winsLeaderboard, List<User> killsLeaderboard, List<User> deathsLeaderboard,
                      List<User> powerupsLeaderboard, List<User> tagsLeaderboard) {
        this.winsLeaderboard = winsLeaderboard;
        this.killsLeaderboard = killsLeaderboard;
        this.deathsLeaderboard = deathsLeaderboard;
        this.powerupsLeaderboard = powerupsLeaderboard;
        this.tagsLeaderboard = tagsLeaderboard;
    }

    // Getters and setters
    public List<User> getWinsLeaderboard() { return winsLeaderboard; }
    public void setWinsLeaderboard(List<User> winsLeaderboard) { this.winsLeaderboard = winsLeaderboard; }

    public List<User> getKillsLeaderboard() { return killsLeaderboard; }
    public void setKillsLeaderboard(List<User> killsLeaderboard) { this.killsLeaderboard = killsLeaderboard; }

    public List<User> getDeathsLeaderboard() { return deathsLeaderboard; }
    public void setDeathsLeaderboard(List<User> deathsLeaderboard) { this.deathsLeaderboard = deathsLeaderboard; }

    public List<User> getPowerupsLeaderboard() { return powerupsLeaderboard; }
    public void setPowerupsLeaderboard(List<User> powerupsLeaderboard) { this.powerupsLeaderboard = powerupsLeaderboard; }

    public List<User> getTagsLeaderboard() { return tagsLeaderboard; }
    public void setTagsLeaderboard(List<User> tagsLeaderboard) { this.tagsLeaderboard = tagsLeaderboard; }

    @Override
    public String toString() {
        return "Leaderboard{" +
                "winsLeaderboard=" + winsLeaderboard +
                ", killsLeaderboard=" + killsLeaderboard +
                ", deathsLeaderboard=" + deathsLeaderboard +
                ", powerupsLeaderboard=" + powerupsLeaderboard +
                ", tagsLeaderboard=" + tagsLeaderboard +
                '}';
    }
}
