package com.bole.zkpauth.grpc;


import com.bole.zkpauth.*;
import com.bole.zkpauth.AuthGrpc;
import com.bole.zkpauth.AuthenticationAnswerRequest;
import com.bole.zkpauth.AuthenticationAnswerResponse;
import com.bole.zkpauth.AuthenticationChallengeRequest;
import com.bole.zkpauth.AuthenticationChallengeResponse;
import com.bole.zkpauth.ErrorResponse;
import com.bole.zkpauth.RegisterRequest;
import com.bole.zkpauth.RegisterResponse;
import com.bole.zkpauth.util.ZkpUtil;
import com.google.protobuf.ByteString;
import com.bole.zkpauth.cache.KeyCache;
import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to cover possible scenarios for the grpc server
 * @com.bole.zkpauth.grpc.VerifierServer
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class VerifierServerTest {


    private static Server grpServer;

    private static  ManagedChannel channel;

    private static AuthGrpc.AuthBlockingStub stub;

    @BeforeAll
    public void launchRpcServer() throws Exception {
        log.info("Start verifier grpc server");
        grpServer = ServerBuilder
                .forPort(8582)
                .addService(new VerifierServer()).build();

        grpServer.start();

        log.debug("Build channel/stub");
        channel = ManagedChannelBuilder.forAddress("localhost", 8582)
                .usePlaintext()
                .build();

        stub = AuthGrpc.newBlockingStub(channel);

    }

    @AfterAll
    public void shutdownRpcServer() throws Exception {
        log.debug("shutting down");
        channel.shutdown();
        if (channel.isTerminated()) {
            grpServer.shutdown();
            grpServer.awaitTermination();
        }

    }

    @BeforeEach
    public void startKafkaCache() {
        KeyCache.getKeys().put("blopes",new BigInteger[]{
                BigInteger.valueOf(109), //p
                BigInteger.valueOf(9), // g
                BigInteger.valueOf(27)}); //h
    }

    @AfterEach
    public void cleanKafkaCache() {
        KeyCache.getKeys().remove("blopes");
    }

    /**
     * createAuthenticationChallenge(com.bole.zkpauth.AuthenticationChallengeRequest request,
     * io.grpc.stub.StreamObserver<com.bole.zkpauth.AuthenticationChallengeResponse> responseObserver)
     */
    @Test
    public void givenAValidInput_thenGotNoErrorMessage() {
        //mocking expected values from client
        String user = "blopes";
        BigInteger y1 = BigInteger.valueOf(23);
        BigInteger y2 = BigInteger.valueOf(9);


        log.info("Register process request sent");

        RegisterResponse response = stub.register(RegisterRequest.newBuilder()
                .setUser(user)
                .setY1(ByteString.copyFrom(y1.toByteArray()))
                .setY2(ByteString.copyFrom(y2.toByteArray()))
                .build());

        assertNotNull(response,"Server unable to return successfully");

    }

    @Test
    public void givenAInvalidInput_whenRegister_thenGotErrorMessage() throws Exception {
        //mocking expected values from client
        String user = "";
        BigInteger y1 = BigInteger.valueOf(23);
        BigInteger y2 = BigInteger.valueOf(9);


        log.debug("Register process request sent");

        StatusRuntimeException thrown =
                Assertions.assertThrows(StatusRuntimeException.class,
                        () -> stub.register(RegisterRequest.newBuilder()
                                .setUser(user)
                                .setY1(ByteString.copyFrom(y1.toByteArray()))
                                .setY2(ByteString.copyFrom(y2.toByteArray()))
                                .build()));

        assertEquals(Status.INVALID_ARGUMENT.getCode(), thrown.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Invalid input for the user parameter", thrown.getMessage());
        Metadata metadata = Status.trailersFromThrowable(thrown);
        ErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(ErrorResponse.getDefaultInstance()));
        assertEquals("",errorResponse.getUser(), "Invalid user");

    }

    /**
     * For
     * createAuthenticationChallenge(com.bole.zkpauth.AuthenticationChallengeRequest request,
     * io.grpc.stub.StreamObserver<com.bole.zkpauth.AuthenticationChallengeResponse> responseObserver) {
     */
    @Test
    public void givenAValidInput_whenChallenge_thenGotValidS() {
        //mocking expected values from client
        String user = "blopes";
        BigInteger r1 = BigInteger.valueOf(23);
        BigInteger r2 = BigInteger.valueOf(9);


        log.debug("Challenge process request sent");

        com.bole.zkpauth.AuthenticationChallengeRequest authenticationChallengeRequest =
                AuthenticationChallengeRequest.newBuilder()
                        .setUser(user)
                        .setR1(ByteString.copyFrom(r1.toByteArray()))
                        .setR2(ByteString.copyFrom(r2.toByteArray())).build();


        AuthenticationChallengeResponse authenticationChallengeResponse =
                stub.createAuthenticationChallenge(authenticationChallengeRequest);


        Optional<ByteString> c = Optional.ofNullable(authenticationChallengeResponse.getC())
                .filter(Predicate.not(ByteString::isEmpty));

        Optional<String> authId = Optional.ofNullable(authenticationChallengeResponse.getAuthId())
                .filter(Predicate.not(String::isEmpty));


        assertTrue(c.isPresent(),"Random C cannot be empty");
        assertTrue(authId.isPresent(),"AuthId cannot be empty");

    }

    @Test
    public void givenAInvalidInput_whenChallenge_thenGotErrorMessage() throws Exception {
        //mocking expected values from client
        String user = "";
        BigInteger r1 = BigInteger.valueOf(23);
        BigInteger r2 = BigInteger.valueOf(9);


        log.debug("Register process request sent");

        StatusRuntimeException thrown =
                Assertions.assertThrows(StatusRuntimeException.class,
                        () -> stub.createAuthenticationChallenge(AuthenticationChallengeRequest.newBuilder()
                                .setUser(user)
                                .setR1(ByteString.copyFrom(r1.toByteArray()))
                                .setR2(ByteString.copyFrom(r2.toByteArray())).build()));

        assertEquals(Status.INVALID_ARGUMENT.getCode(), thrown.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Invalid input for the user parameter", thrown.getMessage());
        Metadata metadata = Status.trailersFromThrowable(thrown);
        ErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(ErrorResponse.getDefaultInstance()));
        assertEquals("",errorResponse.getUser(), "Invalid user");

    }

    /**
     * For
     * verifyAuthentication(
     * com.bole.zkpauth.AuthenticationAnswerRequest request,
     * io.grpc.stub.StreamObserver<com.bole.zkpauth.AuthenticationAnswerResponse> responseObserver)
     */

    @Test
    public void givenAValidInput_whenVerify_thenGotValidSessionId() {
        //mocking expected values from client
        Object[] answer = initForVerify();

        String authId = (String) answer[0];
        BigInteger c = (BigInteger) answer[1];

        BigInteger s = computeS(
                BigInteger.valueOf(7), //k
                c,
                BigInteger.valueOf(3), // x
                BigInteger.valueOf(54)); //q

        log.debug("Verify process request sent");

        AuthenticationAnswerRequest authenticationAnswerRequest =
                AuthenticationAnswerRequest.newBuilder()
                        .setAuthId(authId)
                        .setS(ByteString.copyFrom(s.toByteArray())).build();

        AuthenticationAnswerResponse authenticationAnswerResponse =
                stub.verifyAuthentication(authenticationAnswerRequest);


        Optional<String> session = Optional.ofNullable(authenticationAnswerResponse.getSessionId())
                .filter(Predicate.not(String::isEmpty));

        assertTrue(session.isPresent(),"Invalid session id");

    }

    @Test
    public void givenAValidInput_InvalidP_whenVerify_thenGotNoSessionId() {
        //mocking expected values from client
        Object[] answer = initForVerify();

        String authId = (String) answer[0];
        BigInteger c = (BigInteger) answer[1];

        BigInteger s = computeS(
                BigInteger.valueOf(7), //k
                c,
                BigInteger.valueOf(31), // x
                BigInteger.valueOf(54)); //q


        log.debug("Verify process request sent");

        AuthenticationAnswerRequest authenticationAnswerRequest =
                AuthenticationAnswerRequest.newBuilder()
                        .setAuthId(authId)
                        .setS(ByteString.copyFrom(s.toByteArray())).build();

        AuthenticationAnswerResponse authenticationAnswerResponse =
                stub.verifyAuthentication(authenticationAnswerRequest);


        Optional<String> session = Optional.ofNullable(authenticationAnswerResponse.getSessionId())
                .filter(Predicate.not(String::isEmpty));

        assertTrue(!session.isPresent(),"A valid session id was returned");

    }


    @Test
    public void givenAValidInput_and_InValidPubKeys_whenVerify_thenGotNoSessionId() {
        //mocking expected values from client
        Object[] answer = initForVerify();

        String authId = (String) answer[0];
        BigInteger c = (BigInteger) answer[1];

        BigInteger s = computeS(
                BigInteger.valueOf(7), //k
                c,
                BigInteger.valueOf(31), // x
                BigInteger.valueOf(54)); //q


        //replace pub keys with wrong ones
        KeyCache.getKeys().put("blopes",new BigInteger[]{
                BigInteger.valueOf(119), //p
                BigInteger.valueOf(19), // g
                BigInteger.valueOf(127)}); //h

        log.debug("Verify process request sent");

        AuthenticationAnswerRequest authenticationAnswerRequest =
                AuthenticationAnswerRequest.newBuilder()
                        .setAuthId(authId)
                        .setS(ByteString.copyFrom(s.toByteArray())).build();

        AuthenticationAnswerResponse authenticationAnswerResponse =
                stub.verifyAuthentication(authenticationAnswerRequest);


        Optional<String> session = Optional.ofNullable(authenticationAnswerResponse.getSessionId())
                .filter(Predicate.not(String::isEmpty));

        assertTrue(!session.isPresent(),"A valid session id was returned");

    }

    @Test
    public void givenAValidInput_InvalidAuthId_whenVerify_thenGotErrorMessage() {
        //mocking expected values from client

        String authId = "";
        BigInteger c = BigInteger.valueOf(1);

        BigInteger s = computeS(
                BigInteger.valueOf(7), //k
                c,
                BigInteger.valueOf(31), // x
                BigInteger.valueOf(54)); //q

        log.debug("Verify process request sent");

        StatusRuntimeException thrown =
                Assertions.assertThrows(StatusRuntimeException.class,
                        () -> stub.verifyAuthentication(AuthenticationAnswerRequest.newBuilder()
                                .setAuthId(authId)
                                .setS(ByteString.copyFrom(s.toByteArray())).build()));

        assertEquals(Status.INVALID_ARGUMENT.getCode(), thrown.getStatus().getCode());
        assertEquals("INVALID_ARGUMENT: Invalid input for the auth id parameter", thrown.getMessage());
        Metadata metadata = Status.trailersFromThrowable(thrown);
        ErrorResponse errorResponse = metadata.get(ProtoUtils.keyForProto(ErrorResponse.getDefaultInstance()));
        assertEquals(authId,errorResponse.getUser(), "Invalid authId");

    }


    /***************
     * Helpers
     ***************/



    private Object[] initForVerify() {
        //mocking expected values from client
        String user = "blopes";
        BigInteger y1 = BigInteger.valueOf(75);
        BigInteger y2 = BigInteger.valueOf(63);

        BigInteger[] r1r2 = computeR1R2(
                BigInteger.valueOf(7), //k
                KeyCache.getKeys().get(user)[0], //q
                KeyCache.getKeys().get(user)[1], //g
                KeyCache.getKeys().get(user)[2]); //h

        BigInteger r1 = r1r2[0];
        BigInteger r2 = r1r2[1];

        stub.register(RegisterRequest.newBuilder()
                .setUser(user)
                .setY1(ByteString.copyFrom(y1.toByteArray()))
                .setY2(ByteString.copyFrom(y2.toByteArray()))
                .build());

        AuthenticationChallengeRequest authenticationChallengeRequest =
                AuthenticationChallengeRequest.newBuilder()
                        .setUser(user)
                        .setR1(ByteString.copyFrom(r1.toByteArray()))
                        .setR2(ByteString.copyFrom(r2.toByteArray()))
                        .build();

        AuthenticationChallengeResponse authenticationChallengeResponse =
                stub.createAuthenticationChallenge(authenticationChallengeRequest);

        return new Object[]{authenticationChallengeResponse.getAuthId(),
                new BigInteger(authenticationChallengeResponse.getC().toByteArray())};
    }

    /**
     Compute the value of the math form
     s = k - c.x (mod q)
     *
     * @param k java.math.BigInteger
     * @param c java.math.BigInteger
     * @param x java.math.BigInteger
     * @param q java.math.BigInteger
     * @return java.math.BigInteger
     */
    private BigInteger computeS(BigInteger k, BigInteger c, BigInteger x, BigInteger q) {
        return k.subtract(c.multiply(x)).mod(q);

    }

    /**
     * Compute R1 and R2
     * r1 = g^k mod p
     * r2 = h^k mod p
     *
     * @param k java.math.BigInteger
     * @param p java.math.BigInteger
     * @param g java.math.BigInteger
     * @param h java.math.BigInteger
     * @return java.math.BigInteger[]
     */
    public static BigInteger[] computeR1R2(BigInteger k, BigInteger p, BigInteger g, BigInteger h) {
        BigInteger[] rS = new BigInteger[2];

        rS[0] = g.pow(k.intValue()).mod(p);
        rS[1] = h.pow(k.intValue()).mod(p);

        return rS;
    }



    @Test
    public void givenOtherK_ComputeR1R2_GotR1R2() {
        BigInteger k = BigInteger.valueOf(1000);
        BigInteger p = BigInteger.valueOf(109);
        BigInteger g = BigInteger.valueOf(9);
        BigInteger h = BigInteger.valueOf(27);
        BigInteger y1 = BigInteger.valueOf(75);
        BigInteger y2 = BigInteger.valueOf(63);
        BigInteger c = BigInteger.valueOf(69539);
        BigInteger s = BigInteger.valueOf(27);

        try {
            BigInteger[] keys = ZkpUtil.computeR1R2(p,g,h,s,c,y1,y2);
            Assertions.assertEquals(2,keys.length,"Fail to compute exactly 2 keys");
        } catch (ArithmeticException ex) {
            fail("Should be able to compute R1 and R2 keys");

        }

    }
}
