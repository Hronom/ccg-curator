package com.github.hronom.ccg.curator.server.components.business;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MainManager {
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ObjectFactory<Room> roomComponentObjectFactory;

    private final AtomicLong playerIdGenerator = new AtomicLong();

    private ConcurrentHashMap<Long, Room> roomsByPlayerId = new ConcurrentHashMap<>();

    @Autowired
    private PlayersManager playersManager;

    @Autowired
    private RoomsManager roomsManager;

    public MainManager() throws Exception {
        System.out.println();
    }

    @PreDestroy
    public void cleanUp() throws Exception {
    }

    public long login(String playerName) throws Exception {
        return playersManager.createPlayer(playerName).getId();
    }

    public void joinRoom(long playerId, String roomName) {
        Player player = playersManager.getPlayer(playerId);
        Room room = roomsManager.getRoom(roomName);
        room.addPlayer(player);
        roomsByPlayerId.put(playerId, room);
    }

    public Room getRoom(long playerId) {
        return roomsByPlayerId.get(playerId);
    }
}
