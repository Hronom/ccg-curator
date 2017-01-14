package com.github.hronom.ccg.curator.server.components.business;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PreDestroy;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Room {
    private static final Logger logger = LogManager.getLogger();

    private final String name;

    private final CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();

    private final ConcurrentHashMap<Player, String> submitedCards = new ConcurrentHashMap<>();

    public Room(String nameArg) {
        name = nameArg;
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        System.out.println("destroyed");
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void submitCard(Player player, String cardName) {
        if (!submitedCards.containsKey(player)) {
            submitedCards.put(player, cardName);
            checkIsAllSubmited();
        }
    }

    private void checkIsAllSubmited() {
        for (Player player : players) {
            if (!submitedCards.containsKey(player)) {
                return;
            }
        }

        // TODO
    }
}
