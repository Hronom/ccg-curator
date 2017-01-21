package com.github.hronom.ccg.curator.server.components;

import com.github.hronom.ccg.curator.CardRevealedReply;
import com.github.hronom.ccg.curator.CcgCuratorGrpc;
import com.github.hronom.ccg.curator.DiceThrowedReply;
import com.github.hronom.ccg.curator.JoinRoomReply;
import com.github.hronom.ccg.curator.JoinRoomRequest;
import com.github.hronom.ccg.curator.LoginReply;
import com.github.hronom.ccg.curator.LoginRequest;
import com.github.hronom.ccg.curator.PlayerEnterRoomReply;
import com.github.hronom.ccg.curator.PlayerLeftRroomReply;
import com.github.hronom.ccg.curator.RoomEventReply;
import com.github.hronom.ccg.curator.SubmitCardReply;
import com.github.hronom.ccg.curator.SubmitCardRequest;
import com.github.hronom.ccg.curator.ThrowDiceReply;
import com.github.hronom.ccg.curator.ThrowDiceRequest;
import com.github.hronom.ccg.curator.server.components.business.CardAlreadySubmittedException;
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

    private final ConcurrentHashMap<Player, StreamObserver<RoomEventReply>>
        roomEventReplyMap
        = new ConcurrentHashMap<>();

    @Autowired
    private MainManager mainManager;

    @Autowired
    private PlayersManager playersManager;

    @Autowired
    private RoomsManager roomsManager;

    @PreDestroy
    public void cleanUp() throws Exception {
        for (StreamObserver<RoomEventReply> streamObserver : roomEventReplyMap.values()) {
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
    public void joinRoom(JoinRoomRequest req, StreamObserver<RoomEventReply> responseObserver) {
        Player player = playersManager.getPlayer(req.getPlayerId());
        if (player != null) {
            if (!mainManager.isPlayerInRoom(player)) {
                try {
                    Room room = roomsManager.getRoom(req.getRoomName(), req.getRoomPassword());
                    roomEventReplyMap.put(player, responseObserver);
                    mainManager.joinRoom(player, room);
                    RoomEventReply reply =
                        RoomEventReply
                            .newBuilder()
                            .setJoinRoomReply(JoinRoomReply.newBuilder()
                            .setCode(JoinRoomReply.Codes.JOINED))
                            .build();
                    responseObserver.onNext(reply);
                } catch (RoomBadPasswordException exception) {
                    RoomEventReply reply =
                        RoomEventReply
                            .newBuilder()
                            .setJoinRoomReply(JoinRoomReply.newBuilder()
                            .setCode(JoinRoomReply.Codes.BAD_PASSWORD))
                            .build();
                    responseObserver.onNext(reply);
                    responseObserver.onCompleted();
                }
            } else {
                RoomEventReply reply =
                    RoomEventReply
                        .newBuilder()
                        .setJoinRoomReply(JoinRoomReply.newBuilder()
                        .setCode(JoinRoomReply.Codes.ALREADY_IN_ROOM))
                        .build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        } else {
            RoomEventReply reply =
                RoomEventReply
                    .newBuilder()
                    .setJoinRoomReply(
                        JoinRoomReply
                            .newBuilder()
                            .setCode(JoinRoomReply.Codes.BAD_PLAYER_ID)
                    )
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void submitCard(SubmitCardRequest req, StreamObserver<SubmitCardReply> responseObserver) {
        Player player = playersManager.getPlayer(req.getPlayerId());
        if (player != null) {
            try {
                Room room = mainManager.getRoom(player);
                room.submitCard(player, req.getCardName());
                SubmitCardReply reply =
                    SubmitCardReply
                        .newBuilder()
                        .setCode(SubmitCardReply.Codes.SUBMITTED)
                        .build();
                responseObserver.onNext(reply);
            } catch (CardAlreadySubmittedException e) {
                SubmitCardReply reply =
                    SubmitCardReply
                        .newBuilder()
                        .setCode(SubmitCardReply.Codes.CARD_ALREADY_SUBMITTED)
                        .build();
                responseObserver.onNext(reply);
            }
        } else {
            SubmitCardReply reply =
                SubmitCardReply
                    .newBuilder()
                    .setCode(SubmitCardReply.Codes.BAD_PLAYER_ID)
                    .build();
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

    public void sendPlayerEnterRoom(Player sendToPlayer, Player whoEnter) {
        StreamObserver<RoomEventReply> responseObserver = roomEventReplyMap.get(sendToPlayer);
        if (responseObserver != null) {
            RoomEventReply reply =
                RoomEventReply
                    .newBuilder()
                    .setPlayerEnterRoomReply(
                        PlayerEnterRoomReply
                            .newBuilder()
                            .setPlayerName(whoEnter.getName())
                            .build()
                    )
                    .build();
            responseObserver.onNext(reply);
        }
    }

    public void sendPlayerLeftRoom(Player sendToPlayer, Player whoLeft) {
        StreamObserver<RoomEventReply> responseObserver = roomEventReplyMap.get(sendToPlayer);
        if (responseObserver != null) {
            RoomEventReply reply =
                RoomEventReply
                    .newBuilder()
                    .setPlayerLeftRroomReply(
                        PlayerLeftRroomReply
                            .newBuilder()
                            .setPlayerName(whoLeft.getName())
                            .build()
                    )
                    .build();
            responseObserver.onNext(reply);
        }
    }

    public void sendShowdownCard(Player sendToPlayer, Player cardOwner, String cardName) {
        StreamObserver<RoomEventReply> responseObserver = roomEventReplyMap.get(sendToPlayer);
        if (responseObserver != null) {
            RoomEventReply reply =
                RoomEventReply
                    .newBuilder()
                    .setCardRevealedReply(
                        CardRevealedReply
                            .newBuilder()
                            .setPlayerName(cardOwner.getName())
                            .setCardName(cardName)
                            .build()
                    )
                    .build();
            responseObserver.onNext(reply);
        }
    }

    public void sendThrowDice(Player sendToPlayer, Player playerThatThrow, String value) {
        StreamObserver<RoomEventReply> responseObserver = roomEventReplyMap.get(sendToPlayer);
        if (responseObserver != null) {
            RoomEventReply reply =
                RoomEventReply
                    .newBuilder()
                    .setDiceThrowedReply(
                        DiceThrowedReply
                            .newBuilder()
                            .setPlayerName(playerThatThrow.getName())
                            .setDiceValue(value)
                            .build()
                    )
                    .build();
            responseObserver.onNext(reply);
        }
    }
}
