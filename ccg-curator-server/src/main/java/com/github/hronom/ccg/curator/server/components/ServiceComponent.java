package com.github.hronom.ccg.curator.server.components;

import com.github.hronom.ccg.curator.CcgCuratorGrpc;
import com.github.hronom.ccg.curator.LoginReply;
import com.github.hronom.ccg.curator.LoginRequest;
import com.github.hronom.ccg.curator.server.components.business.Player;
import com.github.hronom.ccg.curator.server.components.business.PlayersManager;
import com.github.hronom.ccg.curator.server.components.business.RoomsManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ServiceComponent extends CcgCuratorGrpc.CcgCuratorImplBase {
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private PlayersManager playersManager;

    @Autowired
    private RoomsManager roomsManager;

    @Override
    public StreamObserver<LoginRequest> login(StreamObserver<LoginReply> responseObserver) {
        /*while (true) {
            LoginReply reply =
                LoginReply
                    .newBuilder()
                    .setPlayerId(playersManager.createPlayer(req.getPlayerName()).getId())
                    .build();
            responseObserver.onNext(reply);
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(3));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //responseObserver.onCompleted();
        }*/

        return new StreamObserver<LoginRequest>() {
            private LoginReply loginReply;

            @Override
            public void onNext(LoginRequest value) {
                loginReply =
                    LoginReply
                        .newBuilder()
                        .setPlayerId(playersManager.createPlayer(value.getPlayerName()).getId())
                        .build();
                responseObserver.onNext(loginReply);
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof StatusException) {
                    StatusException statusException = (StatusException) t;
                    if (statusException.getStatus() == Status.CANCELLED) {
                        Player player = playersManager.getPlayer(loginReply.getPlayerId());
                        if (player != null) {
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

    /*@Override
    public void subscribeOnCardsShowdown(SubscribeOnCardsShowdownRequest req, StreamObserver<CardRevealedReply> responseObserver) {
        while (true) {
            CardRevealedReply reply =
                CardRevealedReply
                    .newBuilder()
                    .setPlayerName("test")
                    .setCardName(String.valueOf(UUID.randomUUID())).build();
            responseObserver.onNext(reply);
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(3));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //responseObserver.onCompleted();
        }
    }

    @Override
    public void setCard(SetCardRequest req, StreamObserver<SetCardReply> responseObserver) {
        SetCardReply reply = SetCardReply.newBuilder().setSetted(true).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void subscribeOnThrowingDice(SubscribeOnThrowingDiceRequest req, StreamObserver<DiceThrowedReply> responseObserver) {
        while (true) {
            DiceThrowedReply reply =
                DiceThrowedReply
                    .newBuilder()
                    .setPlayerName("test")
                    .setDiceValue(String.valueOf(UUID.randomUUID())).build();
            responseObserver.onNext(reply);
            //responseObserver.onCompleted();
        }
    }

    @Override
    public void throwDice(ThrowDiceRequest req, StreamObserver<ThrowDiceReply> responseObserver) {
        ThrowDiceReply reply = ThrowDiceReply.newBuilder().setDiceValue("1").build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }*/
}
