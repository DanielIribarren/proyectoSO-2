package com.mycompany.proyectoso_2.scheduler;

import com.mycompany.proyectoso_2.process.ProcessControlBlock;

public interface DiskSchedulingStrategy {

    ProcessControlBlock[] order(
            ProcessControlBlock[] pendingProcesses,
            int currentHead,
            HeadDirection direction
    );
}
