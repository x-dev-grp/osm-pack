package com.abiooc.inventory_service.controller;



import com.abiooc.inventory_service.dto.SKUDto;
import com.abiooc.inventory_service.entity.SKU;
import com.abiooc.inventory_service.service.SKUService;
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
@CrossOrigin(origins = "*")
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

    @GetMapping("/code/{code}")
    public ResponseEntity<SKUDto> getSkuByCode(@PathVariable String code) {
        return ResponseEntity.ok(skuService.getSkuByCode(code));
    }

    @PostMapping
    public ResponseEntity<SKUDto> createSku(@RequestBody SKUDto skuDto) {
        return new ResponseEntity<>(skuService.createSku(skuDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SKUDto> updateSku(@PathVariable UUID id, @RequestBody SKUDto skuDto) {
        return ResponseEntity.ok(skuService.updateSku(id, skuDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSku(@PathVariable UUID id) {
        skuService.deleteSku(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverSku(@PathVariable UUID id) {
        skuService.desactiverSku(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/activer")
    public ResponseEntity<Void> activerSku(@PathVariable UUID id) {
        skuService.activerSku(id);
        return ResponseEntity.ok().build();
    }



    @Override
    protected String getResourceName() {
        return "SKU";
    }
}