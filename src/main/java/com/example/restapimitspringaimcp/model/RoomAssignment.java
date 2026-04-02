package com.example.restapimitspringaimcp.model;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record RoomAssignment(
        String longName,        // z.B. "Algorithmen und Datenstrukturen Übung"
        String name,            // Kurzname/Modulnummer: "MINB212"
        int startTime,          // Start in Minuten ab 0 Uhr
        int endTime,            // Ende in Minuten ab 0 Uhr
        String interval,        // "WEEKLY" oder "SINGLE"

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate firstDate,    // Erster Termin (Wichtig für Semester-Check)

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate lastDate,     // Letzter Termin

        String[] rooms,         // Ein Termin kann in mehreren Räumen sein (z.B. E-201, E-203)
        String contact,         // Prof / Ansprechpartner
        List<Cancellation> cancellations // Enthält abgesagte Termine
) {}
