package com.osm.inventory_service.repository;



import com.osm.inventory_service.entity.ProduitFinal;
import com.osm.inventory_service.Enum.ProduitFinalType;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProduitFinalRepository extends BaseRepository<ProduitFinal> {
    Optional<ProduitFinal> findByIdAndIsDeletedFalse(UUID id);
    Optional<ProduitFinal> findByNameAndIsDeletedFalse(String name);
    Optional<ProduitFinal> findByCodeAndIsDeletedFalse(String code);
    List<ProduitFinal> findByIsDeletedFalse();
    List<ProduitFinal> findByActifTrueAndIsDeletedFalse();
    List<ProduitFinal> findByTypeAndIsDeletedFalse(ProduitFinalType type);

}
