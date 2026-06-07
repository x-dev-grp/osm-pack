package com.osm.inventory_service.repository;

import com.osm.inventory_service.entity.Fournisseur;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FournisseurRepository extends BaseRepository<Fournisseur> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIsDeletedFalse(String email);

    boolean existsByEmailAndIsDeletedFalseAndIdNot(String email, UUID id);

    boolean existsByTelephone(String telephone);

    boolean existsByTelephoneAndIsDeletedFalse(String telephone);

    boolean existsByTelephoneAndIsDeletedFalseAndIdNot(String telephone, UUID id);

    boolean existsByNumeroTva(String numeroTva);

    boolean existsByNumeroTvaAndIsDeletedFalse(String numeroTva);

    boolean existsByNumeroTvaAndIsDeletedFalseAndIdNot(String numeroTva, UUID id);

    List<Fournisseur> findByActifTrue();

    List<Fournisseur> findByActifTrueAndIsDeletedFalse();
}
