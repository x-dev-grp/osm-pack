package com.osm.inventory_service.repository;

import com.osm.inventory_service.entity.StockSec;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockSecRepository extends BaseRepository<StockSec> {

    Optional<StockSec> findByArticleId(UUID articleId);

    List<StockSec> findByEmplacementId(UUID emplacementId);

    @Query("SELECT s FROM StockSec s WHERE s.emplacement.zone = :zone")
    List<StockSec> findByZone(@Param("zone") String zone);

    @Query("SELECT s FROM StockSec s WHERE s.emplacement.disponible = true")
    List<StockSec> findByEmplacementDisponible();



}