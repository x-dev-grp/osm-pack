package com.osm.inventory_service.entity;

import com.osm.inventory_service.Enum.CategorieFournisseur;
import com.xdev.communicator.models.enums.Currency;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "fournisseurs")
@Data
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class Fournisseur extends BaseEntity {

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
    private Currency currency;
    private Integer delaiLivraisonMoyen;
    private String conditionsPaiement;
    private Boolean actif = true;
    private String certifications;
    private java.time.LocalDateTime dateDerniereCommande;
    @Enumerated(EnumType.STRING)
    private CategorieFournisseur categorieFournisseur;



}