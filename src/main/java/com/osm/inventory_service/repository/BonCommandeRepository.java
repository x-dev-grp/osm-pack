package com.osm.inventory_service.repository;

import com.osm.inventory_service.entity.BonCommande;
import com.osm.inventory_service.Enum.StatutBonCommande;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonCommandeRepository extends BaseRepository<BonCommande> {

    List<BonCommande> findByStatus(StatutBonCommande statut);

    Long countBonCommandesByStatus(StatutBonCommande statut);

    @Query("SELECT COUNT(b) FROM BonCommande b WHERE b.status = :status AND COALESCE(b.isDeleted, FALSE) = FALSE")
    long countByStatusNotDeleted(@Param("status") StatutBonCommande status);

    boolean existsByNumeroBC(String numeroBC);
}
