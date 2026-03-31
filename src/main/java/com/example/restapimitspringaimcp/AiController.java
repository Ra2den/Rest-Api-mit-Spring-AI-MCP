package com.example.restapimitspringaimcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

@Component
class RaumZeitTools {

    private final RaumZeitService raumZeitService;

    public RaumZeitTools(RaumZeitService raumZeitService) {
        this.raumZeitService = raumZeitService;
    }

    @Tool(description = "Gibt eine Liste aller Räume zurück. Wird nach einem Gebäude gefiltert. Priorisiere Räume die nicht im UG sind. Die folgenden Raumtypen werden nicht angezeigt und herausgefiltert : Büro, Serverraum, Sekretariat, Online")
    public List getRooms(
            @ToolParam(description = "Building ist ein Einzelner Buchstabe aus [E,F,M,K,B,I,N,LI]. Rufe niemals ohne diesen Parameter auf, frage sonst nach dem Gebäude falls du dir unsicher bist")String building
    ) {
        System.out.println("Rufe getRooms mit Gebäude : " + building + " auf");
        return raumZeitService.fetchRooms(building);
    }

    @Tool(description = "Gibt eine Liste aller zur jetzigen Uhrzeit nicht belegten Räume zurück. Wird nach einem Gebäude gefiltert")
    public  List freeRooms(
            @ToolParam(description = "Building ist ein Einzelner Buchstabe aus [E,F,M,K,B,I,N,LI]. Rufe niemals ohne diesen Parameter auf, frage sonst nach dem Gebäude falls du dir unsicher bist. Die folgenden Raumtypen werden nicht angezeigt und herausgefiltert : Büro, Serverraum, Sekretariat, Online")String building
    ) {
        System.out.println("Rufe getFreeRooms mit Gebäude : " + building + " auf");
        return raumZeitService.getFreeRooms(building);
    }

    /*
    @Tool(description = "Liefert eine Liste aller Fakultätsnamen und deren zugehörige KÜRZEL. Nutze dieses Tool ZUERST, wenn der Nutzer einen Namen (z.B. 'Informatik') nennt, du aber das Kürzel für die Raumabfrage noch nicht kennst.")
    public List getFaculties(){
        return raumZeitService.fetchFaculties();
    }

     */
}

@RestController
class AiController {

    private final ChatClient chatClient;

    public AiController(ChatClient.Builder builder, RaumZeitTools raumZeitTools) {
        this.chatClient = builder
                .defaultTools(raumZeitTools) // Name der Methode oben
                .build();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String q) {
        return chatClient.prompt()
                .system("""
                Du bist ein hilfreicher Assistent für das RaumZeit-System der Hochschule Karlsruhe.
                Benutze die dir gegebenen Tools.
                Du willst dem Benutzer auskünfte über Räume, Studiengänge oder Veranstaltungen geben.
                Antworte kompakt. Falls dir informationen fehlen, oder dir etwas unklar ist, frage nach.
                Treffe niemals annahmen.
                """)
                .user(q)
                .call()
                .content();
    }
}