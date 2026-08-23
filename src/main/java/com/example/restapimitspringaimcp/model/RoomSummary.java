package com.example.restapimitspringaimcp.model;
import jdk.jfr.Description;

public record RoomSummary(
        String name,

        @Description("Die Kategorie des Raums. WICHTIG: Ignoriere Büros und Serverräume, außer es wird explizit danach gefragt.")
        RoomType roomType,

        @Description("Die maximale Anzahl der Sitzplätze/Stühle für Personen.")
        int capacity,

        @Description("Technische Ausstattung für Präsentationen (z.B. Beamer, Projektor).")
        String videoEquipment,

        @Description("Anzahl der Computer-Arbeitsplätze. Der Wert -1 bedeutet: Es sind keine PCs vorhanden.")
        int pcWorkstations
) {}