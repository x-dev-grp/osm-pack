/*package com.abiooc.inventory_service.service;

import com.abiooc.inventory_service.dto.StatistiquesDTO;
import com.abiooc.inventory_service.entity.*;
import com.abiooc.inventory_service.Enum.*;
import com.abiooc.inventory_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
            stats.setTotalArticles(articleRepository.count());
            stats.setArticlesEnAlerte(compterArticlesEnAlerteSimple());

            // ATTENTION: dans ton modèle actuel, il n’y a pas de prix unitaire.
            // Cette "valeurTotaleStock" est donc une somme de quantités.
            stats.setValeurTotaleStock(calculerValeurTotaleStockSimple());

            stats.setTauxRupture(calculerTauxRupture());
            stats.setJoursCouvertureMoyen(30);

            stats.setBonsEnAttente(compterBonsEnAttenteSimple());
            stats.setBonsValidesMois(0L);
            stats.setMontantAchatsMois(0.0);
            stats.setDelaiValidationMoyen(0.0);

            stats.setTopArticlesValeur(getTopArticlesSimple());
            stats.setArticlesRuptureFrequente(new ArrayList<>());

            // ✅ PLUS DE DONNÉES FAUSSES: mouvements par mois depuis DB
            stats.setMouvementsParMois(getMouvementsParMoisFromDb(6));

            stats.setAchatsParFournisseur(new HashMap<>());
            stats.setAlertesParCategorie(getAlertesParCategorieSimple());

        } catch (Exception e) {
            e.printStackTrace();
            setDefaultValues(stats);
        }

        return stats;
    }

    public List<Map<String, Object>> getArticlesCritiques() {
        List<Map<String, Object>> articlesCritiques = new ArrayList<>();
        try {
            List<ArticleSec> articles = articleRepository.findByActifTrue();

            for (ArticleSec a : articles) {
                StockSec stock = stockRepository.findByArticle(a).orElse(null);
                if (stock != null && a.getStockMinimum() != null &&
                        stock.getQuantiteActuelle() <= a.getStockMinimum()) {

                    Map<String, Object> articleMap = new HashMap<>();
                    articleMap.put("id", a.getId());
                    articleMap.put("sku", a.getSku());
                    articleMap.put("nom", a.getNom());
                    articleMap.put("stockActuel", stock.getQuantiteActuelle());
                    articleMap.put("stockMinimum", a.getStockMinimum());
                    articleMap.put("uniteMesure", a.getUniteMesure());
                    articleMap.put("categorie", a.getCategorie());

                    // ratio critique (0..1) pour trier
                    double ratio = (a.getStockMinimum() != null && a.getStockMinimum() > 0)
                            ? (stock.getQuantiteActuelle() * 1.0 / a.getStockMinimum())
                            : 0.0;

                    articleMap.put("ratio", ratio);
                    articlesCritiques.add(articleMap);
                }
            }

            // Trier par ratio asc (plus critique d’abord)
            articlesCritiques.sort((m1, m2) -> Double.compare(
                    (double) m1.getOrDefault("ratio", 0.0),
                    (double) m2.getOrDefault("ratio", 0.0)
            ));

            // Enlever la clé ratio avant retour si tu veux
            for (Map<String, Object> m : articlesCritiques) {
                m.remove("ratio");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return articlesCritiques;
    }

    public List<Map<String, Object>> getMouvementsRecents(int limit) {

        List<Map<String, Object>> mouvementsRecents = new ArrayList<>();

        try {

            List<MouvementStockSec> mouvements = mouvementRepository.findAll();

            // Trier par date DESC
            mouvements.sort((m1, m2) ->
                    m2.getDateMouvement().compareTo(m1.getDateMouvement())
            );

            // Limiter le nombre
            List<MouvementStockSec> mouvementsLimites =
                    mouvements.stream().limit(limit).toList();

            for (MouvementStockSec mvt : mouvementsLimites) {

                Map<String, Object> mvtMap = new HashMap<>();
                mvtMap.put("id", mvt.getId());
                mvtMap.put("typeMouvement", mvt.getTypeMouvement());
                mvtMap.put("quantite", mvt.getQuantite());
                mvtMap.put("dateMouvement", mvt.getDateMouvement());
                mvtMap.put("motif", mvt.getMotif());

                Map<String, Object> articleMap = new HashMap<>();
                ArticleSec article = mvt.getArticle();

                if (article != null) {
                    articleMap.put("id", article.getId());
                    articleMap.put("sku", article.getSku());
                    articleMap.put("nom", article.getNom());
                    articleMap.put("uniteMesure", article.getUniteMesure());
                    articleMap.put("categorie", article.getCategorie());
                }

                mvtMap.put("article", articleMap);
                mouvementsRecents.add(mvtMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mouvementsRecents;
    }
    private Double calculerTauxRupture() {
        try {
            long totalArticles = articleRepository.count();
            if (totalArticles == 0) return 0.0;

            long articlesEnRupture = 0;
            List<ArticleSec> articles = articleRepository.findByActifTrue();
            for (ArticleSec a : articles) {
                StockSec stock = stockRepository.findByArticle(a).orElse(null);
                if (stock != null && stock.getQuantiteActuelle() != null && stock.getQuantiteActuelle() <= 0) {
                    articlesEnRupture++;
                }
            }
            return (articlesEnRupture * 100.0) / totalArticles;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void setDefaultValues(StatistiquesDTO stats) {
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
        stats.setMouvementsParMois(new LinkedHashMap<>());
        stats.setAchatsParFournisseur(new HashMap<>());
        stats.setAlertesParCategorie(new HashMap<>());
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
                if (stock != null && stock.getQuantiteActuelle() != null) {
                    total += stock.getQuantiteActuelle() * 1.0; // somme de quantités
                }
            }
            return total;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private List<Map<String, Object>> getTopArticlesSimple() {
        // Conserver ton existant si tu l’as déjà implémenté.
        // Ici: retour vide pour éviter d’inventer des attributs.
        return new ArrayList<>();
    }

    // ✅ DB: mouvements groupés par mois
    private Map<String, Integer> getMouvementsParMoisFromDb(int nbMois) {
        Map<String, Integer> result = new LinkedHashMap<>();

        YearMonth fin = YearMonth.now();
        YearMonth debut = fin.minusMonths(nbMois - 1);

        LocalDateTime start = debut.atDay(1).atStartOfDay();
        LocalDateTime end = fin.atEndOfMonth().atTime(23, 59, 59);

        List<MouvementStockSec> mouvements = mouvementRepository.findByDateMouvementBetween(start, end);

        Map<YearMonth, Long> grouped = mouvements.stream()
                .filter(m -> m.getDateMouvement() != null)
                .collect(Collectors.groupingBy(m -> YearMonth.from(m.getDateMouvement()), Collectors.counting()));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);

        for (YearMonth ym = debut; !ym.isAfter(fin); ym = ym.plusMonths(1)) {
            String label = ym.atDay(1).format(fmt);
            int count = grouped.getOrDefault(ym, 0L).intValue();
            result.put(label, count);
        }

        return result;
    }

    private Map<String, Integer> getAlertesParCategorieSimple() {
        Map<String, Integer> map = new HashMap<>();
        try {
            List<ArticleSec> articles = articleRepository.findByActifTrue();
            for (ArticleSec a : articles) {
                StockSec stock = stockRepository.findByArticle(a).orElse(null);
                if (stock != null && a.getStockMinimum() != null &&
                        stock.getQuantiteActuelle() <= a.getStockMinimum()) {
                    String cat = String.valueOf(a.getCategorie());
                    map.put(cat, map.getOrDefault(cat, 0) + 1);
                }
            }
        } catch (Exception e) {
            // ignorer
        }
        return map;
    }
}*/