package com.mycompany.proyectoso_2.scheduler;

import com.mycompany.proyectoso_2.process.ProcessControlBlock;

public class SstfSchedulingStrategy implements DiskSchedulingStrategy {

    @Override
    public ProcessControlBlock[] order(
            ProcessControlBlock[] pendingProcesses,
            int currentHead,
            HeadDirection direction
    ) {
        ProcessControlBlock[] source = SchedulerArraySupport.copyProcesses(pendingProcesses);
        ProcessControlBlock[] ordered = new ProcessControlBlock[source.length];
        boolean[] used = new boolean[source.length];
        int headPosition = currentHead;

        for (int orderedIndex = 0; orderedIndex < ordered.length; orderedIndex++) {
            int bestIndex = findClosestIndex(source, used, headPosition);
            ordered[orderedIndex] = source[bestIndex];
            used[bestIndex] = true;
            headPosition = source[bestIndex].getRequestedPosition();
        }

        return ordered;
    }

    private int findClosestIndex(
            ProcessControlBlock[] processes,
            boolean[] used,
            int headPosition
    ) {
        int bestIndex = -1;
        int bestDistance = Integer.MAX_VALUE;

        for (int index = 0; index < processes.length; index++) {
            if (used[index]) {
                continue;
            }
            int distance = Math.abs(processes[index].getRequestedPosition() - headPosition);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = index;
            }
        }

        return bestIndex;
    }
}
