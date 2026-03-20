package com.mycompany.proyectoso_2.filesystem;

public class FileSystemTree {

    private final DirectoryNode root;

    public FileSystemTree() {
        root = new DirectoryNode("/", "system", EntryVisibility.SYSTEM);
    }

    public DirectoryNode getRoot() {
        return root;
    }

    public FSNode findNode(String path) {
        validatePath(path);
        if ("/".equals(path)) {
            return root;
        }

        String[] segments = splitPath(path);
        FSNode current = root;
        for (int index = 0; index < segments.length; index++) {
            if (!(current instanceof DirectoryNode directory)) {
                return null;
            }
            current = findChild(directory, segments[index]);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public DirectoryNode createDirectory(
            String parentPath,
            String name,
            String owner,
            EntryVisibility visibility
    ) {
        DirectoryNode parent = requireDirectory(parentPath);
        DirectoryNode newDirectory = new DirectoryNode(name, owner, visibility);
        parent.addChild(newDirectory);
        return newDirectory;
    }

    public FileNode createFile(
            String parentPath,
            String name,
            String owner,
            EntryVisibility visibility,
            int sizeInBlocks
    ) {
        DirectoryNode parent = requireDirectory(parentPath);
        FileNode newFile = new FileNode(name, owner, visibility, sizeInBlocks);
        parent.addChild(newFile);
        return newFile;
    }

    public FSNode removeNode(String path) {
        validatePath(path);
        if ("/".equals(path)) {
            throw new IllegalArgumentException("La raiz del sistema no se puede eliminar.");
        }

        String parentPath = getParentPath(path);
        String nodeName = getNodeName(path);
        DirectoryNode parent = requireDirectory(parentPath);
        int childIndex = parent.indexOfChild(nodeName);
        if (childIndex < 0) {
            throw new IllegalArgumentException("No existe la ruta: " + path + ".");
        }
        return parent.removeChildAt(childIndex);
    }

    public FSNode renameNode(String path, String newName) {
        validatePath(path);
        if ("/".equals(path)) {
            throw new IllegalArgumentException("La raiz del sistema no se puede renombrar.");
        }

        String parentPath = getParentPath(path);
        DirectoryNode parent = requireDirectory(parentPath);
        int childIndex = parent.indexOfChild(getNodeName(path));
        if (childIndex < 0) {
            throw new IllegalArgumentException("No existe la ruta: " + path + ".");
        }
        if (parent.indexOfChild(newName) >= 0) {
            throw new IllegalArgumentException("Ya existe un nodo con nombre " + newName + ".");
        }

        FSNode node = parent.getChildAt(childIndex);
        node.rename(newName);
        return node;
    }

    public DirectoryNode requireDirectory(String path) {
        FSNode node = findNode(path);
        if (node == null) {
            throw new IllegalArgumentException("No existe la ruta: " + path + ".");
        }
        if (!(node instanceof DirectoryNode directory)) {
            throw new IllegalArgumentException("La ruta no apunta a un directorio: " + path + ".");
        }
        return directory;
    }

    public void clear() {
        while (root.getChildrenCount() > 0) {
            root.removeChildAt(0);
        }
    }

    private FSNode findChild(DirectoryNode directory, String name) {
        for (int index = 0; index < directory.getChildrenCount(); index++) {
            FSNode child = directory.getChildAt(index);
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    private String[] splitPath(String path) {
        String normalizedPath = path.substring(1);
        return normalizedPath.split("/");
    }

    private String getParentPath(String path) {
        int separatorIndex = path.lastIndexOf('/');
        if (separatorIndex <= 0) {
            return "/";
        }
        return path.substring(0, separatorIndex);
    }

    private String getNodeName(String path) {
        int separatorIndex = path.lastIndexOf('/');
        return path.substring(separatorIndex + 1);
    }

    private void validatePath(String path) {
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException("La ruta debe empezar por '/'.");
        }
    }
}
