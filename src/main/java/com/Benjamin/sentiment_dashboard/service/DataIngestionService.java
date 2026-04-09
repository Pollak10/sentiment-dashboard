package com.Benjamin.sentiment_dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.List;
import java.util.Map;

@Service
public class DataIngestionService {

    @Autowired
    private KafkaProducerService kafkaProducer;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.news.key}")
    private String newsApiKey;

    private static final List<String> SYMBOLS =
            List.of("BTC", "ETH", "AAPL", "TSLA");

    private static final List<String> QUERIES =
            List.of("Bitcoin", "Ethereum", "Apple stock", "Tesla stock");

    @Scheduled(fixedRate = 300000)
    public void fetchNewsData() {
        System.out.println("Fetching news data...");

        for (int i = 0; i < SYMBOLS.size(); i++) {
            String symbol = SYMBOLS.get(i);
            String query = QUERIES.get(i);

            try {
                String url = "https://newsapi.org/v2/everything" +
                        "?q=" + query +
                        "&sortBy=publishedAt" +
                        "&pageSize=2" +
                        "&apiKey=" + newsApiKey;

                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "SentimentDashboard/1.0");

                HttpEntity<String> request =
                        new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                        url, HttpMethod.GET, request, Map.class
                );

                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> articles =
                        (List<Map<String, Object>>) body.get("articles");

                if (articles != null) {
                    for (Map<String, Object> article : articles) {
                        String title =
                                (String) article.get("title");
                        if (title != null && !title.isEmpty()) {
                            kafkaProducer.sendMessage(symbol, title);
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("Error fetching news for " +
                        symbol + ": " + e.getMessage());
            }
        }
    }
}