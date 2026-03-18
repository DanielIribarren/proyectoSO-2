package com.mycompany.proyectoso_2.structures;

public class LinkedStack<T> implements CustomStack<T> {

    private final SinglyLinkedList<T> elements;

    public LinkedStack() {
        elements = new SinglyLinkedList<>();
    }

    @Override
    public void push(T value) {
        elements.addFirst(value);
    }

    @Override
    public T pop() {
        return elements.removeFirst();
    }

    @Override
    public T peek() {
        return elements.getFirst();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public void clear() {
        elements.clear();
    }
}
