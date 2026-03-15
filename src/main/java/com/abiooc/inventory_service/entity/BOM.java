package com.abiooc.inventory_service.entity;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "boms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BOM extends BaseEntity {

    @Column(name = "quantite_theorique")
    private Float quantiteTheorique;

    @Column(name = "quantite_reelle")
    private Float quantiteReelle;

    @Column(name = "motif_ajustement")
    private String motifAjustement;
}