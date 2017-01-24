package com.github.hronom.ccg.curator.client.controllers;

import com.github.hronom.ccg.curator.CcgCuratorGrpc;
import com.github.hronom.ccg.curator.JoinRoomReply;
import com.github.hronom.ccg.curator.JoinRoomRequest;
import com.github.hronom.ccg.curator.LoginReply;
import com.github.hronom.ccg.curator.LoginRequest;
import com.github.hronom.ccg.curator.RoomEventReply;
import com.github.hronom.ccg.curator.SubmitCardReply;
import com.github.hronom.ccg.curator.SubmitCardRequest;
import com.github.hronom.ccg.curator.ThrowDiceReply;
import com.github.hronom.ccg.curator.ThrowDiceRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
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
    private TextField tfServerAddress;

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

    private ResourceBundle resourceBundle;

    private ManagedChannel channel;
    private CcgCuratorGrpc.CcgCuratorBlockingStub blockingStub;
    private CcgCuratorGrpc.CcgCuratorStub stub;

    private StreamObserver<LoginReply> loginStreamObserver;

    private final AtomicBoolean logged = new AtomicBoolean(false);
    private final AtomicLong playerId = new AtomicLong(-1);

    private final AtomicBoolean joinedRoom = new AtomicBoolean(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;

        bLogin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(tfServerAddress.getText())
                        // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                        // needing certificates.
                        .usePlaintext(true);
                    channel = channelBuilder.build();
                    blockingStub = CcgCuratorGrpc.newBlockingStub(channel);
                    stub = CcgCuratorGrpc.newStub(channel);

                    tfServerAddress.setDisable(true);
                    tfPlayerName.setDisable(true);
                    bLogin.setDisable(true);

                    loginStreamObserver = new StreamObserver<LoginReply>() {
                        @Override
                        public void onNext(LoginReply value) {
                            playerId.set(value.getPlayerId());
                            logged.set(true);
                            println("playerLogged", value.getPlayerId());
                            tfServerAddress.setDisable(true);
                            tfPlayerName.setDisable(true);
                            bLogin.setDisable(true);
                        }

                        @Override
                        public void onError(Throwable t) {
                            logger.error("Error", t);
                            logged.set(false);
                            joinedRoom.set(false);
                            if (t instanceof StatusRuntimeException) {
                                println(((StatusRuntimeException) t).getStatus().toString());
                            }
                            tfServerAddress.setDisable(false);
                            tfPlayerName.setDisable(false);
                            bLogin.setDisable(false);
                            tfRoomName.setDisable(false);
                            tfRoomPassword.setDisable(false);
                            bJoinRoom.setDisable(false);
                            tfCardName.setDisable(false);
                            bSubmitCard.setDisable(false);
                            tfDiceValues.setDisable(false);
                            bThrowDice.setDisable(false);
                        }

                        @Override
                        public void onCompleted() {
                            logged.set(false);
                            joinedRoom.set(false);
                            println("loggedSessionComplete");
                            tfServerAddress.setDisable(false);
                            tfPlayerName.setDisable(false);
                            bLogin.setDisable(false);
                            tfRoomName.setDisable(false);
                            tfRoomPassword.setDisable(false);
                            bJoinRoom.setDisable(false);
                            tfCardName.setDisable(false);
                            bSubmitCard.setDisable(false);
                            tfDiceValues.setDisable(false);
                            bThrowDice.setDisable(false);
                        }
                    };
                    StreamObserver<LoginRequest> requests = stub.login(loginStreamObserver);
                    LoginRequest request = LoginRequest.newBuilder()
                        .setPlayerName(tfPlayerName.getText()).build();
                    requests.onNext(request);
                } catch (Exception exception) {
                    logger.error("Error", exception);
                    println("Error", exception.getMessage());
                    tfServerAddress.setDisable(false);
                    tfPlayerName.setDisable(false);
                    bLogin.setDisable(false);
                    tfRoomName.setDisable(false);
                    tfRoomPassword.setDisable(false);
                    bJoinRoom.setDisable(false);
                    tfCardName.setDisable(false);
                    bSubmitCard.setDisable(false);
                    tfDiceValues.setDisable(false);
                    bThrowDice.setDisable(false);
                }
            }
        });

        bJoinRoom.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!logged.get()) {
                    println("loginFirst");
                } else {
                    tfRoomName.setDisable(true);
                    tfRoomPassword.setDisable(true);
                    bJoinRoom.setDisable(true);
                    JoinRoomRequest joinRoomRequest =
                        JoinRoomRequest
                            .newBuilder()
                            .setPlayerId(playerId.get())
                            .setRoomName(tfRoomName.getText())
                            .setRoomPassword(tfRoomPassword.getText())
                            .build();
                    stub.joinRoom(joinRoomRequest, new StreamObserver<RoomEventReply>() {
                        @Override
                        public void onNext(RoomEventReply value) {
                            if (value.getValueCase() == RoomEventReply.ValueCase.JOINROOMREPLY) {
                                if (value.getJoinRoomReply().getCode() ==
                                    JoinRoomReply.Codes.JOINED) {
                                    joinedRoom.set(true);
                                    println("joinedRoom", tfRoomName.getText());
                                    tfRoomName.setDisable(true);
                                    tfRoomPassword.setDisable(true);
                                    bJoinRoom.setDisable(true);
                                } else if (value.getJoinRoomReply().getCode() ==
                                           JoinRoomReply.Codes.ALREADY_IN_ROOM) {
                                    joinedRoom.set(false);
                                    println("roomNotJoinedAlreadyInRoom", tfRoomName.getText());
                                    tfRoomName.setDisable(false);
                                    tfRoomPassword.setDisable(false);
                                    bJoinRoom.setDisable(false);
                                } else if (value.getJoinRoomReply().getCode() ==
                                           JoinRoomReply.Codes.BAD_PLAYER_ID) {
                                    joinedRoom.set(false);
                                    println("roomNotJoinedBadPlayerId", tfRoomName.getText());
                                    tfRoomName.setDisable(false);
                                    tfRoomPassword.setDisable(false);
                                    bJoinRoom.setDisable(false);
                                } else if (value.getJoinRoomReply().getCode() ==
                                           JoinRoomReply.Codes.BAD_PASSWORD) {
                                    joinedRoom.set(false);
                                    println("roomNotJoinedBadPassword", tfRoomName.getText());
                                    tfRoomName.setDisable(false);
                                    tfRoomPassword.setDisable(false);
                                    bJoinRoom.setDisable(false);
                                }
                            } else if (value.getValueCase() == RoomEventReply.ValueCase.PLAYERENTERROOMREPLY) {
                                println(
                                    "playerEnterRoom",
                                    value.getPlayerEnterRoomReply().getPlayerName()
                                );
                            } else if (value.getValueCase() == RoomEventReply.ValueCase.PLAYERLEFTRROOMREPLY) {
                                println(
                                    "playerLeftRoom",
                                    value.getPlayerLeftRroomReply().getPlayerName()
                                );
                            } else if (value.getValueCase() ==
                                       RoomEventReply.ValueCase.CARDSUBMITTEDREPLY) {
                                println(
                                    "cardSubmitted",
                                    value.getCardSubmittedReply().getPlayerName()
                                );
                                tfCardName.setDisable(false);
                                bSubmitCard.setDisable(false);
                            } else if (value.getValueCase() ==
                                       RoomEventReply.ValueCase.CARDREVEALEDREPLY) {
                                println(
                                    "cardRevealed",
                                    value.getCardRevealedReply().getPlayerName(),
                                    value.getCardRevealedReply().getCardName()
                                );
                                tfCardName.setDisable(false);
                                bSubmitCard.setDisable(false);
                            } else if (value.getValueCase() ==
                                       RoomEventReply.ValueCase.DICETHROWEDREPLY) {
                                println(
                                    "diceThrowed",
                                    value.getDiceThrowedReply().getPlayerName(),
                                    value.getDiceThrowedReply().getDiceValue()
                                );
                                tfDiceValues.setDisable(false);
                                bThrowDice.setDisable(false);
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            logger.error("Error", t);
                            joinedRoom.set(false);
                            println("leavedRoom", tfRoomName.getText());
                            tfRoomName.setDisable(false);
                            tfRoomPassword.setDisable(false);
                            bJoinRoom.setDisable(false);
                            tfCardName.setDisable(false);
                            bSubmitCard.setDisable(false);
                            tfDiceValues.setDisable(false);
                            bThrowDice.setDisable(false);
                        }

                        @Override
                        public void onCompleted() {
                            joinedRoom.set(false);
                            println("leavedRoom", tfRoomName.getText());
                            tfRoomName.setDisable(false);
                            tfRoomPassword.setDisable(false);
                            bJoinRoom.setDisable(false);
                            tfCardName.setDisable(false);
                            bSubmitCard.setDisable(false);
                            tfDiceValues.setDisable(false);
                            bThrowDice.setDisable(false);
                        }
                    });
                }
            }
        });

        bSubmitCard.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tfCardName.setDisable(true);
                bSubmitCard.setDisable(true);
                try {
                    SubmitCardRequest request =
                        SubmitCardRequest
                            .newBuilder()
                            .setPlayerId(playerId.get())
                            .setCardName(tfCardName.getText())
                            .build();
                    SubmitCardReply reply = blockingStub.submitCard(request);
                    /*if (reply.getCode() == SubmitCardReply.Codes.SUBMITTED) {
                        println("Card submitted \"" + tfCardName.getText() + "\"");
                        println();
                    } else if (reply.getCode() == SubmitCardReply.Codes.CARD_ALREADY_SUBMITTED) {
                        println("Card already submitted \"" + tfCardName.getText() + "\"");
                        println();
                    }*/
                } catch (Exception exception) {
                    logger.error("Error", exception);
                    tfCardName.setDisable(false);
                    bSubmitCard.setDisable(false);
                }
            }
        });

        bThrowDice.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tfDiceValues.setDisable(true);
                bThrowDice.setDisable(true);
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
                        println("diceNotThrowed", tfDiceValues.getText());
                    }
                } catch (Exception exception) {
                    logger.error("Error", exception);
                    tfDiceValues.setDisable(false);
                    bThrowDice.setDisable(false);
                }
            }
        });
    }

    private void println(String key, Object... args) {
        MessageFormat formatter = new MessageFormat(resourceBundle.getString(key));
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tfOutput.appendText(simpleDateFormat.format(date) + " - " + formatter.format(args));
                tfOutput.appendText("\n");
            }
        });
    }
}
