package com.github.hronom.ccg.curator.server.components.business;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private final AtomicLong playerIdGenerator = new AtomicLong();

    private final Object modificationLock = new Object();
    private ConcurrentHashMap<Player, Room> roomsByPlayer = new ConcurrentHashMap<>();

    @Autowired
    private PlayersManager playersManager;

    @Autowired
    private RoomsManager roomsManager;

    @PreDestroy
    public void cleanUp() throws Exception {
    }

    public long login(String playerName) throws Exception {
        return playersManager.createPlayer(playerName).getId();
    }

    public boolean isPlayerInRoom(Player player) {
        return roomsByPlayer.containsKey(player);
    }

    public void joinRoom(Player player, Room room) {
        synchronized (modificationLock) {
            room.addPlayer(player);
            roomsByPlayer.put(player, room);
        }
    }

    public void leaveRoom(Player player, Room room) {
        synchronized (modificationLock) {
            room.removePlayer(player);
            roomsByPlayer.remove(player, room);
        }
    }

    public void leaveRooms(Player player) {
        synchronized (modificationLock) {
            Room room = roomsByPlayer.get(player);
            if (room != null) {
                room.removePlayer(player);
                roomsByPlayer.remove(player, room);
                if (room.getPlayers().isEmpty()) {
                    roomsManager.removeRoom(room);
                }
            }
        }
    }

    public Room getRoom(Player player) {
        return roomsByPlayer.get(player);
    }
}
