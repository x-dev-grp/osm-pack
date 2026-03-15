package com.abiooc.inventory_service.entity;

import com.abiooc.inventory_service.Enum.UniteMesure;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.abiooc.inventory_service.Enum.CategorieArticle;

@Entity
@Table(name = "articles_secs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSec extends BaseEntity {

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieArticle categorie;

    @ManyToOne
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    private Integer stockMinimum = 0;

    private Integer stockMaximum = 0;

    private Boolean actif = true;

    @ManyToOne
    private SKU reference;

    @Enumerated(EnumType.STRING)
    private UniteMesure um;
}