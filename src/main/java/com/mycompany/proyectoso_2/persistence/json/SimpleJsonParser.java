package com.mycompany.proyectoso_2.persistence.json;

public class SimpleJsonParser {

    private final String source;
    private int index;

    public SimpleJsonParser(String source) {
        if (source == null) {
            throw new IllegalArgumentException("El contenido JSON no puede ser nulo.");
        }
        this.source = source;
    }

    public JsonObjectValue parseObject() {
        skipWhitespace();
        expect('{');
        JsonObjectValue objectValue = new JsonObjectValue();
        skipWhitespace();
        if (peek() == '}') {
            index++;
            return objectValue;
        }

        while (true) {
            String propertyName = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            objectValue.put(propertyName, parseValue());
            skipWhitespace();

            char next = peek();
            if (next == '}') {
                index++;
                return objectValue;
            }
            expect(',');
            skipWhitespace();
        }
    }

    private JsonValue parseValue() {
        skipWhitespace();
        char current = peek();
        if (current == '"') {
            return new JsonStringValue(parseString());
        }
        if (current == '{') {
            return parseObject();
        }
        if (current == '[') {
            return parseArray();
        }
        return new JsonNumberValue(parseInt());
    }

    private JsonArrayValue parseArray() {
        expect('[');
        JsonArrayValue arrayValue = new JsonArrayValue();
        skipWhitespace();
        if (peek() == ']') {
            index++;
            return arrayValue;
        }

        while (true) {
            arrayValue.add(parseValue());
            skipWhitespace();
            char next = peek();
            if (next == ']') {
                index++;
                return arrayValue;
            }
            expect(',');
            skipWhitespace();
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder value = new StringBuilder();

        while (index < source.length()) {
            char current = source.charAt(index);
            index++;
            if (current == '"') {
                return value.toString();
            }
            if (current == '\\') {
                value.append(parseEscapedCharacter());
                continue;
            }
            value.append(current);
        }

        throw new IllegalStateException("Cadena JSON sin cerrar.");
    }

    private char parseEscapedCharacter() {
        if (index >= source.length()) {
            throw new IllegalStateException("Secuencia de escape JSON incompleta.");
        }

        char escaped = source.charAt(index);
        index++;
        return switch (escaped) {
            case '"', '\\', '/' -> escaped;
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            default -> throw new IllegalStateException("Escape JSON no soportado: \\" + escaped + ".");
        };
    }

    private int parseInt() {
        int start = index;
        if (peek() == '-') {
            index++;
        }
        while (index < source.length() && Character.isDigit(source.charAt(index))) {
            index++;
        }
        return Integer.parseInt(source.substring(start, index));
    }

    private void expect(char expected) {
        if (peek() != expected) {
            throw new IllegalStateException("Se esperaba '" + expected + "' en posicion " + index + ".");
        }
        index++;
    }

    private char peek() {
        if (index >= source.length()) {
            throw new IllegalStateException("Fin inesperado del JSON.");
        }
        return source.charAt(index);
    }

    private void skipWhitespace() {
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
    }
}
