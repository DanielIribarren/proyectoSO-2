package com.mycompany.proyectoso_2.persistence;

import com.mycompany.proyectoso_2.filesystem.EntryVisibility;

public class SavedFile {

    private final String path;
    private final String owner;
    private final EntryVisibility visibility;
    private final int sizeInBlocks;
    private final int[] blocks;
    private final int colorId;

    public SavedFile(
            String path,
            String owner,
            EntryVisibility visibility,
            int sizeInBlocks,
            int[] blocks,
            int colorId
    ) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("La ruta del archivo es obligatoria.");
        }
        this.path = path;
        this.owner = owner;
        this.visibility = visibility;
        this.sizeInBlocks = sizeInBlocks;
        this.blocks = copyBlocks(blocks);
        this.colorId = colorId;
    }

    public String getPath() {
        return path;
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

    public int[] getBlocks() {
        return copyBlocks(blocks);
    }

    public int getColorId() {
        return colorId;
    }

    private int[] copyBlocks(int[] source) {
        if (source == null) {
            return new int[0];
        }
        int[] copy = new int[source.length];
        for (int index = 0; index < source.length; index++) {
            copy[index] = source[index];
        }
        return copy;
    }
}
