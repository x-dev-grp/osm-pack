package com.abiooc.inventory_service.service;

import com.abiooc.inventory_service.entity.ArticleSec;
import com.abiooc.inventory_service.repository.ArticleSecRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ArticleSecService {

    @Autowired
    private ArticleSecRepository articleSecRepository;

    public List<ArticleSec> getAllArticles() {
        return articleSecRepository.findAll();
    }

    public ArticleSec getArticleById(Long id) {
        return articleSecRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec id: " + id));
    }

    public ArticleSec getArticleBySku(String sku) {
        return articleSecRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec sku: " + sku));
    }

    public ArticleSec createArticle(ArticleSec article) {
        // Vérifier si le SKU existe déjà
        if (articleSecRepository.findBySku(article.getSku()).isPresent()) {
            throw new RuntimeException("Un article avec ce SKU existe déjà: " + article.getSku());
        }
        return articleSecRepository.save(article);
    }

    public ArticleSec updateArticle(Long id, ArticleSec articleDetails) {
        ArticleSec article = getArticleById(id);
        article.setNom(articleDetails.getNom());
        article.setCategorie(articleDetails.getCategorie());
        article.setFournisseurDefaut(articleDetails.getFournisseurDefaut());
        article.setStockMinimum(articleDetails.getStockMinimum());
        article.setStockMaximum(articleDetails.getStockMaximum());
        article.setUniteMesure(articleDetails.getUniteMesure());
        article.setActif(articleDetails.getActif());
        return articleSecRepository.save(article);
    }

    public void deleteArticle(Long id) {
        ArticleSec article = getArticleById(id);
        article.setActif(false);
        articleSecRepository.save(article);
    }
}