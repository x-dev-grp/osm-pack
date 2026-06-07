package com.osm.inventory_service.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InventoryUsageBlockersDto {

    private List<Blocker> blockers = new ArrayList<>();

    public boolean isBlocked() {
        return blockers != null && !blockers.isEmpty();
    }

    @Data
    public static class Blocker {
        private String code;
        private String message;
        private long count;
    }
}
