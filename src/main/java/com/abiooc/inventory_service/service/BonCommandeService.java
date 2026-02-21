package com.abiooc.inventory_service.service;

import com.abiooc.inventory_service.Enum.StatutBonCommande;
import com.abiooc.inventory_service.entity.*;
import com.abiooc.inventory_service.repository.BonCommandeRepository;
import com.abiooc.inventory_service.repository.LigneBonCommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BonCommandeService {

    @Autowired
    private BonCommandeRepository bonCommandeRepository;

    @Autowired
    private LigneBonCommandeRepository ligneBonCommandeRepository;

    @Autowired
    private StockSecService stockSecService;

    public List<BonCommande> getAllBonsCommande() {
        return bonCommandeRepository.findAll();
    }

    public List<BonCommande> getBonsCommandeByStatut(StatutBonCommande statut) {
        return bonCommandeRepository.findByStatut(statut);
    }

    public BonCommande getBonCommandeById(Long id) {
        return bonCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé avec id: " + id));
    }

    @Transactional
    public BonCommande creerBonCommandeAutomatique(ArticleSec article, Integer quantite, String fournisseur) {
        BonCommande bc = new BonCommande();
        bc.setNumeroBC(genererNumeroBC());
        bc.setFournisseur(fournisseur);
        bc.setStatut(StatutBonCommande.EN_ATTENTE);
        bc.setDateCreation(LocalDateTime.now());

        bc = bonCommandeRepository.save(bc);

        LigneBonCommande ligne = new LigneBonCommande();
        ligne.setBonCommande(bc);
        ligne.setArticle(article);
        ligne.setQuantiteCommandee(quantite);
        ligne.setQuantiteRecue(0);

        ligneBonCommandeRepository.save(ligne);

        return bc;
    }
    @Transactional
    public BonCommande validerBonCommande(Long id) {
        BonCommande bc = getBonCommandeById(id);
        bc.setStatut(StatutBonCommande.VALIDE);
        bc.setDateValidation(LocalDateTime.now());

        return bonCommandeRepository.save(bc);
    }

    @Transactional
    public BonCommande refuserBonCommande(Long id, String motif) {
        BonCommande bc = getBonCommandeById(id);
        bc.setStatut(StatutBonCommande.ANNULE);
        bc.setMotifRefus(motif);
        return bonCommandeRepository.save(bc);
    }

    @Transactional
    public BonCommande receptionnerCommande(Long id, Integer quantiteRecue) {
        BonCommande bc = bonCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé avec id: " + id));

        if (bc.getStatut() != StatutBonCommande.VALIDE) {
            throw new RuntimeException("Seules les commandes validées peuvent être réceptionnées. Statut actuel: " + bc.getStatut());
        }

        List<LigneBonCommande> lignes = bc.getLignes();
        if (lignes == null || lignes.isEmpty()) {
            throw new RuntimeException("Ce bon de commande n'a pas de lignes.");
        }

        // Si plusieurs lignes, répartir la quantité reçue proportionnellement ou par ligne
        for (LigneBonCommande ligne : lignes) {
            int nouvelleQuantiteRecue = ligne.getQuantiteRecue() + quantiteRecue;
            ligne.setQuantiteRecue(nouvelleQuantiteRecue);
            ligneBonCommandeRepository.save(ligne);

            stockSecService.entreeStock(
                    ligne.getArticle().getId(),
                    quantiteRecue,
                    "Réception commande " + bc.getNumeroBC()
            );
        }

        boolean tousRecus = lignes.stream()
                .allMatch(l -> l.getQuantiteRecue() >= l.getQuantiteCommandee());

        bc.setStatut(tousRecus ? StatutBonCommande.RECU : StatutBonCommande.PARTIELLEMENT_RECU);

        return bonCommandeRepository.save(bc);
    }

    private String genererNumeroBC() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return "BC-" + LocalDateTime.now().format(formatter) + "-" + (int)(Math.random() * 1000);
    }
}