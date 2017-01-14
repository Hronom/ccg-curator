package com.github.hronom.ccg.curator.server;

import com.github.hronom.ccg.curator.CardRevealedReply;
import com.github.hronom.ccg.curator.CcgCuratorGrpc;
import com.github.hronom.ccg.curator.SubscribeOnCardsShowdownRequest;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.grpc.stub.StreamObserver;

public class CcgCuratorImpl extends CcgCuratorGrpc.CcgCuratorImplBase {

    @Override
    public void subscribeOnCardsShowdown(SubscribeOnCardsShowdownRequest req, StreamObserver<CardRevealedReply> responseObserver) {
        while (true) {
            CardRevealedReply reply =
                CardRevealedReply
                    .newBuilder()
                    .setPlayerName(req.getPlayerName())
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
}
