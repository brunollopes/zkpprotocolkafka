package com.bole.zkpauth.domain;

import java.math.BigInteger;

/**
 * Record to store Prover keys
 * @param userId
 * @param rS
 */
public record AuthSession(String userId, BigInteger[] rS) { }
