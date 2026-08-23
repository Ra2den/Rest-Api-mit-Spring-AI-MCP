package com.example.restapimitspringaimcp;

import com.example.restapimitspringaimcp.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


@Service
public class RaumZeitService {
    private final Logger logger = LoggerFactory.getLogger(RaumZeitService.class);
    private final RestClient restClient;

    List<String> uncommonTypes = List.of("Büro", "Serverraum", "Sekretariat", "Online"); //Exkursion, Konferenzraum?


    @Value("${RAUMZEIT_BEARER_TOKEN}")
    private String apiToken;

    @Value("${raumzeit.baseurl}")
    private String baseUrl;

    public RaumZeitService(RestClient.Builder builder, @Value("${raumzeit.baseurl}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

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

                    return java.util.Arrays.stream(room.roomType().longNames())
                            .noneMatch(name -> {
                                return uncommonTypes.stream()
                                        .anyMatch(name::equalsIgnoreCase);
                            });
                })
                .toList();
        System.out.println("all relevant Rooms :" + blorp);
        return blorp;
    }

    public List<RoomSummary> getUncommonRooms(String building) {
        List<RoomSummary> allRooms = restClient.get()
                .uri("/api/v1/rooms/all")
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<RoomSummary>>() {});

        if (allRooms == null) {
            return List.of();
        }

        List<RoomSummary> filteredRooms = allRooms.stream()
                .filter(room -> {
                    if (building == null || building.isBlank()) return true;
                    if (room.name() == null) return false;
                    String[] parts = room.name().split("-");
                    return parts.length > 0 && parts[0].equalsIgnoreCase(building);
                })
                .filter(room -> {
                    if (room.roomType() == null || room.roomType().longNames() == null) return true;

                    return java.util.Arrays.stream(room.roomType().longNames())
                            .anyMatch(name -> {
                                return uncommonTypes.stream()
                                        .anyMatch(name::equalsIgnoreCase);
                            });

        }).toList();
        System.out.println("Uncommon Rooms : " + filteredRooms);
        return filteredRooms;
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

    private List<RoomAvailability> getRoomAssignments(String roomName) {
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

    private List<RoomDetail> getRoomDetail(String roomName){
        List<RoomDetail> allRooms = restClient.get()
                .uri("/api/v1/rooms/all")
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<RoomDetail>>() {});

        if (allRooms == null) {
            return List.of();
        }

        return  allRooms.stream()
                .filter(room -> Objects.equals(room.name(), roomName))
                .toList();
    }

    private List<RoomAssignment> getRoomWeekAssignment(String roomName){
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/timetables/room/{room}")
                        .queryParam("week", "true")
                        .build(roomName))
                .header("Authorization", "Bearer " + apiToken)
                .header("Accept", "application/json") // Wichtig laut Doku!
                .retrieve()
                .body(new ParameterizedTypeReference<List<RoomAssignment>>() {});
    }

    public List<FullRoomInfo> getSingleRoomInfo(String roomName) {

        List<RoomDetail> details = getRoomDetail(roomName);
        List<RoomAssignment> assignments = getRoomWeekAssignment(roomName);

        if (details == null || details.isEmpty()) {
            return List.of();
        }

        FullRoomInfo fullInfo = new FullRoomInfo(details.getFirst(), assignments);
        System.out.println("Volle Rauminfo : " + fullInfo);

        return List.of(fullInfo);
    }

    public List<CourseOfStudy> getCoursesOfStudy(String faculty) {
        List<CourseOfStudy> allCourses = restClient.get()
                .uri("/api/v1/coursesofstudy/")
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CourseOfStudy>>() {});

        if (allCourses == null) return List.of();

        if (faculty != null && !faculty.isBlank()) {
            return allCourses.stream()
                    .filter(course -> course.facultyName() != null &&
                            course.facultyName().equalsIgnoreCase(faculty))
                    .toList();
        }

        return allCourses;
    }

    public List<FacultySummary> fetchFaculties(){
        List<FacultySummary> message = restClient.get()
                .uri("/api/v1/departments/")
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<FacultySummary>>() {});

        assert message != null;
        List<FacultySummary> tmp = message.stream()
                .filter(FacultySummary::faculty) // Behält nur Einträge, bei denen faculty true ist
                .toList();
        System.out.println("Faculty List : " + tmp);
        return tmp;
    }

    public List<StripedMHB> getModules(String course,int ern){
        List<StripedMHB> message = restClient.get()
                .uri("api/v1/mhb/modules" + course + "/" + ern)
                .header("Authorization", "Bearer " + apiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<StripedMHB>>() {});
        System.out.println("stripped modulehandbook : " + message);
        return message;
    }
}