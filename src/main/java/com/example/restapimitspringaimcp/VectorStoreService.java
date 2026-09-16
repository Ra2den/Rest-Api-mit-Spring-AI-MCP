package com.example.restapimitspringaimcp;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Component
public class VectorStoreService implements CommandLineRunner {

    private final SimpleVectorStore vectorStore;
    private final File vectorStoreFile = new File("hka-vector-store.json");

    public VectorStoreService(SimpleVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String @NonNull ... args) throws Exception {
        if (vectorStoreFile.exists()) {
            System.out.println("Lade VectorStore von Festplatte...");
            vectorStore.load(vectorStoreFile);
            System.out.println("VectorStore erfolgreich geladen");
            return;
        }

        System.out.println("Kein VectorStore gefunden. Lese Dokumente ein...");


        File file = new ClassPathResource("mhb_minb.txt").getFile();
        String inhalt = Files.readString(file.toPath());

        Document riesigesDokument = new Document(inhalt, Map.of("studiengang", "INFB", "typ", "Modulhandbuch"));


        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(100)
                .withMinChunkLengthToEmbed(5)
                .withKeepSeparator(true)
                .build();
        List<Document> kleineChunks = splitter.apply(List.of(riesigesDokument));
        System.out.println("Dokument gechunkt");

        System.out.println("Baue VectorStore...");
        vectorStore.add(kleineChunks);
        vectorStore.save(vectorStoreFile);

        System.out.println("Vektoren berechnet und in hka-vector-store.json gesichert");
    }
}