package com.osm.inventory_service.entity;

import com.osm.inventory_service.Enum.StatutBonCommande;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bons_commandes")
@Data
@NoArgsConstructor
@Audited
@AllArgsConstructor
public class BonCommande extends BaseEntity {

    @Column(name = "numero_bc", unique = true, nullable = false)
    private String numeroBC;

    @Column(name = "motif_refus")
    private String motifRefus;


    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "date_reception_prevue")
    private LocalDateTime dateReceptionPrevue;

    @Enumerated(EnumType.STRING)
    private StatutBonCommande status = StatutBonCommande.EN_ATTENTE;

    @OneToMany(mappedBy = "bonCommande", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LigneBonCommande> lignes = new ArrayList<>();


}