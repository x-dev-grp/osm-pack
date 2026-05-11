package com.osm.inventory_service.controller;

import com.osm.inventory_service.dto.ProductDto;
import com.osm.inventory_service.entity.Product;
import com.osm.inventory_service.entity.ProductType;
import com.osm.inventory_service.service.ProductService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/inventaire/products", "/api/inventaire/skus"})
public class ProductController extends BaseControllerImpl<Product, ProductDto, ProductDto> {

    private final ProductService productService;

    @Autowired
    public ProductController(BaseService<Product, ProductDto, ProductDto> baseService,
                             ModelMapper modelMapper,
                             ProductService productService) {
        super(baseService, modelMapper);
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            List<ProductDto> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable UUID id) {
        try {
            ProductDto product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody ProductDto productDto) {
        try {
            ProductDto created = productService.createProduct(productDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable UUID id, @RequestBody ProductDto productDto) {
        try {
            ProductDto updated = productService.updateProduct(id, productDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/actifs")
    public ResponseEntity<?> getActiveProducts() {
        try {
            List<ProductDto> actifs = productService.getAllActiveProducts();
            return ResponseEntity.ok(actifs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getProductsByType(@PathVariable ProductType type) {
        try {
            return ResponseEntity.ok(productService.getProductsByType(type));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverProduct(@PathVariable UUID id) {
        try {
            productService.desactiverProduct(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerProduct(@PathVariable UUID id) {
        try {
            productService.activerProduct(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected String getResourceName() {
        return "Product";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
