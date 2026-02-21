package com.abiooc.inventory_service.service;

import com.abiooc.inventory_service.entity.*;
import com.abiooc.inventory_service.repository.StockSecRepository;
import com.abiooc.inventory_service.repository.MouvementStockSecRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import com.abiooc.inventory_service.Enum.TypeMouvement;
import java.util.List;

@Service
public class StockSecService {

    @Autowired
    private StockSecRepository stockSecRepository;

    @Autowired
    private MouvementStockSecRepository mouvementStockSecRepository;

    @Autowired
    private ArticleSecService articleSecService;

    public StockSec getStockByArticle(Long articleId) {
        ArticleSec article = articleSecService.getArticleById(articleId);
        return stockSecRepository.findByArticle(article)
                .orElse(new StockSec(article, 0));
    }

    public List<MouvementStockSec> getHistoriqueMouvements(Long articleId) {
        ArticleSec article = articleSecService.getArticleById(articleId);
        return mouvementStockSecRepository.findByArticleOrderByDateMouvementDesc(article);
    }

    @Transactional
    public StockSec entreeStock(Long articleId, Integer quantite, String motif) {
        ArticleSec article = articleSecService.getArticleById(articleId);

        // Récupérer ou créer le stock
        StockSec stock = stockSecRepository.findByArticle(article)
                .orElse(new StockSec(article, 0));

        // Augmenter la quantité
        stock.setQuantiteActuelle(stock.getQuantiteActuelle() + quantite);
        stock.setDateDerniereMaj(LocalDateTime.now());
        stockSecRepository.save(stock);

        // Enregistrer le mouvement
        MouvementStockSec mouvement = new MouvementStockSec();
        mouvement.setArticle(article);
        mouvement.setQuantite(quantite);
        mouvement.setTypeMouvement(TypeMouvement.ENTREE);
        mouvement.setMotif(motif);
        mouvementStockSecRepository.save(mouvement);

        return stock;
    }

    @Transactional
    public StockSec sortieStock(Long articleId, Integer quantite, String motif) {
        ArticleSec article = articleSecService.getArticleById(articleId);

        StockSec stock = stockSecRepository.findByArticle(article)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé pour l'article: " + article.getSku()));

        // Vérifier stock suffisant
        if (stock.getQuantiteActuelle() < quantite) {
            throw new RuntimeException("Stock insuffisant. Disponible: " + stock.getQuantiteActuelle());
        }

        // Diminuer la quantité
        stock.setQuantiteActuelle(stock.getQuantiteActuelle() - quantite);
        stock.setDateDerniereMaj(LocalDateTime.now());
        stockSecRepository.save(stock);

        // Enregistrer le mouvement
        MouvementStockSec mouvement = new MouvementStockSec();
        mouvement.setArticle(article);
        mouvement.setQuantite(quantite);
        mouvement.setTypeMouvement(TypeMouvement.SORTIE);
        mouvement.setMotif(motif);
        mouvementStockSecRepository.save(mouvement);

        return stock;
    }
}