package com.osm.inventory_service.entity;

import com.xdev.xdevbase.entities.BaseEntity;
import com.xdev.xdevbase.models.UniteMesure;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.osm.inventory_service.Enum.CategorieArticle;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "articles_secs")
@Data
@Audited
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

    @Enumerated(EnumType.STRING)
    private UniteMesure um;
}