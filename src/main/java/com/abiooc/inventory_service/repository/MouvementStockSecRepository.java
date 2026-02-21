package com.abiooc.inventory_service.repository;

import com.abiooc.inventory_service.Enum.TypeMouvement;
import com.abiooc.inventory_service.entity.ArticleSec;
import com.abiooc.inventory_service.entity.MouvementStockSec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface MouvementStockSecRepository extends JpaRepository<MouvementStockSec, Long> {
    List<MouvementStockSec> findByArticleOrderByDateMouvementDesc(ArticleSec article);

    @Query
            ("SELECT COUNT(m) FROM MouvementStockSec m WHERE m.typeMouvement = :type AND m.dateMouvement BETWEEN :debut AND :fin")
    Long countByTypeAndDateBetween(@Param("type") TypeMouvement type,
                                   @Param("debut") LocalDateTime debut,
                                   @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(m) FROM MouvementStockSec m WHERE m.typeMouvement = :type AND m.motif LIKE %:statut% AND m.dateMouvement BETWEEN :debut AND :fin")
    Long countByTypeAndStatutAndDateBetween(@Param("type") TypeMouvement type,
                                            @Param("statut") String statut,
                                            @Param("debut") LocalDateTime debut,
                                            @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(m.quantite) FROM MouvementStockSec m WHERE m.article = :article AND m.typeMouvement = :type AND m.dateMouvement BETWEEN :debut AND :fin")
    Integer sumQuantiteByArticleAndTypeAndDateBetween(@Param("article") ArticleSec article,
                                                      @Param("type") TypeMouvement type,
                                                      @Param("debut") LocalDateTime debut,
                                                      @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(m) FROM MouvementStockSec m WHERE m.dateMouvement BETWEEN :debut AND :fin")
    Integer countByDateBetween(@Param("debut") LocalDateTime debut,
                               @Param("fin") LocalDateTime fin);

    @Query("SELECT m.article.sku as sku, m.article.nom as nom, COUNT(m) as nbRuptures " +
            "FROM MouvementStockSec m " +
            "WHERE m.typeMouvement = 'SORTIE' AND m.motif LIKE '%Stock insuffisant%' " +
            "AND m.dateMouvement BETWEEN :debut AND :fin " +
            "GROUP BY m.article.sku, m.article.nom " +
            "ORDER BY nbRuptures DESC")
    List<Map<String, Object>> findTopArticlesRupture(@Param("debut") LocalDateTime debut,
                                                     @Param("fin") LocalDateTime fin);
}