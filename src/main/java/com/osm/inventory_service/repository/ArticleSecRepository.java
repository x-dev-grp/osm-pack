package com.osm.inventory_service.repository;

import com.osm.inventory_service.Enum.CategorieArticle;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.Fournisseur;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleSecRepository extends BaseRepository<ArticleSec> {

    boolean existsByNomAndFournisseur(String nom, Fournisseur fournisseur);

    boolean existsByNomAndFournisseurAndIsDeletedFalse(String nom, Fournisseur fournisseur);

    List<ArticleSec> findByActifTrue();

    List<ArticleSec> findByActifTrueAndIsDeletedFalse();

    List<ArticleSec> findByCategorie(CategorieArticle categorie);

    List<ArticleSec> findByCategorieAndIsDeletedFalse(CategorieArticle categorie);

    @Query("SELECT a FROM ArticleSec a WHERE a.actif = TRUE AND COALESCE(a.isDeleted, FALSE) = FALSE")
    List<ArticleSec> findAllActiveNotDeleted();

    @Query("SELECT COUNT(a) FROM ArticleSec a WHERE a.actif = TRUE AND COALESCE(a.isDeleted, FALSE) = FALSE")
    long countActiveNotDeleted();

    long countByFournisseur_IdAndIsDeletedFalse(UUID fournisseurId);
}
