package com.bole.zkpauth.domain;


import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Register Client request pojo with json representation
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class RegisterClientRequest {
    @NotNull
    private String number;

   @NotNull
    private String user;
}
