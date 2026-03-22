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
import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/boms")
public class BomController extends BaseControllerImpl<BOM, BOMDto, BOMDto> {

    private final BomService bomService;

    @Autowired
    public BomController(BaseService<BOM, BOMDto, BOMDto> baseService, ModelMapper modelMapper, BomService bomService) {
        super(baseService, modelMapper);
        this.bomService = bomService;
    }


    @GetMapping("/all")
    public ResponseEntity<List<BOMDto>> getAllBoms() {
        return ResponseEntity.ok(bomService.getAllBoms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BOMDto> getBomById(@PathVariable UUID id) {
        return ResponseEntity.ok(bomService.getBomById(id));
    }

    @GetMapping("/sku/{skuId}")
    public ResponseEntity<List<BOMDto>> getBomsBySku(@PathVariable UUID skuId) {
        return ResponseEntity.ok(bomService.getBomsBySku(skuId));
    }

    @PostMapping("/create")
    public ResponseEntity<BOMDto> createBom(@RequestBody BOMDto bomDto, @RequestParam(required = false, defaultValue = "system") String username) {
        BOMDto created = bomService.createBom(bomDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BOMDto> updateBom(@PathVariable UUID id, @RequestBody BOMDto bomDto) {
        BOMDto updated = bomService.updateBom(id, bomDto);
        return ResponseEntity.ok(updated);
    }
    @Override
    protected String getResourceName() {
        return "BOM";
    }

}