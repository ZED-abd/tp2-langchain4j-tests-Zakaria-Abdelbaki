package com.zakariaabdelbaki.tp2;

import com.zakariaabdelbaki.tp2.outil.meteo.MeteoTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.service.AiServices;

public class Test7Outils {
    public static void main(String[] args) {
        boolean trace = args.length > 0 && "--trace".equals(args[0]);
        GoogleGenAiChatModel modele = Gemini.modele(0.2)
                .logRequests(trace)
                .logResponses(trace)
                .build();
        AssistantMeteo assistant = AiServices.builder(AssistantMeteo.class)
                .chatModel(modele)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .tools(new MeteoTool())
                .build();
        Conversation.lire(assistant::chat);
    }
}
