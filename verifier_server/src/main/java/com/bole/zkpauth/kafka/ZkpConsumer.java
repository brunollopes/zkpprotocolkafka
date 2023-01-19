package com.bole.zkpauth.kafka;

import com.bole.zkpauth.cache.KeyCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Kafka Consumer thread to listening for incoming new messages
 * which will be converted to BigInteger array and added to the
 * server cache ready to be used for the ZKP.
 */
@Slf4j
public class ZkpConsumer  implements Runnable {

    private CountDownLatch countDownLatch;
    private Consumer<String, String> consumer;


    @Override
    public void run() {
        countDownLatch = new CountDownLatch(1);
        final Properties properties = new Properties();

        // Setup Producer Properties
        String bootstrapServers = "kafka:9092";
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "zkp-consumer");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton("zkptopic"));

        final Duration pollTimeout = Duration.ofMillis(100);

        try {
            while (true) {
                final ConsumerRecords<String, String> consumerRecords = consumer.poll(pollTimeout);
                for (final ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    String key = consumerRecord.key();
                    String value = consumerRecord.value();

                    log.debug("Getting consumer record key: '" + key
                            + "', value: '" + value
                            + "', partition: " + consumerRecord.partition()
                            + " and offset: " + consumerRecord.offset()
                            + " at " + new Date(consumerRecord.timestamp()));

                    String[] keys = value.split(",");
                    BigInteger[] bKeys = new BigInteger[keys.length];

                    for (int i=0; i<keys.length ;i++) {
                        bKeys[i] = new BigInteger(String.valueOf(keys[i]));
                    }
                    KeyCache.getKeys().put(key,bKeys);

                }
            }
        } catch (WakeupException e) {
            log.debug("Consumer poll woke up");
        } finally {
            consumer.close();
            countDownLatch.countDown();
        }
    }

    void shutdown() throws InterruptedException {
        consumer.wakeup();
        countDownLatch.await();
        log.info("Consumer closed");
    }
}
