package com.example.kstreamdemo.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Player {
    @JsonProperty
    private String id;
    @JsonProperty("name")
    private String name;
}
