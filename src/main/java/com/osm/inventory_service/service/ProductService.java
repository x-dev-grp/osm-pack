package com.osm.inventory_service.service;

import com.osm.inventory_service.dto.ProductDto;
import com.osm.inventory_service.entity.Product;
import com.osm.inventory_service.entity.ProductType;
import com.osm.inventory_service.repository.ProductRepository;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService extends BaseServiceImpl<Product, ProductDto, ProductDto> {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ProductService(BaseRepository<Product> repository, ProductRepository productRepository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
        return modelMapper.map(product, ProductDto.class);
    }

    public List<ProductDto> getAllActiveProducts() {
        return productRepository.findByActifTrue().stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }

    public List<ProductDto> getProductsByType(ProductType type) {
        return productRepository.findByType(type).stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        String name = resolveName(productDto);
        String code = resolveCode(productDto, name);
        ensureNameIsAvailable(name, null);
        ensureCodeIsAvailable(code, null);

        Product product = new Product();
        applyProductDto(product, productDto, name, code);
        product.setDeleted(false);
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDto.class);
    }

    @Transactional
    public ProductDto updateProduct(UUID id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
        String name = resolveName(productDto);
        String code = resolveCode(productDto, name);
        ensureNameIsAvailable(name, id);
        ensureCodeIsAvailable(code, id);
        applyProductDto(existingProduct, productDto, name, code);

        Product updatedProduct = productRepository.save(existingProduct);
        return modelMapper.map(updatedProduct, ProductDto.class);
    }

    @Transactional
    public void desactiverProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
        product.setActif(false);
        productRepository.save(product);
    }

    @Transactional
    public void activerProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouve avec id: " + id));
        product.setActif(true);
        productRepository.save(product);
    }

    private void applyProductDto(Product product, ProductDto productDto, String name, String code) {
        ProductType type = productDto.getType() == null ? ProductType.NON_VRAC : productDto.getType();

        product.setName(name);
        product.setCode(code);
        product.setType(type);
        product.setCategory(trimToNull(productDto.getCategory()));
        product.setUnitOfMeasure(resolveUnitOfMeasure(productDto, type));
        product.setDescription(trimToNull(productDto.getDescription()));
        product.setGrade(trimToNull(productDto.getGrade()));
        product.setOrigin(trimToNull(productDto.getOrigin()));
        product.setHarvestCampaign(trimToNull(productDto.getHarvestCampaign()));
        if (productDto.getActif() != null) {
            product.setActif(productDto.getActif());
        } else if (product.getActif() == null) {
            product.setActif(Boolean.TRUE);
        }

        if (type == ProductType.VRAC) {
            product.setVolume(null);
            product.setPackagingType(null);
            product.setBarcode(null);
            product.setUnitsPerCarton(null);
            product.setCartonsPerPallet(null);
            product.setNetWeight(null);
            product.setGrossWeight(null);
            product.setBrand(null);
            product.setDensity(productDto.getDensity());
            product.setStorageUnit(trimToNull(productDto.getStorageUnit()));
        } else {
            product.setVolume(productDto.getVolume());
            product.setPackagingType(trimToNull(productDto.getPackagingType()));
            product.setBarcode(trimToNull(productDto.getBarcode()));
            product.setUnitsPerCarton(productDto.getUnitsPerCarton());
            product.setCartonsPerPallet(productDto.getCartonsPerPallet());
            product.setNetWeight(productDto.getNetWeight());
            product.setGrossWeight(productDto.getGrossWeight());
            product.setBrand(trimToNull(productDto.getBrand()));
            product.setDensity(null);
            product.setStorageUnit(null);
        }
    }

    private String resolveName(ProductDto productDto) {
        String name = trimToNull(productDto.getName());
        if (name == null) {
            name = trimToNull(productDto.getCode());
        }
        if (name == null) {
            throw new RuntimeException("Le nom du produit est obligatoire");
        }
        return name;
    }

    private String resolveCode(ProductDto productDto, String name) {
        String code = trimToNull(productDto.getCode());
        return code == null ? name : code;
    }

    private String resolveUnitOfMeasure(ProductDto productDto, ProductType type) {
        String unitOfMeasure = trimToNull(productDto.getUnitOfMeasure());
        if (unitOfMeasure != null) {
            return unitOfMeasure;
        }
        return type == ProductType.VRAC ? "L" : "BOTTLE";
    }

    private void ensureNameIsAvailable(String name, UUID currentId) {
        productRepository.findByName(name)
                .filter(product -> currentId == null || !Objects.equals(product.getId(), currentId))
                .ifPresent(product -> {
                    throw new RuntimeException("Un produit avec ce nom existe deja: " + name);
                });
    }

    private void ensureCodeIsAvailable(String code, UUID currentId) {
        productRepository.findByCode(code)
                .filter(product -> currentId == null || !Objects.equals(product.getId(), currentId))
                .ifPresent(product -> {
                    throw new RuntimeException("Un produit avec ce code existe deja: " + code);
                });
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
