package com.abiooc.inventory_service.repository;


import com.abiooc.inventory_service.entity.Client;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends BaseRepository<Client> {

    Optional<Client> findByCodeClient(String codeClient);

    Optional<Client> findByEmail(String email);


    List<Client> findByPrivateLabelTrue();

    List<Client> findByPrivateLabelFalse();

    @Query("SELECT c FROM Client c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.codeClient) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Client> rechercherClients(@Param("searchTerm") String searchTerm);

    boolean existsByCodeClient(String codeClient);

    boolean existsByEmail(String email);
}