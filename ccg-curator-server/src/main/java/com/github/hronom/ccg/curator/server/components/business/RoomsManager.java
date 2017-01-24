package com.github.hronom.ccg.curator.server.components.business;

import com.github.hronom.ccg.curator.server.components.CcgCuratorService;
import com.github.hronom.ccg.curator.server.components.business.exception.RoomBadPasswordException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RoomsManager {
    private static final Logger logger = LogManager.getLogger();

    private final Object modificationLock = new Object();
    private final ConcurrentHashMap<String, Room> roomsByName = new ConcurrentHashMap<>();

    private final ApplicationContext context;

    private final CcgCuratorService ccgCuratorService;

    @Autowired
    public RoomsManager(ApplicationContext contextArg, CcgCuratorService ccgCuratorServiceArg) {
        context = contextArg;
        ccgCuratorService = ccgCuratorServiceArg;
    }

    @PreDestroy
    public void cleanUp() throws Exception {
    }

    public Room getRoom(String name, String password) throws RoomBadPasswordException {
        synchronized (modificationLock) {
            Room room = roomsByName.get(name);
            if (room == null) {
                room = context.getBean(Room.class, ccgCuratorService, name, password);
                roomsByName.put(name, room);
            } else {
                if (!Objects.equals(password, room.getPassword())) {
                    throw new RoomBadPasswordException();
                }
            }
            return room;
        }
    }

    public Collection<Room> getRooms() {
        return Collections.unmodifiableCollection(roomsByName.values());
    }

    public void removeRoom(Room roomArg) {
        synchronized (modificationLock) {
            roomsByName.forEach((name, room) -> {
                if (Objects.equals(roomArg, room)) {
                    roomsByName.remove(name, room);
                }
            });
        }
    }
}
