package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.FournisseurDto;
import com.osm.inventory_service.entity.Fournisseur;
import com.osm.inventory_service.service.FournisseurService;
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

    @GetMapping
    public ResponseEntity<List<FournisseurDto>> getAllFournisseurs() {
        return ResponseEntity.ok(fournisseurService.getAllFournisseurs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FournisseurDto> getFournisseurById(@PathVariable UUID id) {
        return ResponseEntity.ok(fournisseurService.getFournisseurById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<FournisseurDto> createFournisseur(@RequestBody FournisseurDto fournisseurDto) {
        return new ResponseEntity<>(fournisseurService.createFournisseur(fournisseurDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFournisseur(@PathVariable UUID id, @RequestBody FournisseurDto fournisseurDto) {
        try {
            FournisseurDto updated = fournisseurService.updateFournisseur(id, fournisseurDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<FournisseurDto> activerFournisseur(@PathVariable UUID id) {
        return ResponseEntity.ok(fournisseurService.activerFournisseur(id));
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<FournisseurDto> desactiverFournisseur(@PathVariable UUID id) {
        return ResponseEntity.ok(fournisseurService.desactiverFournisseur(id));
    }
    @Override
    protected String getResourceName() {
        return "Fournisseur";
    }
}