package com.example.stockanalyzer.repository;

import com.example.stockanalyzer.model.StockRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRecommendationRepository extends JpaRepository<StockRecommendation, Long> {
    
    List<StockRecommendation> findBySymbolOrderByGeneratedAtDesc(String symbol);
    
    List<StockRecommendation> findByRecommendationOrderByGeneratedAtDesc(StockRecommendation.RecommendationType recommendation);
    
    List<StockRecommendation> findTop10ByOrderByGeneratedAtDesc();
}
