package com.osm.inventory_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osm.inventory_service.Enum.CategorieArticle;
import com.osm.inventory_service.config.ArticleConfig;
import com.osm.inventory_service.dto.ArticleSecDto;
import com.osm.inventory_service.dto.EmplacementStockDto;
import com.osm.inventory_service.dto.FournisseurDto;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.Fournisseur;
import com.osm.inventory_service.entity.StockSec;
import com.osm.inventory_service.repository.ArticleSecRepository;
import com.osm.inventory_service.repository.FournisseurRepository;
import com.osm.inventory_service.repository.StockSecRepository;
import com.xdev.xdevbase.config.TenantContext;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.qr.model.QrCodeInfo;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleSecService extends BaseServiceImpl<ArticleSec, ArticleSecDto, ArticleSecDto> {

    private final ArticleSecRepository articleRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ModelMapper modelMapper;
    private final StockSecService stockSecService;
    private final StockSecRepository stockSecRepository;
    private final InventoryDeleteGuardService deleteGuard;
    private final ObjectMapper objectMapper;

    @Lazy
    @Autowired
    public ArticleSecService(BaseRepository<ArticleSec> repository,
                             ArticleSecRepository articleRepository,
                             FournisseurRepository fournisseurRepository,
                             ModelMapper modelMapper,
                             StockSecService stockSecService,
                             StockSecRepository stockSecRepository,
                             InventoryDeleteGuardService deleteGuard,
                             ObjectMapper objectMapper) {
        super(repository, modelMapper);
        this.articleRepository = articleRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.modelMapper = modelMapper;
        this.stockSecService = stockSecService;
        this.stockSecRepository = stockSecRepository;
        this.deleteGuard = deleteGuard;
        this.objectMapper = objectMapper;
    }

    public ArticleSec getArticleEntityById(UUID id) {
        return articleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec ID: " + id));
    }

    public ArticleSecDto toArticleDto(ArticleSec article) {
        if (article == null) {
            return null;
        }
        StockSec stock = stockSecRepository.findByArticleIdAndIsDeletedFalse(article.getId()).orElse(null);
        return convertToDto(article, stock);
    }

     public List<ArticleSecDto> getAllArticles() {
        Map<UUID, StockSec> stockByArticleId = stockSecRepository.findAllByIsDeletedFalse().stream()
                .filter(stock -> stock.getArticle() != null
                        && stock.getArticle().getId() != null
                        && !Boolean.TRUE.equals(stock.getArticle().getDeleted()))
                .collect(Collectors.toMap(stock -> stock.getArticle().getId(), stock -> stock, (left, right) -> left));

        return articleRepository.findAllByIsDeletedFalse().stream()
                .map(article -> convertToDto(article, stockByArticleId.get(article.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public ArticleSecDto getArticleById(UUID id) {
        ArticleSec article = getArticleEntityById(id);
        StockSec stock = stockSecService.ensureStockEntity(article);
        return convertToDto(article, stock);
    }

    @Transactional
    public ArticleSecDto createArticle(ArticleSecDto articleDto) {
        ArticleSec article = convertToEntity(articleDto);
        article.setActif(true);
        if (article.getStockMinimum() == null) {
            article.setStockMinimum(0);
        }
        if (article.getStockMaximum() == null) {
            article.setStockMaximum(0);
        }
        article.validateConfiguration();

        ArticleSec savedArticle = articleRepository.save(article);
        QrCodeInfo qrInfo = generateQrInfo("ARTICLE", savedArticle.getId());
        ArticleSecDto result = convertToDto(savedArticle);
        result.setPublicCode(qrInfo.getPublicCode());
        result.setQrUrl(qrInfo.getQrUrl());
        result.setQrImageBase64(qrInfo.getQrImageBase64());
        try {
            stockSecService.createStockForArticle(savedArticle.getId());
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du stock: " + e.getMessage());
        }

        return convertToDto(savedArticle);
    }

    @Transactional
    public ArticleSecDto updateArticle(UUID id, ArticleSecDto articleDto) {
        ArticleSec existingArticle = getArticleEntityById(id);
        if (!existingArticle.getNom().equals(articleDto.getNom()) && articleDto.getFournisseur() != null) {
            Fournisseur fournisseur = fournisseurRepository.findByIdAndIsDeletedFalse(articleDto.getFournisseur().getId()).orElse(null);
            if (fournisseur != null && articleRepository.existsByNomAndFournisseurAndIsDeletedFalse(articleDto.getNom(), fournisseur)) {
                throw new RuntimeException("Un article avec ce nom existe déjà pour ce fournisseur");
            }
        }

        existingArticle.setNom(articleDto.getNom());
        existingArticle.setCategorie(articleDto.getCategorie());
        existingArticle.setStockMinimum(articleDto.getStockMinimum());
        existingArticle.setStockMaximum(articleDto.getStockMaximum());
        existingArticle.setActif(articleDto.getActif());
        existingArticle.setUm(articleDto.getUm());
        if (articleDto.getConfiguration() != null) {
            try {
                ArticleConfig newConfig = objectMapper.convertValue(articleDto.getConfiguration(), ArticleConfig.class);
                existingArticle.setConfiguration(newConfig);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Configuration invalide pour la catégorie " + articleDto.getCategorie(), e);
            }
        }

        if (articleDto.getFournisseur() != null) {
            if (existingArticle.getFournisseur() == null ||
                    !articleDto.getFournisseur().getId().equals(existingArticle.getFournisseur().getId())) {
                Fournisseur fournisseur = fournisseurRepository.findByIdAndIsDeletedFalse(articleDto.getFournisseur().getId())
                        .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
                existingArticle.setFournisseur(fournisseur);
            }
        } else {
            existingArticle.setFournisseur(null);
        }
        existingArticle.validateConfiguration();

        ArticleSec updatedArticle = articleRepository.save(existingArticle);
        return convertToDto(updatedArticle);
    }
    @Transactional(readOnly = true)
    public List<ArticleSecDto> getAllActiveArticles() {
        return articleRepository.findByActifTrueAndIsDeletedFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<ArticleSecDto> getArticlesByCategorie(CategorieArticle categorie) {
        return articleRepository.findByCategorieAndIsDeletedFalse(categorie).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ArticleSecDto activerArticle(UUID id) {
        ArticleSec article = getArticleEntityById(id);
        article.setActif(true);
        ArticleSec updatedArticle = articleRepository.save(article);
        return convertToDto(updatedArticle);
    }

    @Transactional
    public ArticleSecDto desactiverArticle(UUID id) {
        ArticleSec article = getArticleEntityById(id);
        deleteGuard.assertArticleCanBeRemoved(id);

        article.setActif(false);
        ArticleSec updatedArticle = articleRepository.save(article);
        return convertToDto(updatedArticle);
    }

    @Transactional
    public void supprimerArticle(UUID id) {
        ArticleSec article = getArticleEntityById(id);
        deleteGuard.assertArticleCanBeRemoved(id);

        stockSecRepository.findByArticleIdAndIsDeletedFalse(id).ifPresent(stock -> {
            stock.setDeleted(true);
            stockSecRepository.save(stock);
        });

        article.setDeleted(true);
        article.setActif(false);
        articleRepository.save(article);
    }

    @Override
    @Transactional
    public ArticleSecDto delete(UUID id) {
        ArticleSecDto dto = getArticleById(id);
        supprimerArticle(id);
        return dto;
    }

    @Override
    @Transactional
    public void remove(UUID id) {
        supprimerArticle(id);
    }

    @Override
    public Set<Action> actionsMapping(ArticleSec articleSec) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ, Action.CREATE, Action.ENTREE_STOCK, Action.SORTIE_STOCK));
        return actions;
    }
    private ArticleSec convertToEntity(ArticleSecDto dto) {
        ArticleSec article = modelMapper.map(dto, ArticleSec.class);
        if (dto.getFournisseur() != null && dto.getFournisseur().getId() != null) {
            Fournisseur fournisseur = fournisseurRepository.findByIdAndIsDeletedFalse(dto.getFournisseur().getId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec ID: " + dto.getFournisseur().getId()));
            article.setFournisseur(fournisseur);
        } else {
            article.setFournisseur(null);
        }
        if (dto.getConfiguration() != null) {
            try {
                ArticleConfig config = objectMapper.convertValue(dto.getConfiguration(), ArticleConfig.class);
                article.setConfiguration(config);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Erreur de conversion de la configuration pour la catégorie " + dto.getCategorie(), e);
            }
        }
        return article;
    }
    private int safeQuantity(Integer value) {
        return value == null ? 0 : value;
    }

    private ArticleSecDto convertToDto(ArticleSec article) {
        StockSec stock = stockSecRepository.findByArticleIdAndIsDeletedFalse(article.getId()).orElse(null);
        return convertToDto(article, stock);
    }

    private ArticleSecDto convertToDto(ArticleSec article, StockSec stock) {
        ArticleSecDto dto = modelMapper.map(article, ArticleSecDto.class);
        if (article.getFournisseur() != null) {
            dto.setFournisseur(modelMapper.map(article.getFournisseur(), FournisseurDto.class));
        }
        if (article.getConfiguration() != null) {
            Map<String, Object> configMap = objectMapper.convertValue(
                    article.getConfiguration(),
                    new TypeReference<Map<String, Object>>() {}
            );
            dto.setConfiguration(configMap);
        }
        dto.setPublicCode(article.getQrHex());
        dto.setQrImageBase64(article.getQrImageBase64());
        applyStockSnapshot(dto, article, stock);

        return dto;
    }

    private void applyStockSnapshot(ArticleSecDto dto, ArticleSec article, StockSec stock) {
        int minimum = safeQuantity(article.getStockMinimum());

        if (stock == null) {
            dto.setQuantiteActuelle(0);
            dto.setQuantiteReservee(0);
            dto.setQuantiteDisponible(0);
            dto.setBelowMinimum(false);
            dto.setEmplacement(null);
            dto.setStockId(null);
            dto.setStockLastModifiedDate(null);
            return;
        }

        int actuelle = safeQuantity(stock.getQuantiteActuelle());
        int reservee = safeQuantity(stock.getQuantiteReservee());

        dto.setStockId(stock.getId());
        dto.setQuantiteActuelle(actuelle);
        dto.setQuantiteReservee(reservee);
        dto.setQuantiteDisponible(actuelle - reservee);
        dto.setBelowMinimum(minimum > 0 && actuelle <= minimum);
        dto.setStockLastModifiedDate(stock.getLastModifiedDate());

        if (stock.getEmplacement() != null) {
            dto.setEmplacement(modelMapper.map(stock.getEmplacement(), EmplacementStockDto.class));
        } else {
            dto.setEmplacement(null);
        }
    }
    @Override
    protected String getEntityType() {
        return "ARTICLE";
    }

    @Override
    protected String getLabel(ArticleSec entity) {
        return entity.getNom();
    }

    @Override
    protected String getStatus(ArticleSec entity) {
        return entity.getActif() ? "ACTIF" : "INACTIF";
    }

    @Override
    protected String getMobileRoute() {
        return "/article/detail";
    }

    @Override
    protected String getWebRoute(ArticleSec entity) {
        if (entity == null || entity.getId() == null) {
            return "/stock/articles";
        }
        return "/stock/articles/" + entity.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public QrResolveResponse resolve(String publicCode) {
        if (publicCode == null || publicCode.isBlank()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }

        String normalizedCode = publicCode.trim().toUpperCase(Locale.ROOT);
        UUID tenantId = TenantContext.getCurrentTenant();

        Optional<ArticleSec> entity = (tenantId == null)
                ? articleRepository.findByQrHex(normalizedCode)
                : articleRepository.findByQrHexAndTenantIdAndIsDeletedFalse(normalizedCode, tenantId);

        if (entity.isEmpty() && tenantId != null) {
            entity = articleRepository.findByQrHex(normalizedCode);
        }

        return entity.map(article -> {
                    if (Boolean.TRUE.equals(article.getDeleted())) {
                        throw new EntityNotFoundException("Article non trouve pour le code : " + publicCode);
                    }
                    QrResolveResponse response = new QrResolveResponse();
                    response.setEntityType(getEntityType());
                    response.setPublicCode(normalizedCode);
                    response.setEntityId(article.getId().toString());
                    response.setLabel(getLabel(article));
                    response.setStatus(getStatus(article));
                    response.setMobileRoute(getMobileRoute());
                    response.setWebRoute(getWebRoute(article));
                    response.setData(convertToDto(article));
                    return response;
                })
                .orElseThrow(() -> new EntityNotFoundException("Article non trouve pour le code : " + publicCode));
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
