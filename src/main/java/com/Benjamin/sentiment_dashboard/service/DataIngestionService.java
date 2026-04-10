package com.Benjamin.sentiment_dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataIngestionService {

    @Autowired
    private KafkaProducerService kafkaProducer;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WatchlistService watchlistService;

    @Value("${api.gnews.key}")
    private String gnewsKey;

    private static final Map<String, String> DEFAULT_SYMBOLS =
            new HashMap<>() {{
                put("BTC", "Bitcoin");
                put("ETH", "Ethereum");
                put("AAPL", "Apple stock");
                put("TSLA", "Tesla stock");
            }};

    @Scheduled(fixedRate = 3600000, initialDelay = 10000)
    public void fetchNewsData() {
        System.out.println("Fetching news data...");

        Map<String, String> allSymbols = new HashMap<>(DEFAULT_SYMBOLS);

        List<String> watchlistSymbols =
                watchlistService.getWatchlistSymbols();
        for (String symbol : watchlistSymbols) {
            if (!allSymbols.containsKey(symbol)) {
                allSymbols.put(symbol, symbol);
            }
        }

        for (Map.Entry<String, String> entry : allSymbols.entrySet()) {
            String symbol = entry.getKey();
            String query = entry.getValue();

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
                        String title =
                                article.get("title").asText();
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

    public void fetchNewsForSymbol(String symbol) {
        System.out.println("Fetching immediate news for: " + symbol);
        try {
            String url = "https://gnews.io/api/v4/search" +
                    "?q=" + symbol +
                    "&lang=en" +
                    "&max=5" +
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