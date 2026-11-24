package com.zakariaabdelbaki.tp2;

import com.google.genai.errors.ApiException;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;

import java.util.function.Supplier;

final class Gemini {
    static final String MODELE = "gemini-3.1-flash-lite";

    private Gemini() {
    }

    static String cle() {
        String cle = System.getenv("GEMINI_KEY");
        if (cle == null || cle.isBlank()) {
            throw new IllegalStateException("GEMINI_KEY manque dans l’environnement.");
        }
        return cle;
    }

    static GoogleGenAiChatModel.Builder modele(double temperature) {
        return GoogleGenAiChatModel.builder()
                .apiKey(cle())
                .modelName(MODELE)
                .temperature(temperature)
                .maxRetries(0);
    }

    static <T> T appeler(Supplier<T> appel) {
        try {
            return appel.get();
        } catch (RuntimeException e) {
            int code = codeHttp(e);
            if (code != 429 && code != 503) {
                throw erreur(e);
            }
            System.err.println("Gemini HTTP " + code + ". Nouvel essai dans 60 secondes.");
            try {
                Thread.sleep(60_000);
                return appel.get();
            } catch (InterruptedException interruption) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Attente interrompue.");
            } catch (RuntimeException nouvelEchec) {
                throw erreur(nouvelEchec);
            }
        }
    }

    private static int codeHttp(Throwable erreur) {
        for (Throwable cause = erreur; cause != null; cause = cause.getCause()) {
            if (cause instanceof ApiException api) {
                return api.code();
            }
        }
        return 0;
    }

    private static IllegalStateException erreur(RuntimeException e) {
        int code = codeHttp(e);
        String detail = code == 0 ? e.getClass().getSimpleName() : "HTTP " + code;
        return new IllegalStateException("Échec de l’appel : " + detail);
    }
}
