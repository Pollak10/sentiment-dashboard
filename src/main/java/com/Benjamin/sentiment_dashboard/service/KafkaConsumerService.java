package com.Benjamin.sentiment_dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @Autowired
    private SentimentService sentimentService;

    @Autowired
    private WebSocketService webSocketService;

    @EventListener
    public void consumeMessage(SentimentEvent event) {
        String message = event.getMessage();
        System.out.println("Received event: " + message);

        try {
            String[] parts = message.split("\\|\\|");
            String symbol = parts[0];
            String content = parts[1];

            var sentimentData = sentimentService.analyzeSentiment(
                    symbol, "news", content
            );

            webSocketService.sendSentimentUpdate(sentimentData);
        } catch (Exception e) {
            System.out.println("Error processing event: " +
                    e.getMessage());
        }
    }
}