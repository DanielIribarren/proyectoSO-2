package com.mycompany.proyectoso_2.scheduler;

import com.mycompany.proyectoso_2.process.ProcessControlBlock;

public class ScanSchedulingStrategy implements DiskSchedulingStrategy {

    @Override
    public ProcessControlBlock[] order(
            ProcessControlBlock[] pendingProcesses,
            int currentHead,
            HeadDirection direction
    ) {
        ProcessControlBlock[] ascending = SchedulerArraySupport.sortByRequestedPosition(
                pendingProcesses,
                true
        );
        ProcessControlBlock[] ordered = new ProcessControlBlock[ascending.length];
        int orderedIndex = 0;

        if (direction == HeadDirection.DOWN) {
            orderedIndex = appendDownwardSweep(ascending, ordered, orderedIndex, currentHead);
            appendUpwardReturn(ascending, ordered, orderedIndex, currentHead);
            return ordered;
        }

        orderedIndex = appendUpwardSweep(ascending, ordered, orderedIndex, currentHead);
        appendDownwardReturn(ascending, ordered, orderedIndex, currentHead);
        return ordered;
    }

    private int appendUpwardSweep(
            ProcessControlBlock[] ascending,
            ProcessControlBlock[] ordered,
            int orderedIndex,
            int currentHead
    ) {
        for (int index = 0; index < ascending.length; index++) {
            if (ascending[index].getRequestedPosition() >= currentHead) {
                ordered[orderedIndex] = ascending[index];
                orderedIndex++;
            }
        }
        return orderedIndex;
    }

    private int appendDownwardReturn(
            ProcessControlBlock[] ascending,
            ProcessControlBlock[] ordered,
            int orderedIndex,
            int currentHead
    ) {
        for (int index = ascending.length - 1; index >= 0; index--) {
            if (ascending[index].getRequestedPosition() < currentHead) {
                ordered[orderedIndex] = ascending[index];
                orderedIndex++;
            }
        }
        return orderedIndex;
    }

    private int appendDownwardSweep(
            ProcessControlBlock[] ascending,
            ProcessControlBlock[] ordered,
            int orderedIndex,
            int currentHead
    ) {
        for (int index = ascending.length - 1; index >= 0; index--) {
            if (ascending[index].getRequestedPosition() <= currentHead) {
                ordered[orderedIndex] = ascending[index];
                orderedIndex++;
            }
        }
        return orderedIndex;
    }

    private int appendUpwardReturn(
            ProcessControlBlock[] ascending,
            ProcessControlBlock[] ordered,
            int orderedIndex,
            int currentHead
    ) {
        for (int index = 0; index < ascending.length; index++) {
            if (ascending[index].getRequestedPosition() > currentHead) {
                ordered[orderedIndex] = ascending[index];
                orderedIndex++;
            }
        }
        return orderedIndex;
    }
}
