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
    public LigneConditionnementController(BaseService<LigneConditionnement, LigneConditionnementDto, LigneConditionnementDto> baseService,
                                          ModelMapper modelMapper,
                                          LigneConditionnementService ligneService) {
        super(baseService, modelMapper);
        this.ligneService = ligneService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLigneById(@PathVariable UUID id) {
        try {
            LigneConditionnementDto ligne = ligneService.getLigneById(id);
            return ResponseEntity.ok(ligne);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllLignes() {
        try {
            List<LigneConditionnementDto> lignes = ligneService.getAllLignes();
            return ResponseEntity.ok(lignes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createLigne(@RequestBody LigneConditionnementDto ligneDto) {
        try {
            LigneConditionnementDto created = ligneService.createLigne(ligneDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLigne(@PathVariable UUID id, @RequestBody LigneConditionnementDto ligneDto) {
        try {
            LigneConditionnementDto updated = ligneService.updateLigne(id, ligneDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverLigne(@PathVariable UUID id) {
        try {
            ligneService.desactiverLigne(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerLigne(@PathVariable UUID id) {
        try {
            ligneService.activerLigne(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/changer-etat")
    public ResponseEntity<?> changerEtat(@PathVariable UUID id, @RequestBody Map<String, Statue> payload) {
        try {
            Statue nouvelEtat = payload.get("etat");
            LigneConditionnementDto updated = ligneService.changerEtat(id, nouvelEtat);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/actifs")
    public ResponseEntity<?> getLignesActives() {
        try {
            List<LigneConditionnementDto> actives = ligneService.getLignesActives();
            return ResponseEntity.ok(actives);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected String getResourceName() {
        return "LigneConditionnement";
    }
}