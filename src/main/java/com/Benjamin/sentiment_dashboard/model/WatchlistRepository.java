package com.Benjamin.sentiment_dashboard.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface WatchlistRepository
        extends JpaRepository<WatchlistItem, Long> {

    Optional<WatchlistItem> findBySymbol(String symbol);

    boolean existsBySymbol(String symbol);

    List<WatchlistItem> findByType(String type);
}