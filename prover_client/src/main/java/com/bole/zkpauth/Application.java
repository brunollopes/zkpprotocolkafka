package com.bole.zkpauth;

import io.micronaut.runtime.Micronaut;

/**
 * ZKP Protocol Prover Server Application
 * Micronaut @micronaut.io was chosen as framework due to its powerful modular capabilities oriented to
 * microservices, EDA with small memory footprints and short startup times
 */
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
