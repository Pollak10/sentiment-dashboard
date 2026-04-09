package com.Benjamin.sentiment_dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "sentiment-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String symbol, String content) {
        String message = symbol + "||" + content;
        kafkaTemplate.send(TOPIC, message);
        System.out.println("Sent to kafka: " + message);
    }
}
