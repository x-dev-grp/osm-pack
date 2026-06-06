package com.osm.inventory_service.repository;



import com.osm.inventory_service.entity.MouvementStockSec;

import com.xdev.xdevbase.repos.BaseRepository;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;



import java.time.LocalDateTime;

import java.util.List;

import java.util.UUID;



@Repository

public interface MouvementStockSecRepository extends BaseRepository<MouvementStockSec> {



    List<MouvementStockSec> findByArticleId(UUID articleId);

    @Query("SELECT m FROM MouvementStockSec m "
            + "WHERE m.article.id = :articleId "
            + "AND COALESCE(m.isDeleted, FALSE) = FALSE "
            + "ORDER BY m.dateMouvement DESC")
    List<MouvementStockSec> findByArticleIdNotDeletedOrderByDateMouvementDesc(@Param("articleId") UUID articleId);



    @Query("SELECT m FROM MouvementStockSec m LEFT JOIN FETCH m.article "

            + "WHERE COALESCE(m.isDeleted, FALSE) = FALSE "

            + "ORDER BY m.dateMouvement DESC")

    List<MouvementStockSec> findRecentNotDeleted(Pageable pageable);



    @Query("SELECT m FROM MouvementStockSec m "

            + "WHERE m.dateMouvement BETWEEN :start AND :end "

            + "AND COALESCE(m.isDeleted, FALSE) = FALSE")

    List<MouvementStockSec> findByDateMouvementBetweenNotDeleted(

            @Param("start") LocalDateTime start,

            @Param("end") LocalDateTime end);

}

