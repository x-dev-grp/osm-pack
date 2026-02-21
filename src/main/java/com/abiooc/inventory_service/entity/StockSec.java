package com.abiooc.inventory_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks_secs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "article_id", unique = true)
    private ArticleSec article;

    @Column(name = "quantite_actuelle", nullable = false)
    private Integer quantiteActuelle = 0;

    private String emplacement;

    @Column(name = "date_derniere_maj")
    private LocalDateTime dateDerniereMaj;

    public StockSec(ArticleSec article, Integer quantiteActuelle) {
        this.article = article;
        this.quantiteActuelle = quantiteActuelle;
        this.dateDerniereMaj = LocalDateTime.now();
    }

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        dateDerniereMaj = LocalDateTime.now();
    }
}