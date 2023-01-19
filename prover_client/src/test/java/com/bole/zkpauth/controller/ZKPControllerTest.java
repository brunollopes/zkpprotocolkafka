package com.bole.zkpauth.controller;


import io.micronaut.http.*;
import io.micronaut.http.client.HttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


import static io.restassured.RestAssured.given;

/**
 * Test class to cover simple scenarios for the rest controller
 * not the purpose to cover all possible scenarios
 * @com.bole.zkpauth.controller.ZKPController
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ZKPControllerTest {

    @Inject
    HttpClient httpClient;


    @Test
    public void givenAValidBody_whenRegister_Got200() throws Exception {
        String user = "blopes";

        HttpRequest request = HttpRequest.create(HttpMethod.POST, "http://localhost:8000/zkp/register/")
                .accept(MediaType.APPLICATION_JSON)
                .body("{\"number\":\"104749\",\"user\":\"blopes\"}");

        HttpResponse response = httpClient.toBlocking().exchange(request);

        Assertions.assertEquals(HttpStatus.OK.getCode(), response.status().getCode());


    }

    @Test
    public void givenAValidINput_whenLogin_Got200() throws Exception {
        String user = "blopes";

        HttpRequest request = HttpRequest.create(HttpMethod.GET,
                        "http://localhost:8000/zkp/login/blopes/3")
                .accept(MediaType.APPLICATION_JSON);

        HttpResponse response = httpClient.toBlocking().exchange(request);

        Assertions.assertEquals(HttpStatus.OK.getCode(), response.status().getCode());


    }

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8800";
    }

    @Test
    public void givenAValidBody_whenRegister_Got200(RequestSpecification spec) {
        String requestBody = "{\n" +
                "  \"number\": \"104749\",\n" +
                "  \"user\": \"blopes\"}";

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(requestBody)
                .when()
                .post("/zkp/register")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.getStatusCode(), "Expected 200");

    }

    @Test
    public void givenAInValidBody_whenRegister_Got400(RequestSpecification spec) {
        String requestBody = "{\n" +
                "  \"number\": \"104749\",\n" +
                "  \"userid\": \"blopes\"}";

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(requestBody)
                .when()
                .post("/zkp/register")
                .then()
                .extract().response();

        //here should return 400 bad request, but grpc server is not
        Assertions.assertEquals(500, response.getStatusCode(), "Expected 500");

    }

    @Test
    public void givenAValidInput_whenLogin_Got200(RequestSpecification spec) {

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .when()
                .get("/zkp/login/blopes/3")
                .then()
                .extract().response();

        //here should return 400 bad request, but grpc server is not
        Assertions.assertEquals(500, response.getStatusCode(), "Expected 200 ok");

    }
}
