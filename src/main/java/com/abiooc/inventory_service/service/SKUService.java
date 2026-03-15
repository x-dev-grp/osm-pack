package com.abiooc.inventory_service.service;



import com.abiooc.inventory_service.dto.SKUDto;
import com.abiooc.inventory_service.entity.SKU;
import com.abiooc.inventory_service.repository.SKURepository;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SKUService extends BaseServiceImpl<SKU, SKUDto, SKUDto> {

    private final SKURepository skuRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public SKUService(BaseRepository<SKU> repository, SKURepository skuRepository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.skuRepository = skuRepository;
        this.modelMapper = modelMapper;
    }

    public List<SKUDto> getAllSkus() {
        return skuRepository.findAll().stream()
                .map(sku -> modelMapper.map(sku, SKUDto.class))
                .collect(Collectors.toList());
    }

    public SKUDto getSkuById(UUID id) {
        SKU sku = skuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SKU non trouvé avec id: " + id));
        return modelMapper.map(sku, SKUDto.class);
    }

    public SKUDto getSkuByCode(String code) {
        SKU sku = skuRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("SKU non trouvé avec code: " + code));
        return modelMapper.map(sku, SKUDto.class);
    }

    @Transactional
    public SKUDto createSku(SKUDto skuDto) {
        if (skuRepository.findByCode(skuDto.getCode()).isPresent()) {
            throw new RuntimeException("Un SKU avec ce code existe déjà: " + skuDto.getCode());
        }

        SKU sku = modelMapper.map(skuDto, SKU.class);
        sku.setDeleted(false);
        SKU savedSku = skuRepository.save(sku);
        return modelMapper.map(savedSku, SKUDto.class);
    }

    @Transactional
    public SKUDto updateSku(UUID id, SKUDto skuDto) {
        SKU existingSku = skuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SKU non trouvé avec id: " + id));
        if (!existingSku.getCode().equals(skuDto.getCode())) {
            if (skuRepository.findByCode(skuDto.getCode()).isPresent()) {
                throw new RuntimeException("Un SKU avec ce code existe déjà: " + skuDto.getCode());
            }
            existingSku.setCode(skuDto.getCode());
        }

        existingSku.setVolume(skuDto.getVolume());
        existingSku.setCategory(skuDto.getCategory());
        existingSku.setUnitesParCoulis(skuDto.getUnitesParCols());
        existingSku.setColisParPalette(skuDto.getColisParPalette());

        SKU updatedSku = skuRepository.save(existingSku);
        return modelMapper.map(updatedSku, SKUDto.class);
    }

    @Transactional
    public void deleteSku(UUID id) {
        SKU sku = skuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SKU non trouvé avec id: " + id));
        skuRepository.delete(sku);
    }

    @Transactional
    public void desactiverSku(UUID id) {
        SKU sku = skuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SKU non trouvé avec id: " + id));
        sku.setDeleted(true);
        skuRepository.save(sku);
    }

    @Transactional
    public void activerSku(UUID id) {
        SKU sku = skuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SKU non trouvé avec id: " + id));
        sku.setDeleted(false); // Réactivation
        skuRepository.save(sku);
    }


}