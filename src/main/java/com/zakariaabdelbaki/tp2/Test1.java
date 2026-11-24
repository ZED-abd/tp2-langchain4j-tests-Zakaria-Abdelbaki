package com.zakariaabdelbaki.tp2;

import dev.langchain4j.model.chat.ChatModel;

public class Test1 {
    static final String QUESTION_SIMPLE = "Donne la traduction anglaise de bonsoir.";

    public static void main(String[] args) {
        ChatModel modele = Gemini.modele(0.7).build();
        String[] questions = args.length > 0
                ? new String[]{String.join(" ", args)}
                : new String[]{QUESTION_SIMPLE, "Quelle heure est-il ?",
                    "Bonjour, je m’appelle Zakaria Abdelbaki.", "Quel est mon nom ?"};
        for (String question : questions) {
            System.out.println("Question : " + question);
            System.out.println("Réponse : " + Gemini.appeler(() -> modele.chat(question)));
        }
    }
}
