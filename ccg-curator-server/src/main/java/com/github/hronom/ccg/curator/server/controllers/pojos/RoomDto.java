package com.github.hronom.ccg.curator.server.controllers.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;

public class RoomDto {
    @JsonProperty
    private String name;
    @JsonProperty
    private LinkedList<PlayerDto> players;

    public RoomDto(String name, LinkedList<PlayerDto> players) {
        this.name = name;
        this.players = players;
    }

    public String getName() {
        return name;
    }

    public LinkedList<PlayerDto> getPlayers() {
        return players;
    }
}
