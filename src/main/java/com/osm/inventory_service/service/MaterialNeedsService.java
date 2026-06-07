package com.osm.inventory_service.service;

import com.osm.inventory_service.Enum.CategorieArticle;
import com.osm.inventory_service.config.ColisConfig;
import com.osm.inventory_service.config.PaletteConfig;
import com.osm.inventory_service.dto.MaterialNeedLineDto;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.BomLine;
import com.osm.inventory_service.entity.StockSec;
import com.osm.inventory_service.exception.ResourceNotFoundException;
import com.osm.inventory_service.repository.ArticleSecRepository;
import com.osm.inventory_service.repository.BomRepository;
import com.osm.inventory_service.repository.StockSecRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MaterialNeedsService {

    private final BomRepository bomRepository;
    private final StockSecRepository stockRepository;
    private final ArticleSecRepository articleRepository;

    public MaterialNeedsService(
            BomRepository bomRepository,
            StockSecRepository stockRepository,
            ArticleSecRepository articleRepository
    ) {
        this.bomRepository = bomRepository;
        this.stockRepository = stockRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public List<MaterialNeedLineDto> computeForBom(UUID bomId, double productionQuantity) {
        if (productionQuantity <= 0) {
            throw new IllegalArgumentException("La quantite de production doit etre positive");
        }

        var bom = bomRepository.findByIdAndIsDeletedFalse(bomId)
                .orElseThrow(() -> new ResourceNotFoundException("BOM non trouvee avec l'id : " + bomId));

        List<MaterialNeedLineDto> lines = new ArrayList<>();
        if (bom.getLines() == null) {
            return lines;
        }

        for (BomLine line : bom.getLines()) {
            if (line.getArticle() == null) {
                continue;
            }
            UUID articleId = line.getArticle().getId();
            double needed = calculateNeededQuantity(line, productionQuantity);
            int neededRounded = (int) Math.ceil(needed - 1e-9);

            MaterialNeedLineDto dto = new MaterialNeedLineDto();
            dto.setArticleId(articleId);
            dto.setArticleName(line.getArticle().getNom());
            dto.setUnitOfMeasure(line.getUnitOfMeasure() != null ? line.getUnitOfMeasure().name() : null);
            dto.setQuantityPerUnit(line.getQuantity());
            dto.setQuantityNeeded(needed);
            dto.setQuantityNeededRounded(neededRounded);

            stockRepository.findByArticleIdAndIsDeletedFalse(articleId).ifPresentOrElse(stock -> fillStock(dto, stock), () -> {
                dto.setQuantiteActuelle(0);
                dto.setQuantiteReservee(0);
                dto.setQuantiteDisponible(0);
                dto.setSufficient(false);
            });

            lines.add(dto);
        }
        return lines;
    }

    private double calculateNeededQuantity(BomLine line, double productionQuantity) {
        ArticleSec article = line.getArticle();
        double rawNeed = line.getQuantity() * productionQuantity;

        if (article == null || article.getCategorie() == null || article.getConfiguration() == null) {
            return rawNeed;
        }

        if (article.getCategorie() == CategorieArticle.COLIS && article.getConfiguration() instanceof ColisConfig colisConfig) {
            int unitsPerColis = colisConfig.getUnitsPerColis();
            if (unitsPerColis > 0) {
                return Math.ceil((productionQuantity / unitsPerColis) * line.getQuantity());
            }
        }

        if (article.getCategorie() == CategorieArticle.PALETTE && article.getConfiguration() instanceof PaletteConfig paletteConfig) {
            int unitsPerPalette = calculateUnitsPerPalette(paletteConfig);
            if (unitsPerPalette > 0) {
                return Math.ceil((productionQuantity / unitsPerPalette) * line.getQuantity());
            }
        }

        return rawNeed;
    }

    private int calculateUnitsPerPalette(PaletteConfig paletteConfig) {
        if (paletteConfig.getColisId() == null || paletteConfig.getColisPerLayer() <= 0 || paletteConfig.getNumberOfLayers() <= 0) {
            return 0;
        }

        int colisPerPalette = paletteConfig.getColisPerLayer() * paletteConfig.getNumberOfLayers();

        return articleRepository.findById(paletteConfig.getColisId())
                .map(ArticleSec::getConfiguration)
                .filter(ColisConfig.class::isInstance)
                .map(ColisConfig.class::cast)
                .map(ColisConfig::getUnitsPerColis)
                .filter(unitsPerColis -> unitsPerColis > 0)
                .map(unitsPerColis -> unitsPerColis * colisPerPalette)
                .orElse(0);
    }

    private void fillStock(MaterialNeedLineDto dto, StockSec stock) {
        int actuelle = safe(stock.getQuantiteActuelle());
        int reservee = safe(stock.getQuantiteReservee());
        int disponible = actuelle - reservee;
        dto.setQuantiteActuelle(actuelle);
        dto.setQuantiteReservee(reservee);
        dto.setQuantiteDisponible(disponible);
        dto.setSufficient(disponible >= dto.getQuantityNeededRounded());
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
