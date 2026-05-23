package com.osm.inventory_service.service;

import com.osm.inventory_service.dto.MaterialNeedLineDto;
import com.osm.inventory_service.entity.BomLine;
import com.osm.inventory_service.entity.StockSec;
import com.osm.inventory_service.exception.ResourceNotFoundException;
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

    public MaterialNeedsService(BomRepository bomRepository, StockSecRepository stockRepository) {
        this.bomRepository = bomRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional(readOnly = true)
    public List<MaterialNeedLineDto> computeForBom(UUID bomId, double productionQuantity) {
        if (productionQuantity <= 0) {
            throw new IllegalArgumentException("La quantite de production doit etre positive");
        }

        var bom = bomRepository.findById(bomId)
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
            double needed = line.getQuantity() * productionQuantity;
            int neededRounded = (int) Math.ceil(needed - 1e-9);

            MaterialNeedLineDto dto = new MaterialNeedLineDto();
            dto.setArticleId(articleId);
            dto.setArticleName(line.getArticle().getNom());
            dto.setUnitOfMeasure(line.getUnitOfMeasure() != null ? line.getUnitOfMeasure().name() : null);
            dto.setQuantityPerUnit(line.getQuantity());
            dto.setQuantityNeeded(needed);
            dto.setQuantityNeededRounded(neededRounded);

            stockRepository.findByArticleId(articleId).ifPresentOrElse(stock -> fillStock(dto, stock), () -> {
                dto.setQuantiteActuelle(0);
                dto.setQuantiteReservee(0);
                dto.setQuantiteDisponible(0);
                dto.setSufficient(false);
            });

            lines.add(dto);
        }
        return lines;
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
