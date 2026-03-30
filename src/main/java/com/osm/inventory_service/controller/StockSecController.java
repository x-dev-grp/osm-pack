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
    public StockSecController(BaseService<StockSec, StockSecDto, StockSecDto> baseService,
                              ModelMapper modelMapper,
                              StockSecService stockService) {
        super(baseService, modelMapper);
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<?> getAllStocks() {
        try {
            List<StockSecDto> stocks = stockService.getAllStocks();
            return ResponseEntity.ok(stocks);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStockById(@PathVariable UUID id) {
        try {
            StockSecDto stock = stockService.getStockById(id);
            return ResponseEntity.ok(stock);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<?> getStockByArticle(@PathVariable UUID articleId) {
        try {
            StockSecDto stock = stockService.getStockByArticle(articleId);
            return ResponseEntity.ok(stock);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/emplacement/{emplacementId}")
    public ResponseEntity<?> getStocksByEmplacement(@PathVariable UUID emplacementId) {
        try {
            List<StockSecDto> stocks = stockService.getStocksByEmplacement(emplacementId);
            return ResponseEntity.ok(stocks);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/zone/{zone}")
    public ResponseEntity<?> getStocksByZone(@PathVariable String zone) {
        try {
            List<StockSecDto> stocks = stockService.getStocksByZone(zone);
            return ResponseEntity.ok(stocks);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/emplacements-disponibles")
    public ResponseEntity<?> getStocksAvecEmplacementDisponible() {
        try {
            List<StockSecDto> stocks = stockService.getStocksAvecEmplacementDisponible();
            return ResponseEntity.ok(stocks);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createStock(@RequestBody StockSecDto stockDto) {
        try {
            StockSecDto created = stockService.createStock(stockDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/article/{articleId}")
    public ResponseEntity<?> createStockForArticle(@PathVariable UUID articleId) {
        try {
            StockSecDto created = stockService.createStockForArticle(articleId);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStock(@PathVariable UUID id, @RequestBody StockSecDto stockDto) {
        try {
            StockSecDto updated = stockService.updateStock(id, stockDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{articleId}/entree")
    public ResponseEntity<?> entreeStock(@PathVariable UUID articleId, @RequestBody Map<String, Object> payload) {
        try {
            Integer quantite = (Integer) payload.get("quantite");
            String motif = (String) payload.get("motif");
            StockSecDto result = stockService.entreeStock(articleId, quantite, motif);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{articleId}/sortie")
    public ResponseEntity<?> sortieStock(@PathVariable UUID articleId, @RequestBody Map<String, Object> payload) {
        try {
            Integer quantite = (Integer) payload.get("quantite");
            String motif = (String) payload.get("motif");
            StockSecDto result = stockService.sortieStock(articleId, quantite, motif);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{articleId}/ajuster")
    public ResponseEntity<?> ajusterStock(@PathVariable UUID articleId, @RequestBody Map<String, Object> payload) {
        try {
            Integer nouvelleQuantite = (Integer) payload.get("quantite");
            String motif = (String) payload.get("motif");
            StockSecDto result = stockService.ajusterStock(articleId, nouvelleQuantite, motif);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{stockId}/assigner-emplacement/{emplacementId}")
    public ResponseEntity<?> assignerEmplacement(@PathVariable UUID stockId, @PathVariable UUID emplacementId) {
        try {
            StockSecDto result = stockService.assignerEmplacement(stockId, emplacementId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{stockId}/retirer-emplacement")
    public ResponseEntity<?> retirerEmplacement(@PathVariable UUID stockId) {
        try {
            StockSecDto result = stockService.retirerEmplacement(stockId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{stockId}/transferer-emplacement/{nouvelEmplacementId}")
    public ResponseEntity<?> transfererEmplacement(@PathVariable UUID stockId, @PathVariable UUID nouvelEmplacementId) {
        try {
            StockSecDto result = stockService.transfererEmplacement(stockId, nouvelEmplacementId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStock(@PathVariable UUID id) {
        try {
            stockService.deleteStock(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mouvements")
    public ResponseEntity<?> getAllMouvements() {
        try {
            List<MouvementStockSecDto> mouvements = stockService.getAllMouvementsDto();
            return ResponseEntity.ok(mouvements);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mouvements/article/{articleId}")
    public ResponseEntity<?> getMouvementsByArticle(@PathVariable UUID articleId) {
        try {
            List<MouvementStockSecDto> mouvements = stockService.getMouvementsByArticleDto(articleId);
            return ResponseEntity.ok(mouvements);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/article/{articleId}")
    public ResponseEntity<?> deleteStockByArticle(@PathVariable UUID articleId) {
        try {
            stockService.deleteStockByArticle(articleId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected String getResourceName() {
        return "StockSec";
    }
}