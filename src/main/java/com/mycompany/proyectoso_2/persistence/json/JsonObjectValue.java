package com.mycompany.proyectoso_2.persistence.json;

import com.mycompany.proyectoso_2.structures.SinglyLinkedList;

public class JsonObjectValue implements JsonValue {

    private final SinglyLinkedList<JsonProperty> properties;

    public JsonObjectValue() {
        properties = new SinglyLinkedList<>();
    }

    public void put(String name, JsonValue value) {
        properties.addLast(new JsonProperty(name, value));
    }

    public JsonValue get(String name) {
        for (int index = 0; index < properties.size(); index++) {
            JsonProperty property = properties.get(index);
            if (property.getName().equals(name)) {
                return property.getValue();
            }
        }
        throw new IllegalArgumentException("No existe la propiedad JSON " + name + ".");
    }

    public boolean has(String name) {
        for (int index = 0; index < properties.size(); index++) {
            if (properties.get(index).getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String getString(String name) {
        return ((JsonStringValue) get(name)).getValue();
    }

    public int getInt(String name) {
        return ((JsonNumberValue) get(name)).getValue();
    }

    public JsonArrayValue getArray(String name) {
        return (JsonArrayValue) get(name);
    }

    public JsonObjectValue getObject(String name) {
        return (JsonObjectValue) get(name);
    }

    public int size() {
        return properties.size();
    }

    public JsonProperty getPropertyAt(int index) {
        return properties.get(index);
    }
}
