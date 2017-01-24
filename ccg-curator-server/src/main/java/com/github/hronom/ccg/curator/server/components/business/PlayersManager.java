package com.github.hronom.ccg.curator.server.components.business;

import com.github.hronom.ccg.curator.server.components.business.exception.PlayerAlreadyLoggedException;
import com.github.hronom.ccg.curator.server.components.business.exception.PlayerBadNameException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
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

    private final Object playerModificationLock = new Object();
    private final ConcurrentHashMap<String, Player> playersByName = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Player> playersById = new ConcurrentHashMap<>();

    private final ApplicationContext context;

    @Autowired
    public PlayersManager(ApplicationContext contextArg) {
        context = contextArg;
    }

    @PreDestroy
    public void cleanUp() throws Exception {
    }

    public Player createPlayer(String playerName)
        throws PlayerAlreadyLoggedException, PlayerBadNameException {
        if (playerName == null) {
            throw new PlayerBadNameException();
        } else if(playerName.isEmpty()) {
            throw new PlayerBadNameException();
        }

        synchronized (playerModificationLock) {
            if (!playersByName.containsKey(playerName)) {
                long id = playerIdGenerator.incrementAndGet();
                Player player = context.getBean(Player.class, id, playerName);
                playersByName.put(playerName, player);
                playersById.put(id, player);
                return player;
            } else {
                throw new PlayerAlreadyLoggedException();
            }
        }
    }

    public void removePlayer(Player player) {
        synchronized (playerModificationLock) {
            playersById.forEach((idArg, playerArg) -> {
                if (Objects.equals(player, playerArg)) {
                    playersByName.remove(player.getName());
                    playersById.remove(idArg);
                }
            });
        }
    }

    public Player getPlayer(long id) {
        return playersById.get(id);
    }

    public Collection<Player> getPlayers() {
        return playersById.values();
    }
}
