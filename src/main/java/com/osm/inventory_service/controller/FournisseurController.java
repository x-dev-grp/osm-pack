package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.FournisseurDto;
import com.osm.inventory_service.entity.Fournisseur;
import com.osm.inventory_service.service.FournisseurService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.services.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/fournisseurs")
public class FournisseurController extends BaseControllerImpl<Fournisseur, FournisseurDto, FournisseurDto> {

    private final FournisseurService fournisseurService;

    @Autowired
    public FournisseurController(BaseService<Fournisseur, FournisseurDto, FournisseurDto> baseService,
                                 ModelMapper modelMapper,
                                 FournisseurService fournisseurService) {
        super(baseService, modelMapper);
        this.fournisseurService = fournisseurService;
    }
    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<FournisseurDto>> getAllFournisseurs() {
        return ResponseEntity.ok(attachPermittedActions(fournisseurService.getAllFournisseurs()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FournisseurDto> getFournisseurById(@PathVariable UUID id) {
        return ResponseEntity.ok(attachPermittedActions(fournisseurService.getFournisseurById(id)));
    }
    @Transactional(readOnly = true)
    @GetMapping("/actifs")
    public ResponseEntity<List<FournisseurDto>> getActiveFournisseurs() {
        return ResponseEntity.ok(attachPermittedActions(fournisseurService.getActiveFournisseurs()));
    }

    @PostMapping("/create")
    public ResponseEntity<FournisseurDto> createFournisseur(@RequestBody FournisseurDto fournisseurDto) {
        return new ResponseEntity<>(attachPermittedActions(fournisseurService.createFournisseur(fournisseurDto)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFournisseur(@PathVariable UUID id, @RequestBody FournisseurDto fournisseurDto) {
        try {
            FournisseurDto updated = fournisseurService.updateFournisseur(id, fournisseurDto);
            return ResponseEntity.ok(attachPermittedActions(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<FournisseurDto> activerFournisseur(@PathVariable UUID id) {
        return ResponseEntity.ok(attachPermittedActions(fournisseurService.activerFournisseur(id)));
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<FournisseurDto> desactiverFournisseur(@PathVariable UUID id) {
        return ResponseEntity.ok(attachPermittedActions(fournisseurService.desactiverFournisseur(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerFournisseur(@PathVariable UUID id) {
        fournisseurService.supprimerFournisseur(id);
        return ResponseEntity.ok().build();
    }

    @Override
    protected String getResourceName() {
        return "Fournisseur";
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