package com.osm.inventory_service.repository;

import com.osm.inventory_service.Enum.TypeEmplacement;
import com.osm.inventory_service.entity.EmplacementStock;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmplacementStockRepository extends BaseRepository<EmplacementStock> {

    Optional<EmplacementStock> findByCode(String code);

    Optional<EmplacementStock> findByCodeAndIsDeletedFalse(String code);

    List<EmplacementStock> findByTypeEmplacementAndIsDeletedFalse(TypeEmplacement type);

    List<EmplacementStock> findByZoneAndIsDeletedFalse(String zone);

    List<EmplacementStock> findByDisponibleTrueAndIsDeletedFalse();

    @Query("SELECT e FROM EmplacementStock e WHERE e.zone = :zone AND e.disponible = true AND COALESCE(e.isDeleted, FALSE) = FALSE")
    List<EmplacementStock> findDisponiblesParZone(@Param("zone") String zone);

    @Query("SELECT e FROM EmplacementStock e WHERE e.reservePour = :client AND COALESCE(e.isDeleted, FALSE) = FALSE")
    List<EmplacementStock> findReservesPour(@Param("client") String client);

    @Query("SELECT e FROM EmplacementStock e WHERE (e.temperatureMin IS NOT NULL OR e.temperatureMax IS NOT NULL) AND COALESCE(e.isDeleted, FALSE) = FALSE")
    List<EmplacementStock> findEmplacementsTemperatureControlee();

    boolean existsByCode(String code);

    boolean existsByCodeAndIsDeletedFalse(String code);
}
