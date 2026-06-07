package com.osm.inventory_service.service;

import com.osm.inventory_service.Enum.TypeMouvement;
import com.osm.inventory_service.dto.ArticleStockSummaryDto;
import com.osm.inventory_service.dto.EmplacementStockDto;
import com.osm.inventory_service.dto.MouvementStockSecDto;
import com.osm.inventory_service.dto.StockSecDto;
import com.osm.inventory_service.exception.InventoryBusinessException;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.EmplacementStock;
import com.osm.inventory_service.entity.MouvementStockSec;
import com.osm.inventory_service.entity.StockSec;
import com.osm.inventory_service.repository.EmplacementStockRepository;
import com.osm.inventory_service.repository.MouvementStockSecRepository;
import com.osm.inventory_service.repository.StockSecRepository;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockSecService extends BaseServiceImpl<StockSec, StockSecDto, StockSecDto> {

    private final StockSecRepository stockRepository;
    private final MouvementStockSecRepository mouvementStockSecRepository;
    private final ArticleSecService articleSecService;
    private final EmplacementStockRepository emplacementRepository;
    private final ModelMapper modelMapper;
    private final InventoryDeleteGuardService deleteGuard;

    @Lazy
    @Autowired
    public StockSecService(BaseRepository<StockSec> repository, StockSecRepository stockRepository, MouvementStockSecRepository mouvementStockSecRepository, ArticleSecService articleSecService, EmplacementStockRepository emplacementRepository, ModelMapper modelMapper, InventoryDeleteGuardService deleteGuard) {
        super(repository, modelMapper);
        this.stockRepository = stockRepository;
        this.mouvementStockSecRepository = mouvementStockSecRepository;
        this.articleSecService = articleSecService;
        this.emplacementRepository = emplacementRepository;
        this.modelMapper = modelMapper;
        this.deleteGuard = deleteGuard;
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private void validateStockInvariants(StockSec stock, String operation) {
        int quantiteActuelle = safe(stock.getQuantiteActuelle());
        int quantiteReservee = safe(stock.getQuantiteReservee());

        if (quantiteActuelle < 0) {
            throw new RuntimeException("Operation " + operation + " refusee: quantite actuelle negative");
        }
        if (quantiteReservee < 0) {
            throw new RuntimeException("Operation " + operation + " refusee: quantite reservee negative");
        }
        if (quantiteReservee > quantiteActuelle) {
            throw new RuntimeException(
                    "Operation " + operation + " refusee: quantite reservee (" + quantiteReservee +
                            ") > quantite actuelle (" + quantiteActuelle + ")"
            );
        }
    }

    private StockSec saveWithValidation(StockSec stock, String operation) {
        validateStockInvariants(stock, operation);
        StockSec savedStock = stockRepository.save(stock);
        if (savedStock.getEmplacement() != null) {
            EmplacementStock emplacement = savedStock.getEmplacement();
            emplacement.setCapaciteActuelle(String.valueOf(safe(savedStock.getQuantiteActuelle())));
            emplacementRepository.save(emplacement);
        }
        return savedStock;
    }

    private StockSecDto convertToDto(StockSec stock) {
        StockSecDto dto = modelMapper.map(stock, StockSecDto.class);

        dto.setQuantiteReservee(safe(stock.getQuantiteReservee()));
        dto.setQuantiteDisponible(safe(stock.getQuantiteActuelle()) - safe(stock.getQuantiteReservee()));

        if (stock.getArticle() != null) {
            dto.setArticleId(stock.getArticle().getId());
            dto.setArticle(articleSecService.toArticleDto(stock.getArticle()));
        }

        if (stock.getEmplacement() != null) {
            dto.setEmplacementId(stock.getEmplacement().getId());
            dto.setEmplacement(modelMapper.map(stock.getEmplacement(), EmplacementStockDto.class));
        }

        return dto;
    }

    @Transactional
    public StockSecDto reserverStock(UUID articleId, Integer quantite) {
        if (articleId == null) {
            throw new RuntimeException("L'identifiant de l'article est obligatoire");
        }
        if (quantite == null || quantite <= 0) {
            throw new RuntimeException("La quantite de reservation doit etre positive");
        }

        StockSec stock = stockRepository.findByArticleIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouve pour cet article"));

        int quantiteActuelle = safe(stock.getQuantiteActuelle());
        int quantiteReservee = safe(stock.getQuantiteReservee());
        int quantiteDisponible = quantiteActuelle - quantiteReservee;

        if (quantiteDisponible < quantite) {
            throw new InventoryBusinessException(
                    "INSUFFICIENT_STOCK",
                    "Stock disponible insuffisant pour la reservation. Disponible: " + quantiteDisponible
            );
        }

        stock.setQuantiteReservee(quantiteReservee + quantite);
        StockSec updatedStock = saveWithValidation(stock, "reserver");
        return convertToDto(updatedStock);
    }

    @Transactional
    public StockSecDto annulerReservation(UUID articleId, Integer quantite) {
        if (articleId == null) {
            throw new RuntimeException("L'identifiant de l'article est obligatoire");
        }
        if (quantite == null || quantite <= 0) {
            throw new RuntimeException("La quantite d'annulation doit etre positive");
        }

        StockSec stock = stockRepository.findByArticleIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour cet article"));

        int quantiteReservee = safe(stock.getQuantiteReservee());
        if (quantiteReservee < quantite) {
            throw new RuntimeException(
                    "Annulation refusee: reserve insuffisant. Reserve: " + quantiteReservee + ", demande: " + quantite
            );
        }

        stock.setQuantiteReservee(quantiteReservee - quantite);
        StockSec updatedStock = saveWithValidation(stock, "annuler-reservation");
        return convertToDto(updatedStock);
    }

    @Transactional
    public StockSecDto consommerReservation(UUID articleId, Integer quantite, String motif) {
        return consommerReservation(articleId, quantite, motif, null, null);
    }

    @Transactional
    public StockSecDto consommerReservation(
            UUID articleId,
            Integer quantite,
            String motif,
            String referenceType,
            UUID referenceId) {
        if (articleId == null) {
            throw new RuntimeException("L'identifiant de l'article est obligatoire");
        }
        if (quantite == null || quantite <= 0) {
            throw new RuntimeException("La quantite de consommation doit etre positive");
        }

        StockSec stock = stockRepository.findByArticleIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouve pour cet article"));

        int quantiteActuelle = safe(stock.getQuantiteActuelle());
        int quantiteReservee = safe(stock.getQuantiteReservee());

        if (quantiteActuelle < quantite) {
            throw new RuntimeException("Stock actuel insuffisant pour la consommation. Disponible: " + quantiteActuelle);
        }
        if (quantiteReservee < quantite) {
            throw new RuntimeException("Stock reserve insuffisant pour la consommation. Reserve: " + quantiteReservee);
        }

        stock.setQuantiteActuelle(quantiteActuelle - quantite);
        stock.setQuantiteReservee(quantiteReservee - quantite);
        StockSec updatedStock = saveWithValidation(stock, "consommer-reservation");

        recordMouvement(
                stock,
                quantite,
                TypeMouvement.SORTIE,
                (motif == null ? "" : motif) + " (Consommation Reservee)",
                referenceType != null ? referenceType : "RESERVATION_CONSUMPTION",
                referenceId);

        return convertToDto(updatedStock);
    }

    public StockSec getStockEntityById(UUID id) {
        return stockRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Stock non trouve avec ID: " + id));
    }

    public List<StockSecDto> getAllStocks() {
        return stockRepository.findAllByIsDeletedFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public StockSecDto getStockById(UUID id) {
        StockSec stock = getStockEntityById(id);
        return convertToDto(stock);
    }

    public StockSecDto getStockByArticle(UUID articleId) {
        ArticleSec article = articleSecService.getArticleEntityById(articleId);
        StockSec stock = stockRepository.findByArticleIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouve pour l'article ID: " + articleId));
        return convertToDto(stock);
    }

    @Transactional
    public StockSecDto createStockForArticle(UUID articleId) {
        ArticleSec article = articleSecService.getArticleEntityById(articleId);
        if (stockRepository.findByArticleIdAndIsDeletedFalse(articleId).isPresent()) {
            throw new RuntimeException("Un stock existe deja pour cet article");
        }

        StockSec stock = new StockSec();
        stock.setArticle(article);
        stock.setQuantiteActuelle(0);
        stock.setQuantiteReservee(0);

        StockSec savedStock = saveWithValidation(stock, "create");
        return convertToDto(savedStock);
    }

    @Transactional
    public StockSecDto entreeStock(UUID articleId, Integer quantite, String motif) {
        if (articleId == null) {
            throw new RuntimeException("L'identifiant de l'article est obligatoire");
        }
        if (quantite == null || quantite <= 0) {
            throw new RuntimeException("La quantite doit etre positive");
        }
        StockSec stock = stockRepository.findByArticleIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouve pour cet article"));

        int quantiteActuelle = safe(stock.getQuantiteActuelle());
        stock.setQuantiteActuelle(quantiteActuelle + quantite);
        StockSec updatedStock = saveWithValidation(stock, "entree");

        recordMouvement(stock, quantite, TypeMouvement.ENTREE, motif, null, null);
        return convertToDto(updatedStock);
    }

    @Transactional
    public StockSecDto sortieStock(UUID articleId, Integer quantite, String motif) {
        return sortieStock(articleId, quantite, motif, null, null);
    }

    @Transactional
    public StockSecDto sortieStock(UUID articleId, Integer quantite, String motif, String referenceType, UUID referenceId) {
        if (articleId == null) {
            throw new RuntimeException("L'identifiant de l'article est obligatoire");
        }
        if (quantite == null || quantite <= 0) {
            throw new RuntimeException("La quantite doit etre positive");
        }
        StockSec stock = stockRepository.findByArticleIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouve pour cet article"));

        int quantiteActuelle = safe(stock.getQuantiteActuelle());
        int quantiteReservee = safe(stock.getQuantiteReservee());
        int quantiteDisponible = quantiteActuelle - quantiteReservee;

        if (quantiteDisponible < quantite) {
            throw new InventoryBusinessException(
                    "INSUFFICIENT_STOCK",
                    "Stock disponible insuffisant (hors reservations). Disponible: " + quantiteDisponible +
                            ", Demande: " + quantite
            );
        }

        stock.setQuantiteActuelle(quantiteActuelle - quantite);
        StockSec updatedStock = saveWithValidation(stock, "sortie");
        recordMouvement(stock, quantite, TypeMouvement.SORTIE, motif, referenceType, referenceId);
        return convertToDto(updatedStock);
    }

    @Transactional(readOnly = true)
    public List<ArticleStockSummaryDto> getAllStockSummaries() {
        return stockRepository.findAllByIsDeletedFalse().stream()
                .filter(stock -> stock.getArticle() != null
                        && stock.getArticle().getId() != null
                        && !Boolean.TRUE.equals(stock.getArticle().getDeleted()))
                .map(stock -> {
                    ArticleStockSummaryDto summary = new ArticleStockSummaryDto();
                    summary.setArticleId(stock.getArticle().getId());
                    int minimum = stock.getArticle().getStockMinimum() != null ? stock.getArticle().getStockMinimum() : 0;
                    int actuelle = safe(stock.getQuantiteActuelle());
                    summary.setBelowMinimum(minimum > 0 && actuelle <= minimum);
                    summary.setQuantiteActuelle(actuelle);
                    summary.setQuantiteReservee(safe(stock.getQuantiteReservee()));
                    summary.setQuantiteDisponible(actuelle - safe(stock.getQuantiteReservee()));
                    return summary;
                })
                .collect(Collectors.toList());
    }

    private void recordMouvement(
            StockSec stock,
            int quantite,
            TypeMouvement type,
            String motif,
            String referenceType,
            UUID referenceId) {
        MouvementStockSec mouvement = new MouvementStockSec();
        mouvement.setArticle(stock.getArticle());
        mouvement.setQuantite(quantite);
        mouvement.setTypeMouvement(type);
        mouvement.setMotif(motif);
        mouvement.setDateMouvement(LocalDateTime.now());
        mouvement.setReferenceType(referenceType);
        mouvement.setReferenceId(referenceId);
        mouvementStockSecRepository.save(mouvement);
    }

    @Transactional
    public StockSecDto ajusterStock(UUID articleId, Integer nouvelleQuantite, String motif) {
        if (articleId == null) {
            throw new RuntimeException("L'identifiant de l'article est obligatoire");
        }

        if (nouvelleQuantite == null) {
            throw new RuntimeException("La quantité d'ajustement est obligatoire");
        }

        StockSec stock = stockRepository.findByArticleIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour cet article"));

        int ancienneQuantite = safe(stock.getQuantiteActuelle());
        int quantiteReservee = safe(stock.getQuantiteReservee());

        // nouvelleQuantite = delta d'ajustement
        // Exemple:
        // ancienneQuantite = 100
        // nouvelleQuantite = -20
        // nouvelle quantité finale = 80
        int quantiteFinale = ancienneQuantite + nouvelleQuantite;

        if (quantiteFinale < 0) {
            throw new RuntimeException(
                    "Ajustement refusé: la quantité finale ne peut pas être négative (" + quantiteFinale + ")"
            );
        }

        if (quantiteFinale < quantiteReservee) {
            throw new RuntimeException(
                    "Ajustement refusé: quantité finale (" + quantiteFinale +
                            ") < quantité réservée (" + quantiteReservee + ")"
            );
        }

        stock.setQuantiteActuelle(quantiteFinale);

        StockSec updatedStock = saveWithValidation(stock, "ajuster");

        MouvementStockSec mouvement = new MouvementStockSec();
        mouvement.setArticle(stock.getArticle());

        // Store only the adjustment delta, not the final stock
        mouvement.setQuantite(nouvelleQuantite);

        mouvement.setTypeMouvement(TypeMouvement.AJUSTEMENT);
        mouvement.setMotif(motif);
        mouvement.setDateMouvement(LocalDateTime.now());

        mouvementStockSecRepository.save(mouvement);

        return convertToDto(updatedStock);
    }

    public List<MouvementStockSecDto> getAllMouvementsDto() {
        return mouvementStockSecRepository.findAllByIsDeletedFalse().stream()
                .map(mouvement -> modelMapper.map(mouvement, MouvementStockSecDto.class))
                .collect(Collectors.toList());
    }
@Transactional(readOnly = true)
    public List<MouvementStockSecDto> getMouvementsByArticleDto(UUID articleId) {
        return mouvementStockSecRepository.findByArticleIdNotDeletedOrderByDateMouvementDesc(articleId).stream()
                .map(mouvement -> toArticleMovementDto(mouvement, articleId))
                .collect(Collectors.toList());
    }

    private MouvementStockSecDto toArticleMovementDto(MouvementStockSec mouvement, UUID articleId) {
        MouvementStockSecDto dto = new MouvementStockSecDto();
        dto.setId(mouvement.getId());
        dto.setTenantId(mouvement.getTenantId());
        dto.setDeleted(mouvement.getDeleted());
        dto.setCreatedBy(mouvement.getCreatedBy());
        dto.setCreatedDate(mouvement.getCreatedDate());
        dto.setLastModifiedBy(mouvement.getLastModifiedBy());
        dto.setLastModifiedDate(mouvement.getLastModifiedDate());
        dto.setExternalId(mouvement.getExternalId());
        dto.setArticleId(articleId);
        dto.setQuantite(mouvement.getQuantite());
        dto.setTypeMouvement(mouvement.getTypeMouvement());
        dto.setMotif(mouvement.getMotif());
        dto.setDateMouvement(mouvement.getDateMouvement());
        return dto;
    }

    public List<StockSecDto> getStocksByEmplacement(UUID emplacementId) {
        emplacementRepository.findByIdAndIsDeletedFalse(emplacementId)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouve avec ID: " + emplacementId));

        return stockRepository.findByEmplacementIdAndIsDeletedFalse(emplacementId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<StockSecDto> getStocksByZone(String zone) {
        return stockRepository.findByZone(zone).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<StockSecDto> getStocksAvecEmplacementDisponible() {
        return stockRepository.findByEmplacementDisponible().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockSecDto assignerEmplacement(UUID stockId, UUID emplacementId) {
        StockSec stock = getStockEntityById(stockId);
        EmplacementStock emplacement = emplacementRepository.findByIdAndIsDeletedFalse(emplacementId)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouve"));

        validateEmplacementCategorie(stock, emplacement);
        validateEmplacementAssignable(stock, emplacement);

        stock.setEmplacement(emplacement);
        emplacement.setDisponible(false);
        emplacement.setCapaciteActuelle(String.valueOf(safe(stock.getQuantiteActuelle())));
        emplacementRepository.save(emplacement);
        StockSec updatedStock = stockRepository.save(stock);
        return convertToDto(updatedStock);
    }

    @Transactional
    public StockSecDto transfererEmplacement(UUID stockId, UUID nouvelEmplacementId) {
        StockSec stock = getStockEntityById(stockId);
        EmplacementStock ancienEmplacement = stock.getEmplacement();
        EmplacementStock nouvelEmplacement = emplacementRepository.findByIdAndIsDeletedFalse(nouvelEmplacementId)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouve"));

        validateEmplacementCategorie(stock, nouvelEmplacement);
        validateEmplacementAssignable(stock, nouvelEmplacement);

        stock.setEmplacement(nouvelEmplacement);
        nouvelEmplacement.setDisponible(false);
        nouvelEmplacement.setCapaciteActuelle(String.valueOf(safe(stock.getQuantiteActuelle())));
        emplacementRepository.save(nouvelEmplacement);

        if (ancienEmplacement != null && !Objects.equals(ancienEmplacement.getId(), nouvelEmplacement.getId())) {
            ancienEmplacement.setDisponible(true);
            ancienEmplacement.setCapaciteActuelle("0");
            emplacementRepository.save(ancienEmplacement);
        }

        StockSec updatedStock = stockRepository.save(stock);
        return convertToDto(updatedStock);
    }

    private void validateEmplacementAssignable(StockSec stock, EmplacementStock emplacement) {
        if (!Boolean.TRUE.equals(emplacement.getActif())) {
            throw new RuntimeException("Emplacement inactif: assignation impossible");
        }

        if (!Boolean.TRUE.equals(emplacement.getDisponible())) {
            if (stock.getEmplacement() == null || !Objects.equals(stock.getEmplacement().getId(), emplacement.getId())) {
                throw new RuntimeException("Emplacement non disponible");
            }
        }

        List<StockSec> occupants = stockRepository.findByEmplacementIdAndIsDeletedFalse(emplacement.getId());
        boolean occupiedByAnotherStock = occupants.stream()
                .anyMatch(s -> s.getId() != null && !s.getId().equals(stock.getId()));

        if (occupiedByAnotherStock) {
            throw new RuntimeException("Emplacement deja assigne a un autre stock");
        }
    }

    private void validateEmplacementCategorie(StockSec stock, EmplacementStock emplacement) {
        if (emplacement.getCategorieArticleStocke() != null) {
            ArticleSec article = stock.getArticle();
            if (article == null) {
                throw new RuntimeException("Le stock n'a pas d'article associe");
            }
            if (!article.getCategorie().equals(emplacement.getCategorieArticleStocke())) {
                throw new RuntimeException(
                        String.format("L'emplacement '%s' accepte uniquement les articles de categorie '%s', " +
                                        "mais l'article est de categorie '%s'",
                                emplacement.getNom() != null ? emplacement.getNom() : emplacement.getCode(),
                                emplacement.getCategorieArticleStocke(),
                                article.getCategorie())
                );
            }
        }
    }

    @Transactional
    public StockSecDto retirerEmplacement(UUID stockId) {
        StockSec stock = getStockEntityById(stockId);
        EmplacementStock current = stock.getEmplacement();
        stock.setEmplacement(null);
        StockSec updatedStock = stockRepository.save(stock);

        if (current != null) {
            List<StockSec> remaining = stockRepository.findByEmplacementIdAndIsDeletedFalse(current.getId());
            boolean stillUsed = remaining.stream().anyMatch(s -> !s.getId().equals(stockId));
            if (!stillUsed) {
                current.setDisponible(true);
                current.setCapaciteActuelle("0");
                emplacementRepository.save(current);
            }
        }

        return convertToDto(updatedStock);
    }

    @Transactional
    public StockSec ensureStockEntity(ArticleSec article) {
        return stockRepository.findByArticleIdAndIsDeletedFalse(article.getId())
                .orElseGet(() -> {
                    StockSec created = new StockSec();
                    created.setArticle(article);
                    created.setQuantiteActuelle(0);
                    created.setQuantiteReservee(0);
                    return saveWithValidation(created, "get-or-create");
                });
    }

    @Transactional
    public StockSecDto getOrCreateStockByArticle(UUID articleId) {
        ArticleSec article = articleSecService.getArticleEntityById(articleId);
        return convertToDto(ensureStockEntity(article));
    }

    @Transactional
    public void supprimerStock(UUID id) {
        StockSec stock = getStockEntityById(id);
        deleteGuard.assertStockCanBeRemoved(stock);
        stock.setDeleted(true);
        stockRepository.save(stock);
    }

    @Override
    @Transactional
    public StockSecDto delete(UUID id) {
        StockSecDto dto = getStockById(id);
        supprimerStock(id);
        return dto;
    }

    @Override
    @Transactional
    public void remove(UUID id) {
        supprimerStock(id);
    }

    @Override
    public Set<Action> actionsMapping(StockSec stockSec) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(
                Action.UPDATE,
                Action.DELETE,
                Action.READ,
                Action.CREATE,
                Action.ENTREE_STOCK,
                Action.SORTIE_STOCK,
                Action.ASSIGN_EMPLACEMENT,
                Action.LIBERER_STOCK,
                Action.RESERVER_STOCK
        ));
        return actions;
    }
}
