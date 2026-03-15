package com.abiooc.inventory_service.service;

import com.abiooc.inventory_service.Enum.CategorieArticle;
import com.abiooc.inventory_service.Enum.UniteMesure;
import com.abiooc.inventory_service.dto.ArticleSecDto;
import com.abiooc.inventory_service.dto.FournisseurDto;
import com.abiooc.inventory_service.dto.SKUDto;
import com.abiooc.inventory_service.entity.ArticleSec;
import com.abiooc.inventory_service.entity.Fournisseur;
import com.abiooc.inventory_service.entity.SKU;
import com.abiooc.inventory_service.repository.ArticleSecRepository;
import com.abiooc.inventory_service.repository.FournisseurRepository;
import com.abiooc.inventory_service.repository.SKURepository;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArticleSecService extends BaseServiceImpl<ArticleSec, ArticleSecDto, ArticleSecDto> {

    private final ArticleSecRepository articleRepository;
    private final FournisseurRepository fournisseurRepository;
    private final SKURepository skuRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ArticleSecService(BaseRepository<ArticleSec> repository, ArticleSecRepository articleRepository, FournisseurRepository fournisseurRepository, SKURepository skuRepository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.articleRepository = articleRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.skuRepository = skuRepository;
        this.modelMapper = modelMapper;
    }

    private ArticleSecDto convertToDto(ArticleSec article) {
        ArticleSecDto dto = modelMapper.map(article, ArticleSecDto.class);

        if (article.getFournisseur() != null) {
            dto.setFournisseur(modelMapper.map(article.getFournisseur(), FournisseurDto.class));
         }
        if (article.getReference() != null) {
            dto.setReferenceId(article.getReference().getId());
            dto.setReference(modelMapper.map(article.getReference(), SKUDto.class));
        }

        return dto;
    }

    private ArticleSec convertToEntity(ArticleSecDto dto) {
        ArticleSec article = modelMapper.map(dto, ArticleSec.class);
        if (dto.getFournisseur() != null) {
            Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseur().getId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec ID: " + dto.getFournisseur().getId()));
            article.setFournisseur(fournisseur);
        }
        if (dto.getReferenceId() != null) {
            SKU sku = skuRepository.findById(dto.getReferenceId())
                    .orElseThrow(() -> new RuntimeException("SKU non trouvé avec ID: " + dto.getReferenceId()));
            article.setReference(sku);
        }

        return article;
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


    public List<ArticleSecDto> rechercherArticles(String nom, CategorieArticle categorie, UUID fournisseurId, Boolean actif) {
        return articleRepository.rechercherArticles(nom, categorie, fournisseurId, actif).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public ArticleSecDto createArticle(ArticleSecDto articleDto) {
        if (articleDto.getFournisseur()  != null) {
            Fournisseur fournisseur = fournisseurRepository.findById(articleDto.getFournisseur().getId()).orElse(null);
            if (fournisseur != null && articleRepository.existsByNomAndFournisseur(articleDto.getNom(), fournisseur)) {
                throw new RuntimeException("Un article avec ce nom existe déjà pour ce fournisseur");
            }
        }

        ArticleSec article = convertToEntity(articleDto);

        if (article.getActif() == null) {
            article.setActif(true);
        }
        if (article.getStockMinimum() == null) {
            article.setStockMinimum(0);
        }
        if (article.getStockMaximum() == null) {
            article.setStockMaximum(0);
        }

        ArticleSec savedArticle = articleRepository.save(article);
        return convertToDto(savedArticle);
    }

    @Transactional
    public List<ArticleSecDto> createArticles(List<ArticleSecDto> articleDtos) {
        return articleDtos.stream()
                .map(this::createArticle)
                .collect(Collectors.toList());
    }

    @Transactional
    public ArticleSecDto updateArticle(UUID id, ArticleSecDto articleDto) {
        ArticleSec existingArticle = getArticleEntityById(id);
        if (!existingArticle.getNom().equals(articleDto.getNom()) && articleDto.getFournisseur().getId() != null) {
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

        if (articleDto.getFournisseur().getId() != null) {
            if (existingArticle.getFournisseur() == null ||
                    !articleDto.getFournisseur().getId().equals(existingArticle.getFournisseur().getId())) {
                Fournisseur fournisseur = fournisseurRepository.findById(articleDto.getFournisseur().getId())
                        .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
                existingArticle.setFournisseur(fournisseur);
            }
        } else {
            existingArticle.setFournisseur(null);
        }


        if (articleDto.getReferenceId() != null) {
            if (existingArticle.getReference() == null ||
                    !articleDto.getReferenceId().equals(existingArticle.getReference().getId())) {
                SKU sku = skuRepository.findById(articleDto.getReferenceId())
                        .orElseThrow(() -> new RuntimeException("SKU non trouvé"));
                existingArticle.setReference(sku);
            }
        } else {
            existingArticle.setReference(null);
        }

        ArticleSec updatedArticle = articleRepository.save(existingArticle);
        return convertToDto(updatedArticle);
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




    @Transactional
    public void deleteArticle(UUID id) {
        ArticleSec article = getArticleEntityById(id);
        articleRepository.delete(article);
    }

}