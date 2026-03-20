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

        assertTrue(controller.getFileSystemTree().findNode("/system/readme.txt") != null,
                "El sistema inicial debe cargar archivos base.");
        assertEquals(3, controller.buildAllocationRows().length,
                "La tabla inicial debe reflejar tres archivos.");

        controller.setSchedulingPolicy(SchedulingPolicy.SCAN);
        controller.setCurrentHeadPosition(6);
        controller.createDirectory("/users/daniel", "projects", EntryVisibility.PRIVATE);
        controller.createFile("/users/daniel/projects", "todo.txt", 2, EntryVisibility.PRIVATE);
        controller.renameNode("/users/daniel/projects/todo.txt", "ideas.txt");

        FSNode renamedNode = controller.getFileSystemTree().findNode("/users/daniel/projects/ideas.txt");
        assertTrue(renamedNode != null, "El archivo renombrado debe existir.");

        controller.simulateFailedCreate("/users/daniel/projects", "crash.tmp", 2);
        assertTrue(controller.getFileSystemTree().findNode("/users/daniel/projects/crash.tmp") == null,
                "El archivo con fallo simulado no debe persistir.");

        controller.deleteNode("/users/daniel/projects/ideas.txt");
        assertTrue(controller.getFileSystemTree().findNode("/users/daniel/projects/ideas.txt") == null,
                "El archivo eliminado no debe existir.");

        assertTrue(controller.buildJournalLines().length >= 3,
                "El journal debe registrar operaciones criticas.");
        assertTrue(controller.buildProcessRows().length >= 5,
                "El historial debe registrar los procesos ejecutados.");
        assertEquals(0, controller.countProcessesByState(ProcessState.BLOCKED),
                "Este flujo no debe dejar procesos bloqueados.");

        controller.setCurrentMode(UserMode.USUARIO);
        boolean blocked = false;
        try {
            controller.createDirectory("/users/daniel", "forbidden", EntryVisibility.PRIVATE);
        } catch (IllegalStateException exception) {
            blocked = true;
        }
        assertTrue(blocked, "El modo usuario debe impedir modificaciones.");

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
}
