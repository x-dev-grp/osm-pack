package com.osm.inventory_service.controller;

import com.osm.inventory_service.Enum.Statue;
import com.osm.inventory_service.dto.LigneConditionnementDto;
import com.osm.inventory_service.entity.LigneConditionnement;
import com.osm.inventory_service.service.LigneConditionnementService;
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
@RequestMapping("/api/inventaire/lignes")
public class LigneConditionnementController extends BaseControllerImpl<LigneConditionnement, LigneConditionnementDto, LigneConditionnementDto> {

    private final LigneConditionnementService ligneService;

    @Autowired
    public LigneConditionnementController(BaseService<LigneConditionnement, LigneConditionnementDto, LigneConditionnementDto> baseService, ModelMapper modelMapper, LigneConditionnementService ligneService) {
        super(baseService, modelMapper);
        this.ligneService = ligneService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<LigneConditionnementDto> getLigneById(@PathVariable UUID id) {
        return ResponseEntity.ok(ligneService.getLigneById(id));
    }
    @GetMapping
    public ResponseEntity<List<LigneConditionnementDto>> getAllLignes() {
        return ResponseEntity.ok(ligneService.getAllLignes());
    }
    @PostMapping("/create")
    public ResponseEntity<LigneConditionnementDto> createLigne(@RequestBody LigneConditionnementDto ligneDto) {
        return new ResponseEntity<>(ligneService.createLigne(ligneDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LigneConditionnementDto> updateLigne(@PathVariable UUID id, @RequestBody LigneConditionnementDto ligneDto) {
        return ResponseEntity.ok(ligneService.updateLigne(id, ligneDto));
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverLigne(@PathVariable UUID id) {
        ligneService.desactiverLigne(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<Void> activerLigne(@PathVariable UUID id) {
        ligneService.activerLigne(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/changer-etat")
    public ResponseEntity<LigneConditionnementDto> changerEtat(
            @PathVariable UUID id,
            @RequestBody Map<String, Statue> payload) {
        Statue nouvelEtat = payload.get("etat");
        return ResponseEntity.ok(ligneService.changerEtat(id, nouvelEtat));
    }
    @GetMapping("/actifs")
    public ResponseEntity<List<LigneConditionnementDto>> getLignesActives() {
        return ResponseEntity.ok(ligneService.getLignesActives());
    }
    @Override
    protected String getResourceName() {
        return "LigneConditionnement";
    }
}