package com.example.stockanalyzer.api;

import com.example.stockanalyzer.model.StockRecommendation;
import com.example.stockanalyzer.service.AutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {
    
    private static final Logger log = Logger.getLogger(AutomationController.class.getName());
    
    @Autowired
    private AutomationService automationService;
    
    @GetMapping("/recommendations")
    public ResponseEntity<List<StockRecommendation>> getLatestRecommendations() {
        try {
            List<StockRecommendation> recommendations = automationService.getLatestRecommendations();
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/recommendations/{symbol}")
    public ResponseEntity<List<StockRecommendation>> getRecommendationsBySymbol(@PathVariable String symbol) {
        try {
            List<StockRecommendation> recommendations = automationService.getRecommendationsBySymbol(symbol.toUpperCase());
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/trigger-analysis")
    public ResponseEntity<Map<String, String>> triggerManualAnalysis() {
        try {
            automationService.triggerManualAnalysis();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Manual analysis triggered successfully");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to trigger analysis: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAutomationStatus() {
        try {
            List<StockRecommendation> latestRecommendations = automationService.getLatestRecommendations();
            
            Map<String, Object> status = new HashMap<>();
            status.put("automationEnabled", true);
            status.put("lastAnalysisTime", latestRecommendations.isEmpty() ? 
                "No analysis performed yet" : 
                latestRecommendations.get(0).getGeneratedAt());
            status.put("totalRecommendations", latestRecommendations.size());
            status.put("nextScheduledRun", "Daily at 9:00 PM EST");
            
            // Count recommendations by type
            Map<String, Long> recommendationCounts = latestRecommendations.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    rec -> rec.getRecommendation().toString(),
                    java.util.stream.Collectors.counting()
                ));
            status.put("recommendationBreakdown", recommendationCounts);
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            List<StockRecommendation> recommendations = automationService.getLatestRecommendations();
            
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalStocksAnalyzed", recommendations.size());
            dashboard.put("lastUpdate", recommendations.isEmpty() ? 
                "No data available" : 
                recommendations.get(0).getGeneratedAt());
            
            // Top recommendations
            List<Map<String, Object>> topRecommendations = recommendations.stream()
                .filter(rec -> rec.getRecommendation() == StockRecommendation.RecommendationType.STRONG_BUY)
                .limit(5)
                .map(rec -> {
                    Map<String, Object> recData = new HashMap<>();
                    recData.put("symbol", rec.getSymbol());
                    recData.put("companyName", rec.getCompanyName());
                    recData.put("currentPrice", rec.getCurrentPrice());
                    recData.put("targetPrice", rec.getTargetPrice());
                    recData.put("riskLevel", rec.getRiskLevel());
                    recData.put("reasoning", rec.getReasoning());
                    return recData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            dashboard.put("topRecommendations", topRecommendations);
            
            // Risk distribution
            Map<String, Long> riskDistribution = recommendations.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    rec -> {
                        double risk = rec.getRiskLevel();
                        if (risk <= 3) return "Low Risk";
                        else if (risk <= 6) return "Medium Risk";
                        else return "High Risk";
                    },
                    java.util.stream.Collectors.counting()
                ));
            dashboard.put("riskDistribution", riskDistribution);
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
