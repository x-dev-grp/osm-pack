package com.osm.inventory_service.service;

import com.osm.inventory_service.dto.EmplacementStockDto;
import com.osm.inventory_service.entity.EmplacementStock;
import com.osm.inventory_service.repository.EmplacementStockRepository;
import com.xdev.xdevbase.config.TenantContext;
import com.xdev.xdevbase.qr.model.QrCodeInfo;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.xdev.communicator.feignServices.BaseFeignService.log;

@Service
public class EmplacementStockService extends BaseServiceImpl<EmplacementStock, EmplacementStockDto, EmplacementStockDto> {

    private final EmplacementStockRepository emplacementRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public EmplacementStockService(BaseRepository<EmplacementStock> repository, EmplacementStockRepository emplacementRepository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.emplacementRepository = emplacementRepository;
        this.modelMapper = modelMapper;
    }
    public List<EmplacementStockDto> getAllEmplacements() {
        return emplacementRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmplacementStockDto getEmplacementById(UUID id) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));
        return convertToDto(emplacement);
    }

    public List<EmplacementStockDto> getEmplacementsReservesPour(String client) {
        return emplacementRepository.findReservesPour(client).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmplacementStockDto createEmplacement(EmplacementStockDto emplacementDto) {
        if (emplacementDto.getNom() == null || emplacementDto.getNom().trim().isEmpty()) {
            throw new ValidationException("Le nom de l'emplacement est obligatoire");
        }
        if (emplacementDto.getTypeEmplacement() == null) {
            throw new ValidationException("Le type d'emplacement est obligatoire");
        }
        EmplacementStock emplacement = modelMapper.map(emplacementDto, EmplacementStock.class);
        emplacement.setCode(generateUniqueCode());
        if (emplacement.getDisponible() == null) {
            emplacement.setDisponible(true);
        }
        if (emplacement.getCapaciteActuelle() == null) {
            emplacement.setCapaciteActuelle("0");
        }
        if (emplacement.getCategorieArticleStocke() != null) {
            if (emplacement.getCapaciteMaximale() != null) {
                try {
                    double capaciteMax = Double.parseDouble(emplacement.getCapaciteMaximale());
                    if (capaciteMax <= 0) {
                        throw new ValidationException("La capacité maximale doit être supérieure à 0");
                    }
                } catch (NumberFormatException e) {
                    throw new ValidationException("La capacité maximale doit être un nombre valide");
                }
            }
        }
        if (emplacement.getTemperatureMin() != null && emplacement.getTemperatureMax() != null) {
            if (emplacement.getTemperatureMin() > emplacement.getTemperatureMax()) {
                throw new ValidationException("La température minimale ne peut pas être supérieure à la température maximale");
            }
        }

        emplacement.setActif(true);
        EmplacementStock savedEmplacement = emplacementRepository.save(emplacement);
        log.info("Emplacement créé avec succès - ID: {}, Code: {}, Type: {}, Catégorie: {}",
                savedEmplacement.getId(),
                savedEmplacement.getCode(),
                savedEmplacement.getTypeEmplacement(),
                savedEmplacement.getCategorieArticleStocke());

        QrCodeInfo qrInfo = generateQrInfo("EMPLACEMENTSTOCK", savedEmplacement.getId());
        EmplacementStockDto result = convertToDto(savedEmplacement);
        result.setPublicCode(qrInfo.getPublicCode());
        result.setQrUrl(qrInfo.getQrUrl());
        result.setQrImageBase64(qrInfo.getQrImageBase64());
        return result;
    }
    @Transactional
    public EmplacementStockDto activerEmplacement(UUID id) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));
        emplacement.setActif(true);
        EmplacementStock updated = emplacementRepository.save(emplacement);
        return convertToDto(updated);
    }
    @Transactional
    public EmplacementStockDto desactiverEmplacement(UUID id) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));
        emplacement.setActif(false);
        EmplacementStock updated = emplacementRepository.save(emplacement);
        return convertToDto(updated);
    }

    @Transactional
    public EmplacementStockDto updateEmplacement(UUID id, EmplacementStockDto emplacementDto) {
        EmplacementStock existingEmplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));

        if (!existingEmplacement.getCode().equals(emplacementDto.getCode())) {
            if (emplacementRepository.existsByCode(emplacementDto.getCode())) {
                throw new RuntimeException("Un emplacement avec ce code existe déjà: " + emplacementDto.getCode());
            }
            existingEmplacement.setCode(emplacementDto.getCode());
        }
        existingEmplacement.setNom(emplacementDto.getNom());
        existingEmplacement.setTypeEmplacement(emplacementDto.getTypeEmplacement());
        existingEmplacement.setCategorieArticleStocke(emplacementDto.getCategorieArticleStocke());  // ✅ NOUVEAU
        existingEmplacement.setCapaciteMaximale(emplacementDto.getCapaciteMaximale());
        existingEmplacement.setCapaciteActuelle(emplacementDto.getCapaciteActuelle());
        existingEmplacement.setZone(emplacementDto.getZone());
        existingEmplacement.setDisponible(emplacementDto.getDisponible());
        existingEmplacement.setReservePour(emplacementDto.getReservePour());
        existingEmplacement.setConditionsSpeciales(emplacementDto.getConditionsSpeciales());
        existingEmplacement.setTemperatureMin(emplacementDto.getTemperatureMin());
        existingEmplacement.setTemperatureMax(emplacementDto.getTemperatureMax());
        existingEmplacement.setDescription(emplacementDto.getDescription());
        existingEmplacement.setNotes(emplacementDto.getNotes());

        EmplacementStock updatedEmplacement = emplacementRepository.save(existingEmplacement);
        return convertToDto(updatedEmplacement);
    }
    @Transactional
    public EmplacementStockDto reserverEmplacement(UUID id, String reservePour) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));

        emplacement.setDisponible(false);
        emplacement.setReservePour(reservePour);

        EmplacementStock updatedEmplacement = emplacementRepository.save(emplacement);
        return convertToDto(updatedEmplacement);
    }

    @Transactional
    public EmplacementStockDto libererEmplacement(UUID id) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));

        emplacement.setDisponible(true);
        emplacement.setReservePour(null);
        emplacement.setCapaciteActuelle("0");

        EmplacementStock updatedEmplacement = emplacementRepository.save(emplacement);
        return convertToDto(updatedEmplacement);
    }

    @Transactional
    public EmplacementStockDto mettreAJourCapacite(UUID id, String nouvelleCapacite) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));

        emplacement.setCapaciteActuelle(nouvelleCapacite);

        EmplacementStock updatedEmplacement = emplacementRepository.save(emplacement);
        return convertToDto(updatedEmplacement);
    }

    @Transactional
    public void deleteEmplacement(UUID id) {
        EmplacementStock emplacement = emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec id: " + id));
        emplacementRepository.delete(emplacement);
    }
    private String generateUniqueCode() {
        return generateBusinessCode("code", "EM");
    }

    private EmplacementStockDto convertToDto(EmplacementStock emplacement) {
        EmplacementStockDto dto = modelMapper.map(emplacement, EmplacementStockDto.class);
        dto.setPublicCode(emplacement.getQrHex());
        dto.setQrImageBase64(emplacement.getQrImageBase64());
        return dto;
    }

    @Override
    protected String getEntityType() {
        return "EMPLACEMENTSTOCK";
    }

    @Override
    protected String getLabel(EmplacementStock entity) {
        if (entity.getNom() != null && !entity.getNom().isBlank()) {
            return entity.getNom();
        }
        return entity.getCode();
    }

    @Override
    protected String getStatus(EmplacementStock entity) {
        if (Boolean.FALSE.equals(entity.getActif())) {
            return "INACTIF";
        }
        return Boolean.TRUE.equals(entity.getDisponible()) ? "DISPONIBLE" : "RESERVE";
    }

    @Override
    protected String getMobileRoute() {
        return "/emplacement/detail";
    }

    @Override
    protected String getWebRoute(EmplacementStock entity) {
        if (entity == null || entity.getId() == null) {
            return "/stock/emplacements";
        }
        return "/stock/emplacements/" + entity.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public QrResolveResponse resolve(String publicCode) {
        if (publicCode == null || publicCode.isBlank()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }

        String normalizedCode = publicCode.trim().toUpperCase(Locale.ROOT);
        UUID tenantId = TenantContext.getCurrentTenant();

        Optional<EmplacementStock> entity = (tenantId == null)
                ? emplacementRepository.findByQrHex(normalizedCode)
                : emplacementRepository.findByQrHexAndTenantIdAndIsDeletedFalse(normalizedCode, tenantId);

        if (entity.isEmpty() && tenantId != null) {
            entity = emplacementRepository.findByQrHex(normalizedCode);
        }

        return entity.map(emplacement -> {
                    QrResolveResponse response = new QrResolveResponse();
                    response.setEntityType(getEntityType());
                    response.setPublicCode(normalizedCode);
                    response.setEntityId(emplacement.getId().toString());
                    response.setLabel(getLabel(emplacement));
                    response.setStatus(getStatus(emplacement));
                    response.setMobileRoute(getMobileRoute());
                    response.setWebRoute(getWebRoute(emplacement));
                    response.setData(convertToDto(emplacement));
                    return response;
                })
                .orElseThrow(() -> new EntityNotFoundException("Emplacement non trouve pour le code : " + publicCode));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QrResolveResponse> searchByCode(String code) {
        try {
            return Optional.ofNullable(resolve(code));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
