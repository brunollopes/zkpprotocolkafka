package com.bole.zkpauth.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Handle Grpc ManagedChannel
 */
public class GrpcUtil {

    private static ManagedChannel channel;
    private static String host = "zkp-verifier";

    public static GrpcClient initGrpcClient () {
        return initGrpcClient(host);
    }

    public static GrpcClient initGrpcClient (String host) {
        GrpcUtil.channel = ManagedChannelBuilder.forAddress(host, 8082)
                .usePlaintext()
                .build();

        return new GrpcClient(channel);
    }

    public static GrpcClient initGrpcClient (ManagedChannel channel) {
        GrpcUtil.channel = channel;
        return new GrpcClient(channel);
    }


    public static void shutdownGrpcClient () throws InterruptedException {
        if(GrpcUtil.channel != null) {
            GrpcUtil.channel.shutdown();
            GrpcUtil.channel.awaitTermination(1, TimeUnit.SECONDS);
        }
    }
}
