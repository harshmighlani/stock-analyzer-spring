package com.example.stockanalyzer.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlmClient {

    private final RestClient http = RestClient.create();

    @Value("${ollama.baseUrl:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:llama3.1:8b}")
    private String ollamaModel;

    public String analyze(String symbol, List<Double> closes) {
        String prompt = buildPrompt(symbol, closes);
        Map<String, Object> body = new HashMap<>();
        body.put("model", ollamaModel);
        body.put("prompt", prompt);
        body.put("stream", false);

        try {
            Map res = http.post()
                    .uri(ollamaBaseUrl + "/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            Object out = res != null ? res.get("response") : null;
            return out != null ? out.toString() : "No response from SLM";
        } catch (Exception e) {
            return "SLM call failed: " + e.getMessage();
        }
    }

    private String buildPrompt(String symbol, List<Double> closes) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a financial assistant.\n");
        sb.append("Given the last month of daily closing prices for symbol ").append(symbol).append(":\\n");
        sb.append(closes.toString()).append("\n\n");
        sb.append("Analyze short-term trend, volatility, and notable patterns in 5-8 sentences.\n");
        sb.append("Avoid giving financial advice; focus on descriptive analysis only.");
        return sb.toString();
    }
}
