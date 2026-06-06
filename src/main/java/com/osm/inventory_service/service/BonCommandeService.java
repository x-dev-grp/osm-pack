package com.osm.inventory_service.service;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.osm.inventory_service.Enum.StatutBonCommande;
import com.osm.inventory_service.dto.ArticleSecDto;
import com.osm.inventory_service.dto.BonCommandeDto;
import com.osm.inventory_service.dto.LigneBonCommandeDto;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.BonCommande;
import com.osm.inventory_service.entity.LigneBonCommande;
import com.osm.inventory_service.repository.BonCommandeRepository;
import com.osm.inventory_service.repository.LigneBonCommandeRepository;
import com.xdev.xdevbase.config.TenantContext;
import com.xdev.xdevbase.qr.model.QrCodeInfo;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.qr.CodeGenerator;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BonCommandeService extends BaseServiceImpl<BonCommande, BonCommandeDto, BonCommandeDto> {

    private final BonCommandeRepository bonCommandeRepository;
    private final LigneBonCommandeRepository ligneBonCommandeRepository;
    private final ArticleSecService articleSecService;
    private final StockSecService stockSecService;
    private static final Logger logger = LoggerFactory.getLogger(BonCommandeService.class);

    @Autowired
    public BonCommandeService(BaseRepository<BonCommande> repository,
                              BonCommandeRepository bonCommandeRepository,
                              LigneBonCommandeRepository ligneBonCommandeRepository,
                              ArticleSecService articleSecService,
                              StockSecService stockSecService,
                              CodeGenerator codeGenerator,
                              ModelMapper modelMapper) {
        super(repository, codeGenerator, modelMapper);
        this.bonCommandeRepository = bonCommandeRepository;
        this.ligneBonCommandeRepository = ligneBonCommandeRepository;
        this.articleSecService = articleSecService;
        this.stockSecService = stockSecService;
    }

    private BonCommandeDto convertToDto(BonCommande bonCommande) {
        BonCommandeDto dto = new BonCommandeDto();
        dto.setId(bonCommande.getId());
        dto.setTenantId(bonCommande.getTenantId());
        dto.setCreatedBy(bonCommande.getCreatedBy());
        dto.setCreatedDate(bonCommande.getCreatedDate());
        dto.setLastModifiedBy(bonCommande.getLastModifiedBy());
        dto.setLastModifiedDate(bonCommande.getLastModifiedDate());
        dto.setExternalId(bonCommande.getExternalId());
        dto.setNumeroBC(bonCommande.getNumeroBC());
        dto.setDateValidation(bonCommande.getDateValidation());
        dto.setDateReceptionPrevue(bonCommande.getDateReceptionPrevue());
        dto.setStatus(bonCommande.getStatus());
        dto.setMotifRefus(bonCommande.getMotifRefus());
        dto.setPublicCode(bonCommande.getQrHex());
        dto.setQrImageBase64(bonCommande.getQrImageBase64());

        if (bonCommande.getLignes() != null && !bonCommande.getLignes().isEmpty()) {
            List<LigneBonCommandeDto> lignesDto = bonCommande.getLignes().stream()
                    .map(ligne -> {
                        LigneBonCommandeDto ligneDto = new LigneBonCommandeDto();
                        ligneDto.setId(ligne.getId());
                        ligneDto.setBonCommandeId(bonCommande.getId());
                        if (ligne.getArticle() != null) {
                            ligneDto.setArticleId(ligne.getArticle().getId());
                            try {
                                ArticleSecDto articleDto = articleSecService.getArticleById(ligne.getArticle().getId());
                                ligneDto.setArticle(articleDto);
                            } catch (Exception e) {
                                logger.error("Impossible de charger l'article {}");
                            }
                        }
                        ligneDto.setQuantiteCommandee(ligne.getQuantiteCommandee());
                        ligneDto.setQuantiteRecue(ligne.getQuantiteRecue());
                        ligneDto.setPrixUnitaire(ligne.getPrixUnitaire());
                        ligneDto.setRemarque(ligne.getRemarque());
                        return ligneDto;
                    })
                    .collect(Collectors.toList());
            dto.setLignes(lignesDto);
        } else {
            dto.setLignes(new ArrayList<>());
        }

        return dto;
    }

    private String genererNumeroBC() {
        return generateBusinessCode("numeroBC", "BO");
    }
    @Transactional(readOnly = true)
    public List<BonCommandeDto> getAllBonsCommande() {
        return bonCommandeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public BonCommandeDto getBonCommandeById(UUID id) {
        BonCommande bonCommande = bonCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé avec id: " + id));
        return convertToDto(bonCommande);
    }

    @Transactional
    public BonCommandeDto createBonCommande(BonCommandeDto bonCommandeDto) {
        if (bonCommandeDto == null) {
            throw new RuntimeException("Le bon de commande ne peut pas être null");
        }
        if (bonCommandeDto.getLignes() == null || bonCommandeDto.getLignes().isEmpty()) {
            throw new RuntimeException("Le bon de commande doit contenir au moins une ligne");
        }
        String numeroBC = genererNumeroBC();
        BonCommande bonCommande = new BonCommande();
        bonCommande.setNumeroBC(numeroBC);
        bonCommande.setStatus(StatutBonCommande.EN_ATTENTE);
        bonCommande.setDateReceptionPrevue(bonCommandeDto.getDateReceptionPrevue());
        List<LigneBonCommande> lignes = new ArrayList<>();
        for (LigneBonCommandeDto ligneDto : bonCommandeDto.getLignes()) {

            if (ligneDto.getArticleId() == null) {
                throw new RuntimeException("Chaque ligne doit contenir un article");
            }
            if (ligneDto.getQuantiteCommandee() == null || ligneDto.getQuantiteCommandee() <= 0) {
                throw new RuntimeException("Quantité commandée invalide");
            }
            ArticleSec article = articleSecService.getArticleEntityById(ligneDto.getArticleId());

            LigneBonCommande ligne = new LigneBonCommande();

            ligne.setBonCommande(bonCommande);
            ligne.setArticle(article);
            ligne.setQuantiteCommandee(ligneDto.getQuantiteCommandee());
            ligne.setQuantiteRecue(0);
            ligne.setPrixUnitaire(ligneDto.getPrixUnitaire());
            ligne.setRemarque(ligneDto.getRemarque());
            lignes.add(ligne);
        }
        bonCommande.setLignes(lignes);
        BonCommande savedBon = bonCommandeRepository.save(bonCommande);
        QrCodeInfo qrInfo = generateQrInfo("BONCOMMANDE", savedBon.getId());
        BonCommandeDto result = convertToDto(savedBon);
        result.setPublicCode(qrInfo.getPublicCode());
        result.setQrUrl(qrInfo.getQrUrl());
        result.setQrImageBase64(qrInfo.getQrImageBase64());
        return result;
    }
    @Transactional
    public BonCommandeDto validerBonCommande(UUID id) {
        BonCommande bc = bonCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé avec id: " + id));

        if (bc.getStatus() != StatutBonCommande.EN_ATTENTE) {
            throw new RuntimeException("Seuls les bons en attente peuvent être validés");
        }
        bc.setStatus(StatutBonCommande.VALIDE);
        bc.setDateValidation(LocalDateTime.now());
        BonCommande updated = bonCommandeRepository.save(bc);
        return convertToDto(updated);
    }

    @Transactional
    public BonCommandeDto refuserBonCommande(UUID id, String motif) {
        BonCommande bc = bonCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé avec id: " + id));

        bc.setStatus(StatutBonCommande.REFUSE);
        bc.setMotifRefus(motif);
        BonCommande updated = bonCommandeRepository.save(bc);
        return convertToDto(updated);
    }

    @Transactional
    public BonCommandeDto receptionnerCommande(UUID bonId, List<LigneBonCommandeDto> lignesRecues) {
        BonCommande bc = bonCommandeRepository.findById(bonId)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé avec id: " + bonId));

        if (bc.getStatus() != StatutBonCommande.VALIDE && bc.getStatus() != StatutBonCommande.PARTIELLEMENT_RECU) {
            throw new RuntimeException("Seules les commandes validées ou partiellement reçues peuvent être réceptionnées. Statut actuel: " + bc.getStatus());
        }
        Map<UUID, LigneBonCommande> ligneMap = bc.getLignes().stream().collect(Collectors.toMap(LigneBonCommande::getId, Function.identity()));

        for (LigneBonCommandeDto rec : lignesRecues) {
            if (rec.getId() == null) {
                throw new RuntimeException("L'identifiant de la ligne est obligatoire");
            }
            LigneBonCommande ligne = ligneMap.get(rec.getId());
            if (ligne == null) {
                throw new RuntimeException("Ligne non trouvée pour l'id: " + rec.getId());
            }

            Integer quantiteRecue = rec.getQuantiteRecue();
            if (quantiteRecue == null || quantiteRecue <= 0) {
                throw new RuntimeException("Quantité reçue invalide pour la ligne " + ligne.getId());
            }

            int dejaRecu = ligne.getQuantiteRecue() != null ? ligne.getQuantiteRecue() : 0;
            int quantiteTotale = ligne.getQuantiteCommandee();

            if (dejaRecu + quantiteRecue > quantiteTotale) {
                throw new RuntimeException(
                        "La quantité reçue dépasse la quantité commandée pour l'article " +
                                ligne.getArticle().getNom() +
                                " (déjà reçu: " + dejaRecu + ", commandé: " + quantiteTotale + ")"
                );
            }
            ligne.setQuantiteRecue(dejaRecu + quantiteRecue);
            if (ligne.getArticle() != null) {
                stockSecService.entreeStock(ligne.getArticle().getId(), quantiteRecue, "Réception commande " + bc.getNumeroBC() + " - ligne " + ligne.getArticle().getNom());
            }

            ligneBonCommandeRepository.save(ligne);
        }
        boolean tousRecus = bc.getLignes().stream().allMatch(l -> l.getQuantiteRecue() >= l.getQuantiteCommandee());
        boolean partiellementRecu = bc.getLignes().stream().anyMatch(l -> l.getQuantiteRecue() > 0 && l.getQuantiteRecue() < l.getQuantiteCommandee());

        if (tousRecus) {
            bc.setStatus(StatutBonCommande.RECU);
        } else if (partiellementRecu) {
            bc.setStatus(StatutBonCommande.PARTIELLEMENT_RECU);
        } else {
            bc.setStatus(StatutBonCommande.VALIDE);
        }
        BonCommande updated = bonCommandeRepository.save(bc);
        return convertToDto(updated);
    }

    @Override
    protected String getEntityType() {
        return "BONCOMMANDE";
    }

    @Override
    protected String getLabel(BonCommande entity) {
        if (entity.getNumeroBC() != null && !entity.getNumeroBC().isBlank()) {
            return entity.getNumeroBC();
        }
        return "Bon de commande " + entity.getId();
    }

    @Override
    protected String getStatus(BonCommande entity) {
        return entity.getStatus() != null ? entity.getStatus().name() : "UNKNOWN";
    }

    @Override
    protected String getMobileRoute() {
        return "/bon-commande/detail";
    }

    @Override
    protected String getWebRoute(BonCommande entity) {
        if (entity == null || entity.getId() == null) {
            return "/stock/bons-commande";
        }
        return "/stock/bons-commande/" + entity.getId();
    }

    @Override
    protected Object getData(BonCommande entity) {
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

        Optional<BonCommande> entity = (tenantId == null)
                ? bonCommandeRepository.findByQrHex(normalizedCode)
                : bonCommandeRepository.findByQrHexAndTenantIdAndIsDeletedFalse(normalizedCode, tenantId);

        if (entity.isEmpty() && tenantId != null) {
            entity = bonCommandeRepository.findByQrHex(normalizedCode);
        }

        return entity.map(bon -> {
                    QrResolveResponse response = new QrResolveResponse();
                    response.setEntityType(getEntityType());
                    response.setPublicCode(normalizedCode);
                    response.setEntityId(bon.getId().toString());
                    response.setLabel(getLabel(bon));
                    response.setStatus(getStatus(bon));
                    response.setMobileRoute(getMobileRoute());
                    response.setWebRoute(getWebRoute(bon));
                    response.setData(getData(bon));
                    return response;
                })
                .orElseThrow(() -> new EntityNotFoundException("Bon de commande non trouve pour le code : " + publicCode));
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
