package com.mycompany.proyectoso_2.scheduler;

import com.mycompany.proyectoso_2.process.ProcessControlBlock;

public class FifoSchedulingStrategy implements DiskSchedulingStrategy {

    @Override
    public ProcessControlBlock[] order(
            ProcessControlBlock[] pendingProcesses,
            int currentHead,
            HeadDirection direction
    ) {
        return SchedulerArraySupport.copyProcesses(pendingProcesses);
    }
}
