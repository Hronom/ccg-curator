package com.github.hronom.ccg.curator.server.components.business;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RoomsManager {
    private static final Logger logger = LogManager.getLogger();

    private final ConcurrentHashMap<String, Room> roomsByName = new ConcurrentHashMap<>();

    public RoomsManager() throws Exception {
    }

    @PreDestroy
    public void cleanUp() throws Exception {
    }

    public Room getRoom(String name) {
        return roomsByName.computeIfAbsent(name, k -> room(name));
    }

    public Collection<Room> getRooms() {
        return roomsByName.values();
    }

    public void removeRoom(Room roomArg) {
        roomsByName.forEach((name, room) -> {
            if (Objects.equals(roomArg, room)) {
                roomsByName.remove(name, room);
            }
        });
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    private Room room(String name) {
        return new Room(name);
    }
}
