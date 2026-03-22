package com.osm.inventory_service.entity;


import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "clients")
@Data
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class Client extends BaseEntity {

    @Column(nullable = false)
    private String nom;

    @Column( unique = true)
    private String codeClient;

    private String email;

    private String telephone;

    private String adresse;

    private String ville;

    private String pays;

    private String codePostal;

    private Boolean privateLabel = false;

    private String siret;

    private String numeroTva;

    @Column(length = 500)
    private String notes;
    private Boolean actif = true;
}