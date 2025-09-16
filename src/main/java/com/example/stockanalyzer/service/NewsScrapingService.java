package com.example.stockanalyzer.service;

import com.example.stockanalyzer.model.NewsAnalysis;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Service
public class NewsScrapingService {
    
    private static final Logger log = Logger.getLogger(NewsScrapingService.class.getName());
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    // Free news sources for stock analysis
    private static final List<String> NEWS_SOURCES = Arrays.asList(
        "https://finance.yahoo.com/news/",
        "https://www.marketwatch.com/latest-news",
        "https://seekingalpha.com/news",
        "https://www.benzinga.com/news",
        "https://www.fool.com/investing/"
    );
    
    private static final List<String> BULLISH_KEYWORDS = Arrays.asList(
        "beat", "exceeded", "growth", "expansion", "acquisition", "merger", "partnership",
        "upgrade", "positive", "strong", "robust", "outperform", "bullish", "rally",
        "surge", "gain", "profit", "earnings", "revenue", "dividend", "buyback"
    );
    
    private static final List<String> BEARISH_KEYWORDS = Arrays.asList(
        "miss", "decline", "loss", "cut", "downgrade", "negative", "weak", "concern",
        "risk", "volatility", "sell-off", "crash", "bearish", "recession", "layoff",
        "bankruptcy", "default", "debt", "lawsuit", "investigation", "scandal"
    );
    
    public NewsAnalysis analyzeStockNews(String symbol, String companyName) {
        
        List<CompletableFuture<List<NewsAnalysis.NewsItem>>> futures = NEWS_SOURCES.stream()
            .map(source -> CompletableFuture.supplyAsync(() -> scrapeNewsFromSource(source, symbol), executorService))
            .collect(Collectors.toList());
        
        List<NewsAnalysis.NewsItem> allNews = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        // Filter and rank news by relevance
        List<NewsAnalysis.NewsItem> relevantNews = filterRelevantNews(allNews, symbol, companyName);
        
        // Extract keywords
        List<String> keywords = extractKeywords(relevantNews);
        
        // Calculate sentiment
        NewsAnalysis.SentimentScore sentiment = calculateSentiment(relevantNews);
        
        NewsAnalysis analysis = new NewsAnalysis();
        analysis.setSymbol(symbol);
        analysis.setCompanyName(companyName);
        analysis.setNewsItems(relevantNews);
        analysis.setKeyKeywords(keywords);
        analysis.setSentiment(sentiment);
        analysis.setAnalyzedAt(LocalDateTime.now());
        return analysis;
    }
    
