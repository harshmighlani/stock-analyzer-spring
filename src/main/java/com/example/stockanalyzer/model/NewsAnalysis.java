package com.example.stockanalyzer.model;

import java.time.LocalDateTime;
import java.util.List;

public class NewsAnalysis {
    
    private String symbol;
    private String companyName;
    private List<NewsItem> newsItems;
    private List<String> keyKeywords;
    private SentimentScore sentiment;
    private LocalDateTime analyzedAt;
    
    public NewsAnalysis() {}
    
    public NewsAnalysis(String symbol, String companyName, List<NewsItem> newsItems, 
                       List<String> keyKeywords, SentimentScore sentiment, LocalDateTime analyzedAt) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.newsItems = newsItems;
        this.keyKeywords = keyKeywords;
        this.sentiment = sentiment;
        this.analyzedAt = analyzedAt;
    }
    
    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public List<NewsItem> getNewsItems() { return newsItems; }
    public void setNewsItems(List<NewsItem> newsItems) { this.newsItems = newsItems; }
    
    public List<String> getKeyKeywords() { return keyKeywords; }
    public void setKeyKeywords(List<String> keyKeywords) { this.keyKeywords = keyKeywords; }
    
    public SentimentScore getSentiment() { return sentiment; }
    public void setSentiment(SentimentScore sentiment) { this.sentiment = sentiment; }
    
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
    
    public static class NewsItem {
        private String title;
        private String content;
        private String source;
        private String url;
        private LocalDateTime publishedAt;
        private Double relevanceScore;
        private SentimentType sentiment;
        
        public NewsItem() {}
        
        public NewsItem(String title, String content, String source, String url, 
                       LocalDateTime publishedAt, Double relevanceScore, SentimentType sentiment) {
            this.title = title;
            this.content = content;
            this.source = source;
            this.url = url;
            this.publishedAt = publishedAt;
            this.relevanceScore = relevanceScore;
            this.sentiment = sentiment;
        }
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public LocalDateTime getPublishedAt() { return publishedAt; }
        public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
        
        public Double getRelevanceScore() { return relevanceScore; }
        public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }
        
        public SentimentType getSentiment() { return sentiment; }
        public void setSentiment(SentimentType sentiment) { this.sentiment = sentiment; }
    }
    
    public static class SentimentScore {
        private Double positive;
        private Double negative;
        private Double neutral;
        private SentimentType overall;
        
        public SentimentScore() {}
        
        public SentimentScore(Double positive, Double negative, Double neutral, SentimentType overall) {
            this.positive = positive;
            this.negative = negative;
            this.neutral = neutral;
            this.overall = overall;
        }
        
        // Getters and Setters
        public Double getPositive() { return positive; }
        public void setPositive(Double positive) { this.positive = positive; }
        
        public Double getNegative() { return negative; }
        public void setNegative(Double negative) { this.negative = negative; }
        
        public Double getNeutral() { return neutral; }
        public void setNeutral(Double neutral) { this.neutral = neutral; }
        
        public SentimentType getOverall() { return overall; }
        public void setOverall(SentimentType overall) { this.overall = overall; }
    }
    
    public enum SentimentType {
        VERY_POSITIVE, POSITIVE, NEUTRAL, NEGATIVE, VERY_NEGATIVE
    }
}
