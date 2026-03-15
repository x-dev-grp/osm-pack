/*package com.abiooc.inventory_service.service;

import com.abiooc.inventory_service.entity.*;
import com.abiooc.inventory_service.repository.ArticleSecRepository;
import com.abiooc.inventory_service.repository.BonCommandeRepository;
import com.abiooc.inventory_service.repository.StockSecRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.abiooc.inventory_service.Enum.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeuilAlerteService {

    @Autowired
    private ArticleSecRepository articleSecRepository;

    @Autowired
    private StockSecRepository stockSecRepository;

    @Autowired
    private BonCommandeRepository bonCommandeRepository;

    @Autowired
    private BonCommandeService bonCommandeService;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public List<BonCommande> verifierSeuilsEtCreerBonsCommandes() {
        System.out.println(" Vérification automatique des seuils - " + LocalDateTime.now());
        List<BonCommande> bonsCrees = new ArrayList<>();

        // 1. Récupérer tous les articles secs actifs
        List<ArticleSec> articles = articleSecRepository.findByActifTrue();

        for (ArticleSec article : articles) {
            // 2. Récupérer le stock actuel
            StockSec stock = stockSecRepository.findByArticle(article)
                    .orElse(new StockSec(article, 0));

            int stockActuel = stock.getQuantiteActuelle();
            int seuilMinimum = article.getStockMinimum() != null ? article.getStockMinimum() : 0;

            // 3. Vérifier si stock < seuil minimum
            if (seuilMinimum > 0 && stockActuel <= seuilMinimum) {

                System.out.println(" Alerte: " + article.getSku() +
                        " - Stock: " + stockActuel +
                        " / Seuil: " + seuilMinimum);

                // 4. Vérifier qu'il n'y a pas déjà une commande en cours
                boolean commandeEnCours = bonCommandeRepository
                        .existsByLignesArticleAndStatutIn(
                                article,
                                List.of(StatutBonCommande.EN_ATTENTE, StatutBonCommande.VALIDE)
                        );

                if (!commandeEnCours) {
                    // 5. Calculer la quantité à commander
                    int stockMaximum = article.getStockMaximum() != null ? article.getStockMaximum() : seuilMinimum * 2;
                    int quantiteACommander = Math.max(stockMaximum - stockActuel, 1);

                    // 6. Créer le bon de commande automatique
                    BonCommande bc = bonCommandeService.creerBonCommandeAutomatique(
                            article,
                            quantiteACommander,
                            article.getFournisseurDefaut() != null ? article.getFournisseurDefaut() : "Fournisseur par défaut"
                    );

                    bonsCrees.add(bc);
                    System.out.println(" Bon de commande créé: " + bc.getNumeroBC());
                }
            }
        }

        System.out.println("Vérification terminée. " + bonsCrees.size() + " bon(s) créé(s).");
        return bonsCrees;
    }


}*/