package com.mycompany.proyectoso_2.simulation;

import com.mycompany.proyectoso_2.disk.ChainedAllocationManager;
import com.mycompany.proyectoso_2.disk.SimulatedDisk;
import com.mycompany.proyectoso_2.filesystem.DirectoryNode;
import com.mycompany.proyectoso_2.filesystem.EntryVisibility;
import com.mycompany.proyectoso_2.filesystem.FSNode;
import com.mycompany.proyectoso_2.filesystem.FSNodeType;
import com.mycompany.proyectoso_2.filesystem.FileNode;
import com.mycompany.proyectoso_2.filesystem.FileSystemTree;
import com.mycompany.proyectoso_2.journal.JournalEntry;
import com.mycompany.proyectoso_2.journal.JournalManager;
import com.mycompany.proyectoso_2.locks.LockManager;
import com.mycompany.proyectoso_2.locks.LockRequest;
import com.mycompany.proyectoso_2.locks.LockType;
import com.mycompany.proyectoso_2.model.SchedulingPolicy;
import com.mycompany.proyectoso_2.model.UserMode;
import com.mycompany.proyectoso_2.process.IORequest;
import com.mycompany.proyectoso_2.process.OperationType;
import com.mycompany.proyectoso_2.process.ProcessControlBlock;
import com.mycompany.proyectoso_2.process.ProcessState;
import com.mycompany.proyectoso_2.scheduler.DiskScheduler;
import com.mycompany.proyectoso_2.scheduler.HeadDirection;
import com.mycompany.proyectoso_2.structures.SinglyLinkedList;

public class SimulationController {

    private static final int TOTAL_BLOCKS = 64;

    private final FileSystemTree fileSystemTree;
    private final SimulatedDisk disk;
    private final ChainedAllocationManager allocationManager;
    private final DiskScheduler diskScheduler;
    private final LockManager lockManager;
    private final JournalManager journalManager;
    private final SinglyLinkedList<SimulationTask> pendingTasks;
    private final SinglyLinkedList<ProcessControlBlock> processHistory;
    private final SinglyLinkedList<String> eventLog;

    private UserMode currentMode;
    private SchedulingPolicy schedulingPolicy;
    private HeadDirection headDirection;
    private int currentHeadPosition;
    private int nextPid;
    private String currentUser;

    public SimulationController() {
        fileSystemTree = new FileSystemTree();
        disk = new SimulatedDisk(TOTAL_BLOCKS);
        allocationManager = new ChainedAllocationManager(disk);
        diskScheduler = new DiskScheduler();
        lockManager = new LockManager();
        journalManager = new JournalManager();
        pendingTasks = new SinglyLinkedList<>();
        processHistory = new SinglyLinkedList<>();
        eventLog = new SinglyLinkedList<>();
        currentMode = UserMode.ADMINISTRADOR;
        schedulingPolicy = SchedulingPolicy.FIFO;
        headDirection = HeadDirection.UP;
        currentHeadPosition = 12;
        nextPid = 1;
        currentUser = "daniel";
        seedInitialState();
    }

    public FileSystemTree getFileSystemTree() {
        return fileSystemTree;
    }

    public SimulatedDisk getDisk() {
        return disk;
    }

    public UserMode getCurrentMode() {
        return currentMode;
    }

    public SchedulingPolicy getSchedulingPolicy() {
        return schedulingPolicy;
    }

    public int getCurrentHeadPosition() {
        return currentHeadPosition;
    }

    public void setCurrentMode(UserMode currentMode) {
        if (currentMode == null) {
            throw new IllegalArgumentException("El modo de usuario es obligatorio.");
        }
        this.currentMode = currentMode;
        appendEvent("[MODE] Cambio a modo " + currentMode + ".");
    }

    public void setSchedulingPolicy(SchedulingPolicy schedulingPolicy) {
        if (schedulingPolicy == null) {
            throw new IllegalArgumentException("La politica de planificacion es obligatoria.");
        }
        this.schedulingPolicy = schedulingPolicy;
        appendEvent("[SCHED] Politica activa: " + schedulingPolicy + ".");
    }

