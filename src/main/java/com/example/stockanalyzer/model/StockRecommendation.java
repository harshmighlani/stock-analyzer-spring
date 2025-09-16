package com.example.stockanalyzer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stock_recommendations")
public class StockRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(nullable = false)
    private String companyName;
    
    @Column(nullable = false)
    private Double currentPrice;
    
    @Column(nullable = false)
    private Double previousClose;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendationType recommendation;
    
    @Column(length = 1000)
    private String reasoning;
    
    @Column
    private Double targetPrice;
    
    @Column
    private Double stopLoss;
    
    @Column
    private Double riskLevel; // 1-10 scale
    
    @ElementCollection
    @CollectionTable(name = "recommendation_keywords", joinColumns = @JoinColumn(name = "recommendation_id"))
    @Column(name = "keyword")
    private List<String> keyKeywords;
    
    @ElementCollection
    @CollectionTable(name = "recommendation_sources", joinColumns = @JoinColumn(name = "recommendation_id"))
    @Column(name = "source")
    private List<String> newsSources;
    
    @Column(nullable = false)
    private LocalDateTime generatedAt;
    
    @Column(nullable = false)
    private LocalDateTime analysisDate;
    
    public enum RecommendationType {
        STRONG_BUY, BUY, HOLD, SELL, STRONG_SELL
    }
    
    // Constructors
    public StockRecommendation() {}
    
    public StockRecommendation(Long id, String symbol, String companyName, Double currentPrice, 
                              Double previousClose, RecommendationType recommendation, String reasoning,
                              Double targetPrice, Double stopLoss, Double riskLevel, 
                              List<String> keyKeywords, List<String> newsSources,
                              LocalDateTime generatedAt, LocalDateTime analysisDate) {
        this.id = id;
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.previousClose = previousClose;
        this.recommendation = recommendation;
        this.reasoning = reasoning;
        this.targetPrice = targetPrice;
        this.stopLoss = stopLoss;
        this.riskLevel = riskLevel;
        this.keyKeywords = keyKeywords;
        this.newsSources = newsSources;
        this.generatedAt = generatedAt;
        this.analysisDate = analysisDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
    
    public Double getPreviousClose() { return previousClose; }
    public void setPreviousClose(Double previousClose) { this.previousClose = previousClose; }
    
    public RecommendationType getRecommendation() { return recommendation; }
    public void setRecommendation(RecommendationType recommendation) { this.recommendation = recommendation; }
    
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    
    public Double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(Double targetPrice) { this.targetPrice = targetPrice; }
    
    public Double getStopLoss() { return stopLoss; }
    public void setStopLoss(Double stopLoss) { this.stopLoss = stopLoss; }
    
    public Double getRiskLevel() { return riskLevel; }
    public void setRiskLevel(Double riskLevel) { this.riskLevel = riskLevel; }
    
    public List<String> getKeyKeywords() { return keyKeywords; }
    public void setKeyKeywords(List<String> keyKeywords) { this.keyKeywords = keyKeywords; }
    
    public List<String> getNewsSources() { return newsSources; }
    public void setNewsSources(List<String> newsSources) { this.newsSources = newsSources; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public LocalDateTime getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(LocalDateTime analysisDate) { this.analysisDate = analysisDate; }
}
