package com.zakariaabdelbaki.tp2;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Function;

final class Conversation {
    private Conversation() {
    }

    static void lire(Function<String, String> assistant) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("Posez votre question (fin pour quitter).");
            while (scanner.hasNextLine()) {
                String question = scanner.nextLine().strip();
                if ("fin".equalsIgnoreCase(question)) {
                    break;
                }
                if (!question.isBlank()) {
                    System.out.println("Question : " + question);
                    try {
                        System.out.println("Assistant : " + Gemini.appeler(() -> assistant.apply(question)));
                    } catch (IllegalStateException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }
}
