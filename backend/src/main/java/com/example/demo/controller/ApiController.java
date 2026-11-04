package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./allowed_root}")
    private String allowedRoot;

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody DeleteRequest req) {
        List<String> deleted = new ArrayList<>();
        List<Map<String, String>> errors = new ArrayList<>();
        Path root = Paths.get(allowedRoot).toAbsolutePath().normalize();

        if (req == null || req.paths == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "paths required"));
        }

        for (String p : req.paths) {
            try {
                if (p == null || p.isBlank()) throw new IllegalArgumentException("empty path");
                Path target = root.resolve(p).normalize();
                if (!target.startsWith(root)) throw new IllegalArgumentException("outside allowed root");
                if (Files.isSymbolicLink(target)) throw new IllegalArgumentException("symlink refused");
                if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) throw new IllegalArgumentException("not found");
                if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) throw new IllegalArgumentException("not a regular file");
                Files.delete(target);
                deleted.add(p);
            } catch (Exception e) {
                errors.add(Map.of("path", String.valueOf(p), "error", e.getMessage()));
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deleted", deleted);
        body.put("errors", errors);
        return ResponseEntity.ok(body);
    }

    public static class DeleteRequest {
        public List<String> paths;
    }
}
