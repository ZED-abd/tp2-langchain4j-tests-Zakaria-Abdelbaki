package com.zakariaabdelbaki.tp2;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.Map;

public class Test3Traducteur {
    public static void main(String[] args) {
        ChatModel modele = Gemini.modele(0.2).build();
        String texte = "Je prends le train demain matin.";
        Prompt anglais = new PromptTemplate("Traduis ce texte en anglais : {{it}}").apply(texte);
        Prompt espagnol = new PromptTemplate("Traduis ce texte en {{langue}} : {{texte}}")
                .apply(Map.of("langue", "espagnol", "texte", texte));
        for (Prompt prompt : new Prompt[]{anglais, espagnol}) {
            System.out.println("Question : " + prompt.text());
            System.out.println("Réponse : " + Gemini.appeler(() -> modele.chat(prompt.text())));
        }
    }
}
