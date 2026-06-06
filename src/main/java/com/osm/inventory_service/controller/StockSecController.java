package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.ArticleStockSummaryDto;
import com.osm.inventory_service.dto.MouvementStockSecDto;
import com.osm.inventory_service.dto.StockSecDto;
import com.osm.inventory_service.entity.StockSec;
import com.osm.inventory_service.service.StockSecService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @GetMapping("/summary")
    public ResponseEntity<List<ArticleStockSummaryDto>> getStockSummary() {
        return ResponseEntity.ok(stockService.getAllStockSummaries());
    }

    @GetMapping
    public ResponseEntity<?> getAllStocks() {
        try {
            List<StockSecDto> stocks = stockService.getAllStocks();
            return ResponseEntity.ok(attachPermittedActions(stocks));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStockById(@PathVariable UUID id) {
        try {
            StockSecDto stock = stockService.getStockById(id);
            return ResponseEntity.ok(attachPermittedActions(stock));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<?> getStockByArticle(@PathVariable UUID articleId) {
        try {
            StockSecDto stock = stockService.getOrCreateStockByArticle(articleId);
            return ResponseEntity.ok(attachPermittedActions(stock));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/emplacement/{emplacementId}")
    public ResponseEntity<?> getStocksByEmplacement(@PathVariable UUID emplacementId) {
        try {
            List<StockSecDto> stocks = stockService.getStocksByEmplacement(emplacementId);
            return ResponseEntity.ok(attachPermittedActions(stocks));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/zone/{zone}")
    public ResponseEntity<?> getStocksByZone(@PathVariable String zone) {
        try {
            List<StockSecDto> stocks = stockService.getStocksByZone(zone);
            return ResponseEntity.ok(attachPermittedActions(stocks));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/emplacements-disponibles")
    public ResponseEntity<?> getStocksAvecEmplacementDisponible() {
        try {
            List<StockSecDto> stocks = stockService.getStocksAvecEmplacementDisponible();
            return ResponseEntity.ok(attachPermittedActions(stocks));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/article/{articleId}")
    public ResponseEntity<?> createStockForArticle(@PathVariable UUID articleId) {
        try {
            StockSecDto created = stockService.createStockForArticle(articleId);
            return new ResponseEntity<>(attachPermittedActions(created), HttpStatus.CREATED);
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
            return ResponseEntity.ok(attachPermittedActions(result));
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
            return ResponseEntity.ok(attachPermittedActions(result));
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
            return ResponseEntity.ok(attachPermittedActions(result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{articleId}/reserver")
    public ResponseEntity<?> reserverStock(@PathVariable UUID articleId, @RequestBody Map<String, Object> payload) {
        try {
            Integer quantite = (Integer) payload.get("quantite");
            StockSecDto result = stockService.reserverStock(articleId, quantite);
            return ResponseEntity.ok(attachPermittedActions(result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{articleId}/annuler-reservation")
    public ResponseEntity<?> annulerReservation(@PathVariable UUID articleId, @RequestBody Map<String, Object> payload) {
        try {
            Integer quantite = (Integer) payload.get("quantite");
            StockSecDto result = stockService.annulerReservation(articleId, quantite);
            return ResponseEntity.ok(attachPermittedActions(result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{articleId}/consommer-reservation")
    public ResponseEntity<?> consommerReservation(@PathVariable UUID articleId, @RequestBody Map<String, Object> payload) {
        try {
            Integer quantite = (Integer) payload.get("quantite");
            String motif = (String) payload.get("motif");
            String referenceType = payload.get("referenceType") != null ? payload.get("referenceType").toString() : null;
            UUID referenceId = payload.get("referenceId") != null
                    ? UUID.fromString(payload.get("referenceId").toString())
                    : null;
            StockSecDto result = stockService.consommerReservation(articleId, quantite, motif, referenceType, referenceId);
            return ResponseEntity.ok(attachPermittedActions(result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{stockId}/assigner-emplacement/{emplacementId}")
    public ResponseEntity<?> assignerEmplacement(@PathVariable UUID stockId, @PathVariable UUID emplacementId) {
        try {
            StockSecDto result = stockService.assignerEmplacement(stockId, emplacementId);
            return ResponseEntity.ok(attachPermittedActions(result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{stockId}/retirer-emplacement")
    public ResponseEntity<?> retirerEmplacement(@PathVariable UUID stockId) {
        try {
            StockSecDto result = stockService.retirerEmplacement(stockId);
            return ResponseEntity.ok(attachPermittedActions(result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{stockId}/transferer-emplacement/{nouvelEmplacementId}")
    public ResponseEntity<?> transfererEmplacement(@PathVariable UUID stockId, @PathVariable UUID nouvelEmplacementId) {
        try {
            StockSecDto result = stockService.transfererEmplacement(stockId, nouvelEmplacementId);
            return ResponseEntity.ok(attachPermittedActions(result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mouvements")
    public ResponseEntity<?> getAllMouvements() {
        try {
            List<MouvementStockSecDto> mouvements = stockService.getAllMouvementsDto();
            return ResponseEntity.ok(attachPermittedActions(mouvements, "MOUVEMENTSTOCKSEC", Set.of(Action.READ, Action.CREATE, Action.UPDATE, Action.DELETE)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @Transactional(readOnly = true)
    @GetMapping("/mouvements/article/{articleId}")
    public ResponseEntity<?> getMouvementsByArticle(@PathVariable UUID articleId) {
        try {
            List<MouvementStockSecDto> mouvements = stockService.getMouvementsByArticleDto(articleId);
            return ResponseEntity.ok(attachPermittedActions(mouvements, "MOUVEMENTSTOCKSEC", Set.of(Action.READ, Action.CREATE, Action.UPDATE, Action.DELETE)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected String getResourceName() {
        return "StockSec";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
