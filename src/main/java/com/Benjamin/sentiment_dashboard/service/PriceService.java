package com.Benjamin.sentiment_dashboard.service;

import com.Benjamin.sentiment_dashboard.model.StockPrice;
import com.Benjamin.sentiment_dashboard.model.StockPriceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.Benjamin.sentiment_dashboard.service.WatchlistService;

@Service
public class PriceService {

    @Autowired
    private StockPriceRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WatchlistService watchlistService;

    @Value("${api.finnhub.key}")
    private String finnhubKey;

    @Value("${api.coingecko.key}")
    private String coingeckoKey;

    private static final List<String> DEFAULT_STOCKS =
            List.of("AAPL", "TSLA");

    private static final List<String> DEFAULT_CRYPTO =
            List.of("BTC", "ETH");

    @Scheduled(fixedRate = 60000)
    public void fetchAllPrices() {
        System.out.println("Fetching prices...");

        List<String> stocks = new ArrayList<>(DEFAULT_STOCKS);
        List<String> crypto = new ArrayList<>(DEFAULT_CRYPTO);

        List<String> watchlistSymbols =
                watchlistService.getWatchlistSymbols();

        for (String symbol : watchlistSymbols) {
            if (!stocks.contains(symbol) &&
                    !crypto.contains(symbol)) {
                String type = watchlistService.getWatchlist()
                        .stream()
                        .filter(w -> w.getSymbol().equals(symbol))
                        .findFirst()
                        .map(w -> w.getType())
                        .orElse("stock");

                if (type.equals("crypto")) {
                    crypto.add(symbol);
                } else {
                    stocks.add(symbol);
                }
            }
        }

        for (String symbol : stocks) {
            fetchStockPrice(symbol);
        }

        fetchCryptoPrices(crypto);
    }

    private void fetchStockPrice(String symbol) {
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

            if (currentPrice == 0) return;

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
                    ": $" + currentPrice +
                    " (" + percentChange + "%)");

        } catch (Exception e) {
            System.out.println("Error fetching price for " +
                    symbol + ": " + e.getMessage());
        }
    }

    private void fetchCryptoPrices(List<String> cryptoSymbols) {
        try {
            String ids = cryptoSymbols.stream()
                    .map(s -> s.toLowerCase()
                            .replace("btc", "bitcoin")
                            .replace("eth", "ethereum"))
                    .reduce((a, b) -> a + "," + b)
                    .orElse("bitcoin,ethereum");

            String url = "https://api.coingecko.com/api/v3/simple/price" +
                    "?ids=" + ids +
                    "&vs_currencies=usd" +
                    "&include_24hr_change=true" +
                    "&x_cg_demo_api_key=" + coingeckoKey;

            ResponseEntity<String> response =
                    restTemplate.getForEntity(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());

            for (String symbol : cryptoSymbols) {
                String coinId = symbol.toLowerCase()
                        .replace("btc", "bitcoin")
                        .replace("eth", "ethereum");
                JsonNode coinData = node.get(coinId);
                if (coinData != null) {
                    saveCryptoPrice(symbol, coinData);
                }
            }

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
        List<String> defaultSymbols =
                List.of("BTC", "ETH", "AAPL", "TSLA");
        List<String> watchlistSymbols =
                watchlistService.getWatchlistSymbols();

        List<String> allSymbols = new ArrayList<>(defaultSymbols);
        for (String symbol : watchlistSymbols) {
            if (!allSymbols.contains(symbol)) {
                allSymbols.add(symbol);
            }
        }

        List<StockPrice> prices = new ArrayList<>();
        for (String symbol : allSymbols) {
            Optional<StockPrice> latest =
                    repository.findTopBySymbolOrderByTimestampDesc(symbol);
            latest.ifPresent(prices::add);
        }
        return prices;
    }
    public void fetchPriceForSymbol(String symbol, String type) {
        System.out.println("Fetching immediate price for: " + symbol);
        if (type.equals("crypto")) {
            fetchCryptoPrices(List.of(symbol));
        } else {
            fetchStockPrice(symbol);
        }
    }
}