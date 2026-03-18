package com.mycompany.proyectoso_2.structures;

public interface CustomList<T> extends LinearStructure<T> {

    void addFirst(T value);

    void addLast(T value);

    void insertAt(int index, T value);

    T getFirst();

    T getLast();

    T get(int index);

    T set(int index, T value);

    T removeFirst();

    T removeLast();

    T removeAt(int index);

    boolean contains(T value);

    int indexOf(T value);
}
