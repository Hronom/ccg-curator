package com.github.hronom.ccg.curator.server.components.business;

import com.github.hronom.ccg.curator.server.components.MainServiceManager;

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

    private final MainServiceManager mainServiceManager;

    private final Random random = new Random();

    public Room(MainServiceManager mainServiceManagerArg, String nameArg, String passwordArg) {
        mainServiceManager = mainServiceManagerArg;
        name = nameArg;
        password = passwordArg;
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        System.out.println("destroyed");
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void addPlayer(Player player) {
        players.add(player);
        // Send notifications
        for (Player playerInRooom : players) {
            if (player != playerInRooom) {
                mainServiceManager.sendPlayerEnterRoom(player, playerInRooom);
            }
        }
        for (Player playerToSend : players) {
            if (playerToSend != player) {
                mainServiceManager.sendPlayerEnterRoom(playerToSend, player);
            }
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);
        // Send notifications
        for (Player playerToSend : players) {
            mainServiceManager.sendPlayerLeftRoom(playerToSend, player);
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
            mainServiceManager.sendThrowDice(playerToSend, player, diceValues[randomPos]);
        }
    }

    private void sendPlayerSubmitCard(Player whoSubmitCard) {
        for (Player playerToSend : players) {
            if (playerToSend != whoSubmitCard) {
                mainServiceManager.sendPlayerSubmitCard(playerToSend, whoSubmitCard);
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
                mainServiceManager.sendShowdownCard(playerToSend, entry.getKey(), entry.getValue());
            }
        }
        submitedCards.clear();
    }
}
