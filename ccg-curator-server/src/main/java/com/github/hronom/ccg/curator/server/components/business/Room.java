package com.github.hronom.ccg.curator.server.components.business;

import com.github.hronom.ccg.curator.server.components.CcgCuratorService;
import com.github.hronom.ccg.curator.server.components.business.exception.CardAlreadySubmittedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PreDestroy;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Room {
    private static final Logger logger = LogManager.getLogger();

    private final String name;
    private final String password;

    private final CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();

    private final ConcurrentHashMap<Player, String> submitedCards = new ConcurrentHashMap<>();

    private final CcgCuratorService ccgCuratorService;

    private final Random random = new Random();

    public Room(CcgCuratorService ccgCuratorServiceArg, String nameArg, String passwordArg) {
        ccgCuratorService = ccgCuratorServiceArg;
        name = nameArg;
        password = passwordArg;
    }

    @PreDestroy
    public void cleanUp() throws Exception {
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void addPlayer(Player player) {
        players.add(player);

        for (Player playerInRooom : players) {
            if (player != playerInRooom) {
                ccgCuratorService.sendPlayerEnterRoom(player, playerInRooom);
            }
        }
        for (Player playerToSend : players) {
            if (playerToSend != player) {
                ccgCuratorService.sendPlayerEnterRoom(playerToSend, player);
            }
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);

        for (Player playerToSend : players) {
            ccgCuratorService.sendPlayerLeftRoom(playerToSend, player);
        }

        submitedCards.remove(player);

        if (isAllPlayersSubmitCards()) {
            sendSubmittedCards();
        }
    }

    public Collection<Player> getPlayers() {
        return players;
    }

    public void submitCard(Player player, String cardName) throws CardAlreadySubmittedException {
        if (!submitedCards.containsKey(player)) {
            submitedCards.put(player, cardName);
            sendPlayerSubmitCard(player);
            if (isAllPlayersSubmitCards()) {
                sendSubmittedCards();
            }
        } else {
            throw new CardAlreadySubmittedException();
        }
    }

    public void throwDice(Player player, String[] diceValues) {
        int randomPos = random.nextInt(diceValues.length);
        // Send notifications
        for (Player playerToSend : players) {
            ccgCuratorService.sendThrowDice(playerToSend, player, diceValues[randomPos]);
        }
    }

    private void sendPlayerSubmitCard(Player whoSubmitCard) {
        for (Player playerToSend : players) {
            if (playerToSend != whoSubmitCard) {
                ccgCuratorService.sendPlayerSubmitCard(playerToSend, whoSubmitCard);
            }
        }
    }

    private boolean isAllPlayersSubmitCards() {
        for (Player playerInRoom : players) {
            if (!submitedCards.containsKey(playerInRoom)) {
                return false;
            }
        }
        return true;
    }

    private void sendSubmittedCards() {
        for (Map.Entry<Player, String> entry : submitedCards.entrySet()) {
            for (Player playerToSend : players) {
                ccgCuratorService.sendShowdownCard(playerToSend, entry.getKey(), entry.getValue());
            }
        }
        submitedCards.clear();
    }
}
