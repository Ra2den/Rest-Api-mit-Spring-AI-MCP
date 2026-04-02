package com.example.restapimitspringaimcp.model;
import jdk.jfr.Description;

public record RoomDetail(
        @Description("Der offizielle Raumname, z.B. E-301")
        String name,
        @Description("Allgemeine Bezeichnung, z.B. Hörsaal")
        String longName,
        @Description("Maximale Personenanzahl bei normaler Nutzung")
        int capacity,
        @Description("Maximale Personenanzahl während Prüfungen")
        int capacityExam,
        @Description("Zugehörige Fakultäten (z.B. IWI)")
        String[] departmentNames,
        @Description("Verfügbares Audio-Equipment (Lautsprecher, Mikrofone)")
        String audioEquipment,
        @Description("Verfügbares Video-Equipment (Beamer, Anschlüsse)")
        String videoEquipment,
        @Description("Sonstige Ausstattung wie Tafeln oder Whiteboards")
        String otherEquipment,
        @Description("Anzahl der PC-Arbeitsplätze im Raum")
        int pcWorkstations,
        @Description("Der Typ des Raums (z.B. Hörsaal, Labor)")
        String type
) {
    public RoomDetail(String name, String longName, int capacity, int capacityExam,
                      String[] departmentNames, String audioEquipment, String videoEquipment,
                      String otherEquipment, int pcWorkstations, RoomType roomType) {

        this(name, longName, capacity, capacityExam, departmentNames,
                audioEquipment, videoEquipment, otherEquipment, pcWorkstations,
                roomType != null && roomType.longNames() != null && roomType.longNames().length > 0
                        ? roomType.longNames()[0] : "Unbekannt");
    }
}
