package com.github.hronom.ccg.curator.server.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;

import javax.annotation.PreDestroy;

import io.grpc.Server;
import io.grpc.ServerBuilder;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GrpcServer {
    private static final Logger logger = LogManager.getLogger();

    private final int serverPort = 50051;
    private Server server;

    @Autowired
    public GrpcServer(CcgCuratorService ccgCuratorService) throws Exception {
        start(ccgCuratorService);
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        stop();
    }

    private void start(CcgCuratorService ccgCuratorService) throws IOException {
        server = ServerBuilder
            .forPort(serverPort)
            .addService(ccgCuratorService)
            .build()
            .start();
        logger.info("Server started, listening on " + serverPort);
    }

    private void stop() {
        System.out.println("Shutting down gRPC server since JVM is shutting down");
        server.shutdown();
        System.out.println("Server shut down");
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
