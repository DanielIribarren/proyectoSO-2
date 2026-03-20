package com.mycompany.proyectoso_2.disk;

import com.mycompany.proyectoso_2.filesystem.FileNode;

public class ChainedAllocationManager {

    private final SimulatedDisk disk;
    private int nextColorId;

    public ChainedAllocationManager(SimulatedDisk disk) {
        if (disk == null) {
            throw new IllegalArgumentException("El disco simulado es obligatorio.");
        }
        this.disk = disk;
        nextColorId = 1;
    }

    public SimulatedDisk getDisk() {
        return disk;
    }

    public void allocateFile(FileNode file) {
        validateFile(file);
        if (file.getFirstBlockIndex() >= 0) {
            throw new IllegalStateException("El archivo ya tiene bloques asignados.");
        }
        if (file.getSizeInBlocks() == 0) {
            file.setColorId(-1);
            return;
        }
        if (!disk.hasEnoughFreeBlocks(file.getSizeInBlocks())) {
            throw new IllegalStateException("No hay espacio suficiente para el archivo.");
        }

        int[] selectedBlocks = selectFreeBlocks(file.getSizeInBlocks());
        int colorId = nextColorId++;

        for (int index = 0; index < selectedBlocks.length; index++) {
            int currentBlockIndex = selectedBlocks[index];
            int nextBlockIndex = index == selectedBlocks.length - 1 ? -1 : selectedBlocks[index + 1];
            disk.occupyBlock(currentBlockIndex, file.getPath(), nextBlockIndex, colorId);
        }

        file.setFirstBlockIndex(selectedBlocks[0]);
        file.setColorId(colorId);
    }

    public void releaseFile(FileNode file) {
        validateFile(file);
        if (file.getFirstBlockIndex() < 0) {
            file.setColorId(-1);
            return;
        }

        int currentBlockIndex = file.getFirstBlockIndex();
        int safetyCounter = 0;
        while (currentBlockIndex >= 0) {
            DiskBlock currentBlock = disk.getBlock(currentBlockIndex);
            int nextBlockIndex = currentBlock.getNextBlockIndex();
            disk.releaseBlock(currentBlockIndex);
            currentBlockIndex = nextBlockIndex;

            safetyCounter++;
            if (safetyCounter > disk.getTotalBlocks()) {
                throw new IllegalStateException("Se detecto una cadena de bloques invalida.");
            }
        }

        file.setFirstBlockIndex(-1);
        file.setColorId(-1);
    }

    public int[] getAllocatedBlocks(FileNode file) {
        validateFile(file);
        if (file.getFirstBlockIndex() < 0) {
            return new int[0];
        }

        int[] blockIndexes = new int[disk.getTotalBlocks()];
        int count = 0;
        int currentBlockIndex = file.getFirstBlockIndex();

        while (currentBlockIndex >= 0) {
            if (count == blockIndexes.length) {
                throw new IllegalStateException("La cadena del archivo excede el tamano del disco.");
            }
            blockIndexes[count] = currentBlockIndex;
            currentBlockIndex = disk.getBlock(currentBlockIndex).getNextBlockIndex();
            count++;
        }

        int[] compactIndexes = new int[count];
        for (int index = 0; index < count; index++) {
            compactIndexes[index] = blockIndexes[index];
        }
        return compactIndexes;
    }

    private int[] selectFreeBlocks(int requiredBlocks) {
        int[] selectedBlocks = new int[requiredBlocks];
        int selectedCount = 0;

        for (int blockIndex = 0; blockIndex < disk.getTotalBlocks(); blockIndex++) {
            if (!disk.getBlock(blockIndex).isFree()) {
                continue;
            }
            selectedBlocks[selectedCount] = blockIndex;
            selectedCount++;
            if (selectedCount == requiredBlocks) {
                return selectedBlocks;
            }
        }

        throw new IllegalStateException("No fue posible seleccionar todos los bloques requeridos.");
    }

    private void validateFile(FileNode file) {
        if (file == null) {
            throw new IllegalArgumentException("El archivo no puede ser nulo.");
        }
    }
}
