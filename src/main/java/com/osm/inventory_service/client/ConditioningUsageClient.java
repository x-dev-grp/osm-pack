package com.osm.inventory_service.client;

import com.osm.inventory_service.dto.InventoryUsageBlockersDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "conditioning-service",
        url = "${conditioning.service.url}",
        configuration = com.xdev.xdevsecurity.config.FeignConfiguration.class
)
public interface ConditioningUsageClient {

    @GetMapping("/api/ordreConditionement/inventory-usage/articles/{articleId}")
    InventoryUsageBlockersDto getArticleUsageBlockers(@PathVariable("articleId") UUID articleId);

    @GetMapping("/api/ordreConditionement/inventory-usage/products/{productId}")
    InventoryUsageBlockersDto getProductUsageBlockers(@PathVariable("productId") UUID productId);
}
