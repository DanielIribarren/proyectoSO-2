package com.mycompany.proyectoso_2.structures;

final class LinkedNode<T> {

    private T value;
    private LinkedNode<T> next;

    LinkedNode(T value) {
        this.value = value;
    }

    T getValue() {
        return value;
    }

    void setValue(T value) {
        this.value = value;
    }

    LinkedNode<T> getNext() {
        return next;
    }

    void setNext(LinkedNode<T> next) {
        this.next = next;
    }
}
