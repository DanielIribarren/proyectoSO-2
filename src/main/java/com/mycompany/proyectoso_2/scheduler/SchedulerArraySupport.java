package com.mycompany.proyectoso_2.scheduler;

import com.mycompany.proyectoso_2.process.ProcessControlBlock;

final class SchedulerArraySupport {

    private SchedulerArraySupport() {
    }

    static ProcessControlBlock[] copyProcesses(ProcessControlBlock[] source) {
        validateProcesses(source);
        ProcessControlBlock[] copy = new ProcessControlBlock[source.length];
        for (int index = 0; index < source.length; index++) {
            copy[index] = source[index];
        }
        return copy;
    }

    static ProcessControlBlock[] sortByRequestedPosition(
            ProcessControlBlock[] source,
            boolean ascending
    ) {
        ProcessControlBlock[] copy = copyProcesses(source);
        for (int current = 1; current < copy.length; current++) {
            ProcessControlBlock key = copy[current];
            int previous = current - 1;
            while (previous >= 0 && shouldMove(copy[previous], key, ascending)) {
                copy[previous + 1] = copy[previous];
                previous--;
            }
            copy[previous + 1] = key;
        }
        return copy;
    }

    private static boolean shouldMove(
            ProcessControlBlock left,
            ProcessControlBlock right,
            boolean ascending
    ) {
        if (ascending) {
            return left.getRequestedPosition() > right.getRequestedPosition();
        }
        return left.getRequestedPosition() < right.getRequestedPosition();
    }

    private static void validateProcesses(ProcessControlBlock[] processes) {
        if (processes == null) {
            throw new IllegalArgumentException("La cola de procesos no puede ser nula.");
        }
        for (int index = 0; index < processes.length; index++) {
            if (processes[index] == null) {
                throw new IllegalArgumentException("La cola contiene un proceso nulo en " + index + ".");
            }
        }
    }
}
