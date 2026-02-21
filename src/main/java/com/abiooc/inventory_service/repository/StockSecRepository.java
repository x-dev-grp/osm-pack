package com.abiooc.inventory_service.repository;

import com.abiooc.inventory_service.entity.ArticleSec;
import com.abiooc.inventory_service.entity.StockSec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StockSecRepository extends JpaRepository<StockSec, Long> {
    Optional<StockSec> findByArticle(ArticleSec article);
}