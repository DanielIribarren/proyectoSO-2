package com.mycompany.proyectoso_2.locks;

import com.mycompany.proyectoso_2.process.ProcessControlBlock;

public class LockGrant {

    private final ProcessControlBlock process;
    private final LockType lockType;

    public LockGrant(ProcessControlBlock process, LockType lockType) {
        if (process == null) {
            throw new IllegalArgumentException("El proceso con lock activo es obligatorio.");
        }
        if (lockType == null) {
            throw new IllegalArgumentException("El tipo de lock activo es obligatorio.");
        }
        this.process = process;
        this.lockType = lockType;
    }

    public ProcessControlBlock getProcess() {
        return process;
    }

    public LockType getLockType() {
        return lockType;
    }

    public int getPid() {
        return process.getPid();
    }
}
