package com.example.stockanalyzer.api;

import com.example.stockanalyzer.core.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(@RequestParam String symbol) {
        return ResponseEntity.ok(analysisService.analyzeSymbol(symbol));
    }
}
