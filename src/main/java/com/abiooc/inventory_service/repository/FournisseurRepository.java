package com.abiooc.inventory_service.repository;

import com.abiooc.inventory_service.Enum.CategorieFournisseur;
import com.abiooc.inventory_service.entity.Fournisseur;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FournisseurRepository extends BaseRepository<Fournisseur> {

    Optional<Fournisseur> findByCode(String code);

    Optional<Fournisseur> findByEmail(String email);


    List<Fournisseur> findByNomContainingIgnoreCase(String nom);

    List<Fournisseur> findByCategorieFournisseur(CategorieFournisseur categorie);


    List<Fournisseur> findByPays(String pays);


    boolean existsByCode(String code);

    boolean existsByEmail(String email);

}