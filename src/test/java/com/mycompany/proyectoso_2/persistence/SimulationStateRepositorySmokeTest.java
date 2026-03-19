package com.mycompany.proyectoso_2.persistence;

import com.mycompany.proyectoso_2.filesystem.EntryVisibility;
import com.mycompany.proyectoso_2.filesystem.FSNode;
import com.mycompany.proyectoso_2.model.SchedulingPolicy;
import com.mycompany.proyectoso_2.model.UserMode;
import com.mycompany.proyectoso_2.simulation.SimulationController;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SimulationStateRepositorySmokeTest {

    private SimulationStateRepositorySmokeTest() {
    }

    public static void main(String[] args) throws IOException {
        SimulationController sourceController = new SimulationController();
        sourceController.setSchedulingPolicy(SchedulingPolicy.C_SCAN);
        sourceController.setCurrentHeadPosition(21);
        sourceController.createDirectory("/users/daniel", "backup", EntryVisibility.PRIVATE);
        sourceController.createFile("/users/daniel/backup", "state.bin", 4, EntryVisibility.PUBLIC);
        sourceController.setCurrentHeadPosition(21);
        sourceController.setCurrentMode(UserMode.USUARIO);

        Path tempFile = Files.createTempFile("proyecto-so2-state", ".json");
        sourceController.saveToJson(tempFile);

        SimulationController loadedController = new SimulationController();
        loadedController.loadFromJson(tempFile);

        assertEquals(UserMode.USUARIO, loadedController.getCurrentMode(),
                "El modo de usuario debe persistirse.");
        assertEquals(SchedulingPolicy.C_SCAN, loadedController.getSchedulingPolicy(),
                "La politica del scheduler debe persistirse.");
        assertEquals(21, loadedController.getCurrentHeadPosition(),
                "La posicion del cabezal debe persistirse.");

        FSNode backupDir = loadedController.getFileSystemTree().findNode("/users/daniel/backup");
        FSNode restoredFile = loadedController.getFileSystemTree().findNode("/users/daniel/backup/state.bin");
        assertTrue(backupDir != null, "El directorio guardado debe restaurarse.");
        assertTrue(restoredFile != null, "El archivo guardado debe restaurarse.");
        assertEquals(4, findRow(loadedController.buildAllocationRows(), "/users/daniel/backup/state.bin")[1],
                "La tabla de asignacion debe recuperar el tamano del archivo.");
        assertEquals(10, loadedController.getDisk().countUsedBlocks(),
                "El disco debe conservar el total de bloques ocupados.");

        String jsonContent = Files.readString(tempFile);
        assertTrue(jsonContent.contains("\"policy\": \"C_SCAN\""),
                "El JSON debe guardar la politica seleccionada.");
        assertTrue(jsonContent.contains("\"path\": \"/users/daniel/backup/state.bin\""),
                "El JSON debe guardar la ruta del archivo.");

        Files.deleteIfExists(tempFile);
        System.out.println("OK: pruebas basicas de persistencia JSON completadas.");
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

    private static Object[] findRow(Object[][] rows, String path) {
        for (int index = 0; index < rows.length; index++) {
            if (path.equals(rows[index][0])) {
                return rows[index];
            }
        }
        throw new IllegalStateException("No existe fila para " + path + ".");
    }
}
