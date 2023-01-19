package com.bole.zkpauth.domain;


import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Register Client Request pojo with Keys and json representation
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class RegisterClientWithKeysRequest {
    @NotNull
    private String keyP;
    @NotNull
    private String keyG;
    @NotNull
    private String keyH;
    @NotNull
    private String keyQ;
    @NotNull
    private String keyX;

   @NotNull
    private String user;
}
