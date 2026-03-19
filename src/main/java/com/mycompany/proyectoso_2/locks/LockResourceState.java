package com.mycompany.proyectoso_2.locks;

import com.mycompany.proyectoso_2.structures.SinglyLinkedList;

public class LockResourceState {

    private final String targetPath;
    private final SinglyLinkedList<LockGrant> activeGrants;
    private final SinglyLinkedList<LockRequest> waitingRequests;

    public LockResourceState(String targetPath) {
        if (targetPath == null || targetPath.isBlank()) {
            throw new IllegalArgumentException("La ruta del recurso es obligatoria.");
        }
        this.targetPath = targetPath;
        activeGrants = new SinglyLinkedList<>();
        waitingRequests = new SinglyLinkedList<>();
    }

    public String getTargetPath() {
        return targetPath;
    }

    public SinglyLinkedList<LockGrant> getActiveGrants() {
        return activeGrants;
    }

    public SinglyLinkedList<LockRequest> getWaitingRequests() {
        return waitingRequests;
    }
}
