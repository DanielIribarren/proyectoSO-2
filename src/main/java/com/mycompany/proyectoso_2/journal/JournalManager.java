package com.mycompany.proyectoso_2.journal;

import com.mycompany.proyectoso_2.disk.ChainedAllocationManager;
import com.mycompany.proyectoso_2.filesystem.FSNode;
import com.mycompany.proyectoso_2.filesystem.FileNode;
import com.mycompany.proyectoso_2.filesystem.FileSystemTree;
import com.mycompany.proyectoso_2.process.OperationType;
import com.mycompany.proyectoso_2.structures.SinglyLinkedList;

public class JournalManager {

    private final SinglyLinkedList<JournalEntry> entries;
    private int nextTransactionId;

    public JournalManager() {
        entries = new SinglyLinkedList<>();
        nextTransactionId = 1;
    }

    public JournalEntry beginCreate(
            String parentPath,
            String fileName,
            String owner,
            com.mycompany.proyectoso_2.filesystem.EntryVisibility visibility,
            int sizeInBlocks
    ) {
        JournalFileSnapshot afterImage = new JournalFileSnapshot(
                parentPath,
                fileName,
                owner,
                visibility,
                sizeInBlocks,
                -1,
                -1
        );
        JournalEntry entry = createEntry(OperationType.CREATE, afterImage.getFullPath());
        entry.setAfterImage(afterImage);
        return entry;
    }

    public JournalEntry beginDelete(FileNode file, int[] affectedBlocks) {
        JournalEntry entry = createEntry(OperationType.DELETE, file.getPath());
        entry.setBeforeImage(JournalFileSnapshot.fromFileNode(file));
        entry.setAffectedBlocks(affectedBlocks);
        return entry;
    }

    public void recordCreateResult(JournalEntry entry, FileNode file, int[] affectedBlocks) {
        validateEntry(entry, OperationType.CREATE);
        entry.setAfterImage(JournalFileSnapshot.fromFileNode(file));
        entry.setAffectedBlocks(affectedBlocks);
    }

    public void markCommitted(JournalEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("La entrada del journal no puede ser nula.");
        }
        entry.setStatus(JournalStatus.COMMITTED);
    }

    public void recoverPending(FileSystemTree fileSystemTree, ChainedAllocationManager allocationManager) {
        if (fileSystemTree == null) {
            throw new IllegalArgumentException("El arbol del sistema de archivos es obligatorio.");
        }
        if (allocationManager == null) {
            throw new IllegalArgumentException("El manejador de asignacion es obligatorio.");
        }

        for (int index = 0; index < entries.size(); index++) {
            JournalEntry entry = entries.get(index);
            if (entry.getStatus() != JournalStatus.PENDING) {
                continue;
            }
            if (entry.getOperationType() == OperationType.CREATE) {
                undoPendingCreate(entry, fileSystemTree, allocationManager);
            } else if (entry.getOperationType() == OperationType.DELETE) {
                undoPendingDelete(entry, fileSystemTree, allocationManager);
            }
            entry.setStatus(JournalStatus.UNDONE);
        }
    }

    public SinglyLinkedList<JournalEntry> getEntries() {
        return entries;
    }

    private JournalEntry createEntry(OperationType operationType, String targetPath) {
        JournalEntry entry = new JournalEntry(nextTransactionId, operationType, targetPath);
        nextTransactionId++;
        entries.addLast(entry);
        return entry;
    }

    private void undoPendingCreate(
            JournalEntry entry,
            FileSystemTree fileSystemTree,
            ChainedAllocationManager allocationManager
    ) {
        releaseBlocks(entry.getAffectedBlocks(), allocationManager);
        FSNode existingNode = fileSystemTree.findNode(entry.getTargetPath());
        if (existingNode != null) {
            fileSystemTree.removeNode(entry.getTargetPath());
        }
    }

    private void undoPendingDelete(
            JournalEntry entry,
            FileSystemTree fileSystemTree,
            ChainedAllocationManager allocationManager
    ) {
        JournalFileSnapshot beforeImage = entry.getBeforeImage();
        if (beforeImage == null) {
            return;
        }
        if (fileSystemTree.findNode(beforeImage.getFullPath()) != null) {
            return;
        }

        FileNode restoredFile = fileSystemTree.createFile(
                beforeImage.getParentPath(),
                beforeImage.getName(),
                beforeImage.getOwner(),
                beforeImage.getVisibility(),
                beforeImage.getSizeInBlocks()
        );
        restoreBlocks(restoredFile, entry.getAffectedBlocks(), beforeImage.getColorId(), allocationManager);
    }

    private void restoreBlocks(
            FileNode file,
            int[] affectedBlocks,
            int colorId,
            ChainedAllocationManager allocationManager
    ) {
        if (affectedBlocks.length == 0) {
            file.setFirstBlockIndex(-1);
            file.setColorId(-1);
            return;
        }

        for (int index = 0; index < affectedBlocks.length; index++) {
            int currentBlock = affectedBlocks[index];
            int nextBlock = index == affectedBlocks.length - 1 ? -1 : affectedBlocks[index + 1];
            allocationManager.getDisk().occupyBlock(currentBlock, file.getPath(), nextBlock, colorId);
        }

        file.setFirstBlockIndex(affectedBlocks[0]);
        file.setColorId(colorId);
    }

    private void releaseBlocks(
            int[] affectedBlocks,
            ChainedAllocationManager allocationManager
    ) {
        for (int index = 0; index < affectedBlocks.length; index++) {
            allocationManager.getDisk().releaseBlock(affectedBlocks[index]);
        }
    }

    private void validateEntry(JournalEntry entry, OperationType expectedType) {
        if (entry == null) {
            throw new IllegalArgumentException("La entrada del journal no puede ser nula.");
        }
        if (entry.getOperationType() != expectedType) {
            throw new IllegalArgumentException("La entrada del journal no corresponde a " + expectedType + ".");
        }
    }
}
