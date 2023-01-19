package com.bole.zkpauth.domain;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;


/**
 * Login Client Response pojo with json representation
 */
@Getter
@Setter
@AllArgsConstructor
@JsonRootName(value = "response")
public class LoginClientResponse {
    @NotNull
    private String message;

}