    private List<NewsAnalysis.NewsItem> scrapeNewsFromSource(String sourceUrl, String symbol) {
        List<NewsAnalysis.NewsItem> newsItems = new ArrayList<>();
        
        try {
            Document doc = Jsoup.connect(sourceUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();
            
            Elements articles = doc.select("article, .article, .news-item, .story");
            
            for (Element article : articles) {
                try {
                    String title = extractText(article.select("h1, h2, h3, .title, .headline"));
                    String content = extractText(article.select("p, .content, .summary"));
                    String url = extractUrl(article.select("a"));
                    
                    if (title != null && !title.isEmpty() && 
                        (title.toLowerCase().contains(symbol.toLowerCase()) || 
                         content.toLowerCase().contains(symbol.toLowerCase()))) {
                        
                        NewsAnalysis.NewsItem newsItem = new NewsAnalysis.NewsItem();
                        newsItem.setTitle(title);
                        newsItem.setContent(content);
                        newsItem.setSource(sourceUrl);
                        newsItem.setUrl(url);
                        newsItem.setPublishedAt(LocalDateTime.now()); // Simplified - would parse actual date
                        newsItem.setRelevanceScore(calculateRelevanceScore(title, content, symbol));
                        newsItem.setSentiment(analyzeSentiment(title + " " + content));
                        
                        newsItems.add(newsItem);
                    }
                } catch (Exception e) {
                }
            }
            
        } catch (IOException e) {
        }
        
        return newsItems;
    }
    
    private String extractText(Elements elements) {
        return elements.stream()
            .map(Element::text)
            .filter(text -> text != null && !text.trim().isEmpty())
            .findFirst()
            .orElse("");
    }
    
    private String extractUrl(Elements elements) {
        return elements.stream()
            .map(element -> element.attr("href"))
            .filter(url -> url != null && !url.isEmpty())
            .findFirst()
            .orElse("");
    }
    
    private List<NewsAnalysis.NewsItem> filterRelevantNews(List<NewsAnalysis.NewsItem> news, String symbol, String companyName) {
        return news.stream()
            .filter(item -> item.getRelevanceScore() > 0.3)
            .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
            .limit(20) // Top 20 most relevant news items
            .collect(Collectors.toList());
    }
    
    private double calculateRelevanceScore(String title, String content, String symbol) {
        String text = (title + " " + content).toLowerCase();
        double score = 0.0;
        
        // Symbol mentions
        if (text.contains(symbol.toLowerCase())) {
            score += 0.5;
        }
        
        // Financial keywords
        List<String> financialKeywords = Arrays.asList("earnings", "revenue", "profit", "loss", "growth", "stock", "shares", "dividend");
        for (String keyword : financialKeywords) {
            if (text.contains(keyword)) {
                score += 0.1;
            }
        }
        
        return Math.min(score, 1.0);
    }
    
    private NewsAnalysis.SentimentType analyzeSentiment(String text) {
        String lowerText = text.toLowerCase();
        
        long bullishCount = BULLISH_KEYWORDS.stream()
            .mapToLong(keyword -> lowerText.split(keyword).length - 1)
            .sum();
        
        long bearishCount = BEARISH_KEYWORDS.stream()
            .mapToLong(keyword -> lowerText.split(keyword).length - 1)
            .sum();
        
        if (bullishCount > bearishCount * 1.5) {
            return NewsAnalysis.SentimentType.POSITIVE;
        } else if (bearishCount > bullishCount * 1.5) {
            return NewsAnalysis.SentimentType.NEGATIVE;
        } else {
            return NewsAnalysis.SentimentType.NEUTRAL;
        }
    }
    
    private List<String> extractKeywords(List<NewsAnalysis.NewsItem> news) {
        Map<String, Integer> keywordCount = new HashMap<>();
        
        for (NewsAnalysis.NewsItem item : news) {
            String text = (item.getTitle() + " " + item.getContent()).toLowerCase();
            
            // Extract important financial terms
            List<String> importantTerms = Arrays.asList(
                "earnings", "revenue", "profit", "growth", "acquisition", "merger",
                "partnership", "expansion", "dividend", "buyback", "upgrade", "downgrade"
            );
            
            for (String term : importantTerms) {
                if (text.contains(term)) {
                    keywordCount.merge(term, 1, Integer::sum);
                }
            }
        }
        
        return keywordCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    private NewsAnalysis.SentimentScore calculateSentiment(List<NewsAnalysis.NewsItem> news) {
        Map<NewsAnalysis.SentimentType, Long> sentimentCount = news.stream()
            .collect(Collectors.groupingBy(NewsAnalysis.NewsItem::getSentiment, Collectors.counting()));
        
        long total = news.size();
        if (total == 0) {
            NewsAnalysis.SentimentScore sentimentScore = new NewsAnalysis.SentimentScore();
            sentimentScore.setPositive(0.0);
            sentimentScore.setNegative(0.0);
            sentimentScore.setNeutral(1.0);
            sentimentScore.setOverall(NewsAnalysis.SentimentType.NEUTRAL);
            return sentimentScore;
        }
        
        double positive = (double) sentimentCount.getOrDefault(NewsAnalysis.SentimentType.POSITIVE, 0L) / total;
        double negative = (double) sentimentCount.getOrDefault(NewsAnalysis.SentimentType.NEGATIVE, 0L) / total;
        double neutral = (double) sentimentCount.getOrDefault(NewsAnalysis.SentimentType.NEUTRAL, 0L) / total;
        
        NewsAnalysis.SentimentType overall = NewsAnalysis.SentimentType.NEUTRAL;
        if (positive > negative && positive > neutral) {
            overall = NewsAnalysis.SentimentType.POSITIVE;
        } else if (negative > positive && negative > neutral) {
            overall = NewsAnalysis.SentimentType.NEGATIVE;
        }
        
        NewsAnalysis.SentimentScore sentimentScore = new NewsAnalysis.SentimentScore();
        sentimentScore.setPositive(positive);
        sentimentScore.setNegative(negative);
        sentimentScore.setNeutral(neutral);
        sentimentScore.setOverall(overall);
        return sentimentScore;
    }
}
