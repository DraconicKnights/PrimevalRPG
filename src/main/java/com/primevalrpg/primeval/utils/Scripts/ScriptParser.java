package com.primevalrpg.primeval.utils.Scripts;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.enums.LoggerLevel;

import java.util.*;
import java.util.stream.Collectors;

public class ScriptParser {
    public static List<ScriptCommand> parse(List<String> lines) {
        return lines.stream()
                .map(String::trim)
                .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                .map(ScriptParser::parseLine)
                .collect(Collectors.toList());
    }

    // in ScriptParser.java
    public static ScriptCommand parseLine(String line) {
        line = line
                .replaceAll("\\bif\\s*:",   "if=")
                .replaceAll("\\bthen\\s*:", "then=")
                .replaceAll("\\belse\\s*:", "else=");

        String triggerEvent = null;
        String trimmed = line.trim();
        if (trimmed.toLowerCase().startsWith("on ") && trimmed.contains(":")) {
            int colon = trimmed.indexOf(':');
            triggerEvent = trimmed.substring(3, colon).trim();
            trimmed = trimmed.substring(colon + 1).trim();
        }

        List<String> parts = splitKeepingQuotes(trimmed);

        PrimevalRPG.getInstance().CustomMobLogger(">>> parseline got tokens: " + parts, LoggerLevel.DEBUG);

        if (parts.isEmpty()) {
            throw new IllegalArgumentException("Cannot parse empty command");
        }

        String name = parts.get(0);
        Map<String,String> args = new HashMap<>();
        List<String> targets = new ArrayList<>();
        for (int i = 1; i < parts.size(); i++) {
            String tok = parts.get(i);
            if (tok.startsWith("@")) {
                targets.add(tok);
            } else if (tok.contains("=")) {
                String[] kv = tok.split("=", 2);
                args.put(kv[0], kv[1]);
            }
        }

        return new ScriptCommand(name, args, targets, triggerEvent);
    }

    /**
     * Splits a string on whitespace, but keeps quoted substrings intact
     * and strips the wrapping quotes.
     */
    public static List<String> splitKeepingQuotes(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (inQuote) {
                if (c == quoteChar) {
                    inQuote = false;
                } else {
                    buf.append(c);
                }
            } else {
                if (c == '"' || c == '\'') {
                    inQuote = true;
                    quoteChar = c;
                } else if (Character.isWhitespace(c)) {
                    if (buf.length() > 0) {
                        result.add(buf.toString());
                        buf.setLength(0);
                    }
                } else {
                    buf.append(c);
                }
            }
        }
        if (buf.length() > 0) {
            result.add(buf.toString());
        }
        return result;
    }
}