package com.mycompany.proyectoso_2.structures;

public interface CustomStack<T> extends LinearStructure<T> {

    void push(T value);

    T pop();

    T peek();
}
