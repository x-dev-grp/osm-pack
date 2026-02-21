package com.abiooc.inventory_service.repository;

import com.abiooc.inventory_service.entity.LigneBonCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneBonCommandeRepository extends JpaRepository<LigneBonCommande, Long> {
}