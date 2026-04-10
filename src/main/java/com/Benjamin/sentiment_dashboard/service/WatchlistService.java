package com.Benjamin.sentiment_dashboard.service;

import com.Benjamin.sentiment_dashboard.model.WatchlistItem;
import com.Benjamin.sentiment_dashboard.model.WatchlistRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WatchlistService {

    @Autowired
    private WatchlistRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.finnhub.key}")
    private String finnhubKey;

    @Value("${api.coingecko.key}")
    private String coingeckoKey;

    public List<WatchlistItem> getWatchlist() {
        return repository.findAll();
    }

    public Map<String, Object> addSymbol(String symbol) {
        symbol = symbol.toUpperCase().trim();

        if (repository.existsBySymbol(symbol)) {
            return Map.of(
                    "success", false,
                    "message", symbol + " is already in your watchlist"
            );
        }

        // Try stock first
        WatchlistItem stockItem = validateStock(symbol);
        if (stockItem != null) {
            repository.save(stockItem);
            return Map.of(
                    "success", true,
                    "message", "Added " + stockItem.getName(),
                    "item", stockItem
            );
        }

        // Try crypto
        WatchlistItem cryptoItem = validateCrypto(symbol);
        if (cryptoItem != null) {
            repository.save(cryptoItem);
            return Map.of(
                    "success", true,
                    "message", "Added " + cryptoItem.getName(),
                    "item", cryptoItem
            );
        }

        return Map.of(
                "success", false,
                "message", "Could not find symbol: " + symbol
        );
    }

    private WatchlistItem validateStock(String symbol) {
        try {
            String url = "https://finnhub.io/api/v1/search" +
                    "?q=" + symbol +
                    "&token=" + finnhubKey;

            ResponseEntity<String> response =
                    restTemplate.getForEntity(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());
            JsonNode results = node.get("result");

            if (results != null && results.isArray()
                    && results.size() > 0) {
                JsonNode first = results.get(0);
                String foundSymbol = first.get("symbol").asText();
                String name = first.get("description").asText();

                if (foundSymbol.equalsIgnoreCase(symbol)) {
                    WatchlistItem item = new WatchlistItem();
                    item.setSymbol(symbol);
                    item.setName(name);
                    item.setType("stock");
                    item.setAddedAt(LocalDateTime.now());
                    return item;
                }
            }
        } catch (Exception e) {
            System.out.println("Stock validation error: " +
                    e.getMessage());
        }
        return null;
    }

    private WatchlistItem validateCrypto(String symbol) {
        try {
            String url = "https://api.coingecko.com/api/v3/search" +
                    "?query=" + symbol;

            ResponseEntity<String> response =
                    restTemplate.getForEntity(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());
            JsonNode coins = node.get("coins");

            if (coins != null && coins.isArray()
                    && coins.size() > 0) {
                JsonNode first = coins.get(0);
                String foundSymbol =
                        first.get("symbol").asText().toUpperCase();
                String name = first.get("name").asText();

                if (foundSymbol.equalsIgnoreCase(symbol)) {
                    WatchlistItem item = new WatchlistItem();
                    item.setSymbol(symbol);
                    item.setName(name);
                    item.setType("crypto");
                    item.setAddedAt(LocalDateTime.now());
                    return item;
                }
            }
        } catch (Exception e) {
            System.out.println("Crypto validation error: " +
                    e.getMessage());
        }
        return null;
    }

    public Map<String, Object> removeSymbol(String symbol) {
        Optional<WatchlistItem> item =
                repository.findBySymbol(symbol.toUpperCase());
        if (item.isPresent()) {
            repository.delete(item.get());
            return Map.of(
                    "success", true,
                    "message", symbol + " removed from watchlist"
            );
        }
        return Map.of(
                "success", false,
                "message", symbol + " not found in watchlist"
        );
    }

    public List<String> getWatchlistSymbols() {
        return repository.findAll()
                .stream()
                .map(WatchlistItem::getSymbol)
                .toList();
    }
}