package com.osm.inventory_service.repository;



import com.osm.inventory_service.entity.Product;
import com.osm.inventory_service.entity.ProductType;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends BaseRepository<Product> {
    Optional<Product> findByName(String name);
    Optional<Product> findByCode(String code);
    List<Product> findByActifTrue();
    List<Product> findByType(ProductType type);

}
