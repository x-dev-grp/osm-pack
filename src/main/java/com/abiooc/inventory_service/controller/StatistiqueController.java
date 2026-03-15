/*package com.abiooc.inventory_service.controller;

import com.abiooc.inventory_service.dto.StatistiquesDTO;
import com.abiooc.inventory_service.service.StatistiqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventaire/statistiques")
@CrossOrigin(origins = "*")
public class StatistiqueController {

    @Autowired
    private StatistiqueService statistiqueService;

    @GetMapping("/dashboard")
    public StatistiquesDTO getDashboard() {
        return statistiqueService.getStatistiquesCompletes();
    }

    @GetMapping("/articles/critiques")
    public List<Map<String, Object>> getArticlesCritiques() {
        return statistiqueService.getArticlesCritiques();
    }

    @GetMapping("/mouvements/recents")
    public List<Map<String, Object>> getMouvementsRecents(@RequestParam(defaultValue = "10") int limit) {
        return statistiqueService.getMouvementsRecents(limit);
    }

    @GetMapping("/stock/taux-rupture")
    public Map<String, Object> getTauxRupture() {
        StatistiquesDTO stats = statistiqueService.getStatistiquesCompletes();
        return Map.of(
                "tauxRupture", stats.getTauxRupture(),
                "articlesEnRupture", (long) Math.round((stats.getTauxRupture() / 100.0) * stats.getTotalArticles()),
                "totalArticles", stats.getTotalArticles()
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
}*/