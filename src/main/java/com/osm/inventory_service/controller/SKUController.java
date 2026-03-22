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
import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/skus")
public class SKUController extends BaseControllerImpl<SKU, SKUDto, SKUDto> {

    private final SKUService skuService;

    @Autowired
    public SKUController(BaseService<SKU, SKUDto, SKUDto> baseService, ModelMapper modelMapper, SKUService skuService) {
        super(baseService, modelMapper);
        this.skuService = skuService;
    }

    @GetMapping
    public ResponseEntity<List<SKUDto>> getAllSkus() {
        return ResponseEntity.ok(skuService.getAllSkus());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SKUDto> getSkuById(@PathVariable UUID id) {
        return ResponseEntity.ok(skuService.getSkuById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<SKUDto> createSku(@RequestBody SKUDto skuDto) {
        return new ResponseEntity<>(skuService.createSku(skuDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SKUDto> updateSku(@PathVariable UUID id, @RequestBody SKUDto skuDto) {
        return ResponseEntity.ok(skuService.updateSku(id, skuDto));
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<SKUDto>> getActiveSKUs() {
        return ResponseEntity.ok(skuService.getAllActiveSKUs());
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverSku(@PathVariable UUID id) {
        skuService.desactiverSku(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<Void> activerSku(@PathVariable UUID id) {
        skuService.activerSku(id);
        return ResponseEntity.ok().build();
    }



    @Override
    protected String getResourceName() {
        return "SKU";
    }
}