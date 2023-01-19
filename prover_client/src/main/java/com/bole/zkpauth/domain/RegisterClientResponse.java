package com.bole.zkpauth.domain;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;


/**
 * Register Client Response pojo with json representation
 */
@Getter
@Setter
@AllArgsConstructor
@JsonRootName(value = "response")
public class RegisterClientResponse {
    @NotNull
    private String message;

}
