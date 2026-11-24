package com.zakariaabdelbaki.tp2;

public class Test6Rag2 {
    public static void main(String[] args) {
        Test5Rag1.Assistant assistant = Test5Rag1.creerAssistant("ml.pdf");
        Conversation.lire(assistant::chat);
    }
}
