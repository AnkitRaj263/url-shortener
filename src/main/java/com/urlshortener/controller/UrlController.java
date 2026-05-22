package com.urlshortener.controller;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UrlController {

    private final UrlService service;

    /**
     * POST /shorten
     * Body: { "url": "https://example.com" }
     * Returns: { "shortCode": "abc1234", "shortUrl": "http://localhost:8080/abc1234" }
     */
    @PostMapping("/shorten")
    public ResponseEntity<Map<String, String>> shorten(@RequestBody Map<String, String> body) {
        String originalUrl = body.get("url");
        if (originalUrl == null || originalUrl.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "url field is required"));
        }
        String code = service.shorten(originalUrl);
        return ResponseEntity.ok(Map.of(
                "shortCode", code,
                "shortUrl", "http://localhost:8080/" + code
        ));
    }

    /**
     * GET /{code}
     * Redirects to the original URL (302 Found)
     */
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        try {
            String originalUrl = service.resolve(code);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(originalUrl))
                    .build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /stats
     * Returns all URL mappings with click counts
     */
    @GetMapping("/stats")
    public ResponseEntity<List<UrlMapping>> stats() {
        return ResponseEntity.ok(service.getAllMappings());
    }

    /**
     * GET /stats/{code}
     * Returns stats for a specific short code
     */
    @GetMapping("/stats/{code}")
    public ResponseEntity<UrlMapping> statsByCode(@PathVariable String code) {
        try {
            return ResponseEntity.ok(service.getByCode(code));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /health
     * Simple health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "message", "URL Shortener is running!"));
    }
}
