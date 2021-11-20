package com.example.kstreamdemo.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Products {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
}
