package com.mycompany.proyectoso_2.persistence.json;

public class JsonStringValue implements JsonValue {

    private final String value;

    public JsonStringValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
