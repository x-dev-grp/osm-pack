package com.abiooc.inventory_service.repository;

import com.abiooc.inventory_service.entity.ArticleSec;
import com.abiooc.inventory_service.entity.BonCommande;
import com.abiooc.inventory_service.Enum.StatutBonCommande;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BonCommandeRepository extends BaseRepository<BonCommande> {

    List<BonCommande> findByStatut(StatutBonCommande statut);

    Long countByStatut(StatutBonCommande statut);

    boolean existsByNumeroBC(String numeroBC);
}