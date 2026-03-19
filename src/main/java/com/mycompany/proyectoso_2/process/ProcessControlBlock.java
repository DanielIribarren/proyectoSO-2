package com.mycompany.proyectoso_2.process;

public class ProcessControlBlock {

    private final int pid;
    private final IORequest request;
    private ProcessState state;

    public ProcessControlBlock(int pid, IORequest request) {
        if (pid <= 0) {
            throw new IllegalArgumentException("El PID debe ser mayor a cero.");
        }
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de E/S es obligatoria.");
        }
        this.pid = pid;
        this.request = request;
        state = ProcessState.NEW;
    }

    public int getPid() {
        return pid;
    }

    public IORequest getRequest() {
        return request;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        if (state == null) {
            throw new IllegalArgumentException("El estado del proceso es obligatorio.");
        }
        this.state = state;
    }

    public OperationType getOperationType() {
        return request.getOperationType();
    }

    public String getTargetPath() {
        return request.getTargetPath();
    }

    public int getRequestedPosition() {
        return request.getRequestedPosition();
    }
}
