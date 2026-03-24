package com.mycompany.proyectoso_2.testcase;

import com.mycompany.proyectoso_2.process.OperationType;

public class SchedulerTestRequest {

    private final int position;
    private final OperationType operationType;

    public SchedulerTestRequest(int position, OperationType operationType) {
        if (position < 0) {
            throw new IllegalArgumentException("La posicion de prueba no puede ser negativa.");
        }
        if (operationType == null) {
            throw new IllegalArgumentException("La operacion de prueba es obligatoria.");
        }
        this.position = position;
        this.operationType = operationType;
    }

    public int getPosition() {
        return position;
    }

    public OperationType getOperationType() {
        return operationType;
    }
}
