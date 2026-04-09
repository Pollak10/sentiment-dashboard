package com.Benjamin.sentiment_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SentimentDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(
				SentimentDashboardApplication.class, args
		);
	}
}
