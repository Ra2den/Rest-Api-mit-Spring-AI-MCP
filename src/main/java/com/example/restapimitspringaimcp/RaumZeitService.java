package com.example.restapimitspringaimcp;

import jdk.jfr.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;


@Service
public class RaumZeitService {
    private final Logger logger = LoggerFactory.getLogger(RaumZeitService.class);
    private final RestClient restClient;

    // Hier ziehen wir den Token aus der Umgebungsvariable
    @Value("${RAUMZEIT_BEARER_TOKEN}")
    private String apiToken;

    public RaumZeitService(RestClient.Builder builder) {
        // Basis-URL deiner API
        this.restClient = builder.baseUrl("https://raumzeit.hka-iwi.de").build();
    }



    public record RoomSummary(
            String name,

            // Wir nennen es camelCase und erklären der KI, dass es die Kategorie ist
            @Description("Die Kategorie des Raums. WICHTIG: Ignoriere Büros und Serverräume, außer es wird explizit danach gefragt.")
            RoomType roomType,

            @Description("Die maximale Anzahl der Sitzplätze/Stühle für Personen.")
            int capacity,

            @Description("Technische Ausstattung für Präsentationen (z.B. Beamer, Projektor).")
            String videoEquipment,

            @Description("Anzahl der Computer-Arbeitsplätze. Der Wert -1 bedeutet: Es sind keine PCs vorhanden.")
            int pcWorkstations
    ) {}

    public record RoomType(
            // Die HKA API liefert hier meist ein Array von Strings (Deutsch/Englisch)
            @Description("Verschiedene Bezeichnungen für den Raumtyp (z.B. 'Hörsaal', 'Lecture hall').")
            String[] longNames
    ) {}

    public record FacultySummary(
            String name,
            String longName,
            boolean faculty
    ){}

    public record RoomAvailability(
            int endTime,
            int startTime
    ){}

    public List<RoomSummary> fetchRooms(String building) {
        List<RoomSummary> allRooms = restClient.get()
                .uri("/api/v1/rooms/all")
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<RoomSummary>>() {});

        if (allRooms == null) {
            return List.of();
        }

        List<RoomSummary> blorp = allRooms.stream()
                .filter(room -> {
                    if (building == null || building.isBlank()) return true;
                    if (room.name() == null) return false;
                    String[] parts = room.name().split("-");
                    return parts.length > 0 && parts[0].equalsIgnoreCase(building);
                })
                .filter(room -> {
                    if (room.roomType() == null || room.roomType().longNames() == null) return true;

                    List<String> excludedTypes = List.of("Büro", "Serverraum", "Sekretariat", "Online"); //Exkursion, Konferenzraum?

                    return java.util.Arrays.stream(room.roomType().longNames())
                            .noneMatch(name -> {
                                return excludedTypes.stream()
                                        .anyMatch(name::equalsIgnoreCase);
                            });
                })
                .toList();
        System.out.println("all relevant Rooms :" + blorp);
        return blorp;
    }

    public List<RoomSummary> getFreeRooms(String building) {
        List<RoomSummary> allRooms = fetchRooms(building);

        int now = LocalTime.now().get(ChronoField.MINUTE_OF_DAY);

        List<RoomSummary> glorp = allRooms.stream()
                .filter(room -> {
                    List<RoomAvailability> assignments = getRoomAssignments(room.name());

                    // Ein Raum ist frei, wenn keine (noneMatch) Belegung jetzt gerade stattfindet
                    // Ineffizient, da N+1 Aufrufe -> check nach allen freien Räumen im Gebäude wäre toll
                    return assignments.stream()
                            .noneMatch(a -> now >= a.startTime() && now <= a.endTime());
                })
                .toList();
        System.out.println("Free Rooms : " + glorp);
        return glorp;
    }

    public List<RoomAvailability> getRoomAssignments(String roomName) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/timetables/room/{room}")
                            .queryParam("week", "false")
                            .build(roomName))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Accept", "application/json") // Wichtig laut Doku!
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<RoomAvailability>>() {});
        } catch (Exception e) {
            System.err.println("Fehler beim Laden für Raum " + roomName + ": " + e.getMessage());
            return List.of();
        }
    }

    public List<FacultySummary> fetchFaculties(){
        List<FacultySummary> message = restClient.get()
                .uri("/api/v1/departments/")
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<FacultySummary>>() {});

        System.out.println("Faculty List : " + message);
        return message;
    }
}