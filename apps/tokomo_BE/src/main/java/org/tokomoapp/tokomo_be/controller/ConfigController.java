package org.tokomoapp.tokomo_be.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tokomoapp.tokomo_be.model.ApiResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class ConfigController {
    
    @Value("${config.path}")
    private String configPath;
    
    @GetMapping("/site-config")
    public ResponseEntity<ApiResponse> getSiteConfig() {
        try {
            Path path = Paths.get(configPath, "site-config.json");
            String config = new String(Files.readAllBytes(path));
            return ResponseEntity.ok(new ApiResponse("ok", "配置获取成功", config));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "读取配置失败: " + e.getMessage()));
        }
    }
} 