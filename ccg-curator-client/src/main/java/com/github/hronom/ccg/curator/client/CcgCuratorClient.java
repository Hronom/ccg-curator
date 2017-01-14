package com.github.hronom.ccg.curator.client;

import com.github.hronom.ccg.curator.CcgCuratorGrpc;
import com.github.hronom.ccg.curator.JoinRoomReply;
import com.github.hronom.ccg.curator.JoinRoomRequest;
import com.github.hronom.ccg.curator.LoginReply;
import com.github.hronom.ccg.curator.LoginRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class CcgCuratorClient {
    private static final Logger logger = LogManager.getLogger();

    private final ManagedChannel channel;
    private final CcgCuratorGrpc.CcgCuratorBlockingStub blockingStub;
    private final CcgCuratorGrpc.CcgCuratorStub stub;

    /** Construct client connecting to HelloWorld server at {@code host:port}. */
    public CcgCuratorClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext(true));
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
    CcgCuratorClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = CcgCuratorGrpc.newBlockingStub(channel);
        stub = CcgCuratorGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void run() throws InterruptedException {
        String playerName = "Hronom";
        final long[] playerId = {0};
        logger.info("Login as " + playerName + "...");
        StreamObserver<LoginReply> streamObserver = new StreamObserver<LoginReply>() {
            @Override
            public void onNext(LoginReply value) {
                //LoginRequest request = LoginRequest.newBuilder().setPlayerName("Hronom").build();
                System.out.println(value.toString());
                playerId[0] = value.getPlayerId();
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Complete");
            }
        };

        StreamObserver<LoginRequest> requests = stub.login(streamObserver);
        LoginRequest request = LoginRequest.newBuilder().setPlayerName(playerName).build();
        requests.onNext(request);

        Thread.sleep(TimeUnit.SECONDS.toMillis(3));

        JoinRoomRequest joinRoomRequest =
            JoinRoomRequest
                .newBuilder()
                .setPlayerId(playerId[0])
                .setRoomName("Test room")
                .build();
        JoinRoomReply joinRoomReply = blockingStub.joinRoom(joinRoomRequest);
        System.out.println("Joined: " + joinRoomReply.getJoined());

        //requests.onCompleted();
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting.
     */
    public static void main(String[] args) throws Exception {
        CcgCuratorClient client = new CcgCuratorClient("localhost", 50051);
        try {
            client.run();
        } finally {
            client.shutdown();
        }
    }
}
