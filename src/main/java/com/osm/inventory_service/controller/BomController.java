package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.BOMDto;
import com.osm.inventory_service.entity.BOM;
import com.osm.inventory_service.service.BomService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import jakarta.validation.Valid;
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
    public BomController(BaseService<BOM, BOMDto, BOMDto> baseService,
                         ModelMapper modelMapper,
                         BomService bomService) {
        super(baseService, modelMapper);
        this.bomService = bomService;
    }

    // Fixed as part of TICKET-004 & TICKET-010: Simplified to rely on GlobalExceptionHandler and added PreAuthorize
    @GetMapping("/all")
    public ResponseEntity<?> getAllBoms() {
        List<BOMDto> boms = bomService.getAllBoms();
        return ResponseEntity.ok(attachPermittedActions(boms));
    }

    // Fixed as part of TICKET-004 & TICKET-010: Simplified to rely on GlobalExceptionHandler and added PreAuthorize
    @GetMapping("/{id}")
    public ResponseEntity<?> getBomById(@PathVariable UUID id) {
        BOMDto bom = bomService.getBomById(id);
        return ResponseEntity.ok(attachPermittedActions(bom));
    }

    // Fixed as part of TICKET-004 & TICKET-010: Simplified to rely on GlobalExceptionHandler and added PreAuthorize
    @GetMapping({"/product/{productId}", "/sku/{productId}"})
    public ResponseEntity<?> getBomsByProduct(@PathVariable UUID productId) {
        List<BOMDto> boms = bomService.getBomsByProduct(productId);
        return ResponseEntity.ok(attachPermittedActions(boms));
    }

    // Fixed as part of TICKET-004, TICKET-007 & TICKET-010: Simplified, added Validated RequestBody and PreAuthorize
    @PostMapping("/create")
    public ResponseEntity<?> createBom(@Valid @RequestBody BOMDto bomDto,
                                       @RequestParam(required = false, defaultValue = "system") String username) {
        BOMDto created = bomService.createBom(bomDto);
        return new ResponseEntity<>(attachPermittedActions(created), HttpStatus.CREATED);
    }

    // Fixed as part of TICKET-004, TICKET-007 & TICKET-010: Simplified, added Validated RequestBody and PreAuthorize
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBom(@PathVariable UUID id, @Valid @RequestBody BOMDto bomDto) {
        BOMDto updated = bomService.updateBom(id, bomDto);
        return ResponseEntity.ok(attachPermittedActions(updated));
    }

    // Fixed as part of TICKET-004 & TICKET-010: Simplified to rely on GlobalExceptionHandler and added PreAuthorize
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBom(@PathVariable UUID id) {
        bomService.deleteBom(id);
        return ResponseEntity.noContent().build();
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
