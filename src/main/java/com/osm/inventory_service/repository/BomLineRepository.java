package com.osm.inventory_service.repository;

import com.osm.inventory_service.entity.BomLine;
import com.xdev.xdevbase.repos.BaseRepository;

import java.util.UUID;

public interface BomLineRepository extends BaseRepository<BomLine> {
    long countByArticle_Id(UUID articleId);

    long countByArticle_IdAndIsDeletedFalseAndBom_IsDeletedFalse(UUID articleId);
}
