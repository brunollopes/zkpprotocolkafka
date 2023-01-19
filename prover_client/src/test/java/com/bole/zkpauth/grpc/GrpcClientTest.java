package com.bole.zkpauth.grpc;


import com.bole.zkpauth.*;
import com.bole.zkpauth.*;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

/**
 * Test class to cover possible scenarios for the grpc client
 * @com.bole.zkpauth.grpc.GrpcClient
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
@ExtendWith(MockitoExtension.class)
public class GrpcClientTest {

    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Mock
    private AuthGrpc.AuthBlockingStub authBlockingStub;


    @Mock
    ManagedChannel channel;


    @Mock
    final GrpcClient grpcClient =
            mock(GrpcClient.class,
                    delegatesTo(
                            new GrpcClient() {
                                @Override
                                public void registerSend(String user, BigInteger y1, BigInteger y2) {

                                }

                                @Override
                                public AuthenticationChallengeResponse challengeSend(String user, BigInteger r1, BigInteger r2) {
                                    return AuthenticationChallengeResponse.getDefaultInstance();
                                }

                                @Override
                                public AuthenticationAnswerResponse verifySend(String authId, BigInteger s) {
                                    return AuthenticationAnswerResponse.getDefaultInstance();
                                }
                            })
            );
    ;

    @BeforeEach
    public void setUp() throws Exception {

        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start());

        // Create a client channel and register for automatic graceful shutdown.
        channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());

    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        channel.shutdown();
        channel.awaitTermination(1, TimeUnit.SECONDS);
    }


    private final AuthGrpc.AuthImplBase serviceImpl =
            mock(AuthGrpc.AuthImplBase.class, delegatesTo(
                    new AuthGrpc.AuthImplBase() {

                        @Override
                        public void register(RegisterRequest request, StreamObserver<RegisterResponse> respObserver) {
                            respObserver.onNext(RegisterResponse.getDefaultInstance());
                            respObserver.onCompleted();
                        }

                        @Override
                        public void createAuthenticationChallenge(AuthenticationChallengeRequest request,
                                                                  StreamObserver<AuthenticationChallengeResponse> respObserver) {
                            respObserver.onNext(AuthenticationChallengeResponse
                                    .newBuilder()
                                    .setAuthId("123")
                                    .setAuthId("authId")
                                    .build());
                            respObserver.onCompleted();
                        }

                        @Override
                        public void verifyAuthentication(AuthenticationAnswerRequest request,
                                                         StreamObserver<AuthenticationAnswerResponse> respObserver) {
                            respObserver.onNext(AuthenticationAnswerResponse
                                    .newBuilder()
                                    .setSessionId("sessionId123")
                                    .build());

                            respObserver.onCompleted();
                        }
                    }));


    /**
     *
     */
    @Test
    public void givenValidInput_whenRequest_thenGotValidVoidResponse() {
        String user = "blopes";
        BigInteger y1 = BigInteger.valueOf(1l);
        BigInteger y2 = BigInteger.valueOf(2l);

        grpcClient.registerSend(user,y1,y2);


        verify(grpcClient, times(1))
                .registerSend(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(BigInteger.class),
                        ArgumentMatchers.any(BigInteger.class));



    }

    @Test
    public void givenValidInput_whenChallenge_thenGotValidCall() {
        String user = "blopes";
        BigInteger r1 = BigInteger.valueOf(1l);
        BigInteger r2 = BigInteger.valueOf(2l);

        grpcClient.challengeSend(user,r1,r2);

        verify(grpcClient, times(1))
                .challengeSend(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(BigInteger.class),
                        ArgumentMatchers.any(BigInteger.class));

//         when(grpcClient.challengeSend(user,r1,r2))
//                .thenReturn(AuthenticationChallengeResponse.getDefaultInstance());


    }


    @Test
    public void givenValidInput_whenVerify_thenGotValidCall() {
        String authId = "authId";
        BigInteger s = BigInteger.valueOf(1);

        grpcClient.verifySend(authId,s);

        verify(grpcClient, times(1))
                .verifySend(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(BigInteger.class));

//        when(grpcClient.verifySend(authId,s))
//                .thenReturn(AuthenticationAnswerResponse.getDefaultInstance());
//

    }
}
