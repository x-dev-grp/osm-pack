package com.abiooc.inventory_service.repository;


import com.abiooc.inventory_service.Enum.Statue;
import com.abiooc.inventory_service.entity.LigneConditionnement;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LigneConditionnementRepository extends BaseRepository<LigneConditionnement> {

    Optional<LigneConditionnement> findByCode(String code);

    List<LigneConditionnement> findByEtat(Statue etat);


    @Query("SELECT l FROM LigneConditionnement l WHERE LOWER(l.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(l.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<LigneConditionnement> rechercherLignes(@Param("searchTerm") String searchTerm);

    @Query("SELECT l FROM LigneConditionnement l WHERE l.dateProchaineMaintenance <= CURRENT_DATE")
    List<LigneConditionnement> findLignesEnMaintenanceRequise();

    boolean existsByCode(String code);
}