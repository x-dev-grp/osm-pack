package com.osm.inventory_service.repository;

import com.osm.inventory_service.Enum.CategorieArticle;
import com.osm.inventory_service.entity.ArticleSec;
import com.osm.inventory_service.entity.Fournisseur;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleSecRepository extends BaseRepository<ArticleSec> {

    boolean existsByNomAndFournisseur(String nom, Fournisseur fournisseur);

    List<ArticleSec> findByActifTrue();
    List<ArticleSec> findByCategorie(CategorieArticle categorie);


    List<ArticleSec> findByCategorie(CategorieArticle categorie);

}