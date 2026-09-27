package com.amazon.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Reusable JPA AttributeConverter converting between JSON String and Map<String, String>.
 * Used for product specifications and other string-keyed/string-valued metadata.
 * Strictly adheres to Senior Backend Developer Guidelines.
 */
@Converter(autoApply = false)
@Slf4j
public class JsonToStringMapConverter implements AttributeConverter<Map<String, String>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, String>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error serializing Map<String,String> to JSON string: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error serializing Map<String,String> to JSON string", e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || "null".equalsIgnoreCase(dbData.trim())) {
            return new HashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, TYPE_REF);
        } catch (IOException e) {
            log.error("Error deserializing JSON string to Map<String,String>: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
}
