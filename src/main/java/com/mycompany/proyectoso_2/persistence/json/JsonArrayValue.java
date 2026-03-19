package com.mycompany.proyectoso_2.persistence.json;

import com.mycompany.proyectoso_2.structures.SinglyLinkedList;

public class JsonArrayValue implements JsonValue {

    private final SinglyLinkedList<JsonValue> values;

    public JsonArrayValue() {
        values = new SinglyLinkedList<>();
    }

    public void add(JsonValue value) {
        values.addLast(value);
    }

    public int size() {
        return values.size();
    }

    public JsonValue get(int index) {
        return values.get(index);
    }
}
