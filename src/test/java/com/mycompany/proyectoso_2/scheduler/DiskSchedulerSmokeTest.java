package com.mycompany.proyectoso_2.scheduler;

import com.mycompany.proyectoso_2.model.SchedulingPolicy;
import com.mycompany.proyectoso_2.process.IORequest;
import com.mycompany.proyectoso_2.process.OperationType;
import com.mycompany.proyectoso_2.process.ProcessControlBlock;

public final class DiskSchedulerSmokeTest {

    private static final int INITIAL_HEAD = 50;
    private static final int[] REQUESTS = {95, 180, 34, 119, 11, 123, 62, 64};

    private DiskSchedulerSmokeTest() {
    }

    public static void main(String[] args) {
        DiskScheduler scheduler = new DiskScheduler();

        assertOrder(
                new int[]{95, 180, 34, 119, 11, 123, 62, 64},
                scheduler.order(buildProcesses(), SchedulingPolicy.FIFO, INITIAL_HEAD, HeadDirection.UP),
                "FIFO debe respetar el orden de llegada."
        );
        assertOrder(
                new int[]{62, 64, 34, 11, 95, 119, 123, 180},
                scheduler.order(buildProcesses(), SchedulingPolicy.SSTF, INITIAL_HEAD, HeadDirection.UP),
                "SSTF debe atender la solicitud mas cercana."
        );
        assertOrder(
                new int[]{62, 64, 95, 119, 123, 180, 34, 11},
                scheduler.order(buildProcesses(), SchedulingPolicy.SCAN, INITIAL_HEAD, HeadDirection.UP),
                "SCAN debe barrer hacia arriba y luego regresar."
        );
        assertOrder(
                new int[]{62, 64, 95, 119, 123, 180, 11, 34},
                scheduler.order(buildProcesses(), SchedulingPolicy.C_SCAN, INITIAL_HEAD, HeadDirection.UP),
                "C-SCAN debe reiniciar al inicio al terminar el barrido."
        );

        System.out.println("OK: pruebas basicas del scheduler completadas.");
    }

    private static ProcessControlBlock[] buildProcesses() {
        ProcessControlBlock[] processes = new ProcessControlBlock[REQUESTS.length];
        for (int index = 0; index < REQUESTS.length; index++) {
            IORequest request = new IORequest(
                    OperationType.READ,
                    "/tests/request-" + REQUESTS[index],
                    REQUESTS[index]
            );
            processes[index] = new ProcessControlBlock(index + 1, request);
        }
        return processes;
    }

    private static void assertOrder(
            int[] expectedPositions,
            ProcessControlBlock[] orderedProcesses,
            String message
    ) {
        if (expectedPositions.length != orderedProcesses.length) {
            throw new IllegalStateException(message
                    + " Longitud esperada: " + expectedPositions.length
                    + ", recibida: " + orderedProcesses.length + ".");
        }
        for (int index = 0; index < expectedPositions.length; index++) {
            int actualPosition = orderedProcesses[index].getRequestedPosition();
            if (expectedPositions[index] != actualPosition) {
                throw new IllegalStateException(message
                        + " Diferencia en posicion " + index
                        + ": esperado " + expectedPositions[index]
                        + ", recibido " + actualPosition + ".");
            }
        }
    }
}
