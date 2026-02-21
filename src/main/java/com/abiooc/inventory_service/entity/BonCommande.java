package com.abiooc.inventory_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.abiooc.inventory_service.Enum.StatutBonCommande;

@Entity
@Table(name = "bons_commandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_bc", unique = true, nullable = false)
    private String numeroBC;

    private String fournisseur;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "date_reception_prevue")
    private LocalDateTime dateReceptionPrevue;

    @Enumerated(EnumType.STRING)
    private StatutBonCommande statut = StatutBonCommande.EN_ATTENTE;

    @OneToMany(mappedBy = "bonCommande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneBonCommande> lignes = new ArrayList<>();


    @Column(name = "motif_refus")
    private String motifRefus;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}