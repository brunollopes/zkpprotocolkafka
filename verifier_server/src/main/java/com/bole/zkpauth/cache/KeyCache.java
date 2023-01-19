package com.bole.zkpauth.cache;

import java.math.BigInteger;

/**
 * Concurrent HashMap to hold pairs <String,BigInteger>
 * with key being user id and value the keys
 */
public class KeyCache {

    private static final ConcurrentMapCache<String, BigInteger[]> keys
            = new ConcurrentMapCache<>(86400000L,86400000L,1000);

    public static ConcurrentMapCache<String, BigInteger[]> getKeys(){
        return  keys;
    }
}
