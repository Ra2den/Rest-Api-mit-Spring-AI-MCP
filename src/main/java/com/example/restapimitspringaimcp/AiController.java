package com.example.restapimitspringaimcp;

import com.example.restapimitspringaimcp.model.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;

@Component
class RaumZeitTools {

    private final RaumZeitService raumZeitService;
    private final VectorStore vectorStore;

    // VectorStore in Konstruktor aufnehmen
    public RaumZeitTools(RaumZeitService raumZeitService, VectorStore vectorStore) {
        this.raumZeitService = raumZeitService;
        this.vectorStore = vectorStore;
    }

    @Tool(description = "Gibt eine Liste aller Räume zurück. Wird nach einem Gebäude gefiltert. Priorisiere Räume die nicht im UG sind. Die folgenden Raumtypen werden nicht angezeigt und herausgefiltert : Büro, Serverraum, Sekretariat, Online")
    public List<RoomSummary> getRooms(
            @ToolParam(description = "Building ist ein Einzelner Buchstabe aus [E,F,M,K,B,I,N,LI]. Rufe niemals ohne diesen Parameter auf, frage sonst nach dem Gebäude falls du dir unsicher bist")
            String building
    ) {
        System.out.println("Rufe getRooms mit Gebäude : " + building + " auf");
        return raumZeitService.fetchRooms(building);
    }

    @Tool(description = "Gibt eine Liste aller zur jetzigen Uhrzeit nicht belegten Räume zurück. Wird nach einem Gebäude gefiltert")
    public List<RoomSummary> freeRooms(
            @ToolParam(description = "Building ist ein Einzelner Buchstabe aus [E,F,M,K,B,I,N,LI]. Rufe niemals ohne diesen Parameter auf, frage sonst nach dem Gebäude falls du dir unsicher bist. Die folgenden Raumtypen werden nicht angezeigt und herausgefiltert : Büro, Serverraum, Sekretariat, Online")
            String building
    ) {
        System.out.println("Rufe getFreeRooms mit Gebäude : " + building + " auf");
        return raumZeitService.getFreeRooms(building);
    }

    @Tool(description = "Gibt doe vollständige Information zu einem EINZELNEN Raum. Diese besteht aus statischen Rauminformationen, und der Raumbelegung für die aktuelle Woche. Formatiere die Raumbelegung nach Wochentagen(Montag,Dienstag,Mittwoch,Donnerstag,Freitag")
    public List<FullRoomInfo> getFullRoomInfo(
            @ToolParam(description = "es wird ein einzelner Raum uebergeben. Dieser ist so aufgebaut - Gebauede-Raum (z.B. E-203). Rufe die funktion niemals ohne diesen Parameter auf. Falls du dir unsicher bist frage nochmal nach")
            String room
    ) {
        System.out.println("Bekomme alle Infos zu Raum : " + room);
        return raumZeitService.getSingleRoomInfo((room));
    }

    @Tool(description = "Gibt eine Liste aller besonderen Räume zurück also alle Räume mit der beschreibung Büro, Serverraum, Sekretariat, Online. Wird nach einem Gebäude gefiltert. Priorisiere Räume die nicht im UG sind.")
    public List<RoomSummary> uncommonRooms(
            @ToolParam(description = "Building ist ein Einzelner Buchstabe aus [E,F,M,K,B,I,N,LI]. Rufe niemals ohne diesen Parameter auf, frage sonst nach dem Gebäude falls du dir unsicher bist. Die folgenden Raumtypen werden nicht angezeigt und herausgefiltert : Büro, Serverraum, Sekretariat, Online")
            String building
    ) {
        System.out.println("Unnormale Räume werden in Gebäude " + building + " werden aufgerufen...");
        return raumZeitService.getUncommonRooms(building);
    }

    @Tool(description = "Zeigt Studiengänge einer Fakultät. Nutze dies NUR, wenn der Nutzer explizit das Wort 'Studiengänge' erwähnt.")
    public List<CourseOfStudy> getCoursesOfStudy(
            @ToolParam(description = "Das Kürzel der Fakultät (z.B. 'IWI', 'W', 'EIT', 'AB', 'IMM', 'MMT'). Falls du dir unsicher bist, ob die fakultät existiert, treffe keine annahme sondern rufe getFaculties auf um die annahme zu überprüfen")
            String faculty
    ) {
        System.out.println("hole Studiengänge für : " + faculty);
        return raumZeitService.getCoursesOfStudy(faculty);
    }

    @Tool(description = "Liefert eine Liste aller Fakultätsnamen und deren zugehörige KÜRZEL. Nutze dieses Tool ZUERST, wenn der Nutzer einen Namen (z.B. 'Informatik') nennt, du aber das Kürzel für die Raumabfrage noch nicht kennst.")
    public List<FacultySummary> getFaculties() {
        return raumZeitService.fetchFaculties();
    }

    /*
        @Tool(description = "Liefert das Modulhandbuch eines Fachs zurück. Falls du das Fachkürzel nicht kennst, kannst du das über getCourseOfStudy herausfinden. Zudem brauchst du die Prüfungsordnungsnummer. Falls du diese nicht hast MUSST du nachfragen")
        public List<StripedMHB> getMHB(
                @ToolParam(description = "")
        )
    */
    @Tool(description = "@Tool(description = \"Durchsucht die Modulhandbücher. WICHTIG: Rufe dieses Tool MAXIMAL EINMAL pro Benutzerfrage auf! Wenn du beim ersten Aufruf nichts findest, suche NICHT weiter, sondern antworte sofort: 'Dazu habe ich keine Infos gefunden.'\")")
    public String searchHochschulWissen(
            @ToolParam(description = "Der exakte Name des Fachs, z.B. 'Game Programming'") String query
    ) {
        System.out.println("Durchsuche lokales Wissen (Vektordatenbank) nach: " + query);

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .similarityThreshold(0.2)
                        .topK(5) // Holt die 5 relevantesten Antworten
                        .build()

        );

        if (results.isEmpty()) {
            return "Keine passenden Informationen im lokalen Hochschul-Wissen gefunden.";
        }

        String gefundenerText = results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // Prompt Tuning :
        /*
          HIER SIND DIE GEFUNDENEN DATEN AUS DEM MODULHANDBUCH:

                %s

                WICHTIGE ANWEISUNG AN DICH (KI):
                1. Beantworte die Frage des Nutzers AUSSCHLIESSLICH basierend auf den obigen Daten.
                2. Erfinde absolut keine Namen, Dozenten, Fächer oder ECTS-Punkte.
                3. Wenn die gesuchte Information in den obigen Daten nicht zu finden ist, antworte exakt mit: "Das geht aus dem aktuellen Modulhandbuch nicht hervor."
         */
        return gefundenerText;
    }
}

@RestController
class AiController {

    private final ChatClient chatClient;

    public AiController(
            ChatClient.Builder builder,
            RaumZeitTools raumZeitTools,
            @Value("classpath:systemprompt.txt") Resource systemPromptResource
    ) {
        this.chatClient = builder
                .defaultSystem(systemPromptResource)
                .defaultTools(raumZeitTools)
                .build();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String q) {
        return chatClient.prompt()
                .user(q)
                .call()
                .content();
    }
}