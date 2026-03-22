package com.osm.inventory_service.repository;

import com.osm.inventory_service.entity.LigneBonCommande;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface LigneBonCommandeRepository extends BaseRepository<LigneBonCommande> {

    @Transactional
    void deleteByBonCommandeId(UUID bonCommandeId);
}