    public void setCurrentHeadPosition(int currentHeadPosition) {
        if (currentHeadPosition < 0 || currentHeadPosition >= disk.getTotalBlocks()) {
            throw new IllegalArgumentException("La posicion del cabezal esta fuera del disco.");
        }
        this.currentHeadPosition = currentHeadPosition;
        appendEvent("[HEAD] Cabezal ubicado en " + currentHeadPosition + ".");
    }

    public void createDirectory(String parentPath, String name, EntryVisibility visibility) {
        ensureAdministrator("crear directorios");
        String normalizedParentPath = normalizeParentPath(parentPath);
        String targetPath = buildPath(normalizedParentPath, name);
        enqueueTask(
                OperationType.CREATE,
                targetPath,
                currentHeadPosition,
                LockType.EXCLUSIVE,
                () -> {
                    fileSystemTree.createDirectory(
                            normalizedParentPath,
                            name,
                            getCurrentOwner(),
                            visibility
                    );
                    appendEvent("[FS] Directorio creado: " + targetPath + ".");
                }
        );
    }

    public void createFile(String parentPath, String name, int sizeInBlocks, EntryVisibility visibility) {
        ensureAdministrator("crear archivos");
        String normalizedParentPath = normalizeParentPath(parentPath);
        int requestedPosition = findFirstFreeBlockIndex();
        String targetPath = buildPath(normalizedParentPath, name);
        enqueueTask(
                OperationType.CREATE,
                targetPath,
                requestedPosition,
                LockType.EXCLUSIVE,
                () -> {
                    JournalEntry journalEntry = journalManager.beginCreate(
                            normalizedParentPath,
                            name,
                            getCurrentOwner(),
                            visibility,
                            sizeInBlocks
                    );
                    FileNode file = fileSystemTree.createFile(
                            normalizedParentPath,
                            name,
                            getCurrentOwner(),
                            visibility,
                            sizeInBlocks
                    );
                    allocationManager.allocateFile(file);
                    journalManager.recordCreateResult(
                            journalEntry,
                            file,
                            allocationManager.getAllocatedBlocks(file)
                    );
                    journalManager.markCommitted(journalEntry);
                    appendEvent("[FS] Archivo creado: " + targetPath
                            + " (" + sizeInBlocks + " bloques).");
                }
        );
    }

    public void renameNode(String path, String newName) {
        ensureAdministrator("renombrar archivos o directorios");
        FSNode targetNode = requireNode(path);
        String requestedPath = targetNode.getPath();
        enqueueTask(
                OperationType.UPDATE,
                requestedPath,
                resolveRequestedPosition(targetNode),
                LockType.EXCLUSIVE,
                () -> {
                    FSNode renamedNode = fileSystemTree.renameNode(requestedPath, newName);
                    appendEvent("[FS] Nodo renombrado a " + renamedNode.getPath() + ".");
                }
        );
    }

    public void deleteNode(String path) {
        ensureAdministrator("eliminar archivos o directorios");
        FSNode targetNode = requireNode(path);
        enqueueTask(
                OperationType.DELETE,
                targetNode.getPath(),
                resolveRequestedPosition(targetNode),
                LockType.EXCLUSIVE,
                () -> {
                    deleteNodeInternal(targetNode);
                    appendEvent("[FS] Nodo eliminado: " + path + ".");
                }
        );
    }

