package com.Benjamin.sentiment_dashboard.controller;

import org.springframework.web.bind.annotation.*;
import com.Benjamin.sentiment_dashboard.service.WatchlistService;
import com.Benjamin.sentiment_dashboard.model.WatchlistItem;
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
@CrossOrigin(origins = {
        "https://sentiment-dashboard-frontend.vercel.app",
        "http://localhost:5173"
})
public class SentimentController {
    @Autowired
    private SentimentService sentimentService;

    @Autowired
    private PriceService priceService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private WatchlistService watchlistService;

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

    @GetMapping("/watchlist")
    public List<WatchlistItem> getWatchlist() {
        return watchlistService.getWatchlist();
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

    @PostMapping("/watchlist/add")
    public Map<String, Object> addToWatchlist(
            @RequestBody Map<String, String> body) {
        String symbol = body.get("symbol");
        return watchlistService.addSymbol(symbol);
    }

    @DeleteMapping("/watchlist/remove/{symbol}")
    public Map<String, Object> removeFromWatchlist(
            @PathVariable String symbol) {
        return watchlistService.removeSymbol(symbol);
    }
}
