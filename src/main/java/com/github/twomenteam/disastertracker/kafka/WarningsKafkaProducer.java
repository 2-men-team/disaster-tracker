package com.github.twomenteam.disastertracker.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;
import reactor.util.function.Tuple2;

@Component
public class WarningsKafkaProducer {
  public static final String BOOTSTRAP_SERVERS = "";
  public static final String TOPIC = "new-warnings";

  private final KafkaSender<Integer, Integer> sender;

  public WarningsKafkaProducer() {
    Map<String, Object> props = Map.of(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS,
        ProducerConfig.CLIENT_ID_CONFIG, "warnings-kafka-producer",
        ProducerConfig.ACKS_CONFIG, "all",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
    SenderOptions<Integer, Integer> senderOptions = SenderOptions.create(props);

    sender = KafkaSender.create(senderOptions);
  }

  // #TODO: correlation metadata - pass message index instead of "1"
  public Flux<SenderResult<Integer>> sendMessages(String topic, Flux<Tuple2<Integer, Integer>> messages) {
    return sender.send(
        messages.map(tuple ->
            SenderRecord.create(new ProducerRecord<>(topic, tuple.getT1(), tuple.getT2()), 1)));
  }
}
