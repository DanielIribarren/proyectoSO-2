package com.mycompany.proyectoso_2.locks;

import com.mycompany.proyectoso_2.process.IORequest;
import com.mycompany.proyectoso_2.process.OperationType;
import com.mycompany.proyectoso_2.process.ProcessControlBlock;
import com.mycompany.proyectoso_2.process.ProcessState;
import com.mycompany.proyectoso_2.structures.SinglyLinkedList;

public final class LockManagerSmokeTest {

    private LockManagerSmokeTest() {
    }

    public static void main(String[] args) {
        LockManager lockManager = new LockManager();
        ProcessControlBlock readerOne = buildProcess(1, OperationType.READ, "/users/daniel/data.txt", 12);
        ProcessControlBlock writer = buildProcess(2, OperationType.UPDATE, "/users/daniel/data.txt", 12);
        ProcessControlBlock readerTwo = buildProcess(3, OperationType.READ, "/users/daniel/data.txt", 12);

        assertTrue(lockManager.acquireLock(new LockRequest(
                "/users/daniel/data.txt",
                readerOne,
                LockType.SHARED
        )), "El primer lector debe obtener lock compartido.");
        assertFalse(lockManager.acquireLock(new LockRequest(
                "/users/daniel/data.txt",
                writer,
                LockType.EXCLUSIVE
        )), "El escritor debe quedar bloqueado por un lector activo.");
        assertFalse(lockManager.acquireLock(new LockRequest(
                "/users/daniel/data.txt",
                readerTwo,
                LockType.SHARED
        )), "Un lector nuevo debe esperar detras del escritor.");

        assertEquals(ProcessState.BLOCKED, writer.getState(),
                "El escritor debe pasar a BLOCKED.");
        assertEquals(ProcessState.BLOCKED, readerTwo.getState(),
                "El segundo lector debe quedar bloqueado.");
        assertEquals(1, lockManager.countActiveLocks("/users/daniel/data.txt"),
                "Debe existir un solo lock activo al principio.");
        assertEquals(2, lockManager.countWaitingLocks("/users/daniel/data.txt"),
                "Debe haber dos solicitudes en espera.");

        SinglyLinkedList<ProcessControlBlock> awakenedAfterReader = lockManager.releaseLocksByProcess(1);
        assertEquals(1, awakenedAfterReader.size(),
                "Al liberar el lector debe despertar solo al escritor.");
        assertEquals(2, awakenedAfterReader.getFirst().getPid(),
                "El escritor debe despertar primero.");
        assertEquals(ProcessState.READY, writer.getState(),
                "El escritor desbloqueado debe pasar a READY.");
        assertEquals(1, lockManager.countActiveLocks("/users/daniel/data.txt"),
                "El escritor debe quedar con lock exclusivo activo.");
        assertEquals(1, lockManager.countWaitingLocks("/users/daniel/data.txt"),
                "El segundo lector debe seguir esperando.");

        SinglyLinkedList<ProcessControlBlock> awakenedAfterWriter = lockManager.releaseLocksByProcess(2);
        assertEquals(1, awakenedAfterWriter.size(),
                "Al liberar el escritor debe despertar al lector restante.");
        assertEquals(3, awakenedAfterWriter.getFirst().getPid(),
                "El segundo lector debe despertar al final.");
        assertEquals(ProcessState.READY, readerTwo.getState(),
                "El lector desbloqueado debe pasar a READY.");
        assertEquals(1, lockManager.countActiveLocks("/users/daniel/data.txt"),
                "Debe quedar un lock compartido activo.");
        assertEquals(0, lockManager.countWaitingLocks("/users/daniel/data.txt"),
                "La cola de espera debe quedar vacia.");

        System.out.println("OK: pruebas basicas de locks completadas.");
    }

    private static ProcessControlBlock buildProcess(
            int pid,
            OperationType operationType,
            String path,
            int position
    ) {
        return new ProcessControlBlock(pid, new IORequest(operationType, path, position));
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

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new IllegalStateException(message);
        }
    }
}
