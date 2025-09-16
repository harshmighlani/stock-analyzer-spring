package com.example.stockanalyzer.core;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MarketDataClient {

    private final RestClient http = RestClient.create();

    public List<Double> fetchRecentCloses(String symbol) {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?interval=1d&range=1mo";
        ResponseEntity<Map> res = http.get().uri(url).retrieve().toEntity(Map.class);
        Map body = res.getBody();
        if (body == null) return List.of();
        try {
            Map chart = (Map) body.get("chart");
            List result = (List) chart.get("result");
            Map first = (Map) result.get(0);
            Map indicators = (Map) first.get("indicators");
            List quotes = (List) indicators.get("quote");
            Map quote0 = (Map) quotes.get(0);
            List closes = (List) quote0.get("close");
            List<Double> out = new ArrayList<>();
            for (Object o : closes) {
                if (o instanceof Number n) out.add(n.doubleValue());
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }
}
