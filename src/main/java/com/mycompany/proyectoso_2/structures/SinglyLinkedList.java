package com.mycompany.proyectoso_2.structures;

import java.util.Objects;

public class SinglyLinkedList<T> implements CustomList<T> {

    private LinkedNode<T> head;
    private LinkedNode<T> tail;
    private int size;

    @Override
    public void addFirst(T value) {
        LinkedNode<T> newNode = new LinkedNode<>(value);
        newNode.setNext(head);
        head = newNode;
        if (tail == null) {
            tail = newNode;
        }
        size++;
    }

    @Override
    public void addLast(T value) {
        LinkedNode<T> newNode = new LinkedNode<>(value);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }

    @Override
    public void insertAt(int index, T value) {
        validatePositionIndex(index);
        if (index == 0) {
            addFirst(value);
            return;
        }
        if (index == size) {
            addLast(value);
            return;
        }

        LinkedNode<T> previous = getNode(index - 1);
        LinkedNode<T> newNode = new LinkedNode<>(value);
        newNode.setNext(previous.getNext());
        previous.setNext(newNode);
        size++;
    }

    @Override
    public T getFirst() {
        ensureNotEmpty();
        return head.getValue();
    }

    @Override
    public T getLast() {
        ensureNotEmpty();
        return tail.getValue();
    }

    @Override
    public T get(int index) {
        return getNode(index).getValue();
    }

    @Override
    public T set(int index, T value) {
        LinkedNode<T> node = getNode(index);
        T previousValue = node.getValue();
        node.setValue(value);
        return previousValue;
    }

    @Override
    public T removeFirst() {
        ensureNotEmpty();
        T removedValue = head.getValue();
        head = head.getNext();
        size--;
        if (head == null) {
            tail = null;
        }
        return removedValue;
    }

    @Override
    public T removeLast() {
        ensureNotEmpty();
        if (size == 1) {
            return removeFirst();
        }

        LinkedNode<T> previousTail = getNode(size - 2);
        T removedValue = tail.getValue();
        previousTail.setNext(null);
        tail = previousTail;
        size--;
        return removedValue;
    }

    @Override
    public T removeAt(int index) {
        validateElementIndex(index);
        if (index == 0) {
            return removeFirst();
        }
        if (index == size - 1) {
            return removeLast();
        }

        LinkedNode<T> previous = getNode(index - 1);
        LinkedNode<T> removedNode = previous.getNext();
        previous.setNext(removedNode.getNext());
        size--;
        return removedNode.getValue();
    }

    @Override
    public boolean contains(T value) {
        return indexOf(value) >= 0;
    }

    @Override
    public int indexOf(T value) {
        LinkedNode<T> current = head;
        int index = 0;
        while (current != null) {
            if (Objects.equals(current.getValue(), value)) {
                return index;
            }
            current = current.getNext();
            index++;
        }
        return -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    private LinkedNode<T> getNode(int index) {
        validateElementIndex(index);
        LinkedNode<T> current = head;
        int currentIndex = 0;
        while (currentIndex < index) {
            current = current.getNext();
            currentIndex++;
        }
        return current;
    }

    private void ensureNotEmpty() {
        if (isEmpty()) {
            throw new IllegalStateException("La estructura esta vacia.");
        }
    }

    private void validateElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    "Indice fuera de rango: " + index + ", tamano actual: " + size + "."
            );
        }
    }

    private void validatePositionIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(
                    "Posicion fuera de rango: " + index + ", tamano actual: " + size + "."
            );
        }
    }
}
