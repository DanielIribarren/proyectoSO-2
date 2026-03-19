package com.mycompany.proyectoso_2.process;

public class IORequest {

    private final OperationType operationType;
    private final String targetPath;
    private final int requestedPosition;

    public IORequest(OperationType operationType, String targetPath, int requestedPosition) {
        if (operationType == null) {
            throw new IllegalArgumentException("La operacion del proceso es obligatoria.");
        }
        if (targetPath == null || targetPath.isBlank()) {
            throw new IllegalArgumentException("La ruta objetivo es obligatoria.");
        }
        if (requestedPosition < 0) {
            throw new IllegalArgumentException("La posicion solicitada no puede ser negativa.");
        }
        this.operationType = operationType;
        this.targetPath = targetPath;
        this.requestedPosition = requestedPosition;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public int getRequestedPosition() {
        return requestedPosition;
    }
}
