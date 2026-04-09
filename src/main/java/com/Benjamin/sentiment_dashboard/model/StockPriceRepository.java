package com.Benjamin.sentiment_dashboard.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StockPriceRepository
        extends JpaRepository<StockPrice, Long> {

    Optional<StockPrice> findTopBySymbolOrderByTimestampDesc(
            String symbol
    );
}