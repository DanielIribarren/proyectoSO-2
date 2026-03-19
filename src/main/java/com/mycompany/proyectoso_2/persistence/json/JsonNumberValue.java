package com.mycompany.proyectoso_2.persistence.json;

public class JsonNumberValue implements JsonValue {

    private final int value;

    public JsonNumberValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
