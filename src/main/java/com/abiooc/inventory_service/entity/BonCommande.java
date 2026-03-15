package com.abiooc.inventory_service.entity;

import com.abiooc.inventory_service.Enum.StatutBonCommande;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bons_commandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonCommande extends BaseEntity {

    @Column(name = "numero_bc", unique = true, nullable = false)
    private String numeroBC;

    @Column(name = "fournisseur", nullable = false)
    private String fournisseur;

    @Column(name = "motif_refus")
    private String motifRefus;


    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "date_reception_prevue")
    private LocalDateTime dateReceptionPrevue;

    @Enumerated(EnumType.STRING)
    private StatutBonCommande status = StatutBonCommande.EN_ATTENTE;

    @OneToMany(mappedBy = "bonCommande", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<LigneBonCommande> lignes = new ArrayList<>();


}