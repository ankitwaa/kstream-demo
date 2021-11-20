package com.example.kstreamdemo.messages;

import lombok.Data;

@Data
public class ScoreDetails {
    private ScoreEvent scoreEvent;
    private Player player;

    public ScoreEvent getScoreEvent() {
        return scoreEvent;
    }

    public void setScoreEvent(ScoreEvent scoreEvent) {
        this.scoreEvent = scoreEvent;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
