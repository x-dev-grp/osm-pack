package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.BOMDto;
import com.osm.inventory_service.entity.BOM;
import com.osm.inventory_service.service.BomService;
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
@RequestMapping("/api/inventaire/boms")
public class BomController extends BaseControllerImpl<BOM, BOMDto, BOMDto> {

    private final BomService bomService;

    @Autowired
    public BomController(BaseService<BOM, BOMDto, BOMDto> baseService,
                         ModelMapper modelMapper,
                         BomService bomService) {
        super(baseService, modelMapper);
        this.bomService = bomService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBoms() {
        try {
            List<BOMDto> boms = bomService.getAllBoms();
            return ResponseEntity.ok(boms);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBomById(@PathVariable UUID id) {
        try {
            BOMDto bom = bomService.getBomById(id);
            return ResponseEntity.ok(bom);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/sku/{skuId}")
    public ResponseEntity<?> getBomsBySku(@PathVariable UUID skuId) {
        try {
            List<BOMDto> boms = bomService.getBomsBySku(skuId);
            return ResponseEntity.ok(boms);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBom(@RequestBody BOMDto bomDto,
                                       @RequestParam(required = false, defaultValue = "system") String username) {
        try {
            BOMDto created = bomService.createBom(bomDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBom(@PathVariable UUID id, @RequestBody BOMDto bomDto) {
        try {
            BOMDto updated = bomService.updateBom(id, bomDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected String getResourceName() {
        return "BOM";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}