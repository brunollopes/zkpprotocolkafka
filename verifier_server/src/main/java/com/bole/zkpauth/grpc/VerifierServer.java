package com.bole.zkpauth.grpc;

import com.bole.zkpauth.*;
import com.bole.zkpauth.AuthGrpc;
import com.bole.zkpauth.AuthenticationAnswerResponse;
import com.bole.zkpauth.AuthenticationChallengeResponse;
import com.bole.zkpauth.ErrorResponse;
import com.bole.zkpauth.RegisterResponse;
import com.bole.zkpauth.cache.ConcurrentMapCache;
import com.bole.zkpauth.domain.AuthSession;
import com.bole.zkpauth.util.ZkpUtil;
import com.fasterxml.uuid.Generators;
import com.google.protobuf.ByteString;
import com.bole.zkpauth.cache.KeyCache;
import com.bole.zkpauth.exception.CacheValueException;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@OpenAPIDefinition(
        info = @Info(
                title = "ZKP Protocol Verifier grpc server to handle Prover requests for registering keys and " +
                        "authentication login and grant access to resources",
                version = "0.0.1"
        )
)
@Slf4j
public class VerifierServer extends AuthGrpc.AuthImplBase {
    /**
     * to store the values of Y1 and Y2 by user id
     */
    private final ConcurrentMapCache<String, BigInteger[]> registryCache
            = new ConcurrentMapCache<>(86400000L,86400000L,1000);

    /**
     * to store the values of R1 and R2 by user id / sessionId
     */
    private final ConcurrentMapCache<String, AuthSession> authCache
            = new ConcurrentMapCache<>(86400000L,86400000L,1000);

    /**
     * to store the value of C by sessionId
     */
    private final ConcurrentMapCache<String, BigInteger> verifyCache
            = new ConcurrentMapCache<>(86400000L,86400000L,1000);


    /**
     * Handles a request from a prover to register authentication self generated keys
     * @param request com.bole.zkpauth.RegisterRequest
     * @param responseObserver io.grpc.stub.StreamObserver<com.bole.zkpauth.RegisterResponse>
     */
    @Override
    public void register(com.bole.zkpauth.RegisterRequest request,
                         io.grpc.stub.StreamObserver<com.bole.zkpauth.RegisterResponse> responseObserver) {
        ByteString y1 = request.getY1();
        ByteString y2 = request.getY2();
        Optional<String> userId = Optional.ofNullable(request.getUser()).filter(Predicate.not(String::isEmpty));

        log.info("Request received from user: " + userId);
        try {
            if(!userId.isPresent()){
                throw new IllegalArgumentException("Invalid input for the user parameter");
            }
            //add or replace
            registryCache.put(userId.get(), new BigInteger[]{
                    new BigInteger(y1.toByteArray()),
                    new BigInteger(y2.toByteArray())});

            log.info("Response send to the client");
            responseObserver.onNext(RegisterResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            Metadata.Key<ErrorResponse> errorResponseKey = ProtoUtils.keyForProto(ErrorResponse.getDefaultInstance());
            ErrorResponse errorResponse = ErrorResponse.newBuilder()
                    .setUser("")
                    .build();
            Metadata metadata = new Metadata();
            metadata.put(errorResponseKey, errorResponse);

            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(ex.getMessage())
                    .asRuntimeException(metadata));
        }

    }

