package com.github.hronom.ccg.curator.server.components;

import com.github.hronom.ccg.curator.CardRevealedReply;
import com.github.hronom.ccg.curator.CcgCuratorGrpc;
import com.github.hronom.ccg.curator.DiceThrowedReply;
import com.github.hronom.ccg.curator.JoinRoomReply;
import com.github.hronom.ccg.curator.JoinRoomRequest;
import com.github.hronom.ccg.curator.LoginReply;
import com.github.hronom.ccg.curator.LoginRequest;
import com.github.hronom.ccg.curator.SubmitCardReply;
import com.github.hronom.ccg.curator.SubmitCardRequest;
import com.github.hronom.ccg.curator.SubscribeOnCardsShowdownRequest;
import com.github.hronom.ccg.curator.SubscribeOnThrowingDiceRequest;
import com.github.hronom.ccg.curator.ThrowDiceReply;
import com.github.hronom.ccg.curator.ThrowDiceRequest;
import com.github.hronom.ccg.curator.server.components.business.MainManager;
import com.github.hronom.ccg.curator.server.components.business.Player;
import com.github.hronom.ccg.curator.server.components.business.PlayersManager;
import com.github.hronom.ccg.curator.server.components.business.Room;
import com.github.hronom.ccg.curator.server.components.business.RoomBadPasswordException;
import com.github.hronom.ccg.curator.server.components.business.RoomsManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MainServiceManager extends CcgCuratorGrpc.CcgCuratorImplBase {
    private static final Logger logger = LogManager.getLogger();

    private final ConcurrentHashMap<Player, StreamObserver<CardRevealedReply>>
        cardsShowdownMap
        = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, StreamObserver<DiceThrowedReply>>
        throwingDiceMap
        = new ConcurrentHashMap<>();

    @Autowired
    private MainManager mainManager;

    @Autowired
    private PlayersManager playersManager;

    @Autowired
    private RoomsManager roomsManager;

    @PreDestroy
    public void cleanUp() throws Exception {
        for (StreamObserver<CardRevealedReply> streamObserver : cardsShowdownMap.values()) {
            streamObserver.onCompleted();
        }
        for (StreamObserver<DiceThrowedReply> streamObserver : throwingDiceMap.values()) {
            streamObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LoginRequest> login(StreamObserver<LoginReply> responseObserver) {
        return new StreamObserver<LoginRequest>() {
            private Player player;

            @Override
            public void onNext(LoginRequest value) {
                player = playersManager.createPlayer(value.getPlayerName());
                LoginReply loginReply = LoginReply.newBuilder().setPlayerId(player.getId()).build();
                responseObserver.onNext(loginReply);
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof StatusException) {
                    StatusException statusException = (StatusException) t;
                    if (statusException.getStatus() == Status.CANCELLED) {
                        if (player != null) {
                            mainManager.leaveRooms(player);
                            playersManager.removePlayer(player);
                        }
                    }
                }
                logger.error("Error", t);
            }

            @Override
            public void onCompleted() {
                logger.info("Complete");
            }
        };
    }

    @Override
    public void joinRoom(JoinRoomRequest req, StreamObserver<JoinRoomReply> responseObserver) {
        Player player = playersManager.getPlayer(req.getPlayerId());
        if (player != null) {
            try {
                Room room = roomsManager.getRoom(req.getRoomName(), req.getRoomPassword());
                mainManager.joinRoom(player, room);
                JoinRoomReply reply = JoinRoomReply.newBuilder().setCode(JoinRoomReply.Codes.JOINED).build();
                responseObserver.onNext(reply);
            } catch (RoomBadPasswordException exception) {
                JoinRoomReply reply = JoinRoomReply.newBuilder().setCode(JoinRoomReply.Codes.BAD_PASSWORD).build();
                responseObserver.onNext(reply);
            }
        } else {
            JoinRoomReply reply = JoinRoomReply.newBuilder().setCode(JoinRoomReply.Codes.BAD_PLAYER_ID).build();
            responseObserver.onNext(reply);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void subscribeOnCardsShowdown(SubscribeOnCardsShowdownRequest req, StreamObserver<CardRevealedReply> responseObserver) {
        Player player = playersManager.getPlayer(req.getPlayerId());
        if (player != null) {
            cardsShowdownMap.put(player, responseObserver);
        }
    }

    @Override
    public void subscribeOnThrowingDice(SubscribeOnThrowingDiceRequest req, StreamObserver<DiceThrowedReply> responseObserver) {
        Player player = playersManager.getPlayer(req.getPlayerId());
        if (player != null) {
            throwingDiceMap.put(player, responseObserver);
        }
    }

    @Override
    public void submitCard(SubmitCardRequest req, StreamObserver<SubmitCardReply> responseObserver) {
        Player player = playersManager.getPlayer(req.getPlayerId());
        if (player != null) {
            Room room = mainManager.getRoom(player);
            room.submitCard(player, req.getCardName());
            SubmitCardReply reply = SubmitCardReply.newBuilder().setSubmited(true).build();
            responseObserver.onNext(reply);
        } else {
            SubmitCardReply reply = SubmitCardReply.newBuilder().setSubmited(false).build();
            responseObserver.onNext(reply);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void throwDice(ThrowDiceRequest req, StreamObserver<ThrowDiceReply> responseObserver) {
        Player player = playersManager.getPlayer(req.getPlayerId());
        if (player != null) {
            Room room = mainManager.getRoom(player);
            room.throwDice(player, req.getDiceValuesList().toArray(new String[0]));
            ThrowDiceReply reply = ThrowDiceReply.newBuilder().setThrowed(true).build();
            responseObserver.onNext(reply);
        } else {
            ThrowDiceReply reply = ThrowDiceReply.newBuilder().setThrowed(false).build();
            responseObserver.onNext(reply);
        }
        responseObserver.onCompleted();
    }

    public void sendShowdownCard(Player sendToPlayer, Player cardOwner, String cardName) {
        StreamObserver<CardRevealedReply> responseObserver = cardsShowdownMap.get(sendToPlayer);
        if (responseObserver != null) {
            CardRevealedReply reply =
                CardRevealedReply
                    .newBuilder()
                    .setPlayerName(cardOwner.getName())
                    .setCardName(cardName)
                    .build();
            responseObserver.onNext(reply);
        }
    }

    public void sendThrowDice(Player sendToPlayer, Player playerThatThrow, String value) {
        StreamObserver<DiceThrowedReply> responseObserver = throwingDiceMap.get(sendToPlayer);
        if (responseObserver != null) {
            DiceThrowedReply reply =
                DiceThrowedReply
                    .newBuilder()
                    .setPlayerName(playerThatThrow.getName())
                    .setDiceValue(value)
                    .build();
            responseObserver.onNext(reply);
        }
    }
}
