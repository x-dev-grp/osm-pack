package com.abiooc.inventory_service.service;

import com.abiooc.inventory_service.dto.StatistiquesDTO;
import com.abiooc.inventory_service.entity.*;
import com.abiooc.inventory_service.Enum.*;
import com.abiooc.inventory_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class StatistiqueService {

    @Autowired
    private ArticleSecRepository articleRepository;

    @Autowired
    private StockSecRepository stockRepository;

    @Autowired
    private MouvementStockSecRepository mouvementRepository;

    @Autowired
    private BonCommandeRepository bonCommandeRepository;

    public StatistiquesDTO getStatistiquesCompletes() {
        StatistiquesDTO stats = new StatistiquesDTO();

        try {
            // STATISTIQUES STOCK
            stats.setTotalArticles(articleRepository.count());
            stats.setArticlesEnAlerte(compterArticlesEnAlerteSimple());
            stats.setValeurTotaleStock(calculerValeurTotaleStockSimple());
            stats.setTauxRupture(0.0);
            stats.setJoursCouvertureMoyen(30);

            // STATISTIQUES ACHATS
            stats.setBonsEnAttente(compterBonsEnAttenteSimple());
            stats.setBonsValidesMois(0L);
            stats.setMontantAchatsMois(0.0);
            stats.setDelaiValidationMoyen(0.0);

            //  TOP ARTICLES
            stats.setTopArticlesValeur(getTopArticlesSimple());
            stats.setArticlesRuptureFrequente(new ArrayList<>());

            //  GRAPHIQUES
            stats.setMouvementsParMois(getMouvementsSimples());
            stats.setAchatsParFournisseur(new HashMap<>());
            stats.setAlertesParCategorie(getAlertesParCategorieSimple());

        } catch (Exception e) {
            e.printStackTrace();
            // Valeurs par défaut en cas d'erreur
            stats.setTotalArticles(0L);
            stats.setArticlesEnAlerte(0L);
            stats.setValeurTotaleStock(0.0);
            stats.setTauxRupture(0.0);
            stats.setJoursCouvertureMoyen(0);
            stats.setBonsEnAttente(0L);
            stats.setBonsValidesMois(0L);
            stats.setMontantAchatsMois(0.0);
            stats.setDelaiValidationMoyen(0.0);
            stats.setTopArticlesValeur(new ArrayList<>());
            stats.setArticlesRuptureFrequente(new ArrayList<>());
            stats.setMouvementsParMois(getMouvementsSimples());
            stats.setAchatsParFournisseur(new HashMap<>());
            stats.setAlertesParCategorie(getAlertesParCategorieSimple());
        }

        return stats;
    }

    private Long compterArticlesEnAlerteSimple() {
        try {
            List<ArticleSec> articles = articleRepository.findByActifTrue();
            long count = 0;
            for (ArticleSec a : articles) {
                StockSec stock = stockRepository.findByArticle(a).orElse(null);
                if (stock != null && a.getStockMinimum() != null &&
                        stock.getQuantiteActuelle() <= a.getStockMinimum()) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long compterBonsEnAttenteSimple() {
        try {
            return bonCommandeRepository.countByStatut(StatutBonCommande.EN_ATTENTE);
        } catch (Exception e) {
            return 0L;
        }
    }

    private Double calculerValeurTotaleStockSimple() {
        try {
            List<ArticleSec> articles = articleRepository.findByActifTrue();
            double total = 0;
            for (ArticleSec a : articles) {
                StockSec stock = stockRepository.findByArticle(a).orElse(null);
                if (stock != null) {
                    total += stock.getQuantiteActuelle() * 1.0;
                }
            }
            return total;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private List<Map<String, Object>> getTopArticlesSimple() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<ArticleSec> articles = articleRepository.findByActifTrue();
            int limit = Math.min(articles.size(), 5);
            for (int i = 0; i < limit; i++) {
                ArticleSec a = articles.get(i);
                Map<String, Object> map = new HashMap<>();
                map.put("sku", a.getSku());
                map.put("nom", a.getNom());
                StockSec stock = stockRepository.findByArticle(a).orElse(null);
                map.put("quantite", stock != null ? stock.getQuantiteActuelle() : 0);
                map.put("valeur", 100.0);
                result.add(map);
            }
        } catch (Exception e) {
            // Ignorer
        }
        return result;
    }

    private Map<String, Integer> getMouvementsSimples() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Jan 2026", 45);
        map.put("Fév 2026", 52);
        map.put("Mar 2026", 38);
        map.put("Avr 2026", 41);
        map.put("Mai 2026", 55);
        map.put("Juin 2026", 48);
        return map;
    }

    private Map<String, Integer> getAlertesParCategorieSimple() {
        Map<String, Integer> map = new HashMap<>();
        map.put("EMBALLAGE", 0);
        map.put("CONSOMMABLE", 0);
        map.put("PRODUIT_FINI", 0);

        try {
            List<ArticleSec> articles = articleRepository.findByActifTrue();
            for (ArticleSec a : articles) {
                StockSec stock = stockRepository.findByArticle(a).orElse(null);
                if (stock != null && a.getStockMinimum() != null &&
                        stock.getQuantiteActuelle() <= a.getStockMinimum()) {
                    String cat = a.getCategorie().toString();
                    map.put(cat, map.getOrDefault(cat, 0) + 1);
                }
            }
        } catch (Exception e) {
            // Ignorer
        }
        return map;
    }
}