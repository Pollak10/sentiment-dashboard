package com.Benjamin.sentiment_dashboard.service;

import com.Benjamin.sentiment_dashboard.service.KafkaProducerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.List;

@Service
public class DataIngestionService {

    @Autowired
    private KafkaProducerService kafkaProducer;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.gnews.key}")
    private String gnewsKey;

    private static final List<String> SYMBOLS =
            List.of("BTC", "ETH", "AAPL", "TSLA");

    private static final List<String> QUERIES =
            List.of("Bitcoin", "Ethereum", "Apple stock", "Tesla stock");

    @Scheduled(fixedRate = 3600000, initialDelay = 10000)
    public void fetchNewsData() {
        System.out.println("Fetching news data...");

        for (int i = 0; i < SYMBOLS.size(); i++) {
            String symbol = SYMBOLS.get(i);
            String query = QUERIES.get(i);

            try {
                String url = "https://gnews.io/api/v4/search" +
                        "?q=" + query +
                        "&lang=en" +
                        "&max=3" +
                        "&apikey=" + gnewsKey;

                ResponseEntity<String> response =
                        restTemplate.getForEntity(url, String.class);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode body = mapper.readTree(response.getBody());
                JsonNode articles = body.get("articles");

                if (articles != null && articles.isArray()) {
                    for (JsonNode article : articles) {
                        String title = article.get("title").asText();
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