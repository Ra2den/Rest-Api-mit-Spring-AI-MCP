package com.example.restapimitspringaimcp;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfig {

    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
        // baue Vektorstore
        return SimpleVectorStore.builder(embeddingModel).build();
    }
/*
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(2) // Merkt sich die letzten 20 Nachrichten (10 Fragen, 10 Antworten) -> viel zu lang !!! speichere nur 4
                .build();
    }

 */
}