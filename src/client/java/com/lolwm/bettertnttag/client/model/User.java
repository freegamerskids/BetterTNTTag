package com.lolwm.bettertnttag.client.model;

public class User {
    private String _id;
    private String username;
    private int wins;
    private int kills;
    private int deaths;
    private int powerups;
    private int tags;
    private String rank;
    private String rankColor;
    private String plusColor;

    public User() {}

    public User(String _id, String username, int wins, int kills, int deaths, int powerups, int tags, String rank, String rankColor, String plusColor) {
        this._id = _id;
        this.username = username;
        this.wins = wins;
        this.kills = kills;
        this.deaths = deaths;
        this.powerups = powerups;
        this.tags = tags;
        this.rank = rank;
        this.rankColor = rankColor;
        this.plusColor = plusColor;
    }

    // Getters and setters
    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }

    public int getPowerups() { return powerups; }
    public void setPowerups(int powerups) { this.powerups = powerups; }

    public int getTags() { return tags; }
    public void setTags(int tags) { this.tags = tags; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public String getRankColor() { return rankColor; }
    public void setRankColor(String rankColor) { this.rankColor = rankColor; }

    public String getPlusColor() { return plusColor; }
    public void setPlusColor(String plusColor) { this.plusColor = plusColor; }

    @Override
    public String toString() {
        return "User{" +
                "_id='" + _id + '\'' +
                ", username='" + username + '\'' +
                ", wins=" + wins +
                ", kills=" + kills +
                ", deaths=" + deaths +
                ", powerups=" + powerups +
                ", tags=" + tags +
                ", rank='" + rank + '\'' +
                ", rankColor='" + rankColor + '\'' +
                ", plusColor='" + plusColor + '\'' +
                '}';
    }
}
