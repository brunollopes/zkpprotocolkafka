package com.bole.zkpauth.grpc;

import com.bole.zkpauth.*;
import com.google.protobuf.ByteString;
import com.bole.zkpauth.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.logging.Level;

/**
 * Grpc API Prover Client to handle the ZKP Protocol
 * 1. Register API
 * 2. Challenge API
 * 3. Verify API
 */
@Slf4j
@Singleton
public class GrpcClient {

    private ManagedChannel channel;

    private AuthGrpc.AuthBlockingStub stub;

    public GrpcClient() {

    }

    public GrpcClient(ManagedChannel channel) {
        this.channel = channel;
        this.stub = AuthGrpc.newBlockingStub(channel);
    }


    @Operation(summary = "Builds a request to the Verifier server to register with it the generated keys Y1 and Y2")
    @ApiResponses(value = {
            @ApiResponse(content = { @Content(mediaType = "application/grpc",
                    schema = @Schema(implementation = Void.class))
            })
    })
    /**
     * Builds a request to the Verifier server to register with it the generated keys Y1 and Y2
     * @param user the user owner of the keys
     * @param y1 java.math.BigInteger key
     * @param y2 java.math.BigInteger key
     */
    public void registerSend(String user, BigInteger y1, BigInteger y2) {

        log.info("Register process request sent");

        try {

            stub.register(RegisterRequest.newBuilder()
                    .setUser(user)
                    .setY1(ByteString.copyFrom(y1.toByteArray()))
                    .setY2(ByteString.copyFrom(y2.toByteArray()))
                    .build());

            log.info("Register process response received");
        } catch (StatusRuntimeException e) {
            log.warn(Level.WARNING.getName(), "RPC failed: {0}", e.getStatus());
            return;
        }
    }

    /**
     * Builds authentication request to the Verifier Server by sending two generated keys
     * receives in return a random key S
     * @param user the user owner of the keys
     * @param r1 java.math.BigInteger
     * @param r2 java.math.BigInteger
     * @return AuthenticationChallengeResponse
     */
    public AuthenticationChallengeResponse challengeSend(String user, BigInteger r1, BigInteger r2) {
        log.info("Challenge process request sent");
        try {
            AuthenticationChallengeRequest authenticationChallengeRequest =
                    AuthenticationChallengeRequest.newBuilder()
                            .setUser(user)
                            .setR1(ByteString.copyFrom(r1.toByteArray()))
                            .setR2(ByteString.copyFrom(r2.toByteArray()))
                            .build();


            AuthenticationChallengeResponse authenticationChallengeResponse =
                    stub.createAuthenticationChallenge(authenticationChallengeRequest);

            log.info("Challenge process response received");
            return authenticationChallengeResponse;
        } catch (StatusRuntimeException e) {
            log.warn(Level.WARNING.getName(), "RPC failed: {0}", e.getStatus());
            return AuthenticationChallengeResponse.getDefaultInstance();
        }

    }

    /**
     * Builds a request to the Verifier server answering to the challenge
     * @param authId authentication unique id
     * @param s java.math.BigInteger computed value
     * @return AuthenticationAnswerResponse with the final result of the authentication process with the ZKP
     */
    public AuthenticationAnswerResponse verifySend(String authId, BigInteger s) {
        log.info("Verify process request sent");
        try {
            AuthenticationAnswerRequest authenticationAnswerRequest =
                    AuthenticationAnswerRequest.newBuilder()
                            .setAuthId(authId)
                            .setS(ByteString.copyFrom(s.toByteArray())).build();

            AuthenticationAnswerResponse authenticationAnswerResponse =
                    stub.verifyAuthentication(authenticationAnswerRequest);

            log.info("Verify process response received");
            return authenticationAnswerResponse;
        } catch (StatusRuntimeException e) {
            log.warn(Level.WARNING.getName(), "RPC failed: {0}", e.getStatus());
            return AuthenticationAnswerResponse.getDefaultInstance();
        }

    }
}
