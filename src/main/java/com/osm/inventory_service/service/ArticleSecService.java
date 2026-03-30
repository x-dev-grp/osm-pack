package com.osm.inventory_service.service;

import com.osm.inventory_service.dto.ArticleSecDto;
import com.osm.inventory_service.dto.FournisseurDto;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.Fournisseur;
import com.osm.inventory_service.repository.ArticleSecRepository;
import com.osm.inventory_service.repository.FournisseurRepository;
import com.osm.inventory_service.repository.SKURepository;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    @Lazy
    @Autowired
    public ArticleSecService(BaseRepository<ArticleSec> repository, ArticleSecRepository articleRepository, FournisseurRepository fournisseurRepository, SKURepository skuRepository, ModelMapper modelMapper, StockSecService stockSecService) {
        super(repository, modelMapper);
        this.articleRepository = articleRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.modelMapper = modelMapper;
        this.stockSecService = stockSecService;
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

        ArticleSec savedArticle = articleRepository.save(article);
        try {
            stockSecService.createStockForArticle(savedArticle.getId());
        } catch (Exception e) {
            System.err.println("Erreur lors de la création  du stock: " + e.getMessage());
        }

        return convertToDto(savedArticle);
    }

    @Transactional
    public ArticleSecDto updateArticle(UUID id, ArticleSecDto articleDto) {
        ArticleSec existingArticle = getArticleEntityById(id);
        if (!existingArticle.getNom().equals(articleDto.getNom()) && articleDto.getFournisseur()  != null) {
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
        ArticleSec updatedArticle = articleRepository.save(existingArticle);
        return convertToDto(updatedArticle);
    }
    public List<ArticleSecDto> getAllActiveArticles() {
        return articleRepository.findByActifTrue().stream()
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
        article.setActif(false);
        ArticleSec updatedArticle = articleRepository.save(article);
        return convertToDto(updatedArticle);
    }

    @Override
    public Set<Action> actionsMapping(ArticleSec ArticleSec) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ,Action.CREATE,Action.ENTREE_STOCK,Action.SORTIE_STOCK));
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
        return article;
    }
    private ArticleSecDto convertToDto(ArticleSec article) {
        ArticleSecDto dto = modelMapper.map(article, ArticleSecDto.class);

        if (article.getFournisseur() != null) {
            dto.setFournisseur(modelMapper.map(article.getFournisseur(), FournisseurDto.class));
        }
        return dto;
    }


}