    public void simulateFailedCreate(String parentPath, String name, int sizeInBlocks) {
        ensureAdministrator("simular fallos");
        String normalizedParentPath = normalizeParentPath(parentPath);
        String targetPath = buildPath(normalizedParentPath, name);
        int requestedPosition = findFirstFreeBlockIndex();
        enqueueTask(
                OperationType.CREATE,
                targetPath,
                requestedPosition,
                LockType.EXCLUSIVE,
                () -> {
                    JournalEntry journalEntry = journalManager.beginCreate(
                            normalizedParentPath,
                            name,
                            getCurrentOwner(),
                            EntryVisibility.PRIVATE,
                            sizeInBlocks
                    );
                    FileNode file = fileSystemTree.createFile(
                            normalizedParentPath,
                            name,
                            getCurrentOwner(),
                            EntryVisibility.PRIVATE,
                            sizeInBlocks
                    );
                    allocationManager.allocateFile(file);
                    journalManager.recordCreateResult(
                            journalEntry,
                            file,
                            allocationManager.getAllocatedBlocks(file)
                    );
                    appendEvent("[FAIL] Falla simulada antes del commit para " + targetPath + ".");
                    journalManager.recoverPending(fileSystemTree, allocationManager);
                    appendEvent("[RECOVERY] Journal aplicado y sistema restaurado.");
                }
        );
    }

    public Object[][] buildAllocationRows() {
        SinglyLinkedList<FileNode> files = new SinglyLinkedList<>();
        collectFiles(fileSystemTree.getRoot(), files);

        Object[][] rows = new Object[files.size()][4];
        for (int index = 0; index < files.size(); index++) {
            FileNode file = files.get(index);
            rows[index][0] = file.getPath();
            rows[index][1] = file.getSizeInBlocks();
            rows[index][2] = file.getFirstBlockIndex();
            rows[index][3] = file.getColorId() < 0 ? "-" : "Color " + file.getColorId();
        }
        return rows;
    }

    public Object[][] buildProcessRows() {
        Object[][] rows = new Object[processHistory.size()][4];
        for (int index = 0; index < processHistory.size(); index++) {
            ProcessControlBlock process = processHistory.get(index);
            rows[index][0] = process.getPid();
            rows[index][1] = process.getOperationType();
            rows[index][2] = process.getTargetPath();
            rows[index][3] = process.getState();
        }
        return rows;
    }

    public String buildLockDescription() {
        String description = lockManager.describeActiveLocks();
        if (description.isBlank()) {
            return "Sin locks activos.";
        }
        return description;
    }

    public String[] buildEventLogLines() {
        return copyStringList(eventLog);
    }

    public String[] buildJournalLines() {
        SinglyLinkedList<JournalEntry> entries = journalManager.getEntries();
        String[] lines = new String[entries.size()];
        for (int index = 0; index < entries.size(); index++) {
            JournalEntry entry = entries.get(index);
            lines[index] = "T" + entry.getTransactionId()
                    + " | " + entry.getOperationType()
                    + " | " + entry.getTargetPath()
                    + " | " + entry.getStatus();
        }
        return lines;
    }

    public int countProcessesByState(ProcessState state) {
        int count = 0;
        for (int index = 0; index < processHistory.size(); index++) {
            if (processHistory.get(index).getState() == state) {
                count++;
            }
        }
        return count;
    }

    private void deleteNodeInternal(FSNode targetNode) {
        if (targetNode.getType() == FSNodeType.FILE) {
            deleteFileWithJournal((FileNode) targetNode);
            return;
        }

        DirectoryNode directory = (DirectoryNode) targetNode;
        while (directory.getChildrenCount() > 0) {
            FSNode child = directory.getChildAt(0);
            deleteNodeInternal(child);
        }
        fileSystemTree.removeNode(directory.getPath());
    }

    private void deleteFileWithJournal(FileNode file) {
        int[] allocatedBlocks = allocationManager.getAllocatedBlocks(file);
        JournalEntry journalEntry = journalManager.beginDelete(file, allocatedBlocks);
        allocationManager.releaseFile(file);
        fileSystemTree.removeNode(file.getPath());
        journalManager.markCommitted(journalEntry);
    }

