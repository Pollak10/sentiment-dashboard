package com.Benjamin.sentiment_dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void sendMessage(String symbol, String content) {
        String message = symbol + "||" + content;
        System.out.println("Publishing event: " + message);
        eventPublisher.publishEvent(
                new SentimentEvent(this, message)
        );
    }
}