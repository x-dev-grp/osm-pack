package com.osm.inventory_service.service;

import com.osm.inventory_service.dto.ProduitFinalDto;
import com.osm.inventory_service.entity.ProduitFinal;
import com.osm.inventory_service.Enum.ProduitFinalType;
import com.osm.inventory_service.repository.ProduitFinalRepository;
import com.xdev.xdevbase.config.TenantContext;
import com.xdev.xdevbase.qr.model.QrCodeInfo;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.CampaignResolver;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProduitFinalService extends BaseServiceImpl<ProduitFinal, ProduitFinalDto, ProduitFinalDto> {

    private final ProduitFinalRepository produitFinalRepository;
    private final ModelMapper modelMapper;
    private final InventoryDeleteGuardService deleteGuard;

    @Autowired
    public ProduitFinalService(BaseRepository<ProduitFinal> repository,
                               ProduitFinalRepository produitFinalRepository,
                               ModelMapper modelMapper,
                               InventoryDeleteGuardService deleteGuard) {
        super(repository, modelMapper);
        this.produitFinalRepository = produitFinalRepository;
        this.modelMapper = modelMapper;
        this.deleteGuard = deleteGuard;
    }
    @Transactional(readOnly = true)
    public List<ProduitFinalDto> getAllProduitsFinaux() {
        return produitFinalRepository.findByIsDeletedFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProduitFinalDto getProduitFinalById(UUID id) {
        ProduitFinal produitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
        return convertToDto(produitFinal);
    }

    public List<ProduitFinalDto> getAllActiveProduitsFinaux() {
        return produitFinalRepository.findByActifTrueAndIsDeletedFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProduitFinalDto> getProduitsFinauxByType(ProduitFinalType type) {
        return produitFinalRepository.findByTypeAndIsDeletedFalse(type).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProduitFinalDto createProduitFinal(ProduitFinalDto produitFinalDto) {
        String name = resolveName(produitFinalDto);
        String code = generateBusinessCode("code", "PF");
        ensureNameIsAvailable(name, null);
        ensureCodeIsAvailable(code, null);

        ProduitFinal produitFinal = new ProduitFinal();
        applyProduitFinalDto(produitFinal, produitFinalDto, name, code);
        produitFinal.setDeleted(false);
        ProduitFinal savedProduitFinal = produitFinalRepository.save(produitFinal);
        QrCodeInfo qrInfo = generateQrInfo("PRODUITFINAL", savedProduitFinal.getId());
        ProduitFinalDto result = convertToDto(savedProduitFinal);
        result.setPublicCode(qrInfo.getPublicCode());
        result.setQrUrl(qrInfo.getQrUrl());
        result.setQrImageBase64(qrInfo.getQrImageBase64());
        return result;
    }

    @Transactional
    public ProduitFinalDto updateProduitFinal(UUID id, ProduitFinalDto produitFinalDto) {
        ProduitFinal existingProduitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
        String name = resolveName(produitFinalDto);
        String code = existingProduitFinal.getCode();
        ensureNameIsAvailable(name, id);
        ensureCodeIsAvailable(code, id);
        applyProduitFinalDto(existingProduitFinal, produitFinalDto, name, code);

        ProduitFinal updatedProduitFinal = produitFinalRepository.save(existingProduitFinal);
        return convertToDto(updatedProduitFinal);
    }

    @Transactional
    public void desactiverProduitFinal(UUID id) {
        ProduitFinal produitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
        deleteGuard.assertProduitFinalCanBeRemoved(id);
        produitFinal.setActif(false);
        produitFinalRepository.save(produitFinal);
    }

    @Transactional
    public void activerProduitFinal(UUID id) {
        ProduitFinal produitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
        produitFinal.setActif(true);
        produitFinalRepository.save(produitFinal);
    }

    @Transactional
    public void supprimerProduitFinal(UUID id) {
        ProduitFinal produitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
        deleteGuard.assertProduitFinalCanBeRemoved(id);
        produitFinal.setDeleted(true);
        produitFinal.setActif(false);
        produitFinalRepository.save(produitFinal);
    }

    @Override
    @Transactional
    public ProduitFinalDto delete(UUID id) {
        ProduitFinalDto dto = getProduitFinalById(id);
        supprimerProduitFinal(id);
        return dto;
    }

    @Override
    @Transactional
    public void remove(UUID id) {
        supprimerProduitFinal(id);
    }

    private void applyProduitFinalDto(ProduitFinal produitFinal, ProduitFinalDto produitFinalDto, String name, String code) {
        ProduitFinalType type = produitFinalDto.getType() == null ? ProduitFinalType.NON_VRAC : produitFinalDto.getType();

        produitFinal.setName(name);
        produitFinal.setCode(code);
        produitFinal.setType(type);
        produitFinal.setCategory(trimToNull(produitFinalDto.getCategory()));
        produitFinal.setUnitOfMeasure(resolveUnitOfMeasure(produitFinalDto, type));
        produitFinal.setDescription(trimToNull(produitFinalDto.getDescription()));
        produitFinal.setGrade(trimToNull(produitFinalDto.getGrade()));
        produitFinal.setOrigin(trimToNull(produitFinalDto.getOrigin()));
        produitFinal.setHarvestCampaign(resolveHarvestCampaign(produitFinalDto, produitFinal.getHarvestCampaign()));
        if (produitFinalDto.getActif() != null) {
            produitFinal.setActif(produitFinalDto.getActif());
        } else if (produitFinal.getActif() == null) {
            produitFinal.setActif(Boolean.TRUE);
        }

        if (type == ProduitFinalType.VRAC) {
            produitFinal.setVolume(null);
            produitFinal.setPackagingType(null);
            produitFinal.setBarcode(null);
            produitFinal.setUnitsPerCarton(null);
            produitFinal.setCartonsPerPallet(null);
            produitFinal.setNetWeight(null);
            produitFinal.setGrossWeight(null);
            produitFinal.setBrand(null);
            produitFinal.setDensity(produitFinalDto.getDensity());
            produitFinal.setStorageUnit(trimToNull(produitFinalDto.getStorageUnit()));
        } else {
            produitFinal.setVolume(produitFinalDto.getVolume());
            produitFinal.setPackagingType(trimToNull(produitFinalDto.getPackagingType()));
            produitFinal.setBarcode(trimToNull(produitFinalDto.getBarcode()));
            produitFinal.setUnitsPerCarton(produitFinalDto.getUnitsPerCarton());
            produitFinal.setCartonsPerPallet(produitFinalDto.getCartonsPerPallet());
            produitFinal.setNetWeight(produitFinalDto.getNetWeight());
            produitFinal.setGrossWeight(produitFinalDto.getGrossWeight());
            produitFinal.setBrand(trimToNull(produitFinalDto.getBrand()));
            produitFinal.setDensity(null);
            produitFinal.setStorageUnit(null);
        }
    }

    private String resolveName(ProduitFinalDto produitFinalDto) {
        String name = trimToNull(produitFinalDto.getName());
        if (name == null) {
            name = trimToNull(produitFinalDto.getCode());
        }
        if (name == null) {
            throw new RuntimeException("Le nom du produit est obligatoire");
        }
        return name;
    }

    private String resolveUnitOfMeasure(ProduitFinalDto produitFinalDto, ProduitFinalType type) {
        String unitOfMeasure = trimToNull(produitFinalDto.getUnitOfMeasure());
        if (unitOfMeasure != null) {
            return unitOfMeasure;
        }
        return type == ProduitFinalType.VRAC ? "L" : "BOTTLE";
    }

    private String resolveHarvestCampaign(ProduitFinalDto produitFinalDto, String currentValue) {
        String harvestCampaign = trimToNull(produitFinalDto.getHarvestCampaign());
        if (harvestCampaign != null) {
            return harvestCampaign;
        }
        if (currentValue != null) {
            return currentValue;
        }
        return CampaignResolver.resolveCampaignLabel(LocalDate.now());
    }

    private void ensureNameIsAvailable(String name, UUID currentId) {
        produitFinalRepository.findByNameAndIsDeletedFalse(name)
                .filter(produitFinal -> currentId == null || !Objects.equals(produitFinal.getId(), currentId))
                .ifPresent(produitFinal -> {
                    throw new RuntimeException("Un produit avec ce nom existe deja: " + name);
                });
    }

    private void ensureCodeIsAvailable(String code, UUID currentId) {
        produitFinalRepository.findByNameAndIsDeletedFalse(code)
                .filter(produitFinal -> currentId == null || !Objects.equals(produitFinal.getId(), currentId))
                .ifPresent(produitFinal -> {
                    throw new RuntimeException("Un produit avec ce code existe deja: " + code);
                });
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProduitFinalDto convertToDto(ProduitFinal produitFinal) {
        ProduitFinalDto dto = modelMapper.map(produitFinal, ProduitFinalDto.class);
        dto.setPublicCode(produitFinal.getQrHex());
        dto.setQrImageBase64(produitFinal.getQrImageBase64());
        return dto;
    }

    @Override
    protected String getEntityType() {
        return "PRODUITFINAL";
    }

    @Override
    protected String getLabel(ProduitFinal entity) {
        if (entity.getName() != null && !entity.getName().isBlank()) {
            return entity.getName();
        }
        return entity.getCode();
    }

    @Override
    protected String getStatus(ProduitFinal entity) {
        return Boolean.TRUE.equals(entity.getActif()) ? "ACTIF" : "INACTIF";
    }

    @Override
    protected String getMobileRoute() {
        return "/produit-final/detail";
    }

    @Override
    protected String getWebRoute(ProduitFinal entity) {
        if (entity == null || entity.getId() == null) {
            return "/stock/products";
        }
        return "/stock/products/" + entity.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public QrResolveResponse resolve(String publicCode) {
        if (publicCode == null || publicCode.isBlank()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }

        String normalizedCode = publicCode.trim().toUpperCase(Locale.ROOT);
        UUID tenantId = TenantContext.getCurrentTenant();

        Optional<ProduitFinal> entity = (tenantId == null)
                ? produitFinalRepository.findByQrHex(normalizedCode)
                : produitFinalRepository.findByQrHexAndTenantIdAndIsDeletedFalse(normalizedCode, tenantId);

        if (entity.isEmpty() && tenantId != null) {
            entity = produitFinalRepository.findByQrHex(normalizedCode);
        }

        return entity.map(produitFinal -> {
                    QrResolveResponse response = new QrResolveResponse();
                    response.setEntityType(getEntityType());
                    response.setPublicCode(normalizedCode);
                    response.setEntityId(produitFinal.getId().toString());
                    response.setLabel(getLabel(produitFinal));
                    response.setStatus(getStatus(produitFinal));
                    response.setMobileRoute(getMobileRoute());
                    response.setWebRoute(getWebRoute(produitFinal));
                    response.setData(convertToDto(produitFinal));
                    return response;
                })
                .orElseThrow(() -> new EntityNotFoundException("Produit final non trouve pour le code : " + publicCode));
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
