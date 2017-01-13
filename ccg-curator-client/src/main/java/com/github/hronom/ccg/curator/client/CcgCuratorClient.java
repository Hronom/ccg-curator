package com.github.hronom.ccg.curator.client;

import com.github.hronom.ccg.curator.CardRevealedReply;
import com.github.hronom.ccg.curator.CcgCuratorGrpc;
import com.github.hronom.ccg.curator.SubscribeOnCardsShowdownRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class CcgCuratorClient {
    private static final Logger logger = LogManager.getLogger();

    private final ManagedChannel channel;
    private final CcgCuratorGrpc.CcgCuratorBlockingStub blockingStub;

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
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /** Say hello to server. */
    public void greet(String name) {
        logger.info("Will try to greet " + name + " ...");
        SubscribeOnCardsShowdownRequest request = SubscribeOnCardsShowdownRequest.newBuilder().setPlayerName(name).build();
        Iterator<CardRevealedReply> response = blockingStub.subscribeOnCardsShowdown(request);
        while (response.hasNext()) {
            CardRevealedReply reply = response.next();
            logger.info("Greeting: " + reply.getPlayerName() + " " + reply.getCardName());
        }
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting.
     */
    public static void main(String[] args) throws Exception {
        CcgCuratorClient client = new CcgCuratorClient("localhost", 50051);
        try {
      /* Access a service running on the local machine on port 50051 */
            String user = "world";
            if (args.length > 0) {
                user = args[0]; /* Use the arg as the name to greet if provided */
            }
            client.greet(user);
        } finally {
            client.shutdown();
        }
    }
}
