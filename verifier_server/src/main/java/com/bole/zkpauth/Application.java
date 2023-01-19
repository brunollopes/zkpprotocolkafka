package com.bole.zkpauth;

import com.bole.zkpauth.grpc.VerifierServer;
import com.bole.zkpauth.kafka.ZkpHook;
import com.bole.zkpauth.kafka.ZkpConsumer;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

/**
 * ZKP Protocol Verifier Server Application
 * 1. Launch a Kafka consumer thread
 * 2. Launch a GRPC server
 */
public class Application {
    public static void main(String[] args) throws IOException, InterruptedException {

        ZkpConsumer zkpConsumer = new ZkpConsumer();

        new Thread(zkpConsumer).start();
        Runtime.getRuntime().addShutdownHook(new Thread(new ZkpHook(zkpConsumer)));

        Server server = ServerBuilder
                .forPort(8082)
                .addService(new VerifierServer()).build();

        server.start();
        server.awaitTermination();
    }
}
