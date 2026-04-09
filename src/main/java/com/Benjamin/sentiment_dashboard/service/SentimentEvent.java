package com.Benjamin.sentiment_dashboard.service;

import org.springframework.context.ApplicationEvent;

public class SentimentEvent extends ApplicationEvent {

    private final String message;

    public SentimentEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
