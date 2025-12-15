package com.emilyordanov.configmgmt.controller.exception;

public class ConfigurationNotFoundException extends RuntimeException {
    public ConfigurationNotFoundException(String message) {
        super(message);
    }
}
