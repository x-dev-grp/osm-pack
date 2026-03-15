package com.abiooc.inventory_service.controller;

import com.abiooc.inventory_service.Enum.CategorieFournisseur;
import com.abiooc.inventory_service.dto.FournisseurDto;
import com.abiooc.inventory_service.entity.Fournisseur;
import com.abiooc.inventory_service.service.FournisseurService;
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
@CrossOrigin(origins = "*")
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

    @GetMapping("/code/{code}")
    public ResponseEntity<FournisseurDto> getFournisseurByCode(@PathVariable String code) {
        return ResponseEntity.ok(fournisseurService.getFournisseurByCode(code));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<FournisseurDto> getFournisseurByEmail(@PathVariable String email) {
        return ResponseEntity.ok(fournisseurService.getFournisseurByEmail(email));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<FournisseurDto>> rechercherFournisseurs(@RequestParam String q) {
        return ResponseEntity.ok(fournisseurService.rechercherFournisseurs(q));
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<FournisseurDto>> getFournisseursByCategorie(@PathVariable CategorieFournisseur categorie) {
        return ResponseEntity.ok(fournisseurService.getFournisseursByCategorie(categorie));
    }


    @GetMapping("/pays/{pays}")
    public ResponseEntity<List<FournisseurDto>> getFournisseursByPays(@PathVariable String pays) {
        return ResponseEntity.ok(fournisseurService.getFournisseursByPays(pays));
    }

    @PostMapping
    public ResponseEntity<FournisseurDto> createFournisseur(@RequestBody FournisseurDto fournisseurDto) {
        return new ResponseEntity<>(fournisseurService.createFournisseur(fournisseurDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FournisseurDto> updateFournisseur(@PathVariable UUID id, @RequestBody FournisseurDto fournisseurDto) {
        return ResponseEntity.ok(fournisseurService.updateFournisseur(id, fournisseurDto));
    }

    @PatchMapping("/{id}/activer")
    public ResponseEntity<FournisseurDto> activerFournisseur(@PathVariable UUID id) {
        return ResponseEntity.ok(fournisseurService.activerFournisseur(id));
    }

    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<FournisseurDto> desactiverFournisseur(@PathVariable UUID id) {
        return ResponseEntity.ok(fournisseurService.desactiverFournisseur(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFournisseur(@PathVariable UUID id) {
        fournisseurService.deleteFournisseur(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    protected String getResourceName() {
        return "Fournisseur";
    }
}