package com.abiooc.inventory_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "lignes_bons_commandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneBonCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore//solution pour boucle infinie
    @JoinColumn(name = "bon_commande_id", nullable = false)
    private BonCommande bonCommande;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleSec article;

    @Column(name = "quantite_commandee", nullable = false)
    private Integer quantiteCommandee;

    @Column(name = "quantite_recue")
    private Integer quantiteRecue = 0;

    @Column(name = "prix_unitaire")
    private BigDecimal prixUnitaire;

    private String remarque;
}