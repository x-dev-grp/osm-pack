package com.osm.inventory_service.repository;

import com.osm.inventory_service.entity.MouvementStockSec;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface MouvementStockSecRepository extends BaseRepository<MouvementStockSec> {
    List<MouvementStockSec> findByArticleId(UUID articleId);

}