package com.osm.inventory_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osm.inventory_service.Enum.CategorieArticle;
import com.osm.inventory_service.config.ArticleConfig;
import com.osm.inventory_service.dto.ArticleSecDto;
import com.osm.inventory_service.dto.FournisseurDto;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.Fournisseur;
import com.osm.inventory_service.exception.InventoryBusinessException;
import com.osm.inventory_service.entity.StockSec;
import com.osm.inventory_service.repository.ArticleSecRepository;
import com.osm.inventory_service.repository.BomLineRepository;
import com.osm.inventory_service.repository.FournisseurRepository;
import com.osm.inventory_service.repository.StockSecRepository;
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
    private final BomLineRepository bomLineRepository;
    private final ObjectMapper objectMapper;

    @Lazy
    @Autowired
    public ArticleSecService(BaseRepository<ArticleSec> repository,
                             ArticleSecRepository articleRepository,
                             FournisseurRepository fournisseurRepository,
                             ModelMapper modelMapper,
                             StockSecService stockSecService,
                             StockSecRepository stockSecRepository,
                             BomLineRepository bomLineRepository,
                             ObjectMapper objectMapper) {
        super(repository, modelMapper);
        this.articleRepository = articleRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.modelMapper = modelMapper;
        this.stockSecService = stockSecService;
        this.stockSecRepository = stockSecRepository;
        this.bomLineRepository = bomLineRepository;
        this.objectMapper = objectMapper;
    }

    public ArticleSec getArticleEntityById(UUID id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec ID: " + id));
    }

    public List<ArticleSecDto> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ArticleSecDto getArticleById(UUID id) {
        ArticleSec article = getArticleEntityById(id);
        return convertToDto(article);
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
            Fournisseur fournisseur = fournisseurRepository.findById(articleDto.getFournisseur().getId()).orElse(null);
            if (fournisseur != null && articleRepository.existsByNomAndFournisseur(articleDto.getNom(), fournisseur)) {
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
                Fournisseur fournisseur = fournisseurRepository.findById(articleDto.getFournisseur().getId())
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
        return articleRepository.findByActifTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<ArticleSecDto> getArticlesByCategorie(CategorieArticle categorie) {
        return articleRepository.findByCategorie(categorie).stream()
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

        stockSecRepository.findByArticleId(id).ifPresent(stock -> {
            int actuelle = stock.getQuantiteActuelle() != null ? stock.getQuantiteActuelle() : 0;
            int reservee = stock.getQuantiteReservee() != null ? stock.getQuantiteReservee() : 0;
            if (actuelle > 0 || reservee > 0) {
                throw new InventoryBusinessException(
                        "ARTICLE_STOCK_NOT_EMPTY",
                        "Impossible de desactiver l'article : stock actuel=" + actuelle + ", reserve=" + reservee
                );
            }
        });

        if (bomLineRepository.countByArticle_Id(id) > 0) {
            throw new InventoryBusinessException(
                    "ARTICLE_USED_IN_BOM",
                    "Impossible de desactiver l'article : il est utilise dans une ou plusieurs nomenclatures"
            );
        }

        article.setActif(false);
        ArticleSec updatedArticle = articleRepository.save(article);
        return convertToDto(updatedArticle);
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
            Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseur().getId())
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
    private ArticleSecDto convertToDto(ArticleSec article) {
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

        return dto;
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
    @Transactional(readOnly = true)
    public QrResolveResponse resolve(String publicCode) {
        ArticleSec entity = articleRepository.findByQrHex(publicCode)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé pour le code : " + publicCode));

        QrResolveResponse response = new QrResolveResponse();
        response.setEntityType(getEntityType());
        response.setPublicCode(publicCode);
        response.setEntityId(entity.getId().toString());
        response.setLabel(getLabel(entity));
        response.setStatus(getStatus(entity));
        response.setMobileRoute(getMobileRoute());
        response.setData(convertToDto(entity));
        return response;
    }
}
