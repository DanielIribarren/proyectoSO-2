package com.mycompany.proyectoso_2.journal;

import com.mycompany.proyectoso_2.disk.ChainedAllocationManager;
import com.mycompany.proyectoso_2.disk.SimulatedDisk;
import com.mycompany.proyectoso_2.filesystem.EntryVisibility;
import com.mycompany.proyectoso_2.filesystem.FSNode;
import com.mycompany.proyectoso_2.filesystem.FileNode;
import com.mycompany.proyectoso_2.filesystem.FileSystemTree;

public final class JournalManagerSmokeTest {

    private JournalManagerSmokeTest() {
    }

    public static void main(String[] args) {
        FileSystemTree tree = new FileSystemTree();
        tree.createDirectory("/", "users", "system", EntryVisibility.SYSTEM);
        tree.createDirectory("/users", "daniel", "daniel", EntryVisibility.PRIVATE);

        SimulatedDisk disk = new SimulatedDisk(10);
        ChainedAllocationManager allocationManager = new ChainedAllocationManager(disk);
        JournalManager journalManager = new JournalManager();

        JournalEntry createEntry = journalManager.beginCreate(
                "/users/daniel",
                "draft.txt",
                "daniel",
                EntryVisibility.PRIVATE,
                2
        );
        FileNode draft = tree.createFile("/users/daniel", "draft.txt", "daniel", EntryVisibility.PRIVATE, 2);
        allocationManager.allocateFile(draft);
        journalManager.recordCreateResult(createEntry, draft, allocationManager.getAllocatedBlocks(draft));

        assertEquals(2, disk.countUsedBlocks(),
                "El archivo creado debe ocupar dos bloques antes del fallo.");
        journalManager.recoverPending(tree, allocationManager);
        assertEquals(JournalStatus.UNDONE, createEntry.getStatus(),
                "El create pendiente debe pasar a UNDONE tras recovery.");
        assertTrue(tree.findNode("/users/daniel/draft.txt") == null,
                "El archivo pendiente no debe sobrevivir al recovery.");
        assertEquals(0, disk.countUsedBlocks(),
                "Los bloques del create fallido deben liberarse.");

        FileNode report = tree.createFile("/users/daniel", "report.txt", "daniel", EntryVisibility.PRIVATE, 3);
        allocationManager.allocateFile(report);
        int[] reportBlocks = allocationManager.getAllocatedBlocks(report);
        JournalEntry deleteEntry = journalManager.beginDelete(report, reportBlocks);
        allocationManager.releaseFile(report);
        tree.removeNode("/users/daniel/report.txt");

        journalManager.recoverPending(tree, allocationManager);
        assertEquals(JournalStatus.UNDONE, deleteEntry.getStatus(),
                "El delete pendiente debe pasar a UNDONE tras recovery.");

        FSNode restoredNode = tree.findNode("/users/daniel/report.txt");
        assertTrue(restoredNode instanceof FileNode,
                "El archivo eliminado debe restaurarse tras recovery.");

        FileNode restoredFile = (FileNode) restoredNode;
        assertArrayEquals(reportBlocks, allocationManager.getAllocatedBlocks(restoredFile),
                "La cadena de bloques restaurada debe coincidir con la original.");
        assertEquals(3, disk.countUsedBlocks(),
                "El archivo restaurado debe volver a ocupar sus bloques.");

        System.out.println("OK: pruebas basicas de journal completadas.");
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

    private static void assertArrayEquals(int[] expected, int[] actual, String message) {
        if (expected.length != actual.length) {
            throw new IllegalStateException(message
                    + " Longitud esperada: " + expected.length
                    + ", recibida: " + actual.length + ".");
        }
        for (int index = 0; index < expected.length; index++) {
            if (expected[index] != actual[index]) {
                throw new IllegalStateException(message
                        + " Diferencia en posicion " + index
                        + ": esperado " + expected[index]
                        + ", recibido " + actual[index] + ".");
            }
        }
    }
}
