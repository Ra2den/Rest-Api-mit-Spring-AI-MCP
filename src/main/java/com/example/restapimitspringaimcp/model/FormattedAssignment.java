package com.example.restapimitspringaimcp.model;

import java.util.List;

public record FormattedAssignment(
        String zeit,      // "08:00 - 09:30"
        String fach,      // "Algorithmen und Datenstrukturen Übung"
        String dozent,    // "Prof. Dr. Mustermann"
        List<String> absagen // z.B. ["Fällt aus am 2026-05-04"]
) {}