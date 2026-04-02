package com.example.restapimitspringaimcp.model;
import com.fasterxml.jackson.annotation.JsonFormat;

public record Cancellation(
        String reason,          // Warum fällt es aus?
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        String timestamp        // Wann genau fällt es aus?
) {}
