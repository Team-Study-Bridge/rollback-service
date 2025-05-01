package org.example.rollbackservice.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "portone")
public record PortOneProperties(
        @NotBlank String impKey,
        @NotBlank String impSecret
) {}
