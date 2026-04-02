package com.example.restapimitspringaimcp.model;
import jdk.jfr.Description;

public record RoomType(
        // Die HKA API liefert hier meist ein Array von Strings (Deutsch/Englisch)
        @Description("Verschiedene Bezeichnungen für den Raumtyp (z.B. 'Hörsaal', 'Lecture hall').")
        String[] longNames
) {}
