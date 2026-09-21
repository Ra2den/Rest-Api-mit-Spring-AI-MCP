package com.example.restapimitspringaimcp;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class VectorStoreService implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(VectorStoreService.class);
    private final SimpleVectorStore vectorStore;
    private final File vectorStoreFile = new File("hka-vector-store.json");

    public VectorStoreService(SimpleVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String @NonNull ... args) throws Exception {
        if (vectorStoreFile.exists()) {
            logger.info("Lade VectorStore von Festplatte...");
            vectorStore.load(vectorStoreFile);
            logger.info("VectorStore erfolgreich geladen");
            return;
        }

        logger.info("Kein VectorStore gefunden. Lese Dokumente ein...");

        List<String> dateien = List.of("mhb_minb_bachelor.txt", "mhb_infb_bachelor.txt", "mhb_infb_master.txt");
        List<Document> alleChunks = new ArrayList<>();

        for (String dateiname : dateien) {
            File file = new ClassPathResource(dateiname).getFile();
            String inhalt = Files.readString(file.toPath());

            String studiengang = "UNBEKANNT";
            String studiumtyp = "UNBEKANNT";
            if (dateiname.contains("minb")) studiengang = "MINB";
            if (dateiname.contains("infb")) studiengang = "INFB";
            if (dateiname.contains("bachelor")) studiumtyp = "Bachelor";
            if (dateiname.contains("master")) studiumtyp = "Master";

            logger.info("Verarbeite Datei: {} für Studiengang: {} mit Typ: {}", dateiname, studiengang, studiumtyp);


            String[] faecher = inhalt.split("(?=Veranstaltungsname)");

            for (String fachText : faecher) {
                fachText = fachText.trim();


                if (fachText.length() < 100) {
                    continue;
                }

                Document fachDokument = new Document(fachText, Map.of(
                        "studiengang", studiengang,
                        "studiumtyp", studiumtyp,
                        "typ", "Modulhandbuch"
                ));

                alleChunks.add(fachDokument);
            }
        }

        logger.info("Insgesamt {} saubere Fächer-Chunks generiert, baue VectorStore...", alleChunks.size());

        vectorStore.add(alleChunks);
        vectorStore.save(vectorStoreFile);

        logger.info("Vektoren berechnet und in hka-vector-store.json gesichert");
    }
}