    @Override
    public void createAuthenticationChallenge(com.bole.zkpauth.AuthenticationChallengeRequest request,
                                              io.grpc.stub.StreamObserver<com.bole.zkpauth.AuthenticationChallengeResponse> responseObserver) {


        Optional<String> userId = Optional.ofNullable(request.getUser()).filter(Predicate.not(String::isEmpty));

        log.info("Request for Authentication challenge received for user id: " + userId);

        try {

            if (!userId.isPresent()) {
                throw new IllegalArgumentException("Invalid input for the user parameter");
            }

            BigInteger r1 = new BigInteger(request.getR1().toByteArray());
            BigInteger r2 = new BigInteger(request.getR2().toByteArray());

            log.debug("R1: " + r1);
            log.debug("R2: " + r2);

            UUID uuid = Generators.timeBasedGenerator().generate();
            String authId = uuid.toString();

            AuthSession authSession = new AuthSession(userId.get(), new BigInteger[]{r1, r2});
            authCache.put(authId, authSession);
            log.debug("Generated authId with value: " + authId);

            int rnum = (int)Math.floor(Math.random()*(Integer.MAX_VALUE-1000+1)+1000);

            BigInteger c = ZkpUtil.lpf(BigInteger.valueOf(rnum/1000));

            log.debug("Generated c with value: " + c);
            verifyCache.put(authId, c);

            AuthenticationChallengeResponse authenticationChallengeResponse =
                    AuthenticationChallengeResponse.newBuilder()
                            .setAuthId(authId)
                            .setC(ByteString.copyFrom(c.toByteArray()))
                            .build();

            log.debug("Response send to the client");
            responseObserver.onNext(authenticationChallengeResponse);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException ex) {
            Metadata.Key<ErrorResponse> errorResponseKey = ProtoUtils.keyForProto(ErrorResponse.getDefaultInstance());
            ErrorResponse errorResponse = ErrorResponse.newBuilder()
                    .setUser("")
                    .build();
            Metadata metadata = new Metadata();
            metadata.put(errorResponseKey, errorResponse);

            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(ex.getMessage())
                    .asRuntimeException(metadata));
        }

    }


    /**
     * Verify authentication based on math form:
     * r1 = g^s.y1^c and r2 = h^s.y2^c
     */
    @Override
    public void verifyAuthentication(com.bole.zkpauth.AuthenticationAnswerRequest request,
                                     io.grpc.stub.StreamObserver<com.bole.zkpauth.AuthenticationAnswerResponse> responseObserver) {

        log.info("Verify request with auth id: " + request.getAuthId() + " and s: " + request.getS());

        Optional<String> authId = Optional.ofNullable(request.getAuthId()).filter(Predicate.not(String::isEmpty));

        Optional<ByteString> sIn = Optional.ofNullable(request.getS()).filter(Predicate.not(ByteString::isEmpty));

        try {

            if (!authId.isPresent()) {
                throw new IllegalArgumentException("Invalid input for the auth id parameter");
            }

            if (!sIn.isPresent()) {
                throw new IllegalArgumentException("Invalid input for the s parameter");
            }

            BigInteger s = new BigInteger(sIn.get().toByteArray());
            log.debug("Request for Authentication verify received for authId: " + authId);
            log.debug("S: " + s);

            BigInteger c = verifyCache.get(authId.get());

            if(c == null) {
                throw new CacheValueException("Invalid c key");
            }

            AuthSession authSession = authCache.get(authId.get());

            if(authSession == null || authSession.rS().length != 2) {
                throw new CacheValueException("Invalid rS keys");
            }

            BigInteger expectedR1Value = new BigInteger(authSession.rS()[0].toByteArray());
            BigInteger expectedR2Value = new BigInteger(authSession.rS()[1].toByteArray());


            BigInteger[] yS = registryCache.get(authSession.userId());

            if(yS == null || yS.length != 2) {
                throw new CacheValueException("Invalid yS keys");
            }

            BigInteger[] keysPGH = KeyCache.getKeys().get(authSession.userId());

            if(keysPGH == null || keysPGH.length != 3) {
                throw new CacheValueException("Invalid PGH keys");
            }

            BigInteger[] currentR1R2 = ZkpUtil.computeR1R2(
                    keysPGH[0],
                    keysPGH[1],
                    keysPGH[2],
                    new BigInteger(s.toByteArray()),
                    c,
                    yS[0],
                    yS[1]);

            String sessionId = "";


            if (expectedR1Value.equals(currentR1R2[0]) &&
                    expectedR2Value.equals(currentR1R2[1])
            ) {
                log.info("Authentication verified successfully with the correct expected R1 and R2");
                sessionId = UUID.randomUUID().toString();
            } else {
                log.info("Authentication not verified actual R1 and R2 are not equal to the expected R1 and R2");
                log.debug("Actual R1: " + currentR1R2[0]);
                log.debug("Expected R1: " + expectedR2Value);
                log.debug("Actual R2: " + currentR1R2[1]);
                log.debug("Expected R2: " + expectedR2Value);
            }

            AuthenticationAnswerResponse authenticationAnswerResponse =
                    com.bole.zkpauth.AuthenticationAnswerResponse.newBuilder().setSessionId(sessionId).build();

            responseObserver.onNext(authenticationAnswerResponse);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException | CacheValueException ex) {
            Metadata.Key<ErrorResponse> errorResponseKey = ProtoUtils.keyForProto(ErrorResponse.getDefaultInstance());
            ErrorResponse errorResponse = ErrorResponse.newBuilder()
                    .setUser(authId.isPresent() ? authId.get() : "")
                    .build();
            Metadata metadata = new Metadata();
            metadata.put(errorResponseKey, errorResponse);

            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(ex.getMessage())
                    .asRuntimeException(metadata));
        }

    }


}
