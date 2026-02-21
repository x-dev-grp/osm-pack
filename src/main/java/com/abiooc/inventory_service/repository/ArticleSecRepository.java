package com.abiooc.inventory_service.repository;

import com.abiooc.inventory_service.entity.ArticleSec;
import com.abiooc.inventory_service.Enum.CategorieArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface ArticleSecRepository extends JpaRepository<ArticleSec, Long> {
    Optional<ArticleSec> findBySku(String sku);
    List<ArticleSec> findByActifTrue();
    List<ArticleSec> findByCategorie(CategorieArticle categorie);
}