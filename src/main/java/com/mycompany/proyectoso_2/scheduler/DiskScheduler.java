package com.mycompany.proyectoso_2.scheduler;

import com.mycompany.proyectoso_2.model.SchedulingPolicy;
import com.mycompany.proyectoso_2.process.ProcessControlBlock;

public class DiskScheduler {

    private final DiskSchedulingStrategy fifoStrategy;
    private final DiskSchedulingStrategy sstfStrategy;
    private final DiskSchedulingStrategy scanStrategy;
    private final DiskSchedulingStrategy cScanStrategy;

    public DiskScheduler() {
        fifoStrategy = new FifoSchedulingStrategy();
        sstfStrategy = new SstfSchedulingStrategy();
        scanStrategy = new ScanSchedulingStrategy();
        cScanStrategy = new CScanSchedulingStrategy();
    }

    public ProcessControlBlock[] order(
            ProcessControlBlock[] pendingProcesses,
            SchedulingPolicy policy,
            int currentHead,
            HeadDirection direction
    ) {
        if (policy == null) {
            throw new IllegalArgumentException("La politica del scheduler es obligatoria.");
        }
        if (direction == null) {
            throw new IllegalArgumentException("La direccion del cabezal es obligatoria.");
        }

        return switch (policy) {
            case FIFO -> fifoStrategy.order(pendingProcesses, currentHead, direction);
            case SSTF -> sstfStrategy.order(pendingProcesses, currentHead, direction);
            case SCAN -> scanStrategy.order(pendingProcesses, currentHead, direction);
            case C_SCAN -> cScanStrategy.order(pendingProcesses, currentHead, direction);
        };
    }
}
