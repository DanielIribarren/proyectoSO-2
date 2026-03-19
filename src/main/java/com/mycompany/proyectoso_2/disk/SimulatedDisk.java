package com.mycompany.proyectoso_2.disk;

public class SimulatedDisk {

    private final DiskBlock[] blocks;

    public SimulatedDisk(int totalBlocks) {
        if (totalBlocks <= 0) {
            throw new IllegalArgumentException("El disco debe tener al menos un bloque.");
        }
        blocks = new DiskBlock[totalBlocks];
        initializeBlocks();
    }

    public int getTotalBlocks() {
        return blocks.length;
    }

    public DiskBlock getBlock(int blockIndex) {
        validateBlockIndex(blockIndex);
        return blocks[blockIndex];
    }

    public int countFreeBlocks() {
        int freeBlocks = 0;
        for (int blockIndex = 0; blockIndex < blocks.length; blockIndex++) {
            if (blocks[blockIndex].isFree()) {
                freeBlocks++;
            }
        }
        return freeBlocks;
    }

    public int countUsedBlocks() {
        return blocks.length - countFreeBlocks();
    }

    public boolean hasEnoughFreeBlocks(int requiredBlocks) {
        if (requiredBlocks < 0) {
            throw new IllegalArgumentException("La cantidad requerida no puede ser negativa.");
        }
        return countFreeBlocks() >= requiredBlocks;
    }

    public void occupyBlock(int blockIndex, String filePath, int nextBlockIndex, int colorId) {
        validateBlockIndex(blockIndex);
        if (nextBlockIndex >= blocks.length) {
            throw new IllegalArgumentException("El siguiente bloque no existe: " + nextBlockIndex + ".");
        }
        blocks[blockIndex].assignToFile(filePath, nextBlockIndex, colorId);
    }

    public void releaseBlock(int blockIndex) {
        validateBlockIndex(blockIndex);
        blocks[blockIndex].release();
    }

    public void clear() {
        for (int blockIndex = 0; blockIndex < blocks.length; blockIndex++) {
            blocks[blockIndex].release();
        }
    }

    private void initializeBlocks() {
        for (int blockIndex = 0; blockIndex < blocks.length; blockIndex++) {
            blocks[blockIndex] = new DiskBlock(blockIndex);
        }
    }

    private void validateBlockIndex(int blockIndex) {
        if (blockIndex < 0 || blockIndex >= blocks.length) {
            throw new IndexOutOfBoundsException("Bloque fuera de rango: " + blockIndex + ".");
        }
    }
}
