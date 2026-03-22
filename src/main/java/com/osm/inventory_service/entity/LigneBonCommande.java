package com.osm.inventory_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_bons_commandes")
@Data
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class LigneBonCommande extends BaseEntity {

    @ManyToOne
    @JsonIgnore//solution pour boucle infinie
    @JoinColumn(name = "bon_commande_id", nullable = false)
    private BonCommande bonCommande;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleSec article;

    @Column(nullable = false)
    private Integer quantiteCommandee;

    private Integer quantiteRecue = 0;

    private BigDecimal prixUnitaire;

    private String remarque;
}