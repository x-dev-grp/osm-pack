package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.SKUDto;
import com.osm.inventory_service.entity.SKU;
import com.osm.inventory_service.service.SKUService;
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
@RequestMapping("/api/inventaire/skus")
public class SKUController extends BaseControllerImpl<SKU, SKUDto, SKUDto> {

    private final SKUService skuService;

    @Autowired
    public SKUController(BaseService<SKU, SKUDto, SKUDto> baseService,
                         ModelMapper modelMapper,
                         SKUService skuService) {
        super(baseService, modelMapper);
        this.skuService = skuService;
    }

    @GetMapping
    public ResponseEntity<?> getAllSkus() {
        try {
            List<SKUDto> skus = skuService.getAllSkus();
            return ResponseEntity.ok(skus);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSkuById(@PathVariable UUID id) {
        try {
            SKUDto sku = skuService.getSkuById(id);
            return ResponseEntity.ok(sku);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSku(@RequestBody SKUDto skuDto) {
        try {
            SKUDto created = skuService.createSku(skuDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSku(@PathVariable UUID id, @RequestBody SKUDto skuDto) {
        try {
            SKUDto updated = skuService.updateSku(id, skuDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/actifs")
    public ResponseEntity<?> getActiveSKUs() {
        try {
            List<SKUDto> actifs = skuService.getAllActiveSKUs();
            return ResponseEntity.ok(actifs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverSku(@PathVariable UUID id) {
        try {
            skuService.desactiverSku(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerSku(@PathVariable UUID id) {
        try {
            skuService.activerSku(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected String getResourceName() {
        return "SKU";
    }
}