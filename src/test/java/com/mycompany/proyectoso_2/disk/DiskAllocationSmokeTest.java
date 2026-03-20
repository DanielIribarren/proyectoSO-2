package com.mycompany.proyectoso_2.disk;

import com.mycompany.proyectoso_2.filesystem.EntryVisibility;
import com.mycompany.proyectoso_2.filesystem.FileNode;
import com.mycompany.proyectoso_2.filesystem.FileSystemTree;

public final class DiskAllocationSmokeTest {

    private DiskAllocationSmokeTest() {
    }

    public static void main(String[] args) {
        FileSystemTree tree = new FileSystemTree();
        tree.createDirectory("/", "users", "system", EntryVisibility.SYSTEM);
        tree.createDirectory("/users", "daniel", "daniel", EntryVisibility.PRIVATE);

        FileNode fileA = tree.createFile("/users/daniel", "alpha.txt", "daniel", EntryVisibility.PRIVATE, 2);
        FileNode fileB = tree.createFile("/users/daniel", "beta.txt", "daniel", EntryVisibility.PRIVATE, 2);
        FileNode fileC = tree.createFile("/users/daniel", "gamma.txt", "daniel", EntryVisibility.PRIVATE, 3);

        SimulatedDisk disk = new SimulatedDisk(8);
        ChainedAllocationManager allocationManager = new ChainedAllocationManager(disk);

        allocationManager.allocateFile(fileA);
        allocationManager.allocateFile(fileB);
        allocationManager.releaseFile(fileA);
        allocationManager.allocateFile(fileC);

        assertArrayEquals(new int[]{2, 3}, allocationManager.getAllocatedBlocks(fileB),
                "beta.txt debe conservar sus bloques.");
        assertArrayEquals(new int[]{0, 1, 4}, allocationManager.getAllocatedBlocks(fileC),
                "gamma.txt debe quedar fragmentado y encadenado.");
        assertEquals(1, disk.getBlock(0).getNextBlockIndex(),
                "El bloque 0 debe apuntar al bloque 1.");
        assertEquals(4, disk.getBlock(1).getNextBlockIndex(),
                "El bloque 1 debe apuntar al bloque 4.");
        assertEquals(-1, disk.getBlock(4).getNextBlockIndex(),
                "El ultimo bloque debe cerrar la cadena.");
        assertEquals("/users/daniel/gamma.txt", disk.getBlock(4).getFilePath(),
                "El bloque final debe pertenecer a gamma.txt.");
        assertEquals(5, disk.countUsedBlocks(),
                "Despues de la fragmentacion deben quedar cinco bloques ocupados.");

        allocationManager.releaseFile(fileC);
        assertEquals(-1, fileC.getFirstBlockIndex(),
                "El archivo liberado debe perder su primer bloque.");
        assertEquals(-1, fileC.getColorId(),
                "El archivo liberado debe limpiar su color.");
        assertEquals(2, disk.countUsedBlocks(),
                "Solo beta.txt debe quedar ocupando el disco.");

        allocationManager.releaseFile(fileB);
        assertEquals(8, disk.countFreeBlocks(),
                "Todos los bloques deben quedar libres al final.");

        System.out.println("OK: pruebas basicas de asignacion encadenada completadas.");
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
