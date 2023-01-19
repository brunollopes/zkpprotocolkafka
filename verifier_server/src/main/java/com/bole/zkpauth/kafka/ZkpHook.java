package com.bole.zkpauth.kafka;

import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Consumer thread hook
 */
@Slf4j
public class ZkpHook implements  Runnable {

    private final ZkpConsumer zkpConsumer;

    public ZkpHook(final ZkpConsumer zkpConsumer) {
        this.zkpConsumer = zkpConsumer;
    }

    @Override
    public void run() {
        try {
            zkpConsumer.shutdown();
        } catch (InterruptedException e) {
            log.error("Error shutting down consumer", e);
        }
    }
}
