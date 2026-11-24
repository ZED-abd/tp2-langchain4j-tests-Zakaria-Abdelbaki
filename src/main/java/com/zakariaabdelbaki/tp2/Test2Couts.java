package com.zakariaabdelbaki.tp2;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.google.genai.GoogleGenAiTokenUsage;
import dev.langchain4j.model.output.TokenUsage;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Test2Couts {
    public static void main(String[] args) {
        String question = args.length == 0 ? Test1.QUESTION_SIMPLE : String.join(" ", args);
        ChatModel modele = Gemini.modele(0.7).build();
        System.out.println("Question : " + question);
        ChatResponse reponse = Gemini.appeler(() -> modele.chat(UserMessage.from(question)));
        System.out.println("Modèle : " + Gemini.MODELE);
        System.out.println("Réponse : " + reponse.aiMessage().text());
        TokenUsage usage = reponse.tokenUsage();
        if (usage == null || usage.inputTokenCount() == null || usage.outputTokenCount() == null) {
            System.out.println("Compteurs indisponibles.");
            return;
        }
        int entree = usage.inputTokenCount();
        int sortie = usage.outputTokenCount();
        int reflexion = usage instanceof GoogleGenAiTokenUsage gemini && gemini.thoughtsTokenCount() != null
                ? gemini.thoughtsTokenCount() : 0;
        System.out.printf("Tokens : entrée %d, sortie %d, réflexion %d%n", entree, sortie, reflexion);
        afficherCout(Gemini.MODELE, "0.25", "1.50", entree, sortie + reflexion);
        afficherCout("Simulation gemini-3.5-flash-lite", "0.30", "2.50", entree, sortie + reflexion);
    }

    private static void afficherCout(String modele, String tarifEntree, String tarifSortie, int entree, int sortie) {
        BigDecimal cout = new BigDecimal(tarifEntree).multiply(BigDecimal.valueOf(entree))
                .add(new BigDecimal(tarifSortie).multiply(BigDecimal.valueOf(sortie)))
                .movePointLeft(6);
        String requetes = cout.signum() == 0 ? "indéterminé"
                : BigDecimal.ONE.divide(cout, 0, RoundingMode.CEILING).toPlainString();
        System.out.printf("%s : %s USD, %s requêtes pour atteindre 1 USD%n", modele, cout.toPlainString(), requetes);
    }
}
