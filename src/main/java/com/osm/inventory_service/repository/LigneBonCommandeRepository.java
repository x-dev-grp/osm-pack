package com.osm.inventory_service.repository;

import com.osm.inventory_service.Enum.StatutBonCommande;
import com.osm.inventory_service.entity.LigneBonCommande;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface LigneBonCommandeRepository extends BaseRepository<LigneBonCommande> {

    @Transactional
    void deleteByBonCommandeId(UUID bonCommandeId);

    @Query("""
            SELECT COUNT(l)
              FROM LigneBonCommande l
              JOIN l.bonCommande bc
             WHERE l.article.id = :articleId
               AND COALESCE(l.isDeleted, FALSE) = FALSE
               AND COALESCE(bc.isDeleted, FALSE) = FALSE
               AND bc.status IN :statuses
            """)
    long countByArticleIdAndBonCommandeStatusInAndIsDeletedFalse(
            @Param("articleId") UUID articleId,
            @Param("statuses") Collection<StatutBonCommande> statuses
    );
}