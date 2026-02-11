package me.karubidev.devagent.context;

import java.util.List;

public record ContextBundle(String text, List<String> referencedFiles) {
}
