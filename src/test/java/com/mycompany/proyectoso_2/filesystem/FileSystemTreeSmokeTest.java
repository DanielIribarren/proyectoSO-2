package com.mycompany.proyectoso_2.filesystem;

public final class FileSystemTreeSmokeTest {

    private FileSystemTreeSmokeTest() {
    }

    public static void main(String[] args) {
        FileSystemTree tree = new FileSystemTree();

        DirectoryNode users = tree.createDirectory("/", "users", "system", EntryVisibility.SYSTEM);
        DirectoryNode daniel = tree.createDirectory("/users", "daniel", "daniel", EntryVisibility.PRIVATE);
        FileNode notes = tree.createFile("/users/daniel", "notes.txt", "daniel", EntryVisibility.PRIVATE, 3);

        assertEquals("/", tree.getRoot().getPath(), "La raiz debe conservar su ruta.");
        assertEquals(1, users.getChildrenCount(), "Users debe tener un hijo.");
        assertEquals("/users/daniel", daniel.getPath(), "La ruta de daniel es invalida.");
        assertEquals("/users/daniel/notes.txt", notes.getPath(), "La ruta del archivo es invalida.");
        assertEquals(FSNodeType.FILE, tree.findNode("/users/daniel/notes.txt").getType(),
                "La busqueda debe encontrar el archivo.");
        assertTrue(tree.findNode("/users/ghost") == null,
                "Una ruta inexistente debe devolver null.");

        System.out.println("OK: pruebas basicas del arbol de archivos completadas.");
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
