package com.example.kstreamdemo.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class ScoreEvent {
    @JsonProperty("score")
    private String score;
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("player_id")
    private String playerId;
}
