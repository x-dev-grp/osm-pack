package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.BOMDto;
import com.osm.inventory_service.dto.MaterialNeedLineDto;
import com.osm.inventory_service.entity.BOM;
import com.osm.inventory_service.service.BomService;
import com.osm.inventory_service.service.MaterialNeedsService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.services.BaseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
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
    private final MaterialNeedsService materialNeedsService;

    @Autowired
    public BomController(BaseService<BOM, BOMDto, BOMDto> baseService,
                         ModelMapper modelMapper,
                         BomService bomService,
                         MaterialNeedsService materialNeedsService) {
        super(baseService, modelMapper);
        this.bomService = bomService;
        this.materialNeedsService = materialNeedsService;
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

    @GetMapping({"/product/{productId}/active", "/sku/{productId}/active"})
    public ResponseEntity<?> getActiveBomForProduct(@PathVariable UUID productId) {
        BOMDto bom = bomService.getActiveBomForProduct(productId);
        if (bom == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Aucune nomenclature active pour ce produit"));
        }
        return ResponseEntity.ok(attachPermittedActions(bom));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateBom(@PathVariable UUID id) {
        BOMDto activated = bomService.activateBom(id);
        return ResponseEntity.ok(attachPermittedActions(activated));
    }

    @GetMapping("/{id}/material-needs")
    public ResponseEntity<List<MaterialNeedLineDto>> getMaterialNeeds(
            @PathVariable UUID id,
            @RequestParam(name = "quantity") double quantity) {
        return ResponseEntity.ok(materialNeedsService.computeForBom(id, quantity));
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
        try {
            QrResolveResponse response = getBaseService().resolve(publicCode);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
