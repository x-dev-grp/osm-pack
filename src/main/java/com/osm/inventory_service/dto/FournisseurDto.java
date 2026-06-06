package com.osm.inventory_service.dto;

import com.osm.inventory_service.Enum.CategorieFournisseur;
import com.osm.inventory_service.entity.Fournisseur;
import com.xdev.communicator.models.enums.Currency;
import com.xdev.xdevbase.dtos.BaseDto;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FournisseurDto extends BaseDto<Fournisseur> implements Serializable {
    private String code;
    private String nom;
    private String nomCommercial;
    private String email;
    private String telephone;
    private String fax;
    private String siteWeb;
    private String numeroTva;
    private String adresse;
    private String ville;
    private String codePostal;
    private String pays;
    private String contactNom;
    private String contactPrenom;
    private String contactEmail;
    private String contactTelephone;
    private CategorieFournisseur categorieFournisseur;
    private Integer delaiLivraisonMoyen;
    private String conditionsPaiement;
    private Currency currency;
    private Boolean actif;
    private String certifications;
    private LocalDateTime dateDerniereCommande;
    private String publicCode;
    private String qrUrl;
    private String qrImageBase64;
}