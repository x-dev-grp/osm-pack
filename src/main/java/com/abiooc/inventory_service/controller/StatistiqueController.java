package com.abiooc.inventory_service.controller;

import com.abiooc.inventory_service.dto.StatistiquesDTO;
import com.abiooc.inventory_service.service.StatistiqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inventaire/statistiques")
@CrossOrigin(origins = "*")
public class StatistiqueController {

    @Autowired
    private StatistiqueService statistiqueService;

    @GetMapping("/dashboard")
    public StatistiquesDTO getDashboardStats() {
        return statistiqueService.getStatistiquesCompletes();
    }

    @GetMapping("/stock/taux-rupture")
    public Map<String, Object> getTauxRupture() {
        StatistiquesDTO stats = statistiqueService.getStatistiquesCompletes();
        return Map.of(
                "taux", stats.getTauxRupture(),
                "articlesEnAlerte", stats.getArticlesEnAlerte()
        );
    }

    @GetMapping("/achats/delai-validation")
    public Map<String, Object> getDelaiValidation() {
        StatistiquesDTO stats = statistiqueService.getStatistiquesCompletes();
        return Map.of(
                "delaiMoyen", stats.getDelaiValidationMoyen(),
                "bonsEnAttente", stats.getBonsEnAttente()
        );
    }

    @GetMapping("/stock/valeur")
    public Map<String, Object> getValeurStock() {
        StatistiquesDTO stats = statistiqueService.getStatistiquesCompletes();
        return Map.of(
                "valeurTotale", stats.getValeurTotaleStock(),
                "totalArticles", stats.getTotalArticles()
        );
    }
}