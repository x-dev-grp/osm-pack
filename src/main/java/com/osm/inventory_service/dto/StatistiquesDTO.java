package com.osm.inventory_service.dto;

import lombok.*;

import java.util.List;
import java.util.Map;


@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesDTO {
    // Indicateurs stock
    private Long totalArticles;
    private Long articlesEnAlerte;
    private Double valeurTotaleStock;
    private Double tauxRupture;
    private Integer joursCouvertureMoyen;

    // Indicateurs achats
    private Long bonsEnAttente;
    private Long bonsValidesMois;
    private Double montantAchatsMois;
    private Double delaiValidationMoyen; // en heures

    // Top articles
    private List<Map<String, Object>> topArticlesValeur;
    private List<Map<String, Object>> articlesRuptureFrequente;

    // Graphiques
    private Map<String, Integer> mouvementsParMois;
    private Map<String, Double> achatsParFournisseur;
    private Map<String, Integer> alertesParCategorie;
}