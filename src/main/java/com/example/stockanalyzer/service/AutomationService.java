package com.example.stockanalyzer.service;

import com.example.stockanalyzer.model.NewsAnalysis;
import com.example.stockanalyzer.model.StockRecommendation;
import com.example.stockanalyzer.repository.StockRecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Service
public class AutomationService {
    
    private static final Logger log = Logger.getLogger(AutomationService.class.getName());
    
    @Autowired
    private NewsScrapingService newsScrapingService;
    
    @Autowired
    private RecommendationEngineService recommendationEngineService;
    
    @Autowired
    private StockRecommendationRepository recommendationRepository;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    // Run daily at 9 PM EST
    @Scheduled(cron = "0 0 21 * * *", zone = "America/New_York")
    public void runDailyAnalysis() {
        
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    performDailyAnalysis();
                } catch (Exception e) {
                }
            }, executorService);
            
        } catch (Exception e) {
        }
    }
    
    public void performDailyAnalysis() {
        
        try {
            // Get popular stocks to analyze
            List<String> stocksToAnalyze = recommendationEngineService.getPopularStocks();
            
            // Analyze news for each stock
            List<NewsAnalysis> newsAnalyses = stocksToAnalyze.stream()
                .limit(10) // Limit to 10 stocks per day to avoid rate limiting
                .map(symbol -> {
                    try {
                        return newsScrapingService.analyzeStockNews(symbol, getCompanyName(symbol));
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(analysis -> analysis != null)
                .collect(java.util.stream.Collectors.toList());
            
            // Generate recommendations
            List<StockRecommendation> recommendations = recommendationEngineService.generateRecommendations(newsAnalyses);
            
            // Save recommendations to database
            recommendationRepository.saveAll(recommendations);
            
            // Write recommendations to file
            writeRecommendationsToFile(recommendations);
            
            // Log summary
            System.out.println("Daily analysis completed. Generated " + recommendations.size() + " recommendations for " + newsAnalyses.size() + " stocks");
            
            // Log top recommendations
            recommendations.stream()
                .filter(r -> r.getRecommendation() == StockRecommendation.RecommendationType.STRONG_BUY)
                .limit(3)
                .forEach(r -> System.out.println("Strong Buy: " + r.getSymbol() + " at $" + String.format("%.2f", r.getCurrentPrice())));
            
        } catch (Exception e) {
        }
    }
    
    private void writeRecommendationsToFile(List<StockRecommendation> recommendations) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = String.format("stock_recommendations_%s.txt", timestamp);
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("=== DAILY STOCK RECOMMENDATIONS ===\n");
            writer.write("Generated at: " + LocalDateTime.now() + "\n\n");
            
            for (StockRecommendation rec : recommendations) {
                writer.write(String.format("Symbol: %s (%s)\n", rec.getSymbol(), rec.getCompanyName()));
                writer.write(String.format("Current Price: $%.2f\n", rec.getCurrentPrice()));
                writer.write(String.format("Previous Close: $%.2f\n", rec.getPreviousClose()));
                writer.write(String.format("Recommendation: %s\n", rec.getRecommendation()));
                writer.write(String.format("Target Price: $%.2f\n", rec.getTargetPrice()));
                writer.write(String.format("Stop Loss: $%.2f\n", rec.getStopLoss()));
                writer.write(String.format("Risk Level: %.1f/10\n", rec.getRiskLevel()));
                writer.write(String.format("Reasoning: %s\n", rec.getReasoning()));
                writer.write(String.format("Key Keywords: %s\n", String.join(", ", rec.getKeyKeywords())));
                writer.write("---\n\n");
            }
            
            System.out.println("Recommendations written to file: " + filename);
            
        } catch (IOException e) {
            System.out.println("Failed to write recommendations to file: " + e.getMessage());
        }
    }
    
    private String getCompanyName(String symbol) {
        // Simplified company name mapping - in real implementation, would use a service
        switch (symbol) {
            case "AAPL": return "Apple Inc.";
            case "MSFT": return "Microsoft Corporation";
            case "GOOGL": return "Alphabet Inc.";
            case "AMZN": return "Amazon.com Inc.";
            case "TSLA": return "Tesla Inc.";
            case "META": return "Meta Platforms Inc.";
            case "NVDA": return "NVIDIA Corporation";
            case "NFLX": return "Netflix Inc.";
            case "AMD": return "Advanced Micro Devices Inc.";
            case "INTC": return "Intel Corporation";
            case "CRM": return "Salesforce Inc.";
            case "ADBE": return "Adobe Inc.";
            case "PYPL": return "PayPal Holdings Inc.";
            case "UBER": return "Uber Technologies Inc.";
            case "LYFT": return "Lyft Inc.";
            case "SQ": return "Block Inc.";
            case "ROKU": return "Roku Inc.";
            case "ZM": return "Zoom Video Communications Inc.";
            case "DOCU": return "DocuSign Inc.";
            case "SNOW": return "Snowflake Inc.";
            default: return symbol + " Corporation";
        }
    }
    
    // Manual trigger for testing
    public void triggerManualAnalysis() {
        System.out.println("Manual analysis triggered");
        performDailyAnalysis();
    }
    
    // Get latest recommendations
    public List<StockRecommendation> getLatestRecommendations() {
        return recommendationRepository.findAll();
    }
    
    // Get recommendations by symbol
    public List<StockRecommendation> getRecommendationsBySymbol(String symbol) {
        return recommendationRepository.findBySymbolOrderByGeneratedAtDesc(symbol);
    }
}
