package com.bole.zkpauth.controller;

import com.bole.zkpauth.AuthenticationChallengeResponse;
import com.bole.zkpauth.domain.LoginClientResponse;
import com.bole.zkpauth.domain.RegisterClientRequest;
import com.bole.zkpauth.domain.RegisterClientResponse;
import com.bole.zkpauth.exception.BadRequestException;
import com.bole.zkpauth.grpc.GrpcClient;
import com.bole.zkpauth.kafka.ZkpClient;
import com.bole.zkpauth.util.ZKPUtil;
import com.google.protobuf.ByteString;
import com.bole.zkpauth.AuthenticationAnswerResponse;
import com.bole.zkpauth.grpc.GrpcUtil;
import com.bole.zkpauth.util.ConcurrentMapCache;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import java.math.BigInteger;
import java.util.Optional;

@OpenAPIDefinition(
        info = @Info(
                title = "ZKP Prover Protocol REST API",
                version = "0.0.1"
        )
)
/**
 * ZKP Prover Protocol REST API with three endpoints
 * 1. HttpResponse<Optional<RegisterClientResponse>> register(@Body @Valid RegisterClientRequest request)
 * 2. HttpResponse<Optional<RegisterClientResponse>> register(@Body @Valid RegisterClientWithKeysRequest request)
 * 3. HttpResponse<Optional<LoginClientResponse>> login(@PathVariable(name = "user") String user,
 *                                                              @PathVariable(name = "password") String password)
 */
@Slf4j
@Controller("/zkp")
public class ZKPController {

    @Inject
    ZkpClient kafkaClient;


    GrpcClient grpcClient;

    /**
     * to store the values of the keys by userId
     */
    private final ConcurrentMapCache<String, BigInteger[]> keyCache
            = new ConcurrentMapCache<>(86400000L,86400000L,1000);



