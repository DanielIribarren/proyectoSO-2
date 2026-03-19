package com.mycompany.proyectoso_2.persistence.json;

public class JsonProperty {

    private final String name;
    private final JsonValue value;

    public JsonProperty(String name, JsonValue value) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la propiedad es obligatorio.");
        }
        if (value == null) {
            throw new IllegalArgumentException("El valor JSON es obligatorio.");
        }
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public JsonValue getValue() {
        return value;
    }
}
