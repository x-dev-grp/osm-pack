package com.abiooc.inventory_service.controller;

import com.abiooc.inventory_service.Enum.Statue;
import com.abiooc.inventory_service.dto.LigneConditionnementDto;
import com.abiooc.inventory_service.entity.LigneConditionnement;
import com.abiooc.inventory_service.service.LigneConditionnementService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/lignes")
@CrossOrigin(origins = "*")
public class LigneConditionnementController extends BaseControllerImpl<LigneConditionnement, LigneConditionnementDto, LigneConditionnementDto> {

    private final LigneConditionnementService ligneService;

    @Autowired
    public LigneConditionnementController(BaseService<LigneConditionnement, LigneConditionnementDto, LigneConditionnementDto> baseService, ModelMapper modelMapper, LigneConditionnementService ligneService) {
        super(baseService, modelMapper);
        this.ligneService = ligneService;
    }



    @GetMapping
    public ResponseEntity<List<LigneConditionnementDto>> getAllLignes() {
        return ResponseEntity.ok(ligneService.getAllLignes());
    }


    @GetMapping("/{id}")
    public ResponseEntity<LigneConditionnementDto> getLigneById(@PathVariable UUID id) {
        return ResponseEntity.ok(ligneService.getLigneById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<LigneConditionnementDto> getLigneByCode(@PathVariable String code) {
        return ResponseEntity.ok(ligneService.getLigneByCode(code));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<LigneConditionnementDto>> rechercherLignes(@RequestParam String q) {
        return ResponseEntity.ok(ligneService.rechercherLignes(q));
    }

    @GetMapping("/etat/{etat}")
    public ResponseEntity<List<LigneConditionnementDto>> getLignesParEtat(@PathVariable Statue etat) {
        return ResponseEntity.ok(ligneService.getLignesParEtat(etat));
    }

    @PostMapping
    public ResponseEntity<LigneConditionnementDto> createLigne(@RequestBody LigneConditionnementDto ligneDto) {
        return new ResponseEntity<>(ligneService.createLigne(ligneDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LigneConditionnementDto> updateLigne(@PathVariable UUID id, @RequestBody LigneConditionnementDto ligneDto) {
        return ResponseEntity.ok(ligneService.updateLigne(id, ligneDto));
    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverLigne(@PathVariable UUID id) {
        ligneService.desactiverLigne(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/activer")
    public ResponseEntity<Void> activerLigne(@PathVariable UUID id) {
        ligneService.activerLigne(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/changer-etat")
    public ResponseEntity<LigneConditionnementDto> changerEtat(
            @PathVariable UUID id,
            @RequestBody Map<String, Statue> payload) {
        Statue nouvelEtat = payload.get("etat");
        return ResponseEntity.ok(ligneService.changerEtat(id, nouvelEtat));
    }

    @PatchMapping("/{id}/maintenance")
    public ResponseEntity<LigneConditionnementDto> mettreEnMaintenance(
            @PathVariable UUID id,
            @RequestBody Map<String, String> payload) {

        LocalDateTime dateProchaineMaintenance = payload.containsKey("dateProchaineMaintenance")
                ? LocalDateTime.parse(payload.get("dateProchaineMaintenance"))
                : LocalDateTime.now().plusMonths(1);

        return ResponseEntity.ok(ligneService.mettreEnMaintenance(id, dateProchaineMaintenance));
    }

    @PatchMapping("/{id}/remettre-en-service")
    public ResponseEntity<LigneConditionnementDto> remettreEnService(@PathVariable UUID id) {
        return ResponseEntity.ok(ligneService.remettreEnService(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLigne(@PathVariable UUID id) {
        ligneService.deleteLigne(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    protected String getResourceName() {
        return "LigneConditionnement";
    }
}