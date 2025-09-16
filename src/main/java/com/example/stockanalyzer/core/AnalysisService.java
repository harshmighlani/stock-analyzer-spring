package com.example.stockanalyzer.core;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {

    private final MarketDataClient marketDataClient;
    private final SlmClient slmClient;

    public AnalysisService(MarketDataClient marketDataClient, SlmClient slmClient) {
        this.marketDataClient = marketDataClient;
        this.slmClient = slmClient;
    }

    public Map<String, Object> analyzeSymbol(String symbol) {
        List<Double> closes = marketDataClient.fetchRecentCloses(symbol);
        String summary = slmClient.analyze(symbol, closes);
        Map<String, Object> response = new HashMap<>();
        response.put("symbol", symbol);
        response.put("closes", closes);
        response.put("analysis", summary);
        return response;
    }
}
