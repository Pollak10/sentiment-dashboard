package com.Benjamin.sentiment_dashboard.service;

import com.Benjamin.sentiment_dashboard.model.SentimentData;
import com.Benjamin.sentiment_dashboard.model.SentimentDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SentimentService {

    @Autowired
    private SentimentDataRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.huggingface.key}")
    private String huggingFaceKey;

    public SentimentData analyzeSentiment(String symbol,
                                          String source,
                                          String content) throws Exception {
        String url = "https://router.huggingface.co/hf-inference/models/" +
                "ProsusAI/finbert";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + huggingFaceKey);
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");

        String requestBody = "{\"inputs\": \"" +
                content.replace("\"", "'") + "\"}";
        HttpEntity<String> request =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class
        );

        String responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode results = mapper.readTree(responseBody);
        String sentiment = results.get(0).get(0).get("label").asText();
        Double score = results.get(0).get(0).get("score").asDouble();

        SentimentData data = new SentimentData();
        data.setSymbol(symbol);
        data.setSource(source);
        data.setContent(content);
        data.setSentiment(sentiment);
        data.setSentimentScore(score);
        data.setTimestamp(LocalDateTime.now());

        return repository.save(data);
    }

    public List<SentimentData> getBySymbol(String symbol) {
        return repository.findBySymbolOrderByTimestampDesc(symbol);
    }

    public List<SentimentData> getAllSentiments() {
        return repository.findAll();
    }
}