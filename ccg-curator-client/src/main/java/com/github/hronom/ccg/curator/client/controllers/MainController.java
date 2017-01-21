package com.github.hronom.ccg.curator.client.controllers;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Arrays;
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
    private TextField tfPlayerName;

    @FXML
    private Button bLogin;

    @FXML
    private TextField tfRoomName;

    @FXML
    private TextField tfRoomPassword;

    @FXML
    private Button bJoinRoom;

    @FXML
    private TextField tfCardName;

    @FXML
    private Button bSubmitCard;

    @FXML
    private Button bThrowDice;

    @FXML
    private TextField tfDiceValues;

    @FXML
    private TextArea tfOutput;

    private ManagedChannel channel;
    private CcgCuratorGrpc.CcgCuratorBlockingStub blockingStub;
    private CcgCuratorGrpc.CcgCuratorStub stub;

    private StreamObserver<LoginReply> loginStreamObserver;

    private final AtomicBoolean logged = new AtomicBoolean(false);
    private final AtomicLong playerId = new AtomicLong(-1);

    private final AtomicBoolean joinedRoom = new AtomicBoolean(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bLogin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                disableInputs(true);

                loginStreamObserver = new StreamObserver<LoginReply>() {
                    @Override
                    public void onNext(LoginReply value) {
                        playerId.set(value.getPlayerId());
                        logged.set(true);
                        println("Logged, player id " + value.getPlayerId());
                        println();
                        disableInputs(false);
                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.error("Error", t);
                        logged.set(false);
                        joinedRoom.set(false);
                        if (t instanceof StatusRuntimeException) {
                            println(((StatusRuntimeException) t).getStatus().toString());
                            println();
                        }
                        disableInputs(false);
                    }

                    @Override
                    public void onCompleted() {
                        logged.set(false);
                        joinedRoom.set(false);
                        println("Logged session complete, connection to the server closed.");
                        println();
                    }
                };
                StreamObserver<LoginRequest> requests = stub.login(loginStreamObserver);
                LoginRequest request =
                    LoginRequest
                        .newBuilder()
                        .setPlayerName(tfPlayerName.getText())
                        .build();
                requests.onNext(request);
            }
        });

        bJoinRoom.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!logged.get()) {
                    println("Login first");
                    println();
                } else {
                    disableInputs(true);

                    JoinRoomRequest joinRoomRequest =
                        JoinRoomRequest
                            .newBuilder()
                            .setPlayerId(playerId.get())
                            .setRoomName(tfRoomName.getText())
                            .setRoomPassword(tfRoomPassword.getText())
                            .build();
                    stub.joinRoom(joinRoomRequest, new StreamObserver<JoinRoomReply>() {
                        @Override
                        public void onNext(JoinRoomReply value) {
                            if (value.getCode() == JoinRoomReply.Codes.JOINED) {
                                joinedRoom.set(true);
                                println("Joined room \"" + tfRoomName.getText() + "\"");
                                println();
                                disableInputs(false);

                                SubscribeOnCardsShowdownRequest
                                    subscribeOnCardsShowdownRequest
                                    = SubscribeOnCardsShowdownRequest.newBuilder().setPlayerId(playerId.get())
                                    .build();
                                stub.subscribeOnCardsShowdown(subscribeOnCardsShowdownRequest, new StreamObserver<CardRevealedReply>() {
                                    @Override
                                    public void onNext(CardRevealedReply value) {
                                        println(
                                            "Player name \"" + value.getPlayerName() +
                                            "\", card name \"" + value.getCardName() +
                                            "\""
                                        );
                                        println();
                                    }

                                    @Override
                                    public void onError(Throwable t) {

                                    }

                                    @Override
                                    public void onCompleted() {

                                    }
                                });

                                SubscribeOnThrowingDiceRequest
                                    subscribeOnThrowingDiceRequest
                                    = SubscribeOnThrowingDiceRequest.newBuilder().setPlayerId(playerId.get())
                                    .build();
                                stub.subscribeOnThrowingDice(subscribeOnThrowingDiceRequest, new StreamObserver<DiceThrowedReply>() {
                                    @Override
                                    public void onNext(DiceThrowedReply value) {
                                        println(
                                            "Player name \"" + value.getPlayerName() + "\", " +
                                            "dice value \"" + value.getDiceValue() + "\""
                                        );
                                        println();
                                    }

                                    @Override
                                    public void onError(Throwable t) {

                                    }

                                    @Override
                                    public void onCompleted() {

                                    }
                                });
                            } else if (value.getCode() == JoinRoomReply.Codes.BAD_PLAYER_ID) {
                                joinedRoom.set(false);
                                println("Room \"" + tfRoomName.getText() + "\" not joined (Bad player id)");
                                println();
                                disableInputs(false);
                            } else if (value.getCode() == JoinRoomReply.Codes.BAD_PASSWORD) {
                                joinedRoom.set(false);
                                println("Room \"" + tfRoomName.getText() + "\" not joined (Bad password)");
                                println();
                                disableInputs(false);
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            logger.error("Error", t);
                            joinedRoom.set(false);
                            println("Room \"" + tfRoomName.getText() + "\" not joined (Bad player id)");
                            println();
                            disableInputs(false);
                        }

                        @Override
                        public void onCompleted() {
                        }
                    });
                }
            }
        });

        bSubmitCard.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                disableInputs(true);
                try {
                    SubmitCardRequest request = SubmitCardRequest.newBuilder()
                        .setPlayerId(playerId.get()).setCardName(tfCardName.getText()).build();
                    SubmitCardReply reply = blockingStub.submitCard(request);
                    if (reply.getSubmited()) {
                        println("Card submitted \"" + tfCardName.getText() + "\"");
                        println();
                    } else {
                        println("Card not submitted \"" + tfCardName.getText() + "\"");
                        println();
                    }
                } catch (Exception exception) {
                    logger.error("Error", exception);
                }
                disableInputs(false);
            }
        });

        bThrowDice.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                disableInputs(true);
                try {
                    String[] diceValues = tfDiceValues.getText().split(",");
                    ThrowDiceRequest request =
                        ThrowDiceRequest
                            .newBuilder()
                            .setPlayerId(playerId.get())
                            .addAllDiceValues(Arrays.asList(diceValues))
                            .build();
                    ThrowDiceReply reply = blockingStub.throwDice(request);
                    if (!reply.getThrowed()) {
                        println("Dice not throwed \"" + tfDiceValues.getText() + "\"");
                        println();
                    }
                } catch (Exception exception) {
                    logger.error("Error", exception);
                }
                disableInputs(false);
            }
        });

        ManagedChannelBuilder<?> channelBuilder =
//            ManagedChannelBuilder.forTarget("139.59.129.12:50051")
            ManagedChannelBuilder.forTarget("localhost:50051")
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext(true);
        channel = channelBuilder.build();
        blockingStub = CcgCuratorGrpc.newBlockingStub(channel);
        stub = CcgCuratorGrpc.newStub(channel);
    }

    private void disableInputs(boolean disable) {
        tfPlayerName.setDisable(disable);
        bLogin.setDisable(disable);
        tfRoomName.setDisable(disable);
        tfRoomPassword.setDisable(disable);
        bJoinRoom.setDisable(disable);
        tfCardName.setDisable(disable);
        bSubmitCard.setDisable(disable);
        bThrowDice.setDisable(disable);
        tfDiceValues.setDisable(disable);
        tfOutput.setDisable(disable);
    }

    private void println(String text) {
        tfOutput.appendText(text);
        tfOutput.appendText("\n");
    }
    private void println() {
        tfOutput.appendText("\n");
    }
}
