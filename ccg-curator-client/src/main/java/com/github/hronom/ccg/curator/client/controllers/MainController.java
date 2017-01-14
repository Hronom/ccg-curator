package com.github.hronom.ccg.curator.client.controllers;

import com.github.hronom.ccg.curator.CardRevealedReply;
import com.github.hronom.ccg.curator.CcgCuratorGrpc;
import com.github.hronom.ccg.curator.JoinRoomReply;
import com.github.hronom.ccg.curator.JoinRoomRequest;
import com.github.hronom.ccg.curator.LoginReply;
import com.github.hronom.ccg.curator.LoginRequest;
import com.github.hronom.ccg.curator.SubmitCardReply;
import com.github.hronom.ccg.curator.SubmitCardRequest;
import com.github.hronom.ccg.curator.SubscribeOnCardsShowdownRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML
    private TextField tfRoomName;

    @FXML
    private TextField tfRoomPassword;

    @FXML
    private TextField tfPlayerName;

    @FXML
    private Button bEnter;

    @FXML
    private TextField tfCardName;

    @FXML
    private Button bSubmitCard;

    @FXML
    private Button bThrowDice;

    @FXML
    private TextArea tfOutput;

    private ManagedChannel channel;
    private CcgCuratorGrpc.CcgCuratorBlockingStub blockingStub;
    private CcgCuratorGrpc.CcgCuratorStub stub;

    private StreamObserver<LoginReply> loginStreamObserver;

    private final long notSet = -1;
    private final AtomicLong playerId = new AtomicLong(notSet);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bEnter.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                AtomicBoolean errorWhileLogin = new AtomicBoolean(false);
                loginStreamObserver = new StreamObserver<LoginReply>() {
                    @Override
                    public void onNext(LoginReply value) {
                        playerId.set(value.getPlayerId());
                        println("Player id " + value.getPlayerId());
                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.error("Error", t);
                        playerId.set(notSet);
                        errorWhileLogin.set(true);

                        if (t instanceof StatusRuntimeException) {
                            println(((StatusRuntimeException) t).getStatus().toString());
                        }
                    }

                    @Override
                    public void onCompleted() {
                        playerId.set(notSet);
                        println("Complete");
                    }
                };
                StreamObserver<LoginRequest> requests = stub.login(loginStreamObserver);
                LoginRequest request =
                    LoginRequest
                        .newBuilder()
                        .setPlayerName(tfPlayerName.getText())
                        .build();
                requests.onNext(request);

                while (playerId.get() == notSet && !errorWhileLogin.get()) {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                    } catch (InterruptedException e) {
                        logger.error("Error", e);
                    }
                }

                JoinRoomRequest joinRoomRequest =
                    JoinRoomRequest
                        .newBuilder()
                        .setPlayerId(playerId.get())
                        .setRoomName(tfRoomName.getText())
                        .build();
                JoinRoomReply joinRoomReply = blockingStub.joinRoom(joinRoomRequest);

                if (joinRoomReply.getJoined()) {
                    println("Joined room: " + tfRoomName.getText());
                } else {
                    println("Room: " + tfRoomName.getText() + " not joined.");
                    return;
                }

                SubscribeOnCardsShowdownRequest subscribeOnCardsShowdownRequest =
                    SubscribeOnCardsShowdownRequest
                        .newBuilder()
                        .setPlayerId(playerId.get())
                        .build();

                stub.subscribeOnCardsShowdown(subscribeOnCardsShowdownRequest,
                    new StreamObserver<CardRevealedReply>() {
                        @Override
                        public void onNext(CardRevealedReply value) {
                            println("================================================");
                            println(
                                "Player name \"" +
                                value.getPlayerName() +
                                "\", card name \"" +
                                value.getCardName() +
                                "\"");
                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onCompleted() {

                        }
                    }
                );
            }
        });

        bSubmitCard.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SubmitCardRequest submitCardRequest =
                    SubmitCardRequest
                        .newBuilder()
                        .setPlayerId(playerId.get())
                        .setCardName(tfCardName.getText())
                        .build();
                SubmitCardReply submitCardReply = blockingStub.submitCard(submitCardRequest);
                if (submitCardReply.getSubmited()) {
                    println("Card submitted \"" + tfCardName.getText() + "\"");
                } else {
                    println("Card not submitted \"" + tfCardName.getText() + "\"");
                }
            }
        });

        ManagedChannelBuilder<?> channelBuilder =
            ManagedChannelBuilder
                .forAddress("localhost", 50051)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext(true);
        channel = channelBuilder.build();
        blockingStub = CcgCuratorGrpc.newBlockingStub(channel);
        stub = CcgCuratorGrpc.newStub(channel);
    }

    private void println(String text) {
        tfOutput.appendText(text);
        tfOutput.appendText("\n");
    }
}
