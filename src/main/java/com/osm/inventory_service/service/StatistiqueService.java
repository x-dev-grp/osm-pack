package com.osm.inventory_service.service;

import com.osm.inventory_service.Enum.StatutBonCommande;
import com.osm.inventory_service.dto.ArticleCritiqueDto;
import com.osm.inventory_service.dto.MouvementRecentDto;
import com.osm.inventory_service.dto.StatistiquesDTO;
import com.osm.inventory_service.dto.StockDashboardPayloadDto;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.MouvementStockSec;
import com.osm.inventory_service.entity.StockSec;
import com.osm.inventory_service.repository.ArticleSecRepository;
import com.osm.inventory_service.repository.BonCommandeRepository;
import com.osm.inventory_service.repository.MouvementStockSecRepository;
import com.osm.inventory_service.repository.StockSecRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatistiqueService {

    private static final int CHART_MONTHS = 6;

    private final ArticleSecRepository articleRepository;
    private final StockSecRepository stockRepository;
    private final MouvementStockSecRepository mouvementRepository;
    private final BonCommandeRepository bonCommandeRepository;

    public StockDashboardPayloadDto getDashboardPayload(int mouvementLimit) {
        StockDashboardPayloadDto payload = new StockDashboardPayloadDto();
        payload.setStatistiques(buildStatistiques());
        payload.setArticlesCritiques(listArticlesCritiques(50));
        payload.setMouvementsRecents(listMouvementsRecents(mouvementLimit));
        return payload;
    }

    public StatistiquesDTO getStatistiquesCompletes() {
        return buildStatistiques();
    }

    public List<ArticleCritiqueDto> getArticlesCritiques() {
        return listArticlesCritiques(50);
    }

    public List<MouvementRecentDto> getMouvementsRecents(int limit) {
        return listMouvementsRecents(limit);
    }

    private StatistiquesDTO buildStatistiques() {
        StatistiquesDTO stats = new StatistiquesDTO();

        List<ArticleSec> articlesActifs = articleRepository.findAllActiveNotDeleted();
        Map<java.util.UUID, StockSec> stockByArticle = stockRepository.findAllByIsDeletedFalse().stream()
                .filter(s -> s.getArticle() != null && s.getArticle().getId() != null)
                .collect(Collectors.toMap(s -> s.getArticle().getId(), s -> s, (a, b) -> a));

        long totalArticles = articlesActifs.size();
        long articlesEnAlerte = 0;
        long articlesEnRupture = 0;
        double valeurTotale = 0;

        for (ArticleSec article : articlesActifs) {
            StockSec stock = stockByArticle.get(article.getId());
            int actuelle = safe(stock != null ? stock.getQuantiteActuelle() : 0);
            int minimum = safe(article.getStockMinimum());
            int disponible = stock != null
                    ? safe(stock.getQuantiteActuelle()) - safe(stock.getQuantiteReservee())
                    : 0;

            valeurTotale += actuelle;
            if (minimum > 0 && actuelle <= minimum) {
                articlesEnAlerte++;
            }
            if (disponible <= 0) {
                articlesEnRupture++;
            }
        }

        stats.setTotalArticles(totalArticles);
        stats.setArticlesEnAlerte(articlesEnAlerte);
        stats.setValeurTotaleStock(valeurTotale);
        stats.setTauxRupture(totalArticles > 0 ? (articlesEnRupture * 100.0) / totalArticles : 0.0);
        stats.setJoursCouvertureMoyen(30);
        stats.setBonsEnAttente(countBonsEnAttente());
        stats.setBonsValidesMois(0L);
        stats.setMontantAchatsMois(0.0);
        stats.setDelaiValidationMoyen(0.0);
        stats.setTopArticlesValeur(new ArrayList<>());
        stats.setArticlesRuptureFrequente(new ArrayList<>());
        stats.setMouvementsParMois(buildMouvementsParMois(CHART_MONTHS));
        stats.setAchatsParFournisseur(new LinkedHashMap<>());
        stats.setAlertesParCategorie(buildAlertesParCategorie(articlesActifs, stockByArticle));

        return stats;
    }

    private List<ArticleCritiqueDto> listArticlesCritiques(int max) {
        List<ArticleSec> articlesActifs = articleRepository.findAllActiveNotDeleted();

        return articlesActifs.stream()
                .map(article -> {
                    StockSec stock = stockRepository.findByArticleId(article.getId()).orElse(null);
                    int actuelle = safe(stock != null ? stock.getQuantiteActuelle() : 0);
                    int minimum = safe(article.getStockMinimum());
                    int disponible = stock != null
                            ? safe(stock.getQuantiteActuelle()) - safe(stock.getQuantiteReservee())
                            : 0;
                    double ratio = minimum > 0 ? (double) actuelle / minimum : 0;

                    ArticleCritiqueDto dto = new ArticleCritiqueDto();
                    dto.setId(article.getId());
                    dto.setSku(article.getSkuId() != null ? article.getSkuId().toString() : article.getId().toString());
                    dto.setNom(article.getNom());
                    dto.setStockActuel(actuelle);
                    dto.setStockMinimum(minimum);
                    dto.setCategorie(article.getCategorie() != null ? article.getCategorie().name() : null);
                    dto.setStockDisponible(disponible);
                    return Map.entry(ratio, dto);
                })
                .filter(entry -> {
                    ArticleCritiqueDto dto = entry.getValue();
                    int min = safe(dto.getStockMinimum());
                    return min > 0 && safe(dto.getStockActuel()) <= min;
                })
                .sorted(Comparator.comparingDouble(Map.Entry::getKey))
                .limit(max)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private List<MouvementRecentDto> listMouvementsRecents(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        List<MouvementStockSec> mouvements = mouvementRepository.findRecentNotDeleted(
                PageRequest.of(0, safeLimit));

        List<MouvementRecentDto> result = new ArrayList<>();
        for (MouvementStockSec mvt : mouvements) {
            result.add(toMouvementRecentDto(mvt));
        }
        return result;
    }

    private MouvementRecentDto toMouvementRecentDto(MouvementStockSec mvt) {
        MouvementRecentDto dto = new MouvementRecentDto();
        dto.setId(mvt.getId());
        dto.setTypeMouvement(mvt.getTypeMouvement());
        dto.setQuantite(mvt.getQuantite());
        dto.setDateMouvement(mvt.getDateMouvement());
        dto.setMotif(mvt.getMotif());

        ArticleSec article = mvt.getArticle();
        if (article != null) {
            dto.setArticleId(article.getId());
            dto.setArticleSku(article.getSkuId() != null ? article.getSkuId().toString() : article.getId().toString());
            dto.setArticleNom(article.getNom());
            dto.setUniteMesure(article.getUm() != null ? article.getUm().name() : null);
        }
        return dto;
    }

    private Map<String, Integer> buildMouvementsParMois(int nbMois) {
        Map<String, Integer> result = new LinkedHashMap<>();
        YearMonth fin = YearMonth.now();
        YearMonth debut = fin.minusMonths(nbMois - 1L);

        LocalDateTime start = debut.atDay(1).atStartOfDay();
        LocalDateTime end = fin.atEndOfMonth().atTime(23, 59, 59);

        List<MouvementStockSec> mouvements = mouvementRepository.findByDateMouvementBetweenNotDeleted(start, end);

        Map<YearMonth, Long> grouped = mouvements.stream()
                .filter(m -> m.getDateMouvement() != null)
                .collect(Collectors.groupingBy(m -> YearMonth.from(m.getDateMouvement()), Collectors.counting()));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);

        for (YearMonth ym = debut; !ym.isAfter(fin); ym = ym.plusMonths(1)) {
            String label = ym.atDay(1).format(fmt);
            result.put(label, grouped.getOrDefault(ym, 0L).intValue());
        }
        return result;
    }

    private Map<String, Integer> buildAlertesParCategorie(
            List<ArticleSec> articlesActifs,
            Map<java.util.UUID, StockSec> stockByArticle) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (ArticleSec article : articlesActifs) {
            StockSec stock = stockByArticle.get(article.getId());
            int actuelle = safe(stock != null ? stock.getQuantiteActuelle() : 0);
            int minimum = safe(article.getStockMinimum());
            if (minimum > 0 && actuelle <= minimum) {
                String cat = article.getCategorie() != null ? article.getCategorie().name() : "AUTRE";
                map.put(cat, map.getOrDefault(cat, 0) + 1);
            }
        }
        return map;
    }

    private long countBonsEnAttente() {
        try {
            return bonCommandeRepository.countByStatusNotDeleted(StatutBonCommande.EN_ATTENTE);
        } catch (Exception ex) {
            log.warn("Impossible de compter les bons en attente: {}", ex.getMessage());
            try {
                Long legacy = bonCommandeRepository.countBonCommandesByStatus(StatutBonCommande.EN_ATTENTE);
                return legacy != null ? legacy : 0L;
            } catch (Exception fallback) {
                return 0L;
            }
        }
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
