package com.abiooc.inventory_service.dto;

import com.abiooc.inventory_service.Enum.CategorieArticle;
import com.abiooc.inventory_service.Enum.UniteMesure;
import com.abiooc.inventory_service.entity.ArticleSec;
import com.xdev.xdevbase.dtos.BaseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
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
  private   SKUDto reference ;
  private   UniteMesure um;
  private  String nom;
  private  CategorieArticle categorie;
  private  Integer stockMinimum;
  private  Integer stockMaximum;
  private  Boolean actif;
  private   FournisseurDto fournisseur;
  private   UUID referenceId;



}