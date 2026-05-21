package com.osm.inventory_service.service;

import com.osm.inventory_service.dto.ProduitFinalDto;
import com.osm.inventory_service.entity.ProduitFinal;
import com.osm.inventory_service.Enum.ProduitFinalType;
import com.osm.inventory_service.repository.ProduitFinalRepository;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.CampaignResolver;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProduitFinalService extends BaseServiceImpl<ProduitFinal, ProduitFinalDto, ProduitFinalDto> {

    private final ProduitFinalRepository produitFinalRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ProduitFinalService(BaseRepository<ProduitFinal> repository,
                               ProduitFinalRepository produitFinalRepository,
                               ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.produitFinalRepository = produitFinalRepository;
        this.modelMapper = modelMapper;
    }

    public List<ProduitFinalDto> getAllProduitsFinaux() {
        return produitFinalRepository.findByIsDeletedFalse().stream()
                .map(produitFinal -> modelMapper.map(produitFinal, ProduitFinalDto.class))
                .collect(Collectors.toList());
    }

    public ProduitFinalDto getProduitFinalById(UUID id) {
        ProduitFinal produitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
        return modelMapper.map(produitFinal, ProduitFinalDto.class);
    }

    public List<ProduitFinalDto> getAllActiveProduitsFinaux() {
        return produitFinalRepository.findByActifTrueAndIsDeletedFalse().stream()
                .map(produitFinal -> modelMapper.map(produitFinal, ProduitFinalDto.class))
                .collect(Collectors.toList());
    }

    public List<ProduitFinalDto> getProduitsFinauxByType(ProduitFinalType type) {
        return produitFinalRepository.findByTypeAndIsDeletedFalse(type).stream()
                .map(produitFinal -> modelMapper.map(produitFinal, ProduitFinalDto.class))
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
        return modelMapper.map(savedProduitFinal, ProduitFinalDto.class);
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
        return modelMapper.map(updatedProduitFinal, ProduitFinalDto.class);
    }

    @Transactional
    public void desactiverProduitFinal(UUID id) {
        ProduitFinal produitFinal = produitFinalRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
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
        produitFinal.setDeleted(true);
        produitFinal.setActif(false);
        produitFinalRepository.save(produitFinal);
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
}
