package com.mycompany.proyectoso_2.simulation;

import com.mycompany.proyectoso_2.locks.LockRequest;
import com.mycompany.proyectoso_2.process.ProcessControlBlock;

final class SimulationTask {

    private final ProcessControlBlock process;
    private final LockRequest lockRequest;
    private final Runnable action;

    SimulationTask(ProcessControlBlock process, LockRequest lockRequest, Runnable action) {
        if (process == null) {
            throw new IllegalArgumentException("El proceso de simulacion es obligatorio.");
        }
        if (lockRequest == null) {
            throw new IllegalArgumentException("La solicitud de lock es obligatoria.");
        }
        if (action == null) {
            throw new IllegalArgumentException("La accion de simulacion es obligatoria.");
        }
        this.process = process;
        this.lockRequest = lockRequest;
        this.action = action;
    }

    ProcessControlBlock getProcess() {
        return process;
    }

    LockRequest getLockRequest() {
        return lockRequest;
    }

    Runnable getAction() {
        return action;
    }
}
