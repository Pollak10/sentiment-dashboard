package com.Benjamin.sentiment_dashboard.service;

import com.Benjamin.sentiment_dashboard.model.StockPrice;
import com.Benjamin.sentiment_dashboard.model.StockPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PriceService {

    @Autowired
    private StockPriceRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.finnhub.key}")
    private String finnhubKey;

    @Value("${api.coingecko.key}")
    private String coingeckoKey;

    private static final List<String> STOCKS =
            List.of("AAPL", "TSLA");

    private static final List<String> CRYPTO =
            List.of("BTC", "ETH");

    @Scheduled(fixedRate = 60000)
    public void fetchAllPrices() {
        System.out.println("Fetching prices...");
        fetchStockPrices();
        fetchCryptoPrices();
    }

    private void fetchStockPrices() {
        for (String symbol : STOCKS) {
            try {
                String url = "https://finnhub.io/api/v1/quote" +
                        "?symbol=" + symbol +
                        "&token=" + finnhubKey;

                ResponseEntity<String> response =
                        restTemplate.getForEntity(url, String.class);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.getBody());

                Double currentPrice = node.get("c").asDouble();
                Double previousClose = node.get("pc").asDouble();
                Double percentChange = ((currentPrice - previousClose)
                        / previousClose) * 100;
                percentChange = Math.round(percentChange * 100.0)
                        / 100.0;

                StockPrice price = new StockPrice();
                price.setSymbol(symbol);
                price.setCurrentPrice(currentPrice);
                price.setPercentChange(percentChange);
                price.setSource("finnhub");
                price.setTimestamp(LocalDateTime.now());

                repository.save(price);
                System.out.println("Saved price for " + symbol +
                        ": $" + currentPrice + " (" + percentChange + "%)");

            } catch (Exception e) {
                System.out.println("Error fetching price for " +
                        symbol + ": " + e.getMessage());
            }
        }
    }

    private void fetchCryptoPrices() {
        try {
            String url = "https://api.coingecko.com/api/v3/simple/price" +
                    "?ids=bitcoin,ethereum" +
                    "&vs_currencies=usd" +
                    "&include_24hr_change=true" +
                    "&x_cg_demo_api_key=" + coingeckoKey;

            ResponseEntity<String> response =
                    restTemplate.getForEntity(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());

            saveCryptoPrice("BTC", node.get("bitcoin"));
            saveCryptoPrice("ETH", node.get("ethereum"));

        } catch (Exception e) {
            System.out.println("Error fetching crypto prices: " +
                    e.getMessage());
        }
    }

    private void saveCryptoPrice(String symbol, JsonNode data) {
        StockPrice price = new StockPrice();
        price.setSymbol(symbol);
        price.setCurrentPrice(data.get("usd").asDouble());
        price.setPercentChange(
                Math.round(data.get("usd_24h_change").asDouble()
                        * 100.0) / 100.0
        );
        price.setSource("coingecko");
        price.setTimestamp(LocalDateTime.now());
        repository.save(price);
        System.out.println("Saved price for " + symbol +
                ": $" + price.getCurrentPrice() +
                " (" + price.getPercentChange() + "%)");
    }

    public List<StockPrice> getLatestPrices() {
        List<String> symbols = List.of("BTC", "ETH", "AAPL", "TSLA");
        List<StockPrice> prices = new ArrayList<>();
        for (String symbol : symbols) {
            Optional<StockPrice> latest =
                    repository.findTopBySymbolOrderByTimestampDesc(symbol);
            latest.ifPresent(prices::add);
        }
        return prices;
    }
}