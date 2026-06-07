package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.ProduitFinalDto;
import com.osm.inventory_service.entity.ProduitFinal;
import com.osm.inventory_service.Enum.ProduitFinalType;
import com.osm.inventory_service.service.ProduitFinalService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.services.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/inventaire/produits-finis", "/api/inventaire/products", "/api/inventaire/skus"})
public class ProduitFinalController extends BaseControllerImpl<ProduitFinal, ProduitFinalDto, ProduitFinalDto> {

    private final ProduitFinalService produitFinalService;

    @Autowired
    public ProduitFinalController(BaseService<ProduitFinal, ProduitFinalDto, ProduitFinalDto> baseService,
                                  ModelMapper modelMapper,
                                  ProduitFinalService produitFinalService) {
        super(baseService, modelMapper);
        this.produitFinalService = produitFinalService;
    }

    @GetMapping
    public ResponseEntity<?> getAllProduitsFinaux() {
        try {
            List<ProduitFinalDto> produitsFinaux = produitFinalService.getAllProduitsFinaux();
            return ResponseEntity.ok(attachPermittedActions(produitsFinaux));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduitFinalById(@PathVariable UUID id) {
        try {
            ProduitFinalDto produitFinal = produitFinalService.getProduitFinalById(id);
            return ResponseEntity.ok(attachPermittedActions(produitFinal));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProduitFinal(@RequestBody ProduitFinalDto produitFinalDto) {
        try {
            ProduitFinalDto created = produitFinalService.createProduitFinal(produitFinalDto);
            return new ResponseEntity<>(attachPermittedActions(created), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduitFinal(@PathVariable UUID id, @RequestBody ProduitFinalDto produitFinalDto) {
        try {
            ProduitFinalDto updated = produitFinalService.updateProduitFinal(id, produitFinalDto);
            return ResponseEntity.ok(attachPermittedActions(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/actifs")
    public ResponseEntity<?> getActiveProduitsFinaux() {
        try {
            List<ProduitFinalDto> actifs = produitFinalService.getAllActiveProduitsFinaux();
            return ResponseEntity.ok(attachPermittedActions(actifs));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getProduitsFinauxByType(@PathVariable ProduitFinalType type) {
        try {
            return ResponseEntity.ok(attachPermittedActions(produitFinalService.getProduitsFinauxByType(type)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverProduitFinal(@PathVariable UUID id) {
        produitFinalService.desactiverProduitFinal(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerProduitFinal(@PathVariable UUID id) {
        try {
            produitFinalService.activerProduitFinal(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerProduitFinal(@PathVariable UUID id) {
        produitFinalService.supprimerProduitFinal(id);
        return ResponseEntity.ok().build();
    }

    @Override
    protected String getResourceName() {
        return "PRODUITFINAL";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        try {
            QrResolveResponse response = getBaseService().resolve(publicCode);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