    @Operation(summary = "Api endpoint to allow any client to send a request with a Prime number and their user id" +
            " with the capability to generate all the needed keys, including the secret." +
            " 1. Public keys will be shared is a kafka topic" +
            " 2. Computed keys will be sent to the Verifier server to store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Keys successfully registered in the Verifier Server",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Body input invalid ",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid supplied credentials",
                    content = @Content)
    })
    /**
     * Api endpoint to allow any client to send a request with a Prime number and their user id
     * with the capability to generate all the needed keys, including the secret.
     * 1. compute P, G, H, Q and X
     * P - public chosen Prime P used to find the order of G and H, if the given input is not a prime number
     * get the nearest one.
     * H - public key same prime order as G
     * G - public key same prime order as H
     * Q - prime order of G and H
     * X - secret will be the greatest prime factor of P
     *
     * 2. Publish to kafka topic P, H and Q to be consumed by anyone, will be used to share with the server Verifier
     *
     * 3. Start Register process
     * 3.1. Compute Y1 and Y2
     * 3.2. Call Verifier server to register Y1 and Y2
     */
    @Post(value = "register", consumes = "application/json", produces = "application/json")
    public HttpResponse<Optional<RegisterClientResponse>> register(@Body @Valid RegisterClientRequest request) throws InterruptedException {
        log.info("ZKP - register process started: " + request.toString());
        try {

            BigInteger[] pghqx = ZKPUtil.computePGHQX(ZKPUtil.getBigInteger(request.getNumber()));
            String userId = request.getUser();
            keyCache.put(userId,pghqx);

            String pubKeys = pghqx[0].toString() + "," + pghqx[1].toString() + "," +pghqx[2];

            // publish to kafka topic: zkptopic
            kafkaClient.send(userId, pubKeys);

            log.debug("Register process initialized");
            log.debug("P: " + pghqx[0]);
            log.debug("G: " + pghqx[1]);
            log.debug("H: " + pghqx[2]);
            log.debug("X: " + pghqx[4]);
            log.debug("Q: " + pghqx[3]);
            log.debug("User ID: " + request.getUser());

            //compute the Y1 and Y2
            BigInteger[] yS = ZKPUtil.computeY1Y2(pghqx[1],pghqx[2],pghqx[4],pghqx[0]);
            log.debug("computation for Y1 and Y2");
            log.debug("Y1 = " + yS[0]);
            log.debug("Y2 = " + yS[1]);

            grpcClient = GrpcUtil.initGrpcClient();
            //Send Y1 and Y2 to Verifier
            grpcClient.registerSend(userId, yS[0],yS[1]);

            log.info("Register process finished for user id: " + userId);

            HttpResponse<Optional<RegisterClientResponse>> response = HttpResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Optional.of(new RegisterClientResponse("Y1 and Y2 successfully registered in the server.\n Login password: "+ pghqx[4])));

            return response;

        } catch (NumberFormatException | BadRequestException ex) {
            return HttpResponse.badRequest().body(
                    Optional.of(new RegisterClientResponse(ex.getMessage()))
            );
        }  finally {
            GrpcUtil.shutdownGrpcClient();
        }
    }

    @Operation(summary = "Api endpoint to allow any client to send a authentication request " +
            " providing the computed pair of keys based on a random generated value" +
            " 1. Generate random K" +
            " 2. Compute R1 and R1" +
            " 3. Challenge Verifier server" +
            " 4. Compute S as K - C.X (mod Q)" +
            " 5. Verify authentication with Verifier server" +
            " 6. Send back to client received answer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification successfully and session id received",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Body input invalid ",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid supplied credentials",
                    content = @Content)
    })
    /**
     * Api endpoint to allow clients to send a authentication request
     * 1. Generate random K
     * 2. Compute R1 and R1
     * 3. Challenge Verifier server
     * 4. Compute S as K - C.X (mod Q)
     * 5. Verify authentication with Verifier server
     * 6. Send back to client received answer
     *
     * @return HttpResponse<Optional<LoginClientResponse>>
     */
    @Get(value = "login/{user}/{password}", produces = "application/json")
    public HttpResponse<Optional<LoginClientResponse>> login(@PathVariable(name = "user") String user,
                                                             @PathVariable(name = "password") String password) throws InterruptedException {
        log.info("ZKP - login process started");
        try {
            //2 -  LOGIN PROCESS
            //Step #1 - generate a random k
            int rnum = (int)Math.floor(Math.random()*(Integer.MAX_VALUE-1000+1)+1000);

            BigInteger k = BigInteger.valueOf(rnum/1000);

            log.debug("Generated k with value: " + k);
            log.debug("received: " + user + " - " + password);
            //Get public keys
            BigInteger[] pghqx = keyCache.get(user);

            if(pghqx == null || pghqx.length != 5) {
                throw new BadRequestException("The user " + user + " keys have not been registered.");
            }

            BigInteger[] rS = ZKPUtil.computeR1R2(k, pghqx[0], pghqx[1], pghqx[2]);
            log.debug("computation for R1 and R2");
            log.debug("R1 = " + rS[0]);
            log.debug("R2 = " + rS[1]);

            grpcClient = GrpcUtil.initGrpcClient();

            //Send Y1 and Y2 to Verifier
            AuthenticationChallengeResponse authenticationChallengeResponse =
                    grpcClient.challengeSend(user, rS[0], rS[1]);

            ByteString c = authenticationChallengeResponse.getC();
            String authId = authenticationChallengeResponse.getAuthId();

            log.info("Authentication challenge response received");
            log.debug("C = " + new BigInteger(c.toByteArray()));
            log.debug("AuthID = " + authId);

            // now that we have the C for the AuthId lets compute the S
            // S = K - C.X (mod q)

            BigInteger s = ZKPUtil.computeS(k, new BigInteger(c.toByteArray()), new BigInteger(password), pghqx[3]);
            log.debug("Generated s with value: " + s);

            // now that we have the S let's send it to the Verifier
            log.debug("Authentication answer request sent with S = " + s);

            //Send Y1 and Y2 to Verifier
            AuthenticationAnswerResponse authenticationAnswerResponse = grpcClient.verifySend(authId, s);

            String sessionId = authenticationAnswerResponse.getSessionId();

            String resMessage;
            if(sessionId== null || sessionId.isEmpty()) {
                resMessage = "Authentication not verified actual R1 and R2 are not equal to the expected R1 and R2";
            } else {
                resMessage = "Authentication Successfully ended with session ID: " + sessionId;
            }

            HttpResponse<Optional<LoginClientResponse>> response = HttpResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Optional.of(new LoginClientResponse(resMessage)));

            return response;
        } catch ( NumberFormatException | BadRequestException ex) {
            return HttpResponse.badRequest().body(
                    Optional.of(new LoginClientResponse(ex.getMessage())));
        } finally {
            log.info("shutting down Grpc channel");
            GrpcUtil.shutdownGrpcClient();
        }
    }
}
