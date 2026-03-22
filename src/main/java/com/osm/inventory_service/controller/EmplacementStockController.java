package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.EmplacementStockDto;
import com.osm.inventory_service.entity.EmplacementStock;
import com.osm.inventory_service.service.EmplacementStockService;
import com.xdev.xdevbase.apiDTOs.ApiResponse;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.utils.OSMLogger;
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
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/inventaire/emplacements")
public class EmplacementStockController extends BaseControllerImpl<EmplacementStock, EmplacementStockDto, EmplacementStockDto> {

    private static final Logger log = LoggerFactory.getLogger(EmplacementStockController.class);
    private final EmplacementStockService emplacementService;

    @Autowired
    public EmplacementStockController(BaseService<EmplacementStock, EmplacementStockDto, EmplacementStockDto> baseService,
                                      ModelMapper modelMapper,
                                      EmplacementStockService emplacementService) {
        super(baseService, modelMapper);
        this.emplacementService = emplacementService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EmplacementStock, EmplacementStockDto>> getAllEmplacements() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getAllEmplacements");
        try {
            List<EmplacementStockDto> emplacements = emplacementService.getAllEmplacements();
            log.debug("Successfully fetched {} emplacements", emplacements.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "Emplacements retrieved successfully", emplacements));
        } catch (Exception e) {
            log.error("Error fetching all emplacements: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving emplacements: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "getAllEmplacements");
            OSMLogger.logPerformance(this.getClass(), "getAllEmplacements", startTime, System.currentTimeMillis());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmplacementStock, EmplacementStockDto>> getEmplacementById(@PathVariable UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getEmplacementById", id);
        try {
            EmplacementStockDto emplacement = emplacementService.getEmplacementById(id);
            log.debug("Successfully fetched emplacement with id: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Emplacement retrieved successfully", Collections.singletonList(emplacement)));
        } catch ( NotFoundException e) {
            log.warn("Emplacement not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error fetching emplacement with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving emplacement: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "getEmplacementById");
            OSMLogger.logPerformance(this.getClass(), "getEmplacementById", startTime, System.currentTimeMillis());
        }
    }
    @PutMapping("/{id}/activer")
    public ResponseEntity<ApiResponse<EmplacementStock, EmplacementStockDto>> activerEmplacement(@PathVariable UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "activerEmplacement", id);
        try {
            EmplacementStockDto dto = emplacementService.activerEmplacement(id);
            log.info("Emplacement activé avec id: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Emplacement activé avec succès", Arrays.asList(dto)));
        } catch (NotFoundException e) {
            log.warn("Emplacement non trouvé pour activation: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Erreur lors de l'activation de l'emplacement {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur lors de l'activation: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "activerEmplacement");
            OSMLogger.logPerformance(this.getClass(), "activerEmplacement", startTime, System.currentTimeMillis());
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<ApiResponse<EmplacementStock, EmplacementStockDto>> desactiverEmplacement(@PathVariable UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "desactiverEmplacement", id);
        try {
            EmplacementStockDto dto = emplacementService.desactiverEmplacement(id);
            log.info("Emplacement désactivé avec id: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Emplacement désactivé avec succès", Arrays.asList(dto)));
        } catch (NotFoundException e) {
            log.warn("Emplacement non trouvé pour désactivation: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Erreur lors de la désactivation de l'emplacement {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Erreur lors de la désactivation: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "desactiverEmplacement");
            OSMLogger.logPerformance(this.getClass(), "desactiverEmplacement", startTime, System.currentTimeMillis());
        }
    }

    @GetMapping("/reserves/{client}")
    public ResponseEntity<ApiResponse<EmplacementStock, EmplacementStockDto>> getEmplacementsReservesPour(@PathVariable String client) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getEmplacementsReservesPour", client);
        try {
            if (client == null || client.trim().isEmpty()) {
                throw new BadRequestException("Client cannot be empty");
            }
            List<EmplacementStockDto> emplacements = emplacementService.getEmplacementsReservesPour(client);
            log.debug("Successfully fetched {} reserved emplacements for client: {}", emplacements.size(), client);
            return ResponseEntity.ok(new ApiResponse<>(true, "Reserved emplacements retrieved successfully", emplacements));
        } catch (BadRequestException e) {
            log.warn("Bad request for client: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error fetching reserved emplacements for client {}: {}", client, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving reserved emplacements: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "getEmplacementsReservesPour");
            OSMLogger.logPerformance(this.getClass(), "getEmplacementsReservesPour", startTime, System.currentTimeMillis());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<EmplacementStock, EmplacementStockDto>> createEmplacement(@Valid @RequestBody EmplacementStockDto emplacementDto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createEmplacement");
        try {
             EmplacementStockDto created = emplacementService.createEmplacement(emplacementDto);
            log.info("Successfully created emplacement with id: {}", created.getId());
            return new ResponseEntity<>(new ApiResponse<>(true, "Emplacement created successfully", Arrays.asList(created)),
                    HttpStatus.CREATED);
        } catch (ValidationException | BadRequestException e) {
            log.warn("Validation error creating emplacement: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            log.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Error creating emplacement: " + e.getMessage(), null));
        } catch (HttpMessageNotWritableException e) {
            log.error("Serialization error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Error serializing response: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error creating emplacement: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Unexpected error creating emplacement: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "createEmplacement");
            OSMLogger.logPerformance(this.getClass(), "createEmplacement", startTime, System.currentTimeMillis());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmplacementStock, EmplacementStockDto>> updateEmplacement(
            @PathVariable UUID id,
            @Valid @RequestBody EmplacementStockDto emplacementDto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "updateEmplacement", id);
        try {
             EmplacementStockDto updated = emplacementService.updateEmplacement(id, emplacementDto);
            log.info("Successfully updated emplacement with id: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Emplacement updated successfully", Arrays.asList(updated)));
        } catch (NotFoundException e) {
            log.warn("Emplacement not found for update with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (ValidationException | BadRequestException e) {
            log.warn("Validation error updating emplacement with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            log.error("Bad request for emplacement update {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Error updating emplacement: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error updating emplacement with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating emplacement: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "updateEmplacement");
            OSMLogger.logPerformance(this.getClass(), "updateEmplacement", startTime, System.currentTimeMillis());
        }
    }

    @PutMapping("/{id}/reserver")
    public ResponseEntity<ApiResponse<EmplacementStock, EmplacementStockDto>> reserverEmplacement(
            @PathVariable UUID id,
            @RequestBody Map<String, String> payload) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "reserverEmplacement", id);
        try {
            String reservePour = payload.get("reservePour");
            if (reservePour == null || reservePour.trim().isEmpty()) {
                throw new BadRequestException("reservePour is required for reservation");
            }
            EmplacementStockDto reserved = emplacementService.reserverEmplacement(id, reservePour);
            log.info("Successfully reserved emplacement with id: {} for: {}", id, reservePour);
            return ResponseEntity.ok(new ApiResponse<>(true, "Emplacement reserved successfully", Arrays.asList(reserved)));
        } catch (NotFoundException e) {
            log.warn("Emplacement not found for reservation with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (BadRequestException e) {
            log.warn("Bad request for emplacement reservation {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error reserving emplacement with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error reserving emplacement: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "reserverEmplacement");
            OSMLogger.logPerformance(this.getClass(), "reserverEmplacement", startTime, System.currentTimeMillis());
        }
    }

    @PutMapping("/{id}/liberer")
    public ResponseEntity<ApiResponse<EmplacementStock, EmplacementStockDto>> libererEmplacement(@PathVariable UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "libererEmplacement", id);
        try {
            EmplacementStockDto liberated = emplacementService.libererEmplacement(id);
            log.info("Successfully liberated emplacement with id: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Emplacement liberated successfully", Arrays.asList(liberated)));
        } catch (NotFoundException e) {
            log.warn("Emplacement not found for liberation with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (BadRequestException e) {
            log.warn("Bad request for emplacement liberation {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error liberating emplacement with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error liberating emplacement: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "libererEmplacement");
            OSMLogger.logPerformance(this.getClass(), "libererEmplacement", startTime, System.currentTimeMillis());
        }
    }

    @PutMapping("/{id}/capacite")
    public ResponseEntity<ApiResponse<EmplacementStock, EmplacementStockDto>> mettreAJourCapacite(
            @PathVariable UUID id,
            @RequestBody Map<String, String> payload) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "mettreAJourCapacite", id);
        try {
            String nouvelleCapacite = payload.get("capaciteActuelle");
            if (nouvelleCapacite == null || nouvelleCapacite.trim().isEmpty()) {
                throw new BadRequestException("capaciteActuelle is required for capacity update");
            }
            // Validate that it's a valid number
            try {
                Double.parseDouble(nouvelleCapacite);
            } catch (NumberFormatException e) {
                throw new BadRequestException("capaciteActuelle must be a valid number");
            }
            EmplacementStockDto updated = emplacementService.mettreAJourCapacite(id, nouvelleCapacite);
            log.info("Successfully updated capacity for emplacement with id: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Capacity updated successfully", Arrays.asList(updated)));
        } catch (NotFoundException e) {
            log.warn("Emplacement not found for capacity update with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (BadRequestException e) {
            log.warn("Bad request for capacity update {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error updating capacity for emplacement with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating capacity: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "mettreAJourCapacite");
            OSMLogger.logPerformance(this.getClass(), "mettreAJourCapacite", startTime, System.currentTimeMillis());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<EmplacementStock, EmplacementStockDto>> deleteEmplacement(@PathVariable UUID id) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "deleteEmplacement", id);
        try {
            emplacementService.deleteEmplacement(id);
            log.info("Successfully deleted emplacement with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            log.warn("Emplacement not found for deletion with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error deleting emplacement with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error deleting emplacement: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "deleteEmplacement");
            OSMLogger.logPerformance(this.getClass(), "deleteEmplacement", startTime, System.currentTimeMillis());
        }
    }
    /*@GetMapping("/audit/all")
    public ResponseEntity<List<AuditDto>> getAudit() {
        try {
            List<AuditDto> auditList = emplacementService.getAuditEmplacements();
            return ResponseEntity.ok(auditList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }*/

 

    @Override
    protected String getResourceName() {
        return "EmplacementStock".toUpperCase();
    }
}