package com.osm.inventory_service.service;

import com.osm.inventory_service.dto.FournisseurDto;
import com.osm.inventory_service.entity.Fournisseur;
import com.osm.inventory_service.repository.FournisseurRepository;
import com.xdev.communicator.models.enums.Currency;
import com.xdev.xdevbase.config.TenantContext;
import com.xdev.xdevbase.qr.model.QrCodeInfo;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FournisseurService extends BaseServiceImpl<Fournisseur, FournisseurDto, FournisseurDto> {

    private final FournisseurRepository fournisseurRepository;
    private final ModelMapper modelMapper;
    private final InventoryDeleteGuardService deleteGuard;

    @Autowired
    public FournisseurService(BaseRepository<Fournisseur> repository, FournisseurRepository fournisseurRepository, ModelMapper modelMapper, InventoryDeleteGuardService deleteGuard) {
        super(repository, modelMapper);
        this.fournisseurRepository = fournisseurRepository;
        this.modelMapper = modelMapper;
        this.deleteGuard = deleteGuard;
    }

    public List<FournisseurDto> getAllFournisseurs() {
        return fournisseurRepository.findAllByIsDeletedFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FournisseurDto getFournisseurById(UUID id) {
        Fournisseur fournisseur = fournisseurRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec id: " + id));
        return convertToDto(fournisseur);
    }

    @Transactional
    public FournisseurDto createFournisseur(FournisseurDto fournisseurDto) {
        if (!StringUtils.hasText(fournisseurDto.getNom())) {
            throw new RuntimeException("Le nom du fournisseur est obligatoire");
        }
        fournisseurDto.setCode(genererCodeFournisseur());
        if (fournisseurDto.getEmail() != null && !fournisseurDto.getEmail().isEmpty()) {
            if (fournisseurRepository.existsByEmailAndIsDeletedFalse(fournisseurDto.getEmail())) {
                throw new RuntimeException("Un fournisseur avec cet email existe déjà: " + fournisseurDto.getEmail());
            }
        }
        if (fournisseurDto.getTelephone() != null && !fournisseurDto.getTelephone().isEmpty()) {
            if (fournisseurRepository.existsByTelephoneAndIsDeletedFalse(fournisseurDto.getTelephone())) {
                throw new RuntimeException("Un fournisseur avec ce téléphone existe déjà: " + fournisseurDto.getTelephone());
            }
        }
        if (fournisseurDto.getNumeroTva() != null && !fournisseurDto.getNumeroTva().isEmpty()) {
            if (fournisseurRepository.existsByNumeroTvaAndIsDeletedFalse(fournisseurDto.getNumeroTva())) {
                throw new RuntimeException("Un fournisseur avec ce numéro de TVA existe déjà: " + fournisseurDto.getNumeroTva());
            }
        }
        Fournisseur fournisseur = modelMapper.map(fournisseurDto, Fournisseur.class);

        if (fournisseur.getActif() == null) {
            fournisseur.setActif(true);
        }

        Fournisseur savedFournisseur = fournisseurRepository.save(fournisseur);
        QrCodeInfo qrInfo = generateQrInfo("FOURNISSEUR", savedFournisseur.getId());
        FournisseurDto result = convertToDto(savedFournisseur);
        result.setPublicCode(qrInfo.getPublicCode());
        result.setQrUrl(qrInfo.getQrUrl());
        result.setQrImageBase64(qrInfo.getQrImageBase64());
        return result;
    }

    @Transactional
    public FournisseurDto updateFournisseur(UUID id, FournisseurDto fournisseurDto) {
        Fournisseur existingFournisseur = fournisseurRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec id: " + id));
        if (!StringUtils.hasText(fournisseurDto.getNom())) {
            throw new RuntimeException("Le nom du fournisseur est obligatoire");
        }
        if (fournisseurDto.getEmail() != null) {
            if (!fournisseurDto.getEmail().equals(existingFournisseur.getEmail())) {
                if (fournisseurRepository.existsByEmailAndIsDeletedFalseAndIdNot(fournisseurDto.getEmail(), id)) {
                    throw new RuntimeException("Un fournisseur avec cet email existe déjà: " + fournisseurDto.getEmail());
                }
                existingFournisseur.setEmail(fournisseurDto.getEmail());
            }
        } else {
            existingFournisseur.setEmail(null);
        }
        if (fournisseurDto.getTelephone() != null) {
            if (!fournisseurDto.getTelephone().equals(existingFournisseur.getTelephone())) {
                if (fournisseurRepository.existsByTelephoneAndIsDeletedFalseAndIdNot(fournisseurDto.getTelephone(), id)) {
                    throw new RuntimeException("Un fournisseur avec ce téléphone existe déjà: " + fournisseurDto.getTelephone());
                }
                existingFournisseur.setTelephone(fournisseurDto.getTelephone());
            }
        } else {
            existingFournisseur.setTelephone(null);
        }
        if (fournisseurDto.getNumeroTva() != null) {
            if (!fournisseurDto.getNumeroTva().equals(existingFournisseur.getNumeroTva())) {
                if (fournisseurRepository.existsByNumeroTvaAndIsDeletedFalseAndIdNot(fournisseurDto.getNumeroTva(), id)) {
                    throw new RuntimeException("Un fournisseur avec ce numéro de TVA existe déjà: " + fournisseurDto.getNumeroTva());
                }
                existingFournisseur.setNumeroTva(fournisseurDto.getNumeroTva());
            }
        } else {
            existingFournisseur.setNumeroTva(null);
        }
        existingFournisseur.setNom(fournisseurDto.getNom());
        existingFournisseur.setNomCommercial(fournisseurDto.getNomCommercial());
        existingFournisseur.setFax(fournisseurDto.getFax());
        existingFournisseur.setSiteWeb(fournisseurDto.getSiteWeb());
        existingFournisseur.setAdresse(fournisseurDto.getAdresse());
        existingFournisseur.setVille(fournisseurDto.getVille());
        existingFournisseur.setCodePostal(fournisseurDto.getCodePostal());
        existingFournisseur.setPays(fournisseurDto.getPays());
        existingFournisseur.setContactNom(fournisseurDto.getContactNom());
        existingFournisseur.setContactPrenom(fournisseurDto.getContactPrenom());
        existingFournisseur.setContactEmail(fournisseurDto.getContactEmail());
        existingFournisseur.setContactTelephone(fournisseurDto.getContactTelephone());
        existingFournisseur.setCategorieFournisseur(fournisseurDto.getCategorieFournisseur());
        existingFournisseur.setDelaiLivraisonMoyen(fournisseurDto.getDelaiLivraisonMoyen());
        existingFournisseur.setConditionsPaiement(fournisseurDto.getConditionsPaiement());
        existingFournisseur.setCurrency(fournisseurDto.getCurrency());
        existingFournisseur.setActif(fournisseurDto.getActif());
        existingFournisseur.setCertifications(fournisseurDto.getCertifications());

        Fournisseur updatedFournisseur = fournisseurRepository.save(existingFournisseur);
        return convertToDto(updatedFournisseur);
    }


    @Transactional
    public FournisseurDto activerFournisseur(UUID id) {
        Fournisseur fournisseur = fournisseurRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec id: " + id));
        fournisseur.setActif(true);
        Fournisseur updatedFournisseur = fournisseurRepository.save(fournisseur);
        return convertToDto(updatedFournisseur);
    }

    @Transactional
    public FournisseurDto desactiverFournisseur(UUID id) {
        Fournisseur fournisseur = fournisseurRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec id: " + id));
        deleteGuard.assertFournisseurCanBeRemoved(id);
        fournisseur.setActif(false);
        Fournisseur updatedFournisseur = fournisseurRepository.save(fournisseur);
        return convertToDto(updatedFournisseur);
    }

    @Transactional
    public void supprimerFournisseur(UUID id) {
        Fournisseur fournisseur = fournisseurRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec id: " + id));
        deleteGuard.assertFournisseurCanBeRemoved(id);
        fournisseur.setDeleted(true);
        fournisseur.setActif(false);
        fournisseurRepository.save(fournisseur);
    }

    @Override
    @Transactional
    public FournisseurDto delete(UUID id) {
        FournisseurDto dto = getFournisseurById(id);
        supprimerFournisseur(id);
        return dto;
    }

    @Override
    @Transactional
    public void remove(UUID id) {
        supprimerFournisseur(id);
    }

    @Transactional(readOnly = true)
    protected String genererCodeFournisseur() {
        return generateBusinessCode("code", "FO");
    }
    @Transactional(readOnly = true)
    public List<FournisseurDto> getActiveFournisseurs() {
        return fournisseurRepository.findByActifTrueAndIsDeletedFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private FournisseurDto convertToDto(Fournisseur fournisseur) {
        FournisseurDto dto = modelMapper.map(fournisseur, FournisseurDto.class);
        dto.setPublicCode(fournisseur.getQrHex());
        dto.setQrImageBase64(fournisseur.getQrImageBase64());
        return dto;
    }

    @Override
    protected String getEntityType() {
        return "FOURNISSEUR";
    }

    @Override
    protected String getLabel(Fournisseur entity) {
        if (entity.getNom() != null && !entity.getNom().isBlank()) {
            return entity.getNom();
        }
        return entity.getCode();
    }

    @Override
    protected String getStatus(Fournisseur entity) {
        return Boolean.TRUE.equals(entity.getActif()) ? "ACTIF" : "INACTIF";
    }

    @Override
    protected String getMobileRoute() {
        return "/fournisseur/detail";
    }

    @Override
    protected String getWebRoute(Fournisseur entity) {
        if (entity == null || entity.getId() == null) {
            return "/stock/fournisseurs";
        }
        return "/stock/fournisseurs/" + entity.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public QrResolveResponse resolve(String publicCode) {
        if (publicCode == null || publicCode.isBlank()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }

        String normalizedCode = publicCode.trim().toUpperCase(Locale.ROOT);
        UUID tenantId = TenantContext.getCurrentTenant();

        Optional<Fournisseur> entity = (tenantId == null)
                ? fournisseurRepository.findByQrHex(normalizedCode)
                : fournisseurRepository.findByQrHexAndTenantIdAndIsDeletedFalse(normalizedCode, tenantId);

        if (entity.isEmpty() && tenantId != null) {
            entity = fournisseurRepository.findByQrHex(normalizedCode);
        }

        return entity.map(fournisseur -> {
                    if (Boolean.TRUE.equals(fournisseur.getDeleted())) {
                        throw new EntityNotFoundException("Fournisseur non trouve pour le code : " + publicCode);
                    }
                    QrResolveResponse response = new QrResolveResponse();
                    response.setEntityType(getEntityType());
                    response.setPublicCode(normalizedCode);
                    response.setEntityId(fournisseur.getId().toString());
                    response.setLabel(getLabel(fournisseur));
                    response.setStatus(getStatus(fournisseur));
                    response.setMobileRoute(getMobileRoute());
                    response.setWebRoute(getWebRoute(fournisseur));
                    response.setData(convertToDto(fournisseur));
                    return response;
                })
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur non trouve pour le code : " + publicCode));
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
