package com.osm.inventory_service.dto;

import com.osm.inventory_service.Enum.TypeMouvement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MouvementRecentDto {
    private UUID id;
    private TypeMouvement typeMouvement;
    private Integer quantite;
    private LocalDateTime dateMouvement;
    private String motif;
    private UUID articleId;
    private String articleSku;
    private String articleNom;
    private String uniteMesure;
}
