package com.github.hronom.ccg.curator.server.controllers.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerDto {
    @JsonProperty
    private final long id;
    @JsonProperty
    private final String name;

    public PlayerDto(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