    private void enqueueTask(
            OperationType operationType,
            String targetPath,
            int requestedPosition,
            LockType lockType,
            Runnable action
    ) {
        int normalizedPosition = normalizeRequestedPosition(requestedPosition);
        ProcessControlBlock process = new ProcessControlBlock(
                nextPid,
                new IORequest(operationType, targetPath, normalizedPosition)
        );
        nextPid++;
        processHistory.addLast(process);
        pendingTasks.addLast(new SimulationTask(
                process,
                new LockRequest(targetPath, process, lockType),
                action
        ));
        appendEvent("[PROC] PID " + process.getPid()
                + " creado para " + operationType + " en " + targetPath + ".");
        processQueue();
    }

    private void processQueue() {
        while (true) {
            promoteNewProcesses();
            ProcessControlBlock[] readyProcesses = buildReadyProcesses();
            if (readyProcesses.length == 0) {
                return;
            }

            ProcessControlBlock[] orderedProcesses = diskScheduler.order(
                    readyProcesses,
                    schedulingPolicy,
                    currentHeadPosition,
                    headDirection
            );
            ProcessControlBlock nextProcess = orderedProcesses[0];
            SimulationTask task = findTaskByPid(nextProcess.getPid());
            if (task == null) {
                throw new IllegalStateException("No se encontro la tarea del PID " + nextProcess.getPid() + ".");
            }

            if (!lockManager.acquireLock(task.getLockRequest())) {
                appendEvent("[LOCK] PID " + nextProcess.getPid()
                        + " bloqueado en " + nextProcess.getTargetPath() + ".");
                continue;
            }

            nextProcess.setState(ProcessState.RUNNING);
            currentHeadPosition = nextProcess.getRequestedPosition();
            appendEvent("[SCHED] Ejecutando PID " + nextProcess.getPid()
                    + " con " + schedulingPolicy
                    + " en bloque " + currentHeadPosition + ".");

            try {
                task.getAction().run();
                nextProcess.setState(ProcessState.TERMINATED);
                appendEvent("[PROC] PID " + nextProcess.getPid() + " finalizado.");
            } catch (RuntimeException exception) {
                nextProcess.setState(ProcessState.TERMINATED);
                appendEvent("[ERROR] PID " + nextProcess.getPid() + ": " + exception.getMessage());
                throw exception;
            } finally {
                SinglyLinkedList<ProcessControlBlock> awakened = lockManager.releaseLocksByProcess(
                        nextProcess.getPid()
                );
                removePendingTask(nextProcess.getPid());
                logAwakenedProcesses(awakened);
            }
        }
    }

    private void promoteNewProcesses() {
        for (int index = 0; index < pendingTasks.size(); index++) {
            ProcessControlBlock process = pendingTasks.get(index).getProcess();
            if (process.getState() == ProcessState.NEW) {
                process.setState(ProcessState.READY);
            }
        }
    }

    private ProcessControlBlock[] buildReadyProcesses() {
        int readyCount = 0;
        for (int index = 0; index < pendingTasks.size(); index++) {
            ProcessState state = pendingTasks.get(index).getProcess().getState();
            if (state == ProcessState.READY) {
                readyCount++;
            }
        }

        ProcessControlBlock[] readyProcesses = new ProcessControlBlock[readyCount];
        int readyIndex = 0;
        for (int index = 0; index < pendingTasks.size(); index++) {
            ProcessControlBlock process = pendingTasks.get(index).getProcess();
            if (process.getState() == ProcessState.READY) {
                readyProcesses[readyIndex] = process;
                readyIndex++;
            }
        }
        return readyProcesses;
    }

    private SimulationTask findTaskByPid(int pid) {
        for (int index = 0; index < pendingTasks.size(); index++) {
            SimulationTask task = pendingTasks.get(index);
            if (task.getProcess().getPid() == pid) {
                return task;
            }
        }
        return null;
    }

    private void removePendingTask(int pid) {
        for (int index = 0; index < pendingTasks.size(); index++) {
            if (pendingTasks.get(index).getProcess().getPid() == pid) {
                pendingTasks.removeAt(index);
                return;
            }
        }
    }

