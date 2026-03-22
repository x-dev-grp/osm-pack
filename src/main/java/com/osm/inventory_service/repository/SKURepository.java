package com.osm.inventory_service.repository;



import com.osm.inventory_service.entity.SKU;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SKURepository extends BaseRepository<SKU> {
    Optional<SKU> findByCode(String code);
    List<SKU> findByActifTrue();

}