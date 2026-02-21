package com.abiooc.inventory_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.abiooc.inventory_service.Enum.CategorieArticle;

@Entity
@Table(name = "articles_secs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieArticle categorie;

    private String fournisseurDefaut;

    @Column(name = "stock_minimum")
    private Integer stockMinimum = 0;

    @Column(name = "stock_maximum")
    private Integer stockMaximum = 0;

    @Column(name = "unite_mesure")
    private String uniteMesure = "pièce";

    private Boolean actif = true;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}