package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.MouvementStockSecDto;
import com.osm.inventory_service.dto.StockSecDto;
import com.osm.inventory_service.entity.StockSec;
import com.osm.inventory_service.service.StockSecService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/stocks")
public class StockSecController extends BaseControllerImpl<StockSec, StockSecDto, StockSecDto> {

    private final StockSecService stockService;

    @Autowired
    public StockSecController(BaseService<StockSec, StockSecDto, StockSecDto> baseService, ModelMapper modelMapper, StockSecService stockService) {
        super(baseService, modelMapper);
        this.stockService = stockService;
    }


    @GetMapping
    public ResponseEntity<List<StockSecDto>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockSecDto> getStockById(@PathVariable UUID id) {
        return ResponseEntity.ok(stockService.getStockById(id));
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<StockSecDto> getStockByArticle(@PathVariable UUID articleId) {
        return ResponseEntity.ok(stockService.getStockByArticle(articleId));
    }

    @GetMapping("/emplacement/{emplacementId}")
    public ResponseEntity<List<StockSecDto>> getStocksByEmplacement(@PathVariable UUID emplacementId) {
        return ResponseEntity.ok(stockService.getStocksByEmplacement(emplacementId));
    }

    @GetMapping("/zone/{zone}")
    public ResponseEntity<List<StockSecDto>> getStocksByZone(@PathVariable String zone) {
        return ResponseEntity.ok(stockService.getStocksByZone(zone));
    }

    @GetMapping("/emplacements-disponibles")
    public ResponseEntity<List<StockSecDto>> getStocksAvecEmplacementDisponible() {
        return ResponseEntity.ok(stockService.getStocksAvecEmplacementDisponible());
    }

    @PostMapping("/create")
    public ResponseEntity<StockSecDto> createStock(@RequestBody StockSecDto stockDto) {
        return new ResponseEntity<>(stockService.createStock(stockDto), HttpStatus.CREATED);
    }

    @PostMapping("/article/{articleId}")
    public ResponseEntity<StockSecDto> createStockForArticle(@PathVariable UUID articleId) {
        return new ResponseEntity<>(stockService.createStockForArticle(articleId), HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    public ResponseEntity<StockSecDto> updateStock(@PathVariable UUID id, @RequestBody StockSecDto stockDto) {
        return ResponseEntity.ok(stockService.updateStock(id, stockDto));
    }


    @PutMapping("/{articleId}/entree")
    public ResponseEntity<StockSecDto> entreeStock(@PathVariable UUID articleId, @RequestBody Map<String, Object> payload) {
        Integer quantite = (Integer) payload.get("quantite");
        String motif = (String) payload.get("motif");
        return ResponseEntity.ok(stockService.entreeStock(articleId, quantite, motif));
    }

    @PutMapping("/{articleId}/sortie")
    public ResponseEntity<StockSecDto> sortieStock(@PathVariable UUID articleId, @RequestBody Map<String, Object> payload) {
        Integer quantite = (Integer)payload.get("quantite");
        String motif = String.valueOf(payload.get("motif"));
        return ResponseEntity.ok(stockService.sortieStock(articleId, quantite,motif));
    }

    @PutMapping("/{articleId}/ajuster")
    public ResponseEntity<StockSecDto> ajusterStock(
            @PathVariable UUID articleId,
            @RequestBody Map<String, Object> payload) {
        Integer nouvelleQuantite = (Integer) payload.get("quantite");
        String motif = (String) payload.get("motif");
        return ResponseEntity.ok(stockService.ajusterStock(articleId, nouvelleQuantite, motif));
    }


    @PutMapping("/{stockId}/assigner-emplacement/{emplacementId}")
    public ResponseEntity<StockSecDto> assignerEmplacement(@PathVariable UUID stockId, @PathVariable UUID emplacementId) {
        return ResponseEntity.ok(stockService.assignerEmplacement(stockId, emplacementId));
    }

    @PutMapping("/{stockId}/retirer-emplacement")
    public ResponseEntity<StockSecDto> retirerEmplacement(@PathVariable UUID stockId) {
        return ResponseEntity.ok(stockService.retirerEmplacement(stockId));
    }

    @PutMapping("/{stockId}/transferer-emplacement/{nouvelEmplacementId}")
    public ResponseEntity<StockSecDto> transfererEmplacement(
            @PathVariable UUID stockId,
            @PathVariable UUID nouvelEmplacementId) {
        return ResponseEntity.ok(stockService.transfererEmplacement(stockId, nouvelEmplacementId));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable UUID id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/mouvements")
    public ResponseEntity<List<MouvementStockSecDto>> getAllMouvements() {
        return ResponseEntity.ok(stockService.getAllMouvementsDto());
    }

    @GetMapping("/mouvements/article/{articleId}")
    public ResponseEntity<List<MouvementStockSecDto>> getMouvementsByArticle(@PathVariable UUID articleId) {
        return ResponseEntity.ok(stockService.getMouvementsByArticleDto(articleId));
    }

    @DeleteMapping("/article/{articleId}")
    public ResponseEntity<Void> deleteStockByArticle(@PathVariable UUID articleId) {
        stockService.deleteStockByArticle(articleId);
        return ResponseEntity.noContent().build();
    }

    @Override
    protected String getResourceName() {
        return "StockSec";
    }
}