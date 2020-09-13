package com.idexx.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JsonParser {

    private final ObjectMapper objectMapper;

    public JsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T parseJsonString(String string, Class<T> clazz) {
        try {
            return objectMapper.readValue(string, clazz);
        } catch (JsonProcessingException e) {
            log.error("Cannot parse into class: {}", clazz);
            throw new RuntimeException(e);
        }
    }
}
