package com.mycompany.proyectoso_2.structures;

public final class StructureSmokeTest {

    private StructureSmokeTest() {
    }

    public static void main(String[] args) {
        testListOperations();
        testQueueOperations();
        testStackOperations();
        System.out.println("OK: pruebas basicas de estructuras completadas.");
    }

    private static void testListOperations() {
        SinglyLinkedList<String> list = new SinglyLinkedList<>();

        list.addLast("B");
        list.addFirst("A");
        list.addLast("C");
        list.insertAt(2, "X");

        assertEquals(4, list.size(), "La lista debe tener cuatro elementos.");
        assertEquals("A", list.getFirst(), "El primer elemento debe ser A.");
        assertEquals("C", list.getLast(), "El ultimo elemento debe ser C.");
        assertEquals("X", list.get(2), "El elemento en posicion 2 debe ser X.");

        assertEquals("X", list.removeAt(2), "Se debe remover X.");
        assertEquals("A", list.removeFirst(), "Se debe remover A.");
        assertEquals("C", list.removeLast(), "Se debe remover C.");
        assertTrue(list.contains("B"), "La lista debe contener B.");
        assertEquals(0, list.indexOf("B"), "B debe quedar en la posicion 0.");
    }

    private static void testQueueOperations() {
        LinkedQueue<Integer> queue = new LinkedQueue<>();

        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);

        assertEquals(3, queue.size(), "La cola debe tener tres elementos.");
        assertEquals(10, queue.peek(), "El frente de la cola debe ser 10.");
        assertEquals(10, queue.dequeue(), "Debe salir 10 primero.");
        assertEquals(20, queue.dequeue(), "Debe salir 20 despues.");
        assertEquals(1, queue.size(), "La cola debe quedar con un elemento.");
    }

    private static void testStackOperations() {
        LinkedStack<String> stack = new LinkedStack<>();

        stack.push("base");
        stack.push("medio");
        stack.push("tope");

        assertEquals(3, stack.size(), "La pila debe tener tres elementos.");
        assertEquals("tope", stack.peek(), "El tope debe ser tope.");
        assertEquals("tope", stack.pop(), "Debe salir tope primero.");
        assertEquals("medio", stack.pop(), "Debe salir medio despues.");
        assertEquals("base", stack.peek(), "Ahora el tope debe ser base.");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw new IllegalStateException(message
                + " Esperado: " + expected
                + ", recibido: " + actual + ".");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
