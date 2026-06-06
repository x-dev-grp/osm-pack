package com.osm.inventory_service.dto;

import com.osm.inventory_service.Enum.CategorieArticle;

import com.osm.inventory_service.entity.ArticleSec;
import com.xdev.xdevbase.dtos.BaseDto;
import com.xdev.xdevbase.models.UniteMesure;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Data
public class ArticleSecDto extends BaseDto<ArticleSec> implements Serializable {
  private  UUID id;
  private  UUID tenantId;
  private  Boolean isDeleted;
  private  String createdBy;
  private  LocalDateTime createdDate;
  private  String lastModifiedBy;
  private  LocalDateTime lastModifiedDate;
  private  UUID externalId;
  private UniteMesure um;
  private  String nom;
  private  CategorieArticle categorie;
  private  Integer stockMinimum;
  private  Integer stockMaximum;
  private  Boolean actif;
  private   FournisseurDto fournisseur;
  private String publicCode;
  private String qrUrl;
  private String qrImageBase64;
  private Map<String, Object> configuration;

  /** Stock snapshot — single source of truth for list and detail views. */
  private UUID stockId;
  private Integer quantiteActuelle;
  private Integer quantiteReservee;
  private Integer quantiteDisponible;
  private Boolean belowMinimum;
  private LocalDateTime stockLastModifiedDate;
  private EmplacementStockDto emplacement;


}