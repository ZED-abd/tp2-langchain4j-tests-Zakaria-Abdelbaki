package com.zakariaabdelbaki.tp2;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
import dev.langchain4j.model.google.genai.GoogleGenAiEmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Test4Embeddings {
    private static final String[][] COUPLES = {
            {"Le train arrivera à midi.", "Le train entre en gare à douze heures."},
            {"La boutique est ouverte aujourd’hui.", "La boutique est fermée aujourd’hui."},
            {"Le chat dort sur le tapis.", "Une matrice carrée peut être inversible."},
            {"Il pleut depuis ce matin.", "La pluie tombe depuis le début de la journée."},
            {"Lina apprécie le café.", "Lina n’aime pas le café."},
            {"Le colis a été livré hier.", "The parcel was delivered yesterday."}
    };

    public static void main(String[] args) {
        List<TextSegment> segments = Arrays.stream(COUPLES)
                .flatMap(Arrays::stream).map(TextSegment::from).toList();
        String mode = args.length == 0 ? "comparaison" : args[0];
        if (!mode.equals("gemini")) {
            afficher("BGE local", new BgeSmallEnV15EmbeddingModel().embedAll(segments).content());
        }
        if (!mode.equals("local")) {
            long attenteMillis = args.length > 1 ? Long.parseLong(args[1]) : 30_000;
            GoogleGenAiEmbeddingModel modele = GoogleGenAiEmbeddingModel.builder()
                    .apiKey(Gemini.cle())
                    .modelName("gemini-embedding-001")
                    .taskType(GoogleGenAiEmbeddingModel.TaskTypeEnum.SEMANTIC_SIMILARITY)
                    .timeout(Duration.ofMillis(attenteMillis))
                    .maxRetries(0)
                    .build();
            afficher("Gemini", Gemini.appeler(() -> modele.embedAll(segments)).content());
        }
    }

    private static void afficher(String modele, List<Embedding> vecteurs) {
        if (vecteurs.size() != 2 * COUPLES.length || vecteurs.stream().anyMatch(vecteur -> vecteur == null)) {
            throw new IllegalStateException("Le nombre de vecteurs valides ne correspond pas au nombre de phrases.");
        }
        for (int i = 0; i < COUPLES.length; i++) {
            double score = CosineSimilarity.between(vecteurs.get(2 * i), vecteurs.get(2 * i + 1));
            System.out.printf(Locale.ROOT, "%s | %s | %s | %.6f%n", modele, COUPLES[i][0], COUPLES[i][1], score);
        }
    }
}
