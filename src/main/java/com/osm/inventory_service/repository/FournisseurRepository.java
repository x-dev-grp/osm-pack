package com.osm.inventory_service.repository;

import com.osm.inventory_service.entity.Fournisseur;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FournisseurRepository extends BaseRepository<Fournisseur> {

    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
    boolean existsByNumeroTva(String numeroTva);
    List<Fournisseur> findByActifTrue();

}