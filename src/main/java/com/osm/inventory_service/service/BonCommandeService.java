package com.osm.inventory_service.service;

import com.osm.inventory_service.Enum.StatutBonCommande;
import com.osm.inventory_service.dto.BonCommandeDto;
import com.osm.inventory_service.dto.LigneBonCommandeDto;
import com.osm.inventory_service.entity.*;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.BonCommande;
import com.osm.inventory_service.entity.LigneBonCommande;
import com.osm.inventory_service.repository.BonCommandeRepository;
import com.osm.inventory_service.repository.LigneBonCommandeRepository;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BonCommandeService extends BaseServiceImpl<BonCommande, BonCommandeDto, BonCommandeDto> {

    private final BonCommandeRepository bonCommandeRepository;
    private final LigneBonCommandeRepository ligneBonCommandeRepository;
    private final ArticleSecService articleSecService;
    private final StockSecService stockSecService;
    private final ModelMapper modelMapper;

    @Autowired
    public BonCommandeService(BaseRepository<BonCommande> repository,
                              BonCommandeRepository bonCommandeRepository,
                              LigneBonCommandeRepository ligneBonCommandeRepository,
                              ArticleSecService articleSecService,
                              StockSecService stockSecService,
                              ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.bonCommandeRepository = bonCommandeRepository;
        this.ligneBonCommandeRepository = ligneBonCommandeRepository;
        this.articleSecService = articleSecService;
        this.stockSecService = stockSecService;
        this.modelMapper = modelMapper;
    }

    private BonCommandeDto convertToDto(BonCommande bonCommande) {
        BonCommandeDto dto = modelMapper.map(bonCommande, BonCommandeDto.class);

        if (bonCommande.getLignes() != null && !bonCommande.getLignes().isEmpty()) {
            List<LigneBonCommandeDto> lignesDto = bonCommande.getLignes().stream()
                    .map(ligne -> {
                        LigneBonCommandeDto ligneDto = modelMapper.map(ligne, LigneBonCommandeDto.class);
                        ligneDto.setBonCommandeId(bonCommande.getId());
                        if (ligne.getArticle() != null) {
                            ligneDto.setArticleId(ligne.getArticle().getId());
                        }
                        return ligneDto;
                    })
                    .collect(Collectors.toList());
            dto.setLignes(lignesDto);
        }

        return dto;
    }

    private BonCommande convertToEntity(BonCommandeDto dto) {
        BonCommande bonCommande = modelMapper.map(dto, BonCommande.class);
        return bonCommande;
    }

    private String genererNumeroBC() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return "BC-" + LocalDateTime.now().format(formatter) + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    public List<BonCommandeDto> getAllBonsCommande() {
        return bonCommandeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BonCommandeDto getBonCommandeById(UUID id) {
        BonCommande bonCommande = bonCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé avec id: " + id));
        return convertToDto(bonCommande);
    }

    public List<BonCommandeDto> getBonsCommandeByStatut(StatutBonCommande statut) {
        return bonCommandeRepository.findByStatus(statut).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public BonCommandeDto createBonCommande(BonCommandeDto bonCommandeDto) {
        bonCommandeDto.setNumeroBC(genererNumeroBC());
        if (bonCommandeRepository.existsByNumeroBC(bonCommandeDto.getNumeroBC())) {
            throw new RuntimeException("Un bon de commande avec ce numéro existe déjà: " + bonCommandeDto.getNumeroBC());
        }
        if (bonCommandeDto.getStatut() == null) {
            bonCommandeDto.setStatut(StatutBonCommande.EN_ATTENTE);
        }
        bonCommandeDto.setDateCreation(LocalDateTime.now());

        BonCommande bonCommande = convertToEntity(bonCommandeDto);
        BonCommande savedBon = bonCommandeRepository.save(bonCommande);
        if (bonCommandeDto.getLignes() != null && !bonCommandeDto.getLignes().isEmpty()) {
            for (LigneBonCommandeDto ligneDto : bonCommandeDto.getLignes()) {
                LigneBonCommande ligne = new LigneBonCommande();
                ligne.setBonCommande(savedBon);
                ligne.setQuantiteCommandee(ligneDto.getQuantiteCommandee());
                ligne.setQuantiteRecue(0);
                ligne.setPrixUnitaire(ligneDto.getPrixUnitaire());
                ligne.setRemarque(ligneDto.getRemarque());

                if (ligneDto.getArticleId() != null) {
                    ArticleSec article = articleSecService.getArticleEntityById(ligneDto.getArticleId());
                    ligne.setArticle(article);
                }

                ligneBonCommandeRepository.save(ligne);
            }
        }

        return convertToDto(savedBon);
    }

    @Transactional
    public BonCommandeDto updateBonCommande(UUID id, BonCommandeDto bonCommandeDto) {
        BonCommande existingBon = bonCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé avec id: " + id));
        if (existingBon.getStatus() != StatutBonCommande.EN_ATTENTE &&
                existingBon.getStatus() != StatutBonCommande.BROUILLON) {
            throw new RuntimeException("Seuls les bons en attente ou brouillon peuvent être modifiés");
        }

        existingBon.setFournisseur(bonCommandeDto.getFournisseur());
        existingBon.setDateReceptionPrevue(bonCommandeDto.getDateReceptionPrevue());

        if (bonCommandeDto.getStatut() != null) {
            existingBon.setStatus(bonCommandeDto.getStatut());
        }

        bonCommandeRepository.save(existingBon);
        ligneBonCommandeRepository.deleteByBonCommandeId(existingBon.getId());

        if (bonCommandeDto.getLignes() != null && !bonCommandeDto.getLignes().isEmpty()) {
            for (LigneBonCommandeDto ligneDto : bonCommandeDto.getLignes()) {
                LigneBonCommande newLigne = new LigneBonCommande();
                newLigne.setBonCommande(existingBon);
                newLigne.setQuantiteCommandee(ligneDto.getQuantiteCommandee());
                newLigne.setQuantiteRecue(0);
                newLigne.setPrixUnitaire(ligneDto.getPrixUnitaire());
                newLigne.setRemarque(ligneDto.getRemarque());

                if (ligneDto.getArticleId() != null) {
                    ArticleSec article = articleSecService.getArticleEntityById(ligneDto.getArticleId());
                    newLigne.setArticle(article);
                }

                ligneBonCommandeRepository.save(newLigne);
            }
        }

        return convertToDto(existingBon);
    }

    @Transactional
    public void deleteBonCommande(UUID id) {
        BonCommande bonCommande = bonCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé avec id: " + id));
        bonCommandeRepository.delete(bonCommande);
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

        bc.setStatus(StatutBonCommande.ANNULE);
        bc.setMotifRefus(motif);
        BonCommande updated = bonCommandeRepository.save(bc);
        return convertToDto(updated);
    }

    @Transactional
    public BonCommandeDto receptionnerCommande(UUID id, Integer quantiteRecue) {
        BonCommande bc = bonCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé avec id: " + id));

        if (bc.getStatus() != StatutBonCommande.VALIDE) {
            throw new RuntimeException("Seules les commandes validées peuvent être réceptionnées. Statut actuel: " + bc.getStatus());
        }

        List<LigneBonCommande> lignes = bc.getLignes();
        if (lignes == null || lignes.isEmpty()) {
            throw new RuntimeException("Ce bon de commande n'a pas de lignes");
        }

        int resteTotal = quantiteRecue;

        for (LigneBonCommande ligne : lignes) {
            if (resteTotal <= 0) break;

            int resteARecevoir = ligne.getQuantiteCommandee() - ligne.getQuantiteRecue();
            if (resteARecevoir <= 0) continue;

            int qteARecevoir = Math.min(resteARecevoir, resteTotal);
            ligne.setQuantiteRecue(ligne.getQuantiteRecue() + qteARecevoir);
            resteTotal -= qteARecevoir;

            ligneBonCommandeRepository.save(ligne);
            if (ligne.getArticle() != null) {
                stockSecService.entreeStock(  // OK
                        ligne.getArticle().getId(),
                        qteARecevoir,
                        "Réception commande " + bc.getNumeroBC()
                );
            }
        }
        boolean tousRecus = lignes.stream()
                .allMatch(l -> l.getQuantiteRecue() >= l.getQuantiteCommandee());

        bc.setStatus(tousRecus ? StatutBonCommande.RECU : StatutBonCommande.PARTIELLEMENT_RECU);
        BonCommande updated = bonCommandeRepository.save(bc);

        return convertToDto(updated);
    }

    @Transactional
    public BonCommandeDto creerBonCommandeAutomatique(UUID articleId, Integer quantite, String fournisseur) {
        ArticleSec article = articleSecService.getArticleEntityById(articleId);

        BonCommandeDto dto = new BonCommandeDto();
        dto.setFournisseur(fournisseur);
        dto.setStatut(StatutBonCommande.EN_ATTENTE);

        LigneBonCommandeDto ligneDto = new LigneBonCommandeDto();
        ligneDto.setArticleId(articleId);
        ligneDto.setQuantiteCommandee(quantite);
        dto.setLignes(List.of(ligneDto));

        return createBonCommande(dto);
    }

}