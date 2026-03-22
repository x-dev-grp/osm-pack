package com.osm.inventory_service.repository;

import com.osm.inventory_service.entity.BonCommande;
import com.osm.inventory_service.Enum.StatutBonCommande;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BonCommandeRepository extends BaseRepository<BonCommande> {

    List<BonCommande> findByStatus(StatutBonCommande statut);

    Long countBonCommandesByStatus(StatutBonCommande statut);

    boolean existsByNumeroBC(String numeroBC);
}