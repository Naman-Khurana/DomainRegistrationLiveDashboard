package com.project.DomainRegistrationLive.service;

import java.util.List;
import java.util.Set;

public final class KeywordRules {

    private static final Set<String> STOP = Set.copyOf(List.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "can", "do", "for", "from", "get",
            "has", "have", "how", "i", "if", "in", "into", "is", "it", "its", "me", "my", "no",
            "not", "of", "on", "or", "our", "out", "so", "that", "the", "this", "to", "up", "us",
            "was", "we", "what", "when", "where", "who", "why", "will", "with", "you", "your"));


    public static boolean isStop(String word) {
        return word != null && STOP.contains(word);
    }

   // word only contains numbers
    public static boolean isNumeric(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            if (!Character.isDigit(word.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // Allowed in prefix, suffix, rising and mover rankings
    public static boolean isRankable(String word) {
        return word != null && (word.length() > 1) && !isStop(word) && !isNumeric(word);
    }
}
