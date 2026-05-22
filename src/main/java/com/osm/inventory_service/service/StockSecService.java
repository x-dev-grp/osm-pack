package com.osm.inventory_service.service;

import com.osm.inventory_service.Enum.TypeMouvement;
import com.osm.inventory_service.dto.EmplacementStockDto;
import com.osm.inventory_service.dto.MouvementStockSecDto;
import com.osm.inventory_service.dto.StockSecDto;
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
    @Lazy
    @Autowired
    public StockSecService(BaseRepository<StockSec> repository, StockSecRepository stockRepository, MouvementStockSecRepository mouvementStockSecRepository, ArticleSecService articleSecService, EmplacementStockRepository emplacementRepository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.stockRepository = stockRepository;
        this.mouvementStockSecRepository = mouvementStockSecRepository;
        this.articleSecService = articleSecService;
        this.emplacementRepository = emplacementRepository;
        this.modelMapper = modelMapper;
    }

    private StockSecDto convertToDto(StockSec stock) {
        StockSecDto dto = modelMapper.map(stock, StockSecDto.class);

        dto.setQuantiteReservee(stock.getQuantiteReservee() != null ? stock.getQuantiteReservee() : 0);
        dto.setQuantiteDisponible(
                (stock.getQuantiteActuelle() != null ? stock.getQuantiteActuelle() : 0) -
                (stock.getQuantiteReservee() != null ? stock.getQuantiteReservee() : 0)
        );

        if (stock.getArticle() != null) {
            dto.setArticleId(stock.getArticle().getId());
            dto.setArticle(articleSecService.getArticleById(stock.getArticle().getId()));
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
            throw new RuntimeException("La quantité de réservation doit être positive");
        }

        StockSec stock = stockRepository.findByArticleId(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour cet article"));

        Integer quantiteActuelle = stock.getQuantiteActuelle() != null ? stock.getQuantiteActuelle() : 0;
        Integer quantiteReservee = stock.getQuantiteReservee() != null ? stock.getQuantiteReservee() : 0;

        if (quantiteActuelle - quantiteReservee < quantite) {
            throw new RuntimeException("Stock disponible insuffisant pour la réservation. Disponible: " + (quantiteActuelle - quantiteReservee));
        }

        stock.setQuantiteReservee(quantiteReservee + quantite);
        StockSec updatedStock = stockRepository.save(stock);
        return convertToDto(updatedStock);
    }

    @Transactional
    public StockSecDto annulerReservation(UUID articleId, Integer quantite) {
        if (articleId == null) {
            throw new RuntimeException("L'identifiant de l'article est obligatoire");
        }
        if (quantite == null || quantite <= 0) {
            throw new RuntimeException("La quantité d'annulation doit être positive");
        }

        StockSec stock = stockRepository.findByArticleId(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour cet article"));

        Integer quantiteReservee = stock.getQuantiteReservee() != null ? stock.getQuantiteReservee() : 0;

        if (quantiteReservee < quantite) {
            stock.setQuantiteReservee(0);
        } else {
            stock.setQuantiteReservee(quantiteReservee - quantite);
        }

        StockSec updatedStock = stockRepository.save(stock);
        return convertToDto(updatedStock);
    }

    @Transactional
    public StockSecDto consommerReservation(UUID articleId, Integer quantite, String motif) {
        if (articleId == null) {
            throw new RuntimeException("L'identifiant de l'article est obligatoire");
        }
        if (quantite == null || quantite <= 0) {
            throw new RuntimeException("La quantité de consommation doit être positive");
        }

        StockSec stock = stockRepository.findByArticleId(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour cet article"));

        Integer quantiteActuelle = stock.getQuantiteActuelle() != null ? stock.getQuantiteActuelle() : 0;
        Integer quantiteReservee = stock.getQuantiteReservee() != null ? stock.getQuantiteReservee() : 0;

        if (quantiteActuelle < quantite) {
            throw new RuntimeException("Stock actuel insuffisant pour la consommation. Disponible: " + quantiteActuelle);
        }
        if (quantiteReservee < quantite) {
            throw new RuntimeException("Stock reserve insuffisant pour la consommation. Reserve: " + quantiteReservee);
        }

        // Decrease both total stock and reserved stock
        stock.setQuantiteActuelle(quantiteActuelle - quantite);
        stock.setQuantiteReservee(quantiteReservee - quantite);

        StockSec updatedStock = stockRepository.save(stock);

        MouvementStockSec mouvement = new MouvementStockSec();
        mouvement.setArticle(stock.getArticle());
        mouvement.setQuantite(quantite);
        mouvement.setTypeMouvement(TypeMouvement.SORTIE);
        mouvement.setMotif(motif + " (Consommation Réservée)");
        mouvement.setDateMouvement(LocalDateTime.now());
        mouvementStockSecRepository.save(mouvement);

        return convertToDto(updatedStock);
    }

    public StockSec getStockEntityById(UUID id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé avec ID: " + id));
    }

    public List<StockSecDto> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public StockSecDto getStockById(UUID id) {
        StockSec stock = getStockEntityById(id);
        return convertToDto(stock);
    }

    public StockSecDto getStockByArticle(UUID articleId) {
        articleSecService.getArticleById(articleId);

        StockSec stock = stockRepository.findByArticleId(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour l'article ID: " + articleId));
        return convertToDto(stock);
    }


    @Transactional
    public StockSecDto createStockForArticle(UUID articleId) {
        ArticleSec article = articleSecService.getArticleEntityById(articleId);
        if (stockRepository.findByArticleId(articleId).isPresent()) {
            throw new RuntimeException("Un stock existe déjà pour cet article");
        }

        StockSec stock = new StockSec();
        stock.setArticle(article);
        stock.setQuantiteActuelle(0);

        StockSec savedStock = stockRepository.save(stock);
        return convertToDto(savedStock);
    }

    @Transactional
    public StockSecDto entreeStock(UUID articleId, Integer quantite, String motif) {
        if (articleId == null) {
            throw new RuntimeException("L'identifiant de l'article est obligatoire");
        }
        if (quantite == null || quantite <= 0) {
            throw new RuntimeException("La quantité doit être positive");
        }
        StockSec stock = stockRepository.findByArticleId(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour cet article"));

        Integer quantiteActuelle = stock.getQuantiteActuelle() != null ? stock.getQuantiteActuelle() : 0;
        stock.setQuantiteActuelle(quantiteActuelle + quantite);
        StockSec updatedStock = stockRepository.save(stock);
        ArticleSec article = stock.getArticle();

        MouvementStockSec mouvement = new MouvementStockSec();
        mouvement.setArticle(article);
        mouvement.setQuantite(quantite);
        mouvement.setTypeMouvement(TypeMouvement.ENTREE);
        mouvement.setMotif(motif);
        mouvement.setDateMouvement(LocalDateTime.now());
        mouvementStockSecRepository.save(mouvement);
        return convertToDto(updatedStock);
    }

    @Transactional
    public StockSecDto sortieStock(UUID articleId, Integer quantite, String motif) {
        if (articleId == null) {
            throw new RuntimeException("L'identifiant de l'article est obligatoire");
        }
        if (quantite == null || quantite <= 0) {
            throw new RuntimeException("La quantité doit être positive");
        }
        StockSec stock = stockRepository.findByArticleId(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour cet article"));
        Integer quantiteActuelle = stock.getQuantiteActuelle() != null ? stock.getQuantiteActuelle() : 0;
        Integer quantiteReservee = stock.getQuantiteReservee() != null ? stock.getQuantiteReservee() : 0;
        Integer quantiteDisponible = quantiteActuelle - quantiteReservee;

        if (quantiteDisponible < quantite) {
            throw new RuntimeException(
                    "Stock disponible insuffisant (hors reservations). Disponible: " + quantiteDisponible +
                            ", Demande: " + quantite
            );
        }
        stock.setQuantiteActuelle(quantiteActuelle - quantite);
        StockSec updatedStock = stockRepository.save(stock);
        MouvementStockSec mouvement = new MouvementStockSec();
        mouvement.setArticle(stock.getArticle());
        mouvement.setQuantite(quantite);
        mouvement.setTypeMouvement(TypeMouvement.SORTIE);
        mouvement.setMotif(motif);
        mouvement.setDateMouvement(LocalDateTime.now());
        mouvementStockSecRepository.save(mouvement);
        return convertToDto(updatedStock);
    }

    @Transactional
    public StockSecDto ajusterStock(UUID articleId, Integer nouvelleQuantite, String motif) {
        if (articleId == null) {
            throw new RuntimeException("L'identifiant de l'article est obligatoire");
        }
        if (nouvelleQuantite == null || nouvelleQuantite < 0) {
            throw new RuntimeException("La quantité ne peut pas être négative");
        }
        StockSec stock = stockRepository.findByArticleId(articleId)
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour cet article"));
        Integer ancienneQuantite = stock.getQuantiteActuelle() != null ? stock.getQuantiteActuelle() : 0;
        stock.setQuantiteActuelle(nouvelleQuantite);
        StockSec updatedStock = stockRepository.save(stock);

        MouvementStockSec mouvement = new MouvementStockSec();
        mouvement.setArticle(stock.getArticle());
        mouvement.setQuantite(nouvelleQuantite - ancienneQuantite);
        mouvement.setTypeMouvement(TypeMouvement.AJUSTEMENT);
        mouvement.setMotif(motif);
        mouvement.setDateMouvement(LocalDateTime.now());
        mouvementStockSecRepository.save(mouvement);
        return convertToDto(updatedStock);
    }
    public List<MouvementStockSecDto> getAllMouvementsDto() {
        return mouvementStockSecRepository.findAll().stream()
                .map(mouvement -> modelMapper.map(mouvement, MouvementStockSecDto.class))
                .collect(Collectors.toList());
    }

    public List<MouvementStockSecDto> getMouvementsByArticleDto(UUID articleId) {
        articleSecService.getArticleById(articleId);

        return mouvementStockSecRepository.findByArticleId(articleId).stream()
                .map(mouvement -> modelMapper.map(mouvement, MouvementStockSecDto.class))
                .collect(Collectors.toList());
    }

    public List<StockSecDto> getStocksByEmplacement(UUID emplacementId) {
        emplacementRepository.findById(emplacementId)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé avec ID: " + emplacementId));

        return stockRepository.findByEmplacementId(emplacementId).stream()
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
        EmplacementStock emplacement = emplacementRepository.findById(emplacementId)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé"));

        // Validation de la catégorie
        validateEmplacementCategorie(stock, emplacement);

        stock.setEmplacement(emplacement);
        StockSec updatedStock = stockRepository.save(stock);
        return convertToDto(updatedStock);
    }

    @Transactional
    public StockSecDto transfererEmplacement(UUID stockId, UUID nouvelEmplacementId) {
        StockSec stock = getStockEntityById(stockId);
        EmplacementStock nouvelEmplacement = emplacementRepository.findById(nouvelEmplacementId)
                .orElseThrow(() -> new RuntimeException("Emplacement non trouvé"));

        // Validation de la catégorie
        validateEmplacementCategorie(stock, nouvelEmplacement);

        stock.setEmplacement(nouvelEmplacement);
        StockSec updatedStock = stockRepository.save(stock);
        return convertToDto(updatedStock);
    }

    private void validateEmplacementCategorie(StockSec stock, EmplacementStock emplacement) {
        if (emplacement.getCategorieArticleStocke() != null) {
            ArticleSec article = stock.getArticle();
            if (article == null) {
                throw new RuntimeException("Le stock n'a pas d'article associé");
            }
            if (!article.getCategorie().equals(emplacement.getCategorieArticleStocke())) {
                throw new RuntimeException(
                        String.format("L'emplacement '%s' accepte uniquement les articles de catégorie '%s', " +
                                        "mais l'article est de catégorie '%s'",
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
        stock.setEmplacement(null);
        StockSec updatedStock = stockRepository.save(stock);
        return convertToDto(updatedStock);
    }

    @Override
    public Set<Action> actionsMapping(StockSec StockSec) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ,Action.CREATE,Action.ENTREE_STOCK,Action.SORTIE_STOCK,Action.ASSIGN_EMPLACEMENT,Action.LIBERER_STOCK,Action.RESERVER_STOCK));
        return actions;
    }


}
