package com.github.twomenteam.disastertracker.kafka;

import org.apache.kafka.clients.KafkaClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

@Component
public class WarningsKafkaConsumer {
  private static final String BOOTSTRAP_SERVERS = "localhost:9092";
  private static final String TOPIC = "new-warnings";

  private final ReceiverOptions<Integer, Integer> receiverOptions;

  public WarningsKafkaConsumer() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, "warnings-kafka-consumer");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "warning-group");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    receiverOptions = ReceiverOptions.create(props);
  }

  public Flux<ReceiverRecord<Integer, Integer>> consumeMessages(String topic) {
    ReceiverOptions<Integer, Integer> options = receiverOptions.subscription(Collections.singleton(topic));
    return KafkaReceiver.create(options).receive();
  }
}
