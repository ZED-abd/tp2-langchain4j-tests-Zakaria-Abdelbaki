package com.zakariaabdelbaki.tp2;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

public class Test5Rag1 {
    public interface Assistant {
        String chat(String question);
    }

    static Assistant creerAssistant(String chemin) {
        Document document = FileSystemDocumentLoader.loadDocument(chemin);
        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(document, store);
        return AiServices.builder(Assistant.class)
                .chatModel(Gemini.modele(0.2).build())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .contentRetriever(EmbeddingStoreContentRetriever.from(store))
                .build();
    }

    public static void main(String[] args) {
        Assistant assistant = creerAssistant("infos.txt");
        String[] questions = args.length > 0
                ? new String[]{String.join(" ", args)}
                : new String[]{"Comment s’appelle le chat de Pierre ?",
                    "Pierre appelle son chat. Qu’est-ce qu’il pourrait dire ?"};
        for (String question : questions) {
            System.out.println("Question : " + question);
            System.out.println("Réponse : " + Gemini.appeler(() -> assistant.chat(question)));
        }
    }
}
