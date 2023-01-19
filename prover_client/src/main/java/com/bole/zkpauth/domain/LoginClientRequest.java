package com.bole.zkpauth.domain;


import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Login Client request pojo with json representation
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class LoginClientRequest {
   @NotNull
    private String user;

   @NotNull
    private String password;
}
