package com.Benjamin.sentiment_dashboard.service;

import java.util.Map;
import com.Benjamin.sentiment_dashboard.model.SentimentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendSentimentUpdate(SentimentData sentimentData){
        messagingTemplate.convertAndSend(
                "/topic/sentiment",
                sentimentData
        );
        System.out.println("Pushed to WebSocket: " +
                sentimentData.getSymbol() + " " +
                sentimentData.getSentiment());
    }
    public void sendAnalyticsUpdate(Map<String, Object> analytics) {
        messagingTemplate.convertAndSend(
                "/topic/analytics",
                (Object) analytics
        );
    }
}
