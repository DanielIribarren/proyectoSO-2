package com.mycompany.proyectoso_2.structures;

public interface CustomQueue<T> extends LinearStructure<T> {

    void enqueue(T value);

    T dequeue();

    T peek();
}
