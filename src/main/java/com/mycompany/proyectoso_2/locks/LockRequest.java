package com.mycompany.proyectoso_2.locks;

import com.mycompany.proyectoso_2.process.ProcessControlBlock;

public class LockRequest {

    private final String targetPath;
    private final ProcessControlBlock process;
    private final LockType lockType;

    public LockRequest(String targetPath, ProcessControlBlock process, LockType lockType) {
        if (targetPath == null || targetPath.isBlank()) {
            throw new IllegalArgumentException("La ruta del recurso es obligatoria.");
        }
        if (process == null) {
            throw new IllegalArgumentException("El proceso solicitante es obligatorio.");
        }
        if (lockType == null) {
            throw new IllegalArgumentException("El tipo de lock es obligatorio.");
        }
        this.targetPath = targetPath;
        this.process = process;
        this.lockType = lockType;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public ProcessControlBlock getProcess() {
        return process;
    }

    public LockType getLockType() {
        return lockType;
    }
}
