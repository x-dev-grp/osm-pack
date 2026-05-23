package com.osm.inventory_service.repository;

import com.osm.inventory_service.entity.BOM;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BomRepository extends BaseRepository<BOM> {
    List<BOM> findByProduitFinalId(UUID productId);

    Optional<BOM> findFirstByProduitFinalIdAndActiveTrue(UUID productId);

    long countByProduitFinalId(UUID productId);
}