    private void logAwakenedProcesses(SinglyLinkedList<ProcessControlBlock> awakenedProcesses) {
        for (int index = 0; index < awakenedProcesses.size(); index++) {
            ProcessControlBlock awakened = awakenedProcesses.get(index);
            appendEvent("[LOCK] PID " + awakened.getPid() + " vuelve a READY.");
        }
    }

    private int resolveRequestedPosition(FSNode targetNode) {
        if (targetNode.getType() == FSNodeType.FILE) {
            FileNode file = (FileNode) targetNode;
            if (file.getFirstBlockIndex() >= 0) {
                return file.getFirstBlockIndex();
            }
        }
        return currentHeadPosition;
    }

    private int findFirstFreeBlockIndex() {
        for (int blockIndex = 0; blockIndex < disk.getTotalBlocks(); blockIndex++) {
            if (disk.getBlock(blockIndex).isFree()) {
                return blockIndex;
            }
        }
        throw new IllegalStateException("No hay bloques libres disponibles en el disco.");
    }

    private int normalizeRequestedPosition(int requestedPosition) {
        if (requestedPosition < 0) {
            return currentHeadPosition;
        }
        if (requestedPosition >= disk.getTotalBlocks()) {
            return disk.getTotalBlocks() - 1;
        }
        return requestedPosition;
    }

    private void collectFiles(FSNode node, SinglyLinkedList<FileNode> files) {
        if (node.getType() == FSNodeType.FILE) {
            files.addLast((FileNode) node);
            return;
        }
        DirectoryNode directory = (DirectoryNode) node;
        for (int index = 0; index < directory.getChildrenCount(); index++) {
            collectFiles(directory.getChildAt(index), files);
        }
    }

    private FSNode requireNode(String path) {
        FSNode node = fileSystemTree.findNode(path);
        if (node == null) {
            throw new IllegalArgumentException("No existe la ruta: " + path + ".");
        }
        return node;
    }

    private String normalizeParentPath(String parentPath) {
        if (parentPath == null || parentPath.isBlank()) {
            return "/";
        }
        return parentPath;
    }

    private String buildPath(String parentPath, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio.");
        }
        if ("/".equals(parentPath)) {
            return parentPath + name;
        }
        return parentPath + "/" + name;
    }

    private String[] copyStringList(SinglyLinkedList<String> source) {
        String[] copy = new String[source.size()];
        for (int index = 0; index < source.size(); index++) {
            copy[index] = source.get(index);
        }
        return copy;
    }

    private void appendEvent(String event) {
        eventLog.addLast(event);
    }

    private String getCurrentOwner() {
        if (currentMode == UserMode.ADMINISTRADOR) {
            return "admin";
        }
        return currentUser;
    }

    private void ensureAdministrator(String action) {
        if (currentMode != UserMode.ADMINISTRADOR) {
            throw new IllegalStateException("El modo usuario no permite " + action + ".");
        }
    }

    private void seedInitialState() {
        fileSystemTree.createDirectory("/", "system", "system", EntryVisibility.SYSTEM);
        fileSystemTree.createDirectory("/", "users", "system", EntryVisibility.SYSTEM);
        fileSystemTree.createDirectory("/users", "daniel", "daniel", EntryVisibility.PRIVATE);
        createSeedFile("/system", "readme.txt", "system", EntryVisibility.PUBLIC, 1);
        createSeedFile("/system", "config.sys", "system", EntryVisibility.SYSTEM, 2);
        createSeedFile("/users/daniel", "notes.txt", "daniel", EntryVisibility.PRIVATE, 3);
        appendEvent("[BOOT] Sistema inicial cargado.");
        appendEvent("[BOOT] Politica inicial: " + schedulingPolicy + ".");
    }

    private void createSeedFile(
            String parentPath,
            String name,
            String owner,
            EntryVisibility visibility,
            int sizeInBlocks
    ) {
        FileNode file = fileSystemTree.createFile(parentPath, name, owner, visibility, sizeInBlocks);
        allocationManager.allocateFile(file);
    }
}
