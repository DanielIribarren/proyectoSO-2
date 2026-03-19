package com.mycompany.proyectoso_2.journal;

import com.mycompany.proyectoso_2.process.OperationType;

public class JournalEntry {

    private final int transactionId;
    private final OperationType operationType;
    private final String targetPath;
    private JournalStatus status;
    private JournalFileSnapshot beforeImage;
    private JournalFileSnapshot afterImage;
    private int[] affectedBlocks;

    public JournalEntry(int transactionId, OperationType operationType, String targetPath) {
        if (transactionId <= 0) {
            throw new IllegalArgumentException("El transactionId debe ser mayor a cero.");
        }
        if (operationType == null) {
            throw new IllegalArgumentException("La operacion del journal es obligatoria.");
        }
        if (targetPath == null || targetPath.isBlank()) {
            throw new IllegalArgumentException("La ruta objetivo del journal es obligatoria.");
        }
        this.transactionId = transactionId;
        this.operationType = operationType;
        this.targetPath = targetPath;
        status = JournalStatus.PENDING;
        affectedBlocks = new int[0];
    }

    public int getTransactionId() {
        return transactionId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public JournalStatus getStatus() {
        return status;
    }

    public void setStatus(JournalStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado del journal es obligatorio.");
        }
        this.status = status;
    }

    public JournalFileSnapshot getBeforeImage() {
        return beforeImage;
    }

    public void setBeforeImage(JournalFileSnapshot beforeImage) {
        this.beforeImage = beforeImage;
    }

    public JournalFileSnapshot getAfterImage() {
        return afterImage;
    }

    public void setAfterImage(JournalFileSnapshot afterImage) {
        this.afterImage = afterImage;
    }

    public int[] getAffectedBlocks() {
        return copyBlocks(affectedBlocks);
    }

    public void setAffectedBlocks(int[] affectedBlocks) {
        this.affectedBlocks = copyBlocks(affectedBlocks);
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
