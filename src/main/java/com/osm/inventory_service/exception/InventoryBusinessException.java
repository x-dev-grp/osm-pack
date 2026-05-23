package com.osm.inventory_service.exception;

import org.springframework.http.HttpStatus;

public class InventoryBusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public InventoryBusinessException(String code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    public InventoryBusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status != null ? status : HttpStatus.BAD_REQUEST;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
