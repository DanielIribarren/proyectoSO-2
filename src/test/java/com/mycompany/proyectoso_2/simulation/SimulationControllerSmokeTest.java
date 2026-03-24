package com.mycompany.proyectoso_2.simulation;

import com.mycompany.proyectoso_2.filesystem.EntryVisibility;
import com.mycompany.proyectoso_2.filesystem.FSNode;
import com.mycompany.proyectoso_2.model.SchedulingPolicy;
import com.mycompany.proyectoso_2.model.UserMode;
import com.mycompany.proyectoso_2.process.ProcessState;

public final class SimulationControllerSmokeTest {

    private SimulationControllerSmokeTest() {
    }

    public static void main(String[] args) {
        SimulationController controller = new SimulationController();
        controller.startScheduler();

        assertTrue(controller.getFileSystemTree().findNode("/system/readme.txt") != null,
                "El sistema inicial debe cargar archivos base.");
        assertEquals(3, controller.buildAllocationRows().length,
                "La tabla inicial debe reflejar tres archivos.");

        controller.setSchedulingPolicy(SchedulingPolicy.SCAN);
        controller.setCurrentHeadPosition(6);
        controller.createDirectory("/users/daniel", "projects", EntryVisibility.PRIVATE);
        assertTrue(controller.waitUntilIdle(4_000), "La creacion del directorio debe completarse.");
        controller.createFile("/users/daniel/projects", "todo.txt", 2, EntryVisibility.PRIVATE);
        assertTrue(controller.waitUntilIdle(4_000), "La creacion del archivo debe completarse.");
        controller.renameNode("/users/daniel/projects/todo.txt", "ideas.txt");
        assertTrue(controller.waitUntilIdle(4_000), "El renombrado debe completarse.");

        FSNode renamedNode = controller.getFileSystemTree().findNode("/users/daniel/projects/ideas.txt");
        assertTrue(renamedNode != null, "El archivo renombrado debe existir.");

        controller.simulateFailedCreate("/users/daniel/projects", "crash.tmp", 2);
        assertTrue(controller.waitUntilIdle(4_000), "La simulacion de fallo debe completarse.");
        assertTrue(controller.getFileSystemTree().findNode("/users/daniel/projects/crash.tmp") == null,
                "El archivo con fallo simulado no debe persistir.");

        controller.deleteNode("/users/daniel/projects/ideas.txt");
        assertTrue(controller.waitUntilIdle(4_000), "La eliminacion debe completarse.");
        assertTrue(controller.getFileSystemTree().findNode("/users/daniel/projects/ideas.txt") == null,
                "El archivo eliminado no debe existir.");

        assertTrue(controller.buildJournalLines().length >= 3,
                "El journal debe registrar operaciones criticas.");
        assertTrue(controller.buildProcessRows().length >= 5,
                "El historial debe registrar los procesos ejecutados.");
        assertEquals(0, controller.countProcessesByState(ProcessState.BLOCKED),
                "Este flujo no debe dejar procesos bloqueados.");

        controller.setCurrentMode(UserMode.USUARIO);
        controller.pauseScheduler();
        controller.queueRead("/users/daniel/notes.txt");
        assertEquals(ProcessState.NEW, controller.buildProcessRows()[controller.buildProcessRows().length - 1][3],
                "El proceso debe quedar en NEW mientras el scheduler esta pausado.");
        controller.resumeScheduler();
        assertTrue(controller.waitUntilIdle(4_000), "La lectura debe completarse al reanudar.");
        assertTrue(controller.buildVisibleTreeSnapshot().findNode("/system/readme.txt") != null,
                "Los archivos publicos deben seguir visibles en modo usuario.");
        assertTrue(controller.buildVisibleTreeSnapshot().findNode("/system/config.sys") == null,
                "Los archivos de sistema no deben verse en modo usuario.");

        boolean blocked = false;
        try {
            controller.createDirectory("/users/daniel", "forbidden", EntryVisibility.PRIVATE);
        } catch (IllegalStateException exception) {
            blocked = true;
        }
        assertTrue(blocked, "El modo usuario debe impedir modificaciones.");

        controller.setCurrentMode(UserMode.ADMINISTRADOR);
        controller.resumeScheduler();
        controller.createFile("/users/daniel/projects", "interrupt.bin", 2, EntryVisibility.PRIVATE);
        sleep(120);
        controller.interruptCurrentProcess();
        assertTrue(controller.waitUntilIdle(4_000), "La interrupcion debe cerrar el proceso actual.");
        assertTrue(controller.getFileSystemTree().findNode("/users/daniel/projects/interrupt.bin") == null,
                "El archivo interrumpido no debe persistir.");

        System.out.println("OK: pruebas basicas del controlador completadas.");
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

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La prueba fue interrumpida.");
        }
    }
}
