package com.Benjamin.sentiment_dashboard.controller;

import com.Benjamin.sentiment_dashboard.service.AnalyticsService;
import java.util.Map;
import com.Benjamin.sentiment_dashboard.service.PriceService;
import com.Benjamin.sentiment_dashboard.model.StockPrice;
import com.Benjamin.sentiment_dashboard.model.SentimentData;
import com.Benjamin.sentiment_dashboard.service.SentimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sentiments")
@CrossOrigin(origins = "*")
public class SentimentController {

    @Autowired
    private SentimentService sentimentService;

    @Autowired
    private PriceService priceService;

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping
    public List<SentimentData> getAllSentiments() {
        return sentimentService.getAllSentiments();
    }

    @GetMapping("/{symbol}")
    public List<SentimentData> getSentimentsBySymbol(
            @PathVariable String symbol){
        return sentimentService.getBySymbol(symbol);
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Sentiment Dashboard is running!";
    }

    @GetMapping("/prices")
    public List<StockPrice> getLatestPrices() {
        return priceService.getLatestPrices();
    }

    @GetMapping("/analytics")
    public Map<String, Object> getAnalytics() {
        return analyticsService.getAnalytics();
    }

    @PostMapping("/analytics/join")
    public void userJoined(@RequestBody Map<String, String> body,
                           jakarta.servlet.http.HttpServletRequest request) {
        String sessionId = body.get("sessionId");
        String ipAddress = request.getRemoteAddr();
        analyticsService.userJoined(sessionId, ipAddress);
    }

    @PostMapping("/analytics/leave")
    public void userLeft(@RequestBody Map<String, String> body) {
        String sessionId = body.get("sessionId");
        analyticsService.userLeft(sessionId);
    }

    @PostMapping("/analytics/heartbeat")
    public void heartbeat(@RequestBody Map<String, String> body) {
        String sessionId = body.get("sessionId");
        analyticsService.userHeartbeat(sessionId);
    }
}
