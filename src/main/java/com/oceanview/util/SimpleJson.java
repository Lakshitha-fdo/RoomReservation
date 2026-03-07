package com.oceanview.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

        Index index = new Index(1);
        int end = text.length() - 1;
        while (index.value < end) {
            index.value = skipWhitespace(text, index.value, end);
            if (index.value >= end) {
                break;
            }
            if (text.charAt(index.value) == ',') {
                index.value++;
                continue;
            }
            if (text.charAt(index.value) != '"') {
                throw new IllegalArgumentException("Invalid JSON key.");
            }

            int keyEnd = findClosingQuote(text, index.value + 1);
            String key = unescape(text.substring(index.value + 1, keyEnd));
            index.value = skipWhitespace(text, keyEnd + 1, end);
            if (index.value >= end || text.charAt(index.value) != ':') {
                throw new IllegalArgumentException("Missing ':' after key.");
            }
            index.value = skipWhitespace(text, index.value + 1, end);
            if (index.value >= end) {
                throw new IllegalArgumentException("Missing value.");
            }

            ParsedValue value = readValue(text, index, end);
            result.put(key, value.value);
        }

        return result;
    }

    public static List<Map<String, String>> parseObjectArray(String json) {
        List<Map<String, String>> result = new ArrayList<>();
        if (json == null) {
            return result;
        }

        String text = json.trim();
        if (text.isEmpty() || "[]".equals(text)) {
            return result;
        }
        if (!text.startsWith("[") || !text.endsWith("]")) {
            throw new IllegalArgumentException("Invalid JSON array.");
        }

        Index index = new Index(1);
        int end = text.length() - 1;
        while (index.value < end) {
            index.value = skipWhitespace(text, index.value, end);
            if (index.value >= end) {
                break;
            }
            if (text.charAt(index.value) == ',') {
                index.value++;
                continue;
            }

            ParsedValue value = readValue(text, index, end);
            String item = value.value.trim();
            if (!item.startsWith("{") || !item.endsWith("}")) {
                throw new IllegalArgumentException("Array item is not a JSON object.");
            }
            result.add(parseObject(item));
        }

        return result;
    }

    public static String toJson(Map<String, ?> map) {
        return toJsonValue(map);
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

    private static String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder builder = new StringBuilder();
            builder.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append('"').append(escape(String.valueOf(entry.getKey()))).append('"').append(':');
                builder.append(toJsonValue(entry.getValue()));
            }
            builder.append('}');
            return builder.toString();
        }
        if (value instanceof Iterable<?> iterable) {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            boolean first = true;
            for (Object item : iterable) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append(toJsonValue(item));
            }
            builder.append(']');
            return builder.toString();
        }
        return '"' + escape(value.toString()) + '"';
    }

    private static ParsedValue readValue(String text, Index index, int endExclusive) {
        char first = text.charAt(index.value);
        if (first == '"') {
            int valueEnd = findClosingQuote(text, index.value + 1);
            String value = unescape(text.substring(index.value + 1, valueEnd));
            index.value = valueEnd + 1;
            return new ParsedValue(value);
        }
        if (first == '{' || first == '[') {
            int valueEnd = findClosingStructure(text, index.value, first == '{' ? '}' : ']');
            String value = text.substring(index.value, valueEnd + 1);
            index.value = valueEnd + 1;
            return new ParsedValue(value);
        }

        int valueEnd = index.value;
        while (valueEnd < endExclusive && text.charAt(valueEnd) != ',') {
            valueEnd++;
        }
        String value = text.substring(index.value, valueEnd).trim();
        index.value = valueEnd;
        return new ParsedValue(value);
    }

    private static int findClosingStructure(String text, int start, char closingChar) {
        char openingChar = closingChar == '}' ? '{' : '[';
        int depth = 0;
        boolean inString = false;

        for (int i = start; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == openingChar) {
                depth++;
            } else if (current == closingChar) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        throw new IllegalArgumentException("Unclosed JSON structure.");
    }

    private static final class ParsedValue {
        private final String value;

        private ParsedValue(String value) {
            this.value = value;
        }
    }

    private static final class Index {
        private int value;

        private Index(int value) {
            this.value = value;
        }
    }
}
