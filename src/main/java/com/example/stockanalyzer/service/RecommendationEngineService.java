package com.example.stockanalyzer.service;

import com.example.stockanalyzer.core.MarketDataClient;
import com.example.stockanalyzer.model.NewsAnalysis;
import com.example.stockanalyzer.model.StockRecommendation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Service
public class RecommendationEngineService {
    
    private static final Logger log = Logger.getLogger(RecommendationEngineService.class.getName());
    
    @Autowired
    private MarketDataClient marketDataClient;
    
    // Popular stock symbols to analyze
    private static final List<String> POPULAR_STOCKS = Arrays.asList(
        "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NVDA", "NFLX", "AMD", "INTC",
        "CRM", "ADBE", "PYPL", "UBER", "LYFT", "SQ", "ROKU", "ZM", "DOCU", "SNOW"
    );
    
    public List<StockRecommendation> generateRecommendations(List<NewsAnalysis> newsAnalyses) {
        List<StockRecommendation> recommendations = new ArrayList<>();
        
        for (NewsAnalysis analysis : newsAnalyses) {
            try {
                StockRecommendation recommendation = createRecommendation(analysis);
                if (recommendation != null) {
                    recommendations.add(recommendation);
                }
            } catch (Exception e) {
            }
        }
        
        return recommendations;
    }
    
    private StockRecommendation createRecommendation(NewsAnalysis analysis) {
        try {
            // Get current market data
            String marketData = marketDataClient.getMarketData(analysis.getSymbol());
            double currentPrice = extractCurrentPrice(marketData);
            double previousClose = extractPreviousClose(marketData);
            
            // Calculate recommendation based on news sentiment and technical factors
            StockRecommendation.RecommendationType recommendationType = calculateRecommendationType(analysis);
            String reasoning = generateReasoning(analysis, recommendationType);
            
            // Calculate target price and stop loss
            double targetPrice = calculateTargetPrice(currentPrice, recommendationType);
            double stopLoss = calculateStopLoss(currentPrice, recommendationType);
            double riskLevel = calculateRiskLevel(analysis);
            
            StockRecommendation recommendation = new StockRecommendation();
            recommendation.setSymbol(analysis.getSymbol());
            recommendation.setCompanyName(analysis.getCompanyName());
            recommendation.setCurrentPrice(currentPrice);
            recommendation.setPreviousClose(previousClose);
            recommendation.setRecommendation(recommendationType);
            recommendation.setReasoning(reasoning);
            recommendation.setTargetPrice(targetPrice);
            recommendation.setStopLoss(stopLoss);
            recommendation.setRiskLevel(riskLevel);
            recommendation.setKeyKeywords(analysis.getKeyKeywords());
            recommendation.setNewsSources(analysis.getNewsItems().stream()
                .map(NewsAnalysis.NewsItem::getSource)
                .distinct()
                .collect(java.util.stream.Collectors.toList()));
            recommendation.setGeneratedAt(LocalDateTime.now());
            recommendation.setAnalysisDate(LocalDateTime.now());
            return recommendation;
                
        } catch (Exception e) {
            return null;
        }
    }
    
    private StockRecommendation.RecommendationType calculateRecommendationType(NewsAnalysis analysis) {
        NewsAnalysis.SentimentScore sentiment = analysis.getSentiment();
        
        // Base recommendation on sentiment
        if (sentiment.getOverall() == NewsAnalysis.SentimentType.POSITIVE) {
            if (sentiment.getPositive() > 0.7) {
                return StockRecommendation.RecommendationType.STRONG_BUY;
            } else {
                return StockRecommendation.RecommendationType.BUY;
            }
        } else if (sentiment.getOverall() == NewsAnalysis.SentimentType.NEGATIVE) {
            if (sentiment.getNegative() > 0.7) {
                return StockRecommendation.RecommendationType.STRONG_SELL;
            } else {
                return StockRecommendation.RecommendationType.SELL;
            }
        } else {
            return StockRecommendation.RecommendationType.HOLD;
        }
    }
    
