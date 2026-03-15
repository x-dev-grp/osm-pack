package com.abiooc.inventory_service.dto;

import com.abiooc.inventory_service.entity.StockSec;
import com.xdev.xdevbase.dtos.BaseDto;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;


@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockSecDto extends BaseDto<StockSec> implements Serializable {

    private UUID articleId;
    private ArticleSecDto article;
    private Integer quantiteActuelle;
    private UUID emplacementId;
    private EmplacementStockDto emplacement;
}