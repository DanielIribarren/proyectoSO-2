package com.mycompany.proyectoso_2.disk;

public class DiskBlock {

    private final int index;
    private boolean free;
    private String filePath;
    private int nextBlockIndex;
    private int colorId;

    public DiskBlock(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("El indice del bloque no puede ser negativo.");
        }
        this.index = index;
        release();
    }

    public int getIndex() {
        return index;
    }

    public boolean isFree() {
        return free;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getNextBlockIndex() {
        return nextBlockIndex;
    }

    public int getColorId() {
        return colorId;
    }

    public void assignToFile(String filePath, int nextBlockIndex, int colorId) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("La ruta del archivo es obligatoria.");
        }
        if (colorId < 0) {
            throw new IllegalArgumentException("El color del archivo no puede ser negativo.");
        }
        free = false;
        this.filePath = filePath;
        this.nextBlockIndex = nextBlockIndex;
        this.colorId = colorId;
    }

    public void release() {
        free = true;
        filePath = null;
        nextBlockIndex = -1;
        colorId = -1;
    }
}