    private String generateReasoning(NewsAnalysis analysis, StockRecommendation.RecommendationType recommendation) {
        StringBuilder reasoning = new StringBuilder();
        
        reasoning.append("Based on recent news analysis: ");
        
        // Add sentiment-based reasoning
        NewsAnalysis.SentimentScore sentiment = analysis.getSentiment();
        if (sentiment.getOverall() == NewsAnalysis.SentimentType.POSITIVE) {
            reasoning.append("Positive sentiment detected in recent news. ");
        } else if (sentiment.getOverall() == NewsAnalysis.SentimentType.NEGATIVE) {
            reasoning.append("Negative sentiment detected in recent news. ");
        } else {
            reasoning.append("Mixed sentiment in recent news. ");
        }
        
        // Add keyword-based reasoning
        if (!analysis.getKeyKeywords().isEmpty()) {
            reasoning.append("Key themes include: ").append(String.join(", ", analysis.getKeyKeywords())).append(". ");
        }
        
        // Add news count reasoning
        reasoning.append("Analyzed ").append(analysis.getNewsItems().size()).append(" relevant news articles. ");
        
        // Add recommendation-specific reasoning
        switch (recommendation) {
            case STRONG_BUY:
                reasoning.append("Strong positive catalysts identified with low risk factors.");
                break;
            case BUY:
                reasoning.append("Positive outlook with moderate risk factors.");
                break;
            case HOLD:
                reasoning.append("Mixed signals suggest maintaining current position.");
                break;
            case SELL:
                reasoning.append("Negative factors outweigh positive catalysts.");
                break;
            case STRONG_SELL:
                reasoning.append("Significant negative catalysts with high risk factors.");
                break;
        }
        
        return reasoning.toString();
    }
    
    private double calculateTargetPrice(double currentPrice, StockRecommendation.RecommendationType recommendation) {
        switch (recommendation) {
            case STRONG_BUY:
                return currentPrice * 1.15; // 15% upside
            case BUY:
                return currentPrice * 1.08; // 8% upside
            case HOLD:
                return currentPrice; // No change
            case SELL:
                return currentPrice * 0.92; // 8% downside
            case STRONG_SELL:
                return currentPrice * 0.85; // 15% downside
            default:
                return currentPrice;
        }
    }
    
    private double calculateStopLoss(double currentPrice, StockRecommendation.RecommendationType recommendation) {
        switch (recommendation) {
            case STRONG_BUY:
            case BUY:
                return currentPrice * 0.92; // 8% stop loss for long positions
            case HOLD:
                return currentPrice * 0.95; // 5% stop loss
            case SELL:
            case STRONG_SELL:
                return currentPrice * 1.08; // 8% stop loss for short positions
            default:
                return currentPrice * 0.95;
        }
    }
    
    private double calculateRiskLevel(NewsAnalysis analysis) {
        double riskLevel = 5.0; // Base risk level
        
        // Adjust based on sentiment volatility
        NewsAnalysis.SentimentScore sentiment = analysis.getSentiment();
        if (sentiment.getOverall() == NewsAnalysis.SentimentType.POSITIVE) {
            riskLevel -= 1.0;
        } else if (sentiment.getOverall() == NewsAnalysis.SentimentType.NEGATIVE) {
            riskLevel += 1.0;
        }
        
        // Adjust based on news volume
        if (analysis.getNewsItems().size() > 15) {
            riskLevel += 1.0; // More news = more volatility
        }
        
        // Ensure risk level is between 1 and 10
        return Math.max(1.0, Math.min(10.0, riskLevel));
    }
    
    private double extractCurrentPrice(String marketData) {
        // Simplified price extraction - in real implementation, parse JSON response
        try {
            // This is a placeholder - would parse actual market data response
            return 100.0 + new Random().nextDouble() * 200.0; // Random price between 100-300
        } catch (Exception e) {
            return 150.0;
        }
    }
    
    private double extractPreviousClose(String marketData) {
        // Simplified previous close extraction
        try {
            return 100.0 + new Random().nextDouble() * 200.0; // Random price between 100-300
        } catch (Exception e) {
            return 150.0;
        }
    }
    
    public List<String> getPopularStocks() {
        return new ArrayList<>(POPULAR_STOCKS);
    }
}
