package com.abiooc.inventory_service.entity;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stocks_secs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSec extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "article_id", unique = true)

    private ArticleSec article;

    @Column(name = "quantite_actuelle", nullable = false)
    private Integer quantiteActuelle = 0;

    // Association avec EmplacementStock
    @ManyToOne
    @JoinColumn(name = "emplacement_id")
    private EmplacementStock emplacement;

    public StockSec(ArticleSec article, Integer quantiteActuelle) {
        this.article = article;
        this.quantiteActuelle = quantiteActuelle;
    }
}