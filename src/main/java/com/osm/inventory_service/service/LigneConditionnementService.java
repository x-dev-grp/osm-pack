package com.osm.inventory_service.service;

import com.osm.inventory_service.Enum.Statue;
import com.osm.inventory_service.dto.LigneConditionnementDto;
import com.osm.inventory_service.entity.LigneConditionnement;
import com.osm.inventory_service.repository.LigneConditionnementRepository;
import com.xdev.xdevbase.config.TenantContext;
import com.xdev.xdevbase.qr.CodeGenerator;
import com.xdev.xdevbase.qr.model.QrCodeInfo;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LigneConditionnementService extends BaseServiceImpl<LigneConditionnement, LigneConditionnementDto, LigneConditionnementDto> {

    private final LigneConditionnementRepository ligneRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public LigneConditionnementService(BaseRepository<LigneConditionnement> repository,
                                       LigneConditionnementRepository ligneRepository,
                                       CodeGenerator codeGenerator,
                                       ModelMapper modelMapper) {
        super(repository, codeGenerator, modelMapper);
        this.ligneRepository = ligneRepository;
        this.modelMapper = modelMapper;
    }

    public List<LigneConditionnementDto> getAllLignes() {
        return ligneRepository.findAllByIsDeletedFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public LigneConditionnementDto getLigneById(UUID id) {
        LigneConditionnement ligne = ligneRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec id: " + id));
        return convertToDto(ligne);
    }

    @Transactional
    public LigneConditionnementDto createLigne(LigneConditionnementDto ligneDto) {

        LigneConditionnement ligne = modelMapper.map(ligneDto, LigneConditionnement.class);
        ligne.setCode(genererCodeLigne());
        ligne.setActif(true);
        ligne.setEtat(
                ligneDto.getEtat() != null ? ligneDto.getEtat() : Statue.ACTIF
        );
        LigneConditionnement savedLigne = ligneRepository.save(ligne);
        QrCodeInfo qrInfo = generateQrInfo("LIGNECONDITIONNEMENT", savedLigne.getId());
        LigneConditionnementDto result = convertToDto(savedLigne);
        result.setPublicCode(qrInfo.getPublicCode());
        result.setQrUrl(qrInfo.getQrUrl());
        result.setQrImageBase64(qrInfo.getQrImageBase64());
        return result;
    }
    @Transactional
    public LigneConditionnementDto updateLigne(UUID id, LigneConditionnementDto ligneDto) {
        LigneConditionnement existingLigne = ligneRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec id: " + id));
        if (!existingLigne.getCode().equals(ligneDto.getCode())) {
            if (ligneRepository.existsByCodeAndIsDeletedFalse(ligneDto.getCode())) {
                throw new RuntimeException("Une ligne avec ce code existe déjà: " + ligneDto.getCode());
            }
            existingLigne.setCode(ligneDto.getCode());
        }
        existingLigne.setNom(ligneDto.getNom());
        existingLigne.setDescription(ligneDto.getDescription());
        existingLigne.setEtat(ligneDto.getEtat());
        existingLigne.setVitesseNominale(ligneDto.getVitesseNominale());
        existingLigne.setTempsPreparation(ligneDto.getTempsPreparation());
        existingLigne.setTempsNettoyage(ligneDto.getTempsNettoyage());
        existingLigne.setResponsable(ligneDto.getResponsable());
        existingLigne.setDateDerniereMaintenance(ligneDto.getDateDerniereMaintenance());
        existingLigne.setDateProchaineMaintenance(ligneDto.getDateProchaineMaintenance());
        existingLigne.setNotes(ligneDto.getNotes());

        LigneConditionnement updatedLigne = ligneRepository.save(existingLigne);
        return convertToDto(updatedLigne);
    }

    @Transactional
    public void desactiverLigne(UUID id) {
        LigneConditionnement ligne = ligneRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec id: " + id));
        ligne.setActif(false);
        ligneRepository.save(ligne);
    }

    @Transactional
    public void activerLigne(UUID id) {
        LigneConditionnement ligne = ligneRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec id: " + id));
        ligne.setActif(true);
        ligneRepository.save(ligne);
    }

    @Transactional
    public LigneConditionnementDto changerEtat(UUID id, Statue nouvelEtat) {
        LigneConditionnement ligne = ligneRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec id: " + id));

        ligne.setEtat(nouvelEtat);
        LigneConditionnement updatedLigne = ligneRepository.save(ligne);
        return convertToDto(updatedLigne);
    }
    public List<LigneConditionnementDto> getLignesActives() {
        return ligneRepository.findByEtatAndIsDeletedFalse(Statue.ACTIF).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void supprimerLigne(UUID id) {
        LigneConditionnement ligne = ligneRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec id: " + id));
        ligne.setDeleted(true);
        ligne.setActif(false);
        ligneRepository.save(ligne);
    }

    private String genererCodeLigne() {
        return generateBusinessCode("code", "LI");
    }

    private LigneConditionnementDto convertToDto(LigneConditionnement ligne) {
        LigneConditionnementDto dto = modelMapper.map(ligne, LigneConditionnementDto.class);
        dto.setPublicCode(ligne.getQrHex());
        dto.setQrImageBase64(ligne.getQrImageBase64());
        return dto;
    }

    @Override
    protected String getEntityType() {
        return "LIGNECONDITIONNEMENT";
    }

    @Override
    protected String getLabel(LigneConditionnement entity) {
        if (entity.getNom() != null && !entity.getNom().isBlank()) {
            return entity.getNom();
        }
        return entity.getCode();
    }

    @Override
    protected String getStatus(LigneConditionnement entity) {
        if (entity.getEtat() != null) {
            return entity.getEtat().name();
        }
        return entity.isActif() ? "ACTIF" : "INACTIF";
    }

    @Override
    protected String getMobileRoute() {
        return "/ligne/detail";
    }

    @Override
    protected String getWebRoute(LigneConditionnement entity) {
        if (entity == null || entity.getId() == null) {
            return "/stock/lignes";
        }
        return "/stock/lignes/" + entity.getId();
    }

    @Override
    protected Object getData(LigneConditionnement entity) {
        return convertToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public QrResolveResponse resolve(String publicCode) {
        if (publicCode == null || publicCode.isBlank()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }

        String normalizedCode = publicCode.trim().toUpperCase(Locale.ROOT);
        UUID tenantId = TenantContext.getCurrentTenant();

        Optional<LigneConditionnement> entity = (tenantId == null)
                ? ligneRepository.findByQrHex(normalizedCode)
                : ligneRepository.findByQrHexAndTenantIdAndIsDeletedFalse(normalizedCode, tenantId);

        if (entity.isEmpty() && tenantId != null) {
            entity = ligneRepository.findByQrHex(normalizedCode);
        }

        return entity.map(ligne -> {
                    if (Boolean.TRUE.equals(ligne.getDeleted())) {
                        throw new EntityNotFoundException("Ligne non trouvee pour le code : " + publicCode);
                    }
                    QrResolveResponse response = new QrResolveResponse();
                    response.setEntityType(getEntityType());
                    response.setPublicCode(normalizedCode);
                    response.setEntityId(ligne.getId().toString());
                    response.setLabel(getLabel(ligne));
                    response.setStatus(getStatus(ligne));
                    response.setMobileRoute(getMobileRoute());
                    response.setWebRoute(getWebRoute(ligne));
                    response.setData(getData(ligne));
                    return response;
                })
                .orElseThrow(() -> new EntityNotFoundException("Ligne non trouvee pour le code : " + publicCode));
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
