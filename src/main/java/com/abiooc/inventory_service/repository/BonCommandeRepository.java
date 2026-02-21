package com.abiooc.inventory_service.repository;

import com.abiooc.inventory_service.entity.ArticleSec;
import com.abiooc.inventory_service.entity.BonCommande;
import com.abiooc.inventory_service.Enum.StatutBonCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BonCommandeRepository extends JpaRepository<BonCommande, Long> {
    List<BonCommande> findByStatut(StatutBonCommande statut);
    boolean existsByLignesArticleAndStatutIn(ArticleSec article, List<StatutBonCommande> statuts);

    @Query
            ("SELECT AVG(l.prixUnitaire) FROM LigneBonCommande l WHERE l.article = :article")
    Optional<BigDecimal> findPrixMoyenByArticle(@Param("article") ArticleSec article);

    Long countByStatut(StatutBonCommande statut);

    @Query("SELECT COUNT(bc) FROM BonCommande bc WHERE bc.statut = :statut AND bc.dateValidation BETWEEN :debut AND :fin")
    Long countByStatutAndDateValidationBetween(@Param("statut") StatutBonCommande statut,
                                               @Param("debut") LocalDateTime debut,
                                               @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(l.quantiteCommandee * l.prixUnitaire) FROM BonCommande bc JOIN bc.lignes l WHERE bc.statut = :statut AND bc.dateValidation BETWEEN :debut AND :fin")
    Optional<BigDecimal> sumMontantByStatutAndDateBetween(@Param("statut") StatutBonCommande statut,
                                                          @Param("debut") LocalDateTime debut,
                                                          @Param("fin") LocalDateTime fin);

    @Query("SELECT bc.fournisseur, SUM(l.quantiteCommandee * l.prixUnitaire) FROM BonCommande bc JOIN bc.lignes l WHERE bc.dateCreation BETWEEN :debut AND :fin AND bc.statut = 'VALIDE' GROUP BY bc.fournisseur")
    List<Object[]> sumMontantByFournisseurAndDateBetween(@Param("debut") LocalDateTime debut,
                                                         @Param("fin") LocalDateTime fin);

    @Query("SELECT bc FROM BonCommande bc WHERE bc.statut = :statut AND bc.dateValidation BETWEEN :debut AND :fin")
    List<BonCommande> findByStatutAndDateValidationBetween(@Param("statut") StatutBonCommande statut,
                                                           @Param("debut") LocalDateTime debut,
                                                           @Param("fin") LocalDateTime fin);
}