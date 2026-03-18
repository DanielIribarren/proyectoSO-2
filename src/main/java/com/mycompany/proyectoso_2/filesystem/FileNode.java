package com.mycompany.proyectoso_2.filesystem;

public class FileNode extends FSNode {

    private int sizeInBlocks;
    private int firstBlockIndex;
    private int colorId;

    public FileNode(String name, String owner, EntryVisibility visibility, int sizeInBlocks) {
        super(name, owner, visibility);
        setSizeInBlocks(sizeInBlocks);
        firstBlockIndex = -1;
        colorId = -1;
    }

    @Override
    public FSNodeType getType() {
        return FSNodeType.FILE;
    }

    public int getSizeInBlocks() {
        return sizeInBlocks;
    }

    public void setSizeInBlocks(int sizeInBlocks) {
        if (sizeInBlocks < 0) {
            throw new IllegalArgumentException("El tamano del archivo no puede ser negativo.");
        }
        this.sizeInBlocks = sizeInBlocks;
    }

    public int getFirstBlockIndex() {
        return firstBlockIndex;
    }

    public void setFirstBlockIndex(int firstBlockIndex) {
        this.firstBlockIndex = firstBlockIndex;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }
}
