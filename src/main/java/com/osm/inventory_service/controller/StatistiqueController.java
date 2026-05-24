package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.ArticleCritiqueDto;
import com.osm.inventory_service.dto.MouvementRecentDto;
import com.osm.inventory_service.dto.StatistiquesDTO;
import com.osm.inventory_service.dto.StockDashboardPayloadDto;
import com.osm.inventory_service.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventaire/statistiques")
public class StatistiqueController {

    private final StatistiqueService statistiqueService;

    public StatistiqueController(StatistiqueService statistiqueService) {
        this.statistiqueService = statistiqueService;
    }

    /**
     * Agrégat utilisé par le tableau de bord Angular (stats + listes).
     */
    @GetMapping("/dashboard/payload")
    public ResponseEntity<StockDashboardPayloadDto> getDashboardPayload(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statistiqueService.getDashboardPayload(limit));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<StatistiquesDTO> getDashboard() {
        return ResponseEntity.ok(statistiqueService.getStatistiquesCompletes());
    }

    @GetMapping("/articles/critiques")
    public ResponseEntity<List<ArticleCritiqueDto>> getArticlesCritiques() {
        return ResponseEntity.ok(statistiqueService.getArticlesCritiques());
    }

    @GetMapping("/mouvements/recents")
    public ResponseEntity<List<MouvementRecentDto>> getMouvementsRecents(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statistiqueService.getMouvementsRecents(limit));
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
}