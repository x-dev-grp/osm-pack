package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.BonCommandeDto;
import com.osm.inventory_service.dto.LigneBonCommandeDto;
import com.osm.inventory_service.entity.BonCommande;
import com.osm.inventory_service.service.BonCommandeService;
import com.xdev.xdevbase.apiDTOs.ApiResponse;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventaire/bons-commande")
public class BonCommandeController extends BaseControllerImpl<BonCommande, BonCommandeDto, BonCommandeDto> {

    private static final Logger logger = LoggerFactory.getLogger(BonCommandeController.class);
    private final BonCommandeService bonCommandeService;

    @Autowired
    public BonCommandeController(BaseService<BonCommande, BonCommandeDto, BonCommandeDto> baseService,
                                 ModelMapper modelMapper,
                                 BonCommandeService bonCommandeService) {
        super(baseService, modelMapper);
        this.bonCommandeService = bonCommandeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> getAllBonsCommande() {
        logger.info("Fetching all bons commande");
        try {
            List<BonCommandeDto> bonsCommande = bonCommandeService.getAllBonsCommande();
            logger.debug("Successfully fetched {} bons commande", bonsCommande.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "Bons commande retrieved successfully", attachPermittedActions(bonsCommande)));
        } catch (Exception e) {
            logger.error("Error fetching all bons commande: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving bons commande: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> getBonCommandeById(@PathVariable UUID id) {
        logger.info("Fetching bon commande with id: {}", id);
        try {
            BonCommandeDto bonCommande = bonCommandeService.getBonCommandeById(id);
            logger.debug("Successfully fetched bon commande with id: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bon commande retrieved successfully", List.of(attachPermittedActions(bonCommande))));
        } catch (Exception e) {
            logger.warn("Bon commande not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> createBonCommande(
            @Valid @RequestBody BonCommandeDto bonCommandeDto) {

        logger.info("Creating new bon commande");

        try {

            validateBonCommandeDto(bonCommandeDto);

            BonCommandeDto created = bonCommandeService.createBonCommande(bonCommandeDto);

            logger.info("Successfully created bon commande with id: {}", created.getId());

            return new ResponseEntity<>(
                    new ApiResponse<>(true, "Bon commande created successfully", List.of(attachPermittedActions(created))),
                    HttpStatus.CREATED
            );

        } catch (ValidationException | BadRequestException e) {

            logger.warn("Validation error creating bon commande: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));

        } catch (Exception e) {

            logger.error("Error creating bon commande: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error creating bon commande: " + e.getMessage(), null));
        }
    }

    @PostMapping("/{id}/valider")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> validerBonCommande(@PathVariable UUID id) {
        logger.info("Validating bon commande with id: {}", id);
        try {
            BonCommandeDto validated = bonCommandeService.validerBonCommande(id);
            logger.info("Successfully validated bon commande with id: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bon commande validated successfully", null));
        } catch (NotFoundException e) {
            logger.warn("Bon commande not found for validation with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (BadRequestException e) {
            logger.warn("Invalid validation request for bon commande with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error validating bon commande with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error validating bon commande: " + e.getMessage(), null));
        }
    }

    @PostMapping("/{id}/refuser")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> refuserBonCommande(
            @PathVariable UUID id,
            @RequestBody Map<String, String> payload) {
        logger.info("Rejecting bon commande with id: {}", id);
        try {
            String motif = payload.get("motif");
            if (motif == null || motif.trim().isEmpty()) {
                throw new BadRequestException("Motif is required for rejection");
            }
            BonCommandeDto rejected = bonCommandeService.refuserBonCommande(id, motif);
            logger.info("Successfully rejected bon commande with id: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bon commande rejected successfully", null));
        } catch (NotFoundException e) {
            logger.warn("Bon commande not found for rejection with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (BadRequestException e) {
            logger.warn("Invalid rejection request for bon commande with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error rejecting bon commande with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error rejecting bon commande: " + e.getMessage(), null));
        }
    }

    @PostMapping("/{id}/receptionner")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> receptionnerLignesBon(
            @PathVariable UUID id,
            @RequestBody List<LigneBonCommandeDto> lignesRecues) {
        try {
            BonCommandeDto bc = bonCommandeService.receptionnerCommande(id, lignesRecues);
            return ResponseEntity.ok(new ApiResponse<>(true, "Réception enregistrée avec succès", List.of(attachPermittedActions(bc))));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    private void validateBonCommandeDto(BonCommandeDto dto) {
        if (dto == null) {
            throw new ValidationException("Bon commande cannot be null");
        }
        if (dto.getLignes() == null || dto.getLignes().isEmpty()) {
            throw new ValidationException("Au moins une ligne est requise");
        }
    }

    @Override
    protected String getResourceName() {
        return "BonCommande";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}