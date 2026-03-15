package com.abiooc.inventory_service.entity;


import com.abiooc.inventory_service.Enum.Statue;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lignes_conditionnement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneConditionnement extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String nom;

    private String description;

    @Enumerated(EnumType.STRING)
    private Statue etat = Statue.ACTIF;

    private Integer vitesseNominale; // unités par heure

    private Integer tempsPreparation; // en minutes

    private Integer tempsNettoyage; // en minutes

    private String responsable;

    private LocalDateTime dateDerniereMaintenance;

    private LocalDateTime dateProchaineMaintenance;

    private String notes;
}