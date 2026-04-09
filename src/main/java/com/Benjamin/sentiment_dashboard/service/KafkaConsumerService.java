package com.Benjamin.sentiment_dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final String TOPIC = "sentiment-topic";

    @Autowired
    private SentimentService sentimentService;

    @Autowired
    private WebSocketService webSocketService;

    @KafkaListener(topics = TOPIC, groupId = "sentiment-group")
    public void consumeMessage(String message) {
        System.out.println("Received from Kafka: " + message);

        try {
            String[] parts = message.split("\\|\\|");
            String symbol = parts[0];
            String content = parts[1];

            var sentimentData = sentimentService.analyzeSentiment(
                    symbol, "news", content
            );

            webSocketService.sendSentimentUpdate(sentimentData);
        } catch (Exception e) {
            System.out.println("Error processing message: " +
                    e.getMessage());
        }
    }
}
