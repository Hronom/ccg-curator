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
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PlayersManager {
    private static final Logger logger = LogManager.getLogger();

    private final AtomicLong playerIdGenerator = new AtomicLong();

    private final ConcurrentHashMap<Long, Player> playersById = new ConcurrentHashMap<>();

    public PlayersManager() throws Exception {
    }

    @PreDestroy
    public void cleanUp() throws Exception {
    }

    public Player createPlayer(String playerName) {
        long id = playerIdGenerator.incrementAndGet();
        Player player = player(id, playerName);
        playersById.put(id, player);
        return player;
    }

    public void removePlayer(Player player) {
        playersById.forEach((idArg, playerArg) -> {
            if (Objects.equals(player, playerArg)) {
                playersById.remove(idArg);
            }
        });
    }

    public Player getPlayer(long id) {
        return playersById.get(id);
    }

    public Collection<Player> getPlayers() {
        return playersById.values();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    private Player player(long id, String name) {
        return new Player(id, name);
    }
}
