package com.cafe.management.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping(value = {"/api/health", "/health"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("status", "OK");
        resp.put("message", "Your API is running");
        resp.put("service", "cafe-backend");
        resp.put("database", "Online MySQL (Aiven)");
        resp.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}
