package com.osm.inventory_service.controller;

import com.osm.inventory_service.Enum.StatutBonCommande;
import com.osm.inventory_service.dto.BonCommandeDto;
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
            return ResponseEntity.ok(new ApiResponse<>(true, "Bons commande retrieved successfully", bonsCommande));
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
            return ResponseEntity.ok(new ApiResponse<>(true, "Bon commande retrieved successfully", null));
        } catch (Exception e) {
            logger.warn("Bon commande not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> getBonsCommandeByStatut(@PathVariable StatutBonCommande statut) {
        logger.info("Fetching bons commande with statut: {}", statut);
        try {
            if (statut == null) {
                throw new BadRequestException("Statut cannot be null");
            }
            List<BonCommandeDto> bonsCommande = bonCommandeService.getBonsCommandeByStatut(statut);
            logger.debug("Successfully fetched {} bons commande with statut: {}", bonsCommande.size(), statut);
            return ResponseEntity.ok(new ApiResponse<>(true,
                    "Bons commande with statut " + statut + " retrieved successfully", bonsCommande));
        } catch (BadRequestException e) {
            logger.warn("Invalid request for statut: {}", statut);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error fetching bons commande by statut {}: {}", statut, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving bons commande by statut: " + e.getMessage(), null));
        }
    }

    @GetMapping("/en-attente")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> getBonsEnAttente() {
        logger.info("Fetching pending bons commande");
        try {
            List<BonCommandeDto> bonsCommande = bonCommandeService.getBonsCommandeByStatut(StatutBonCommande.EN_ATTENTE);
            logger.debug("Successfully fetched {} pending bons commande", bonsCommande.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "Pending bons commande retrieved successfully", bonsCommande));
        } catch (Exception e) {
            logger.error("Error fetching pending bons commande: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving pending bons commande: " + e.getMessage(), null));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> createBonCommande(@Valid @RequestBody BonCommandeDto bonCommandeDto) {
        logger.info("Creating new bon commande");
        try {
            validateBonCommandeDto(bonCommandeDto);
            BonCommandeDto created = bonCommandeService.createBonCommande(bonCommandeDto);
            logger.info("Successfully created bon commande with id: {}", created.getId());
            return new ResponseEntity<>(new ApiResponse<>(true, "Bon commande created successfully", null),
                    HttpStatus.CREATED);
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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> updateBonCommande(
            @PathVariable UUID id,
            @Valid @RequestBody BonCommandeDto bonCommandeDto) {
        logger.info("Updating bon commande with id: {}", id);
        try {
            validateBonCommandeDto(bonCommandeDto);
            BonCommandeDto updated = bonCommandeService.updateBonCommande(id, bonCommandeDto);
            logger.info("Successfully updated bon commande with id: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bon commande updated successfully", null));
        } catch (NotFoundException e) {
            logger.warn("Bon commande not found for update with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (ValidationException | BadRequestException e) {
            logger.warn("Validation error updating bon commande with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error updating bon commande with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating bon commande: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteBonCommande(@PathVariable UUID id) {
        logger.info("Deleting bon commande with id: {}", id);
        try {
            bonCommandeService.deleteBonCommande(id);
            logger.info("Successfully deleted bon commande with id: {}", id);
            return ResponseEntity.ok(true);
        } catch (NotFoundException e) {
            logger.warn("Bon commande not found for deletion with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(false);
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
    public ResponseEntity<ApiResponse<BonCommande, BonCommandeDto>> receptionnerCommande(
            @PathVariable UUID id,
            @RequestBody Map<String, Integer> payload) {
        logger.info("Receiving bon commande with id: {}", id);
        try {
            Integer quantite = payload.get("quantite");
            if (quantite == null || quantite < 0) {
                throw new BadRequestException("Valid quantity is required for reception");
            }
            BonCommandeDto received = bonCommandeService.receptionnerCommande(id, quantite);
            logger.info("Successfully received bon commande with id: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bon commande received successfully", null));
        } catch (NotFoundException e) {
            logger.warn("Bon commande not found for reception with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (BadRequestException e) {
            logger.warn("Invalid reception request for bon commande with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error receiving bon commande with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error receiving bon commande: " + e.getMessage(), null));
        }
    }



    private void validateBonCommandeDto(BonCommandeDto dto) {
        if (dto == null) {
            throw new ValidationException("Bon commande cannot be null");
        }
        // Add more specific validation as needed
        if (dto.getNumeroBC() == null || dto.getNumeroBC().trim().isEmpty()) {
            throw new ValidationException("Bon commande number is required");
        }
        if (dto.getDateCreation() == null) {
            throw new ValidationException("Commande date is required");
        }
        if (dto.getFournisseur() == null) {
            throw new ValidationException("Fournisseur ID is required");
        }
    }

    @Override
    protected String getResourceName() {
        return "BonCommande";
    }
}