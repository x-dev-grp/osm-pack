package com.abiooc.inventory_service.controller;

import com.abiooc.inventory_service.entity.BonCommande;
import com.abiooc.inventory_service.service.BonCommandeService;
import com.abiooc.inventory_service.service.PdfGeneratorService;
import com.abiooc.inventory_service.service.SeuilAlerteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.abiooc.inventory_service.Enum.StatutBonCommande;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventaire/bons-commande")
@CrossOrigin(origins = "*")
public class BonCommandeController {

    @Autowired
    private BonCommandeService bonCommandeService;
    @Autowired
    private PdfGeneratorService pdfGeneratorService;
    @Autowired
    private SeuilAlerteService seuilAlerteService;

    @GetMapping
    public List<BonCommande> getAllBonsCommande() {
        return bonCommandeService.getAllBonsCommande();
    }
    @GetMapping("/{id}")
    public BonCommande getBonCommandeById(@PathVariable Long id) {
        return bonCommandeService.getBonCommandeById(id);
    }

    @GetMapping("/en-attente")
    public List<BonCommande> getBonsEnAttente() {
        return bonCommandeService.getBonsCommandeByStatut(StatutBonCommande.EN_ATTENTE);
    }
//modifer par ne valide que par l admin
    @PostMapping("/{id}/valider")
    public BonCommande validerBonCommande(@PathVariable Long id) {
        return bonCommandeService.validerBonCommande(id);
    }

    //modifer par ne valide que par l admin
    @PostMapping("/{id}/refuser")
    public BonCommande refuserBonCommande(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        String motif = payload.get("motif");

        return bonCommandeService.refuserBonCommande(id, motif);
    }
    @PostMapping("/verifier-seuils-creeCommandes")
    public List<BonCommande> verifierSeuils() {
        return seuilAlerteService.verifierSeuilsEtCreerBonsCommandes();
    }


    @PostMapping("/{id}/receptionner")
    public BonCommande receptionnerCommande(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Integer quantite = Integer.valueOf(payload.get("quantite").toString());
        return bonCommandeService.receptionnerCommande(id, quantite);
    }


    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        BonCommande bc = bonCommandeService.getBonCommandeById(id);
        byte[] pdfContent = pdfGeneratorService.genererBonCommandePdf(bc);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("BC-" + bc.getNumeroBC() + ".pdf")
                .build());

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

}