package com.osm.inventory_service.entity;

import com.osm.inventory_service.Enum.TypeEmplacement;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;


@Entity
@Table(name = "emplacements_stock")
@Data
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class EmplacementStock extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code;

    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_emplacement", nullable = false)
    private TypeEmplacement typeEmplacement;

    @Column(name = "capacite_maximale")
    private String capaciteMaximale;

    @Column(name = "capacite_actuelle")
    private String capaciteActuelle ;

    @Column(name = "zone")
    private String zone;


    private Boolean disponible = true;

    @Column(name = "reserve_pour")
    private String reservePour;

    @Column(name = "conditions_speciales")
    private String conditionsSpeciales;

    @Column(name = "temperature_min")
    private Double temperatureMin;

    @Column(name = "temperature_max")
    private Double temperatureMax;

    private String description;

    @Column(length = 1000)
    private String notes;
    private Boolean actif = true;

}