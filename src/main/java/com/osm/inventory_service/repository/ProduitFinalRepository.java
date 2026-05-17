package com.osm.inventory_service.repository;



import com.osm.inventory_service.entity.ProduitFinal;
import com.osm.inventory_service.entity.ProductType;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitFinalRepository extends BaseRepository<ProduitFinal> {
    Optional<ProduitFinal> findByIdAndDeletedFalse(java.util.UUID id);
    Optional<ProduitFinal> findByNameAndDeletedFalse(String name);
    Optional<ProduitFinal> findByCodeAndDeletedFalse(String code);
    List<ProduitFinal> findByDeletedFalse();
    List<ProduitFinal> findByActifTrueAndDeletedFalse();
    List<ProduitFinal> findByTypeAndDeletedFalse(ProductType type);

}
