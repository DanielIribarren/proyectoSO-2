package com.mycompany.proyectoso_2.locks;

import com.mycompany.proyectoso_2.process.ProcessControlBlock;
import com.mycompany.proyectoso_2.process.ProcessState;
import com.mycompany.proyectoso_2.structures.SinglyLinkedList;

public class LockManager {

    private final SinglyLinkedList<LockResourceState> resources;

    public LockManager() {
        resources = new SinglyLinkedList<>();
    }

    public boolean acquireLock(LockRequest request) {
        validateRequest(request);

        LockResourceState resourceState = getOrCreateResource(request.getTargetPath());
        if (resourceState.getWaitingRequests().isEmpty()
                && isCompatible(resourceState, request.getLockType())) {
            grantLock(resourceState, request);
            return true;
        }

        request.getProcess().setState(ProcessState.BLOCKED);
        resourceState.getWaitingRequests().addLast(request);
        return false;
    }

    public SinglyLinkedList<ProcessControlBlock> releaseLocksByProcess(int pid) {
        if (pid <= 0) {
            throw new IllegalArgumentException("El PID debe ser mayor a cero.");
        }

        SinglyLinkedList<ProcessControlBlock> awakenedProcesses = new SinglyLinkedList<>();
        for (int resourceIndex = 0; resourceIndex < resources.size(); resourceIndex++) {
            LockResourceState resourceState = resources.get(resourceIndex);
            boolean releasedAnyLock = removeGrantedLocks(resourceState, pid);
            if (releasedAnyLock) {
                promoteWaitingRequests(resourceState, awakenedProcesses);
            }
        }
        return awakenedProcesses;
    }

    public int countActiveLocks(String targetPath) {
        LockResourceState resourceState = findResource(targetPath);
        if (resourceState == null) {
            return 0;
        }
        return resourceState.getActiveGrants().size();
    }

    public int countWaitingLocks(String targetPath) {
        LockResourceState resourceState = findResource(targetPath);
        if (resourceState == null) {
            return 0;
        }
        return resourceState.getWaitingRequests().size();
    }

    public String describeActiveLocks() {
        StringBuilder description = new StringBuilder();
        for (int resourceIndex = 0; resourceIndex < resources.size(); resourceIndex++) {
            LockResourceState resourceState = resources.get(resourceIndex);
            if (resourceState.getActiveGrants().isEmpty()) {
                continue;
            }
            if (!description.isEmpty()) {
                description.append('\n');
            }
            description.append(resourceState.getTargetPath()).append(" -> ");
            appendGrantList(description, resourceState.getActiveGrants());
        }
        return description.toString();
    }

    public void clear() {
        resources.clear();
    }

    private void appendGrantList(
            StringBuilder description,
            SinglyLinkedList<LockGrant> grants
    ) {
        for (int grantIndex = 0; grantIndex < grants.size(); grantIndex++) {
            LockGrant grant = grants.get(grantIndex);
            if (grantIndex > 0) {
                description.append(", ");
            }
            description.append(grant.getLockType())
                    .append(" por PID ")
                    .append(grant.getPid());
        }
    }

    private void promoteWaitingRequests(
            LockResourceState resourceState,
            SinglyLinkedList<ProcessControlBlock> awakenedProcesses
    ) {
        while (!resourceState.getWaitingRequests().isEmpty()) {
            LockRequest nextRequest = resourceState.getWaitingRequests().getFirst();
            if (!isCompatible(resourceState, nextRequest.getLockType())) {
                break;
            }

            resourceState.getWaitingRequests().removeFirst();
            grantLock(resourceState, nextRequest);
            awakenedProcesses.addLast(nextRequest.getProcess());

            if (nextRequest.getLockType() == LockType.EXCLUSIVE) {
                break;
            }
        }
    }

    private void grantLock(LockResourceState resourceState, LockRequest request) {
        resourceState.getActiveGrants().addLast(new LockGrant(
                request.getProcess(),
                request.getLockType()
        ));
        request.getProcess().setState(ProcessState.READY);
    }

    private boolean removeGrantedLocks(LockResourceState resourceState, int pid) {
        boolean removedAnyLock = false;
        int grantIndex = 0;
        while (grantIndex < resourceState.getActiveGrants().size()) {
            LockGrant currentGrant = resourceState.getActiveGrants().get(grantIndex);
            if (currentGrant.getPid() == pid) {
                resourceState.getActiveGrants().removeAt(grantIndex);
                removedAnyLock = true;
                continue;
            }
            grantIndex++;
        }
        return removedAnyLock;
    }

    private boolean isCompatible(LockResourceState resourceState, LockType requestedType) {
        if (requestedType == LockType.EXCLUSIVE) {
            return resourceState.getActiveGrants().isEmpty();
        }
        return !hasExclusiveGrant(resourceState);
    }

    private boolean hasExclusiveGrant(LockResourceState resourceState) {
        for (int grantIndex = 0; grantIndex < resourceState.getActiveGrants().size(); grantIndex++) {
            LockGrant currentGrant = resourceState.getActiveGrants().get(grantIndex);
            if (currentGrant.getLockType() == LockType.EXCLUSIVE) {
                return true;
            }
        }
        return false;
    }

    private LockResourceState getOrCreateResource(String targetPath) {
        LockResourceState resourceState = findResource(targetPath);
        if (resourceState != null) {
            return resourceState;
        }

        LockResourceState newResourceState = new LockResourceState(targetPath);
        resources.addLast(newResourceState);
        return newResourceState;
    }

    private LockResourceState findResource(String targetPath) {
        if (targetPath == null || targetPath.isBlank()) {
            throw new IllegalArgumentException("La ruta del recurso es obligatoria.");
        }
        for (int resourceIndex = 0; resourceIndex < resources.size(); resourceIndex++) {
            LockResourceState resourceState = resources.get(resourceIndex);
            if (resourceState.getTargetPath().equals(targetPath)) {
                return resourceState;
            }
        }
        return null;
    }

    private void validateRequest(LockRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de lock no puede ser nula.");
        }
    }
}
