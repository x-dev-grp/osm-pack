package com.abiooc.inventory_service.controller;

import com.abiooc.inventory_service.entity.MouvementStockSec;
import com.abiooc.inventory_service.entity.StockSec;
import com.abiooc.inventory_service.service.StockSecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventaire/stocks-secs")
@CrossOrigin(origins = "*")
public class StockSecController {

    public static final String ARTICLE_ID = "articleId";
    @Autowired
    private StockSecService stockSecService;

    @GetMapping("/article/{articleId}")
    public StockSec getStockByArticle(@PathVariable Long articleId) {
        return stockSecService.getStockByArticle(articleId);
    }

    @GetMapping("/article/{articleId}/historique")
    public List<MouvementStockSec> getHistorique(@PathVariable Long articleId) {
        return stockSecService.getHistoriqueMouvements(articleId);
    }

    @PostMapping("/entree")
    public StockSec entreeStock(@RequestBody Map<String, Object> payload) {
        Long articleId = Long.valueOf(payload.get(ARTICLE_ID).toString());
        Integer quantite = Integer.valueOf(payload.get("quantite").toString());
        String motif = (String) payload.get("motif");

        return stockSecService.entreeStock(articleId, quantite, motif);
    }

    @PostMapping("/sortie")
    public StockSec sortieStock(@RequestBody Map<String, Object> payload) {
        Long articleId = Long.valueOf(payload.get(ARTICLE_ID).toString());
        Integer quantite = Integer.valueOf(payload.get("quantite").toString());
        String motif = (String) payload.get("motif");
        String utilisateur = (String) payload.getOrDefault("utilisateur", "system");

        return stockSecService.sortieStock(articleId, quantite, motif);
    }

}