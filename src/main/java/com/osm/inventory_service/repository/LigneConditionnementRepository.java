package com.osm.inventory_service.repository;


import com.osm.inventory_service.Enum.Statue;
import com.osm.inventory_service.entity.LigneConditionnement;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LigneConditionnementRepository extends BaseRepository<LigneConditionnement> {

    List<LigneConditionnement> findByEtat(Statue etat);
    boolean existsByCode(String code);
}