package com.mycompany.proyectoso_2.journal;

import com.mycompany.proyectoso_2.filesystem.EntryVisibility;
import com.mycompany.proyectoso_2.filesystem.FileNode;

public class JournalFileSnapshot {

    private final String parentPath;
    private final String name;
    private final String owner;
    private final EntryVisibility visibility;
    private final int sizeInBlocks;
    private final int firstBlockIndex;
    private final int colorId;

    public JournalFileSnapshot(
            String parentPath,
            String name,
            String owner,
            EntryVisibility visibility,
            int sizeInBlocks,
            int firstBlockIndex,
            int colorId
    ) {
        if (parentPath == null || parentPath.isBlank()) {
            throw new IllegalArgumentException("La ruta padre es obligatoria.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo es obligatorio.");
        }
        this.parentPath = parentPath;
        this.name = name;
        this.owner = owner;
        this.visibility = visibility;
        this.sizeInBlocks = sizeInBlocks;
        this.firstBlockIndex = firstBlockIndex;
        this.colorId = colorId;
    }

    public static JournalFileSnapshot fromFileNode(FileNode file) {
        if (file == null) {
            throw new IllegalArgumentException("El archivo a registrar no puede ser nulo.");
        }
        return new JournalFileSnapshot(
                extractParentPath(file.getPath()),
                file.getName(),
                file.getOwner(),
                file.getVisibility(),
                file.getSizeInBlocks(),
                file.getFirstBlockIndex(),
                file.getColorId()
        );
    }

    public String getParentPath() {
        return parentPath;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public EntryVisibility getVisibility() {
        return visibility;
    }

    public int getSizeInBlocks() {
        return sizeInBlocks;
    }

    public int getFirstBlockIndex() {
        return firstBlockIndex;
    }

    public int getColorId() {
        return colorId;
    }

    public String getFullPath() {
        if ("/".equals(parentPath)) {
            return parentPath + name;
        }
        return parentPath + "/" + name;
    }

    private static String extractParentPath(String fullPath) {
        int separatorIndex = fullPath.lastIndexOf('/');
        if (separatorIndex <= 0) {
            return "/";
        }
        return fullPath.substring(0, separatorIndex);
    }
}
