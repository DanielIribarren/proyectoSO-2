package com.mycompany.proyectoso_2.scheduler;

import com.mycompany.proyectoso_2.process.ProcessControlBlock;

public class CScanSchedulingStrategy implements DiskSchedulingStrategy {

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
            appendDownwardWrap(ascending, ordered, orderedIndex, currentHead);
            return ordered;
        }

        orderedIndex = appendUpwardSweep(ascending, ordered, orderedIndex, currentHead);
        appendUpwardWrap(ascending, ordered, orderedIndex, currentHead);
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

    private int appendUpwardWrap(
            ProcessControlBlock[] ascending,
            ProcessControlBlock[] ordered,
            int orderedIndex,
            int currentHead
    ) {
        for (int index = 0; index < ascending.length; index++) {
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

    private int appendDownwardWrap(
            ProcessControlBlock[] ascending,
            ProcessControlBlock[] ordered,
            int orderedIndex,
            int currentHead
    ) {
        for (int index = ascending.length - 1; index >= 0; index--) {
            if (ascending[index].getRequestedPosition() > currentHead) {
                ordered[orderedIndex] = ascending[index];
                orderedIndex++;
            }
        }
        return orderedIndex;
    }
}
