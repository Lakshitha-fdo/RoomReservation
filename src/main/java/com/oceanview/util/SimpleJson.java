package com.oceanview.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SimpleJson {
    private SimpleJson() {
    }

    public static Map<String, String> parseObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        if (json == null) {
            return result;
        }

        String text = json.trim();
        if (text.isEmpty() || "{}".equals(text)) {
            return result;
        }
        if (!text.startsWith("{") || !text.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON object.");
        }

        int i = 1;
        int end = text.length() - 1;
        while (i < end) {
            i = skipWhitespace(text, i, end);
            if (i >= end) {
                break;
            }
            if (text.charAt(i) == ',') {
                i++;
                continue;
            }
            if (text.charAt(i) != '"') {
                throw new IllegalArgumentException("Invalid JSON key.");
            }

            int keyEnd = findClosingQuote(text, i + 1);
            String key = unescape(text.substring(i + 1, keyEnd));
            i = skipWhitespace(text, keyEnd + 1, end);
            if (i >= end || text.charAt(i) != ':') {
                throw new IllegalArgumentException("Missing ':' after key.");
            }
            i = skipWhitespace(text, i + 1, end);
            if (i >= end) {
                throw new IllegalArgumentException("Missing value.");
            }

            String value;
            if (text.charAt(i) == '"') {
                int valueEnd = findClosingQuote(text, i + 1);
                value = unescape(text.substring(i + 1, valueEnd));
                i = valueEnd + 1;
            } else {
                int valueEnd = i;
                while (valueEnd < end && text.charAt(valueEnd) != ',') {
                    valueEnd++;
                }
                value = text.substring(i, valueEnd).trim();
                i = valueEnd;
            }
            result.put(key, value);
        }

        return result;
    }

    public static String toJson(Map<String, ?> map) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');

        boolean first = true;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;

            builder.append('"').append(escape(entry.getKey())).append('"').append(':');
            Object value = entry.getValue();
            if (value == null) {
                builder.append("null");
            } else if (value instanceof Number || value instanceof Boolean) {
                builder.append(value);
            } else {
                builder.append('"').append(escape(value.toString())).append('"');
            }
        }

        builder.append('}');
        return builder.toString();
    }

    private static int skipWhitespace(String text, int index, int endExclusive) {
        int i = index;
        while (i < endExclusive && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return i;
    }

    private static int findClosingQuote(String text, int start) {
        int i = start;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '"' && text.charAt(i - 1) != '\\') {
                return i;
            }
            i++;
        }
        throw new IllegalArgumentException("Unclosed string literal in JSON.");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
