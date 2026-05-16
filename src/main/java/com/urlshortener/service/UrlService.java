package com.urlshortener.service;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository repo;
    private final StringRedisTemplate redis;

    public String shorten(String originalUrl) {
        // Generate a 7-character unique short code
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 7);

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(code);
        mapping.setOriginalUrl(originalUrl);
        repo.save(mapping);

        // Cache in Redis for 24 hours
        redis.opsForValue().set(code, originalUrl, 24, TimeUnit.HOURS);

        return code;
    }

    public String resolve(String code) {
        // Check Redis first (fast path — in-memory)
        String cached = redis.opsForValue().get(code);
        if (cached != null) {
            // Still increment click count in DB asynchronously
            repo.findByShortCode(code).ifPresent(m -> {
                m.setClickCount(m.getClickCount() + 1);
                repo.save(m);
            });
            return cached;
        }

        // Cache miss — query PostgreSQL
        UrlMapping mapping = repo.findByShortCode(code)
                .orElseThrow(() -> new RuntimeException("Short URL not found: " + code));

        mapping.setClickCount(mapping.getClickCount() + 1);
        repo.save(mapping);

        // Re-populate Redis cache
        redis.opsForValue().set(code, mapping.getOriginalUrl(), 24, TimeUnit.HOURS);

        return mapping.getOriginalUrl();
    }

    public List<UrlMapping> getAllMappings() {
        return repo.findAll();
    }

    public UrlMapping getByCode(String code) {
        return repo.findByShortCode(code)
                .orElseThrow(() -> new RuntimeException("Short URL not found: " + code));
    }
}
