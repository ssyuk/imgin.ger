package me.syuk.saenggang.ai;

import java.util.List;

public record AIResponse(String content, List<AIFunction> usedFunctions) {
    public AIResponse(String content, AIFunction usedFunction) {
        this(content, List.of(usedFunction));
    }

    public AIResponse(String content) {
        this(content, List.of());
    }
}
