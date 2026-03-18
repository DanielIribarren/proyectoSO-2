package com.mycompany.proyectoso_2.structures;

public class LinkedQueue<T> implements CustomQueue<T> {

    private final SinglyLinkedList<T> elements;

    public LinkedQueue() {
        elements = new SinglyLinkedList<>();
    }

    @Override
    public void enqueue(T value) {
        elements.addLast(value);
    }

    @Override
    public T dequeue() {
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
