package com.project.usermanagement.Exception;

public class DuplicateResourceException extends RuntimeException {
    
    private final String resourceType;
    private final String fieldValue;
    
    public DuplicateResourceException(String resourceType, String fieldValue) {
        super(resourceType + " already exists: " + fieldValue);
        this.resourceType = resourceType;
        this.fieldValue = fieldValue;
    }
    
    public DuplicateResourceException(String resourceType, String fieldValue, String message) {
        super(message);
        this.resourceType = resourceType;
        this.fieldValue = fieldValue;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public String getFieldValue() {
        return fieldValue;
    }
}
