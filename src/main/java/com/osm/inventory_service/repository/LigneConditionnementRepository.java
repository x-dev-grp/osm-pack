package com.osm.inventory_service.repository;

import com.osm.inventory_service.Enum.Statue;
import com.osm.inventory_service.entity.LigneConditionnement;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneConditionnementRepository extends BaseRepository<LigneConditionnement> {

    List<LigneConditionnement> findByEtat(Statue etat);

    List<LigneConditionnement> findByEtatAndIsDeletedFalse(Statue etat);

    boolean existsByCode(String code);

    boolean existsByCodeAndIsDeletedFalse(String code);
}
