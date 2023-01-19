package com.bole.zkpauth.kafka;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import org.apache.kafka.clients.producer.ProducerConfig;

/**
 * Kafka Client to produce messages with help of the micronaut annotations
 */
@KafkaClient(
        id = "zkp-client",
        acks = KafkaClient.Acknowledge.ALL,
        properties = @Property(name = ProducerConfig.RETRIES_CONFIG, value = "3")
)
public interface ZkpClient {

    @Topic(value = "zkptopic")
    @KafkaClient("zkp-consumer")
    void send(@KafkaKey String user, String keys);
}

