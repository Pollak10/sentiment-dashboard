package com.Benjamin.sentiment_dashboard.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SentimentDataRepository
    extends JpaRepository<SentimentData, Long>{
    List<SentimentData> findBySymbol(String symbol);
    List<SentimentData> findBySource(String source);
    List<SentimentData> findBySymbolOrderByTimestampDesc(String symbol);
}
