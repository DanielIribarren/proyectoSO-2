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
import com.mycompany.proyectoso_2.persistence.SavedDirectory;
import com.mycompany.proyectoso_2.persistence.SavedFile;
import com.mycompany.proyectoso_2.persistence.SimulationSaveData;
import com.mycompany.proyectoso_2.persistence.SimulationStateRepository;
import com.mycompany.proyectoso_2.process.IORequest;
import com.mycompany.proyectoso_2.process.OperationType;
import com.mycompany.proyectoso_2.process.ProcessControlBlock;
import com.mycompany.proyectoso_2.process.ProcessState;
import com.mycompany.proyectoso_2.scheduler.DiskScheduler;
import com.mycompany.proyectoso_2.scheduler.HeadDirection;
import com.mycompany.proyectoso_2.structures.SinglyLinkedList;
import com.mycompany.proyectoso_2.testcase.SchedulerTestCase;
import com.mycompany.proyectoso_2.testcase.SchedulerTestRequest;
import com.mycompany.proyectoso_2.testcase.TestCaseRepository;
import java.io.IOException;
import java.nio.file.Path;

public class SimulationController {

    private static final int TOTAL_BLOCKS = 128;
    private static final int MAX_IO_POSITION = 199;
    private static final long CHECKPOINT_DELAY_MILLIS = 80L;

    private final FileSystemTree fileSystemTree;
    private final SimulatedDisk disk;
    private final ChainedAllocationManager allocationManager;
    private final DiskScheduler diskScheduler;
    private final LockManager lockManager;
    private final JournalManager journalManager;
    private final SimulationStateRepository stateRepository;
    private final TestCaseRepository testCaseRepository;
    private final SinglyLinkedList<SimulationTask> pendingTasks;
    private final SinglyLinkedList<ProcessControlBlock> processHistory;
    private final SinglyLinkedList<String> eventLog;
    private final SinglyLinkedList<Integer> completedRequestPositions;
    private final Object schedulerMonitor;
    private final Thread schedulerWorker;

    private Runnable viewRefreshListener;
    private UserMode currentMode;
    private SchedulingPolicy schedulingPolicy;
    private HeadDirection headDirection;
    private int currentHeadPosition;
    private int nextPid;
    private String currentUser;
    private boolean schedulerStarted;
    private boolean schedulerPaused;
    private boolean shutdownRequested;
    private boolean interruptRequested;
    private SimulationTask currentTask;
    private ProcessControlBlock currentProcess;

    public SimulationController() {
        fileSystemTree = new FileSystemTree();
        disk = new SimulatedDisk(TOTAL_BLOCKS);
        allocationManager = new ChainedAllocationManager(disk);
        diskScheduler = new DiskScheduler();
        lockManager = new LockManager();
        journalManager = new JournalManager();
        stateRepository = new SimulationStateRepository();
        testCaseRepository = new TestCaseRepository();
        pendingTasks = new SinglyLinkedList<>();
        processHistory = new SinglyLinkedList<>();
        eventLog = new SinglyLinkedList<>();
        completedRequestPositions = new SinglyLinkedList<>();
        schedulerMonitor = new Object();
        currentMode = UserMode.ADMINISTRADOR;
        schedulingPolicy = SchedulingPolicy.FIFO;
        headDirection = HeadDirection.UP;
        currentHeadPosition = 12;
        nextPid = 1;
        currentUser = "daniel";
        schedulerStarted = false;
        schedulerPaused = true;
        shutdownRequested = false;
        interruptRequested = false;
        seedInitialStateLocked();
        schedulerWorker = new Thread(this::runSchedulerLoop, "scheduler-worker");
        schedulerWorker.setDaemon(true);
        schedulerWorker.start();
    }

    public void setViewRefreshListener(Runnable viewRefreshListener) {
        synchronized (schedulerMonitor) {
            this.viewRefreshListener = viewRefreshListener;
        }
    }

    public FileSystemTree getFileSystemTree() {
        synchronized (schedulerMonitor) {
            return fileSystemTree;
        }
    }

    public FileSystemTree buildVisibleTreeSnapshot() {
        synchronized (schedulerMonitor) {
            FileSystemTree snapshot = new FileSystemTree();
            snapshot.clear();
            copyVisibleChildrenLocked(fileSystemTree.getRoot(), snapshot.getRoot());
            return snapshot;
        }
    }

    public SimulatedDisk getDisk() {
        synchronized (schedulerMonitor) {
            return disk;
        }
    }

    public UserMode getCurrentMode() {
        synchronized (schedulerMonitor) {
            return currentMode;
        }
    }

    public SchedulingPolicy getSchedulingPolicy() {
        synchronized (schedulerMonitor) {
            return schedulingPolicy;
        }
    }

    public int getCurrentHeadPosition() {
        synchronized (schedulerMonitor) {
            return currentHeadPosition;
        }
    }

    public String getCurrentUser() {
        synchronized (schedulerMonitor) {
            return currentUser;
        }
    }

    public boolean isSchedulerPaused() {
        synchronized (schedulerMonitor) {
            return schedulerPaused;
        }
    }

    public boolean canAdjustHeadPosition() {
        synchronized (schedulerMonitor) {
            return canAdjustHeadPositionLocked();
        }
    }

    public void setCurrentMode(UserMode currentMode) {
        synchronized (schedulerMonitor) {
            if (currentMode == null) {
                throw new IllegalArgumentException("El modo de usuario es obligatorio.");
            }
            this.currentMode = currentMode;
            appendEventLocked("[MODE] Cambio a modo " + currentMode + ".");
        }
    }

    public void setCurrentUser(String currentUser) {
        synchronized (schedulerMonitor) {
            if (currentUser == null || currentUser.isBlank()) {
                throw new IllegalArgumentException("El usuario actual es obligatorio.");
            }
            this.currentUser = currentUser.trim().toLowerCase();
            appendEventLocked("[USER] Usuario activo: " + this.currentUser + ".");
        }
    }

    public void setSchedulingPolicy(SchedulingPolicy schedulingPolicy) {
        synchronized (schedulerMonitor) {
            if (schedulingPolicy == null) {
                throw new IllegalArgumentException("La politica de planificacion es obligatoria.");
            }
            this.schedulingPolicy = schedulingPolicy;
            appendEventLocked("[SCHED] Politica activa: " + schedulingPolicy + ".");
        }
    }

    public void setCurrentHeadPosition(int currentHeadPosition) {
        synchronized (schedulerMonitor) {
            int normalizedPosition = normalizeRequestedPosition(currentHeadPosition);
            if (normalizedPosition == this.currentHeadPosition) {
                return;
            }
            if (!canAdjustHeadPositionLocked()) {
                throw new IllegalStateException(
                        "Pausa el scheduler o espera a que termine el proceso actual antes de mover el cabezal."
                );
            }
            this.currentHeadPosition = normalizedPosition;
            appendEventLocked("[HEAD] Cabezal logico ubicado en " + this.currentHeadPosition + ".");
        }
    }

    public void startScheduler() {
        synchronized (schedulerMonitor) {
            schedulerStarted = true;
            schedulerPaused = false;
            appendEventLocked("[SCHED] Worker del scheduler iniciado.");
            schedulerMonitor.notifyAll();
        }
    }

    public void pauseScheduler() {
        synchronized (schedulerMonitor) {
            schedulerPaused = true;
            appendEventLocked("[SCHED] Scheduler pausado.");
            schedulerMonitor.notifyAll();
        }
    }

    public void resumeScheduler() {
        synchronized (schedulerMonitor) {
            schedulerStarted = true;
            schedulerPaused = false;
            appendEventLocked("[SCHED] Scheduler reanudado.");
            schedulerMonitor.notifyAll();
        }
    }

    public void interruptCurrentProcess() {
        synchronized (schedulerMonitor) {
            if (currentProcess == null) {
                appendEventLocked("[INT] No hay un proceso en ejecucion para interrumpir.");
                return;
            }
            interruptRequested = true;
            appendEventLocked("[INT] Interrupcion solicitada para PID " + currentProcess.getPid() + ".");
            schedulerMonitor.notifyAll();
        }
    }

    public void shutdown() {
        synchronized (schedulerMonitor) {
            shutdownRequested = true;
            schedulerPaused = false;
            interruptRequested = true;
            schedulerMonitor.notifyAll();
        }
    }

    public boolean waitUntilIdle(long timeoutMillis) {
        long deadline = System.currentTimeMillis() + Math.max(timeoutMillis, 0);
        synchronized (schedulerMonitor) {
            while (currentProcess != null || !pendingTasks.isEmpty()) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    return false;
                }
                try {
                    schedulerMonitor.wait(remaining);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return true;
        }
    }

    public void createDirectory(String parentPath, String name, EntryVisibility visibility) {
        synchronized (schedulerMonitor) {
            ensureAdministratorLocked("crear directorios");
            String normalizedParentPath = normalizeParentPath(parentPath);
            String targetPath = buildPath(normalizedParentPath, name);
            enqueueTaskLocked(
                    OperationType.CREATE,
                    targetPath,
                    currentHeadPosition,
                    LockType.EXCLUSIVE,
                    true,
                    () -> {
                        checkpoint("antes de ejecutar");
                        synchronized (schedulerMonitor) {
                            fileSystemTree.createDirectory(
                                    normalizedParentPath,
                                    name,
                                    getCurrentOwnerLocked(),
                                    visibility
                            );
                            appendEventLocked("[FS] Directorio creado: " + targetPath + ".");
                        }
                        checkpoint("despues de commit");
                    }
            );
        }
    }

    public void createFile(String parentPath, String name, int sizeInBlocks, EntryVisibility visibility) {
        synchronized (schedulerMonitor) {
            ensureAdministratorLocked("crear archivos");
            String normalizedParentPath = normalizeParentPath(parentPath);
            String targetPath = buildPath(normalizedParentPath, name);
            int requestedPosition = findFirstFreeIoPositionLocked();
            enqueueTaskLocked(
                    OperationType.CREATE,
                    targetPath,
                    requestedPosition,
                    LockType.EXCLUSIVE,
                    true,
                    () -> runCreateFileTask(
                            normalizedParentPath,
                            name,
                            sizeInBlocks,
                            visibility,
                            requestedPosition,
                            targetPath
                    )
            );
        }
    }

    public void queueRead(String path) {
        synchronized (schedulerMonitor) {
            FileNode file = requireReadableFileLocked(path);
            enqueueTaskLocked(
                    OperationType.READ,
                    file.getPath(),
                    resolveRequestedPositionLocked(file),
                    LockType.SHARED,
                    true,
                    () -> {
                        checkpoint("antes de ejecutar");
                        synchronized (schedulerMonitor) {
                            appendEventLocked("[READ] Lectura completada: " + file.getPath()
                                    + " en posicion " + file.getIoPosition() + ".");
                        }
                        checkpoint("despues de commit");
                    }
            );
        }
    }

    public void renameNode(String path, String newName) {
        synchronized (schedulerMonitor) {
            FSNode targetNode = requireModifiableNodeLocked(path, "renombrar archivos o directorios");
            String requestedPath = targetNode.getPath();
            enqueueTaskLocked(
                    OperationType.UPDATE,
                    requestedPath,
                    resolveRequestedPositionLocked(targetNode),
                    LockType.EXCLUSIVE,
                    true,
                    () -> {
                        checkpoint("antes de ejecutar");
                        synchronized (schedulerMonitor) {
                            FSNode renamedNode = fileSystemTree.renameNode(requestedPath, newName);
                            appendEventLocked("[FS] Nodo renombrado a " + renamedNode.getPath() + ".");
                        }
                        checkpoint("despues de commit");
                    }
            );
        }
    }

    public void deleteNode(String path) {
        synchronized (schedulerMonitor) {
            FSNode targetNode = requireModifiableNodeLocked(path, "eliminar archivos o directorios");
            enqueueTaskLocked(
                    OperationType.DELETE,
                    targetNode.getPath(),
                    resolveRequestedPositionLocked(targetNode),
                    LockType.EXCLUSIVE,
                    true,
                    () -> runDeleteNodeTask(targetNode)
            );
        }
    }

    public void simulateFailedCreate(String parentPath, String name, int sizeInBlocks) {
        synchronized (schedulerMonitor) {
            ensureAdministratorLocked("simular fallos");
            String normalizedParentPath = normalizeParentPath(parentPath);
            String targetPath = buildPath(normalizedParentPath, name);
            int requestedPosition = findFirstFreeIoPositionLocked();
            enqueueTaskLocked(
                    OperationType.CREATE,
                    targetPath,
                    requestedPosition,
                    LockType.EXCLUSIVE,
                    true,
                    () -> {
                        checkpoint("antes de ejecutar");
                        synchronized (schedulerMonitor) {
                            JournalEntry journalEntry = journalManager.beginCreate(
                                    normalizedParentPath,
                                    name,
                                    getCurrentOwnerLocked(),
                                    EntryVisibility.PRIVATE,
                                    sizeInBlocks
                            );
                            FileNode file = fileSystemTree.createFile(
                                    normalizedParentPath,
                                    name,
                                    getCurrentOwnerLocked(),
                                    EntryVisibility.PRIVATE,
                                    sizeInBlocks
                            );
                            file.setIoPosition(requestedPosition);
                            allocationManager.allocateFile(file);
                            journalManager.recordCreateResult(
                                    journalEntry,
                                    file,
                                    allocationManager.getAllocatedBlocks(file)
                            );
                            appendEventLocked("[FAIL] Falla simulada antes del commit para " + targetPath + ".");
                            journalManager.recoverPending(fileSystemTree, allocationManager);
                            appendEventLocked("[RECOVERY] Journal aplicado y sistema restaurado.");
                        }
                        checkpoint("despues de commit");
                    }
            );
        }
    }

    public void loadTestCase(Path path) throws IOException {
        SchedulerTestCase testCase = testCaseRepository.load(path);
        synchronized (schedulerMonitor) {
            ensureNoRunningProcessLocked("cargar un caso de prueba");
            schedulerPaused = true;
            clearSimulationStateLocked();
            currentMode = UserMode.ADMINISTRADOR;
            currentHeadPosition = normalizeRequestedPosition(testCase.getInitialHead());
            initializeBaseDirectoriesLocked();
            loadSystemFilesForCaseLocked(testCase);
            enqueueCaseRequestsLocked(testCase);
            appendEventLocked("[CASE] Caso " + testCase.getTestId()
                    + " cargado. Selecciona politica y reanuda el scheduler.");
            schedulerMonitor.notifyAll();
        }
    }

    public boolean canSeeNode(FSNode node) {
        synchronized (schedulerMonitor) {
            return canSeeNodeLocked(node);
        }
    }

    public boolean canReadNode(FSNode node) {
        synchronized (schedulerMonitor) {
            return canReadNodeLocked(node);
        }
    }

    public boolean canModifyNode(FSNode node) {
        synchronized (schedulerMonitor) {
            return canModifyNodeLocked(node);
        }
    }

    public Object[][] buildAllocationRows() {
        synchronized (schedulerMonitor) {
            SinglyLinkedList<FileNode> files = new SinglyLinkedList<>();
            collectVisibleFilesLocked(fileSystemTree.getRoot(), files);

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
    }

    public Object[][] buildProcessRows() {
        synchronized (schedulerMonitor) {
            Object[][] rows = new Object[processHistory.size()][4];
            for (int index = 0; index < processHistory.size(); index++) {
                ProcessControlBlock process = processHistory.get(index);
                rows[index][0] = process.getPid();
                rows[index][1] = process.getOperationType();
                rows[index][2] = process.getTargetPath() + " @ " + process.getRequestedPosition();
                rows[index][3] = process.getState();
            }
            return rows;
        }
    }

    public String buildLockDescription() {
        synchronized (schedulerMonitor) {
            if (currentMode == UserMode.ADMINISTRADOR) {
                String description = lockManager.describeActiveLocks();
                if (description.isBlank()) {
                    return "Sin locks activos.";
                }
                return description;
            }

            SinglyLinkedList<FileNode> files = new SinglyLinkedList<>();
            collectVisibleFilesLocked(fileSystemTree.getRoot(), files);
            StringBuilder description = new StringBuilder();
            for (int index = 0; index < files.size(); index++) {
                FileNode file = files.get(index);
                int activeLocks = lockManager.countActiveLocks(file.getPath());
                int waitingLocks = lockManager.countWaitingLocks(file.getPath());
                if (activeLocks == 0 && waitingLocks == 0) {
                    continue;
                }
                if (!description.isEmpty()) {
                    description.append('\n');
                }
                description.append(file.getPath())
                        .append(" -> activos: ")
                        .append(activeLocks)
                        .append(", en espera: ")
                        .append(waitingLocks);
            }
            if (description.isEmpty()) {
                return "Sin locks activos.";
            }
            return description.toString();
        }
    }

    public String[] buildEventLogLines() {
        synchronized (schedulerMonitor) {
            return copyStringListLocked(eventLog);
        }
    }

    public String[] buildJournalLines() {
        synchronized (schedulerMonitor) {
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
    }

    public int[] getCompletedRequestPositionsSnapshot() {
        synchronized (schedulerMonitor) {
            int[] snapshot = new int[completedRequestPositions.size()];
            for (int index = 0; index < completedRequestPositions.size(); index++) {
                snapshot[index] = completedRequestPositions.get(index);
            }
            return snapshot;
        }
    }

    public int countProcessesByState(ProcessState state) {
        synchronized (schedulerMonitor) {
            int count = 0;
            for (int index = 0; index < processHistory.size(); index++) {
                if (processHistory.get(index).getState() == state) {
                    count++;
                }
            }
            return count;
        }
    }

    public void saveToJson(Path path) throws IOException {
        SimulationSaveData saveData;
        synchronized (schedulerMonitor) {
            saveData = buildSaveDataLocked();
        }
        stateRepository.save(path, saveData);
        synchronized (schedulerMonitor) {
            appendEventLocked("[JSON] Estado guardado en " + path.getFileName() + ".");
        }
    }

    public void loadFromJson(Path path) throws IOException {
        SimulationSaveData saveData = stateRepository.load(path);
        synchronized (schedulerMonitor) {
            ensureNoRunningProcessLocked("cargar un estado desde JSON");
            schedulerPaused = true;
            applySaveDataLocked(saveData);
            appendEventLocked("[JSON] Estado cargado desde " + path.getFileName() + ".");
            schedulerMonitor.notifyAll();
        }
    }

    private void runSchedulerLoop() {
        while (true) {
            SimulationTask task = waitForNextTask();
            if (task == null) {
                return;
            }
            executeTask(task);
        }
    }

    private SimulationTask waitForNextTask() {
        synchronized (schedulerMonitor) {
            while (!shutdownRequested) {
                promoteNewProcessesLocked();
                if (schedulerStarted && !schedulerPaused) {
                    ProcessControlBlock[] readyProcesses = buildReadyProcessesLocked();
                    if (readyProcesses.length > 0) {
                        ProcessControlBlock[] orderedProcesses = diskScheduler.order(
                                readyProcesses,
                                schedulingPolicy,
                                currentHeadPosition,
                                headDirection
                        );
                        ProcessControlBlock nextProcess = orderedProcesses[0];
                        SimulationTask task = findTaskByPidLocked(nextProcess.getPid());
                        if (task != null) {
                            currentTask = task;
                            currentProcess = nextProcess;
                            return task;
                        }
                    }
                }
                try {
                    schedulerMonitor.wait();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            return null;
        }
    }

    private void executeTask(SimulationTask task) {
        ProcessControlBlock process = task.getProcess();
        boolean lockAcquired = false;

        try {
            checkpoint("antes de lock");
            synchronized (schedulerMonitor) {
                if (!lockManager.acquireLock(task.getLockRequest())) {
                    appendEventLocked("[LOCK] PID " + process.getPid()
                            + " bloqueado en " + process.getTargetPath() + ".");
                    clearCurrentExecutionLocked();
                    schedulerMonitor.notifyAll();
                    return;
                }

                lockAcquired = true;
                process.setState(ProcessState.RUNNING);
                currentHeadPosition = normalizeRequestedPosition(process.getRequestedPosition());
                appendEventLocked("[SCHED] Ejecutando PID " + process.getPid()
                        + " con " + schedulingPolicy
                        + " en posicion " + currentHeadPosition + ".");
            }

            task.getAction().run();

            synchronized (schedulerMonitor) {
                process.setState(ProcessState.TERMINATED);
                completedRequestPositions.addLast(process.getRequestedPosition());
                appendEventLocked("[PROC] PID " + process.getPid() + " finalizado.");
                appendEventLocked("[ORDER] Posicion completada: " + process.getRequestedPosition() + ".");
            }
        } catch (TaskInterruptedException exception) {
            synchronized (schedulerMonitor) {
                journalManager.recoverPending(fileSystemTree, allocationManager);
                process.setState(ProcessState.TERMINATED);
                appendEventLocked("[INT] PID " + process.getPid()
                        + " interrumpido: " + exception.getMessage() + ".");
            }
        } catch (RuntimeException exception) {
            synchronized (schedulerMonitor) {
                journalManager.recoverPending(fileSystemTree, allocationManager);
                process.setState(ProcessState.TERMINATED);
                appendEventLocked("[ERROR] PID " + process.getPid() + ": " + exception.getMessage());
            }
        } finally {
            synchronized (schedulerMonitor) {
                if (lockAcquired) {
                    SinglyLinkedList<ProcessControlBlock> awakened = lockManager.releaseLocksByProcess(
                            process.getPid()
                    );
                    removePendingTaskLocked(process.getPid());
                    logAwakenedProcessesLocked(awakened);
                }
                clearCurrentExecutionLocked();
                interruptRequested = false;
                schedulerMonitor.notifyAll();
            }
        }
    }

    private void runCreateFileTask(
            String parentPath,
            String name,
            int sizeInBlocks,
            EntryVisibility visibility,
            int ioPosition,
            String targetPath
    ) {
        checkpoint("antes de ejecutar");
        synchronized (schedulerMonitor) {
            JournalEntry journalEntry = journalManager.beginCreate(
                    parentPath,
                    name,
                    getCurrentOwnerLocked(),
                    visibility,
                    sizeInBlocks
            );
            FileNode file = fileSystemTree.createFile(
                    parentPath,
                    name,
                    getCurrentOwnerLocked(),
                    visibility,
                    sizeInBlocks
            );
            file.setIoPosition(ioPosition);
            allocationManager.allocateFile(file);
            journalManager.recordCreateResult(
                    journalEntry,
                    file,
                    allocationManager.getAllocatedBlocks(file)
            );
            appendEventLocked("[FS] Archivo preparado: " + targetPath
                    + " (" + sizeInBlocks + " bloques, pos " + ioPosition + ").");
        }
        checkpoint("antes de commit");
        synchronized (schedulerMonitor) {
            JournalEntry journalEntry = journalManager.getEntries().getLast();
            journalManager.markCommitted(journalEntry);
            appendEventLocked("[FS] Archivo creado: " + targetPath + ".");
        }
        checkpoint("despues de commit");
    }

    private void runDeleteNodeTask(FSNode targetNode) {
        checkpoint("antes de ejecutar");
        synchronized (schedulerMonitor) {
            deleteNodeInternalLocked(targetNode);
            appendEventLocked("[FS] Nodo eliminado: " + targetNode.getPath() + ".");
        }
        checkpoint("antes de commit");
        checkpoint("despues de commit");
    }

    private void deleteNodeInternalLocked(FSNode targetNode) {
        if (targetNode.getType() == FSNodeType.FILE) {
            deleteFileWithJournalLocked((FileNode) targetNode);
            return;
        }

        DirectoryNode directory = (DirectoryNode) targetNode;
        while (directory.getChildrenCount() > 0) {
            FSNode child = directory.getChildAt(0);
            deleteNodeInternalLocked(child);
        }
        fileSystemTree.removeNode(directory.getPath());
    }

    private void deleteFileWithJournalLocked(FileNode file) {
        int[] allocatedBlocks = allocationManager.getAllocatedBlocks(file);
        JournalEntry journalEntry = journalManager.beginDelete(file, allocatedBlocks);
        allocationManager.releaseFile(file);
        fileSystemTree.removeNode(file.getPath());
        checkpoint("antes de commit");
        journalManager.markCommitted(journalEntry);
    }

    private void checkpoint(String stage) {
        pauseForVisualization();
        synchronized (schedulerMonitor) {
            while (schedulerPaused && !shutdownRequested) {
                try {
                    schedulerMonitor.wait();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new TaskInterruptedException("el worker fue interrumpido");
                }
            }
            if (shutdownRequested) {
                throw new TaskInterruptedException("el scheduler se esta cerrando");
            }
            if (interruptRequested) {
                throw new TaskInterruptedException("interrupcion manual en " + stage);
            }
        }
    }

    private void pauseForVisualization() {
        try {
            Thread.sleep(CHECKPOINT_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new TaskInterruptedException("el worker fue interrumpido");
        }
    }

    private void loadSystemFilesForCaseLocked(SchedulerTestCase testCase) {
        int[] positions = testCase.getSystemFilePositions();
        String[] names = testCase.getSystemFileNames();
        int[] blocks = testCase.getSystemFileBlocks();
        for (int index = 0; index < positions.length; index++) {
            FileNode file = fileSystemTree.createFile(
                    "/system",
                    names[index],
                    "system",
                    EntryVisibility.PUBLIC,
                    blocks[index]
            );
            file.setIoPosition(normalizeRequestedPosition(positions[index]));
            allocationManager.allocateFile(file);
        }
        appendEventLocked("[CASE] Se cargaron " + positions.length + " archivos del sistema.");
    }

    private void enqueueCaseRequestsLocked(SchedulerTestCase testCase) {
        completedRequestPositions.clear();
        SchedulerTestRequest[] requests = testCase.getRequests();
        int[] positions = testCase.getSystemFilePositions();
        String[] names = testCase.getSystemFileNames();

        for (int index = 0; index < requests.length; index++) {
            SchedulerTestRequest request = requests[index];
            String targetPath = resolveSystemFilePathByPositionLocked(
                    request.getPosition(),
                    positions,
                    names
            );
            enqueueTaskLocked(
                    request.getOperationType(),
                    targetPath,
                    request.getPosition(),
                    resolveLockType(request.getOperationType()),
                    false,
                    () -> {
                        checkpoint("antes de ejecutar");
                        synchronized (schedulerMonitor) {
                            appendEventLocked("[CASE] " + request.getOperationType()
                                    + " sobre " + targetPath
                                    + " en posicion " + request.getPosition() + ".");
                        }
                        checkpoint("despues de commit");
                    }
            );
        }
    }

    private LockType resolveLockType(OperationType operationType) {
        if (operationType == OperationType.READ) {
            return LockType.SHARED;
        }
        return LockType.EXCLUSIVE;
    }

    private String resolveSystemFilePathByPositionLocked(
            int requestedPosition,
            int[] positions,
            String[] names
    ) {
        for (int index = 0; index < positions.length; index++) {
            if (positions[index] == requestedPosition) {
                return "/system/" + names[index];
            }
        }
        throw new IllegalArgumentException(
                "No existe un archivo del sistema asociado a la posicion " + requestedPosition + "."
        );
    }

    private void enqueueTaskLocked(
            OperationType operationType,
            String targetPath,
            int requestedPosition,
            LockType lockType,
            boolean autoStartScheduler,
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
        appendEventLocked("[PROC] PID " + process.getPid()
                + " creado para " + operationType
                + " en " + targetPath
                + " @ " + normalizedPosition + ".");
        if (autoStartScheduler && !schedulerStarted) {
            schedulerStarted = true;
            schedulerPaused = false;
            appendEventLocked("[SCHED] Inicio automatico del scheduler por nueva solicitud.");
        }
        schedulerMonitor.notifyAll();
    }

    private SimulationSaveData buildSaveDataLocked() {
        SinglyLinkedList<SavedDirectory> directories = new SinglyLinkedList<>();
        SinglyLinkedList<SavedFile> files = new SinglyLinkedList<>();
        collectSaveEntriesLocked(fileSystemTree.getRoot(), directories, files);
        return new SimulationSaveData(
                currentMode,
                schedulingPolicy,
                currentHeadPosition,
                currentUser,
                toDirectoryArrayLocked(directories),
                toFileArrayLocked(files)
        );
    }

    private void applySaveDataLocked(SimulationSaveData saveData) {
        clearSimulationStateLocked();
        currentMode = saveData.getUserMode();
        schedulingPolicy = saveData.getSchedulingPolicy();
        currentHeadPosition = normalizeRequestedPosition(saveData.getHeadPosition());
        currentUser = saveData.getCurrentUser();

        SavedDirectory[] directories = saveData.getDirectories();
        for (int index = 0; index < directories.length; index++) {
            SavedDirectory directory = directories[index];
            fileSystemTree.createDirectory(
                    parentPath(directory.getPath()),
                    nameFromPath(directory.getPath()),
                    directory.getOwner(),
                    directory.getVisibility()
            );
        }

        SavedFile[] files = saveData.getFiles();
        for (int index = 0; index < files.length; index++) {
            restoreFileLocked(files[index]);
        }
    }

    private void restoreFileLocked(SavedFile savedFile) {
        FileNode restoredFile = fileSystemTree.createFile(
                parentPath(savedFile.getPath()),
                nameFromPath(savedFile.getPath()),
                savedFile.getOwner(),
                savedFile.getVisibility(),
                savedFile.getSizeInBlocks()
        );
        restoredFile.setIoPosition(savedFile.getIoPosition());

        int[] blocks = savedFile.getBlocks();
        if (blocks.length == 0) {
            restoredFile.setFirstBlockIndex(-1);
            restoredFile.setColorId(-1);
            return;
        }

        for (int index = 0; index < blocks.length; index++) {
            int currentBlock = blocks[index];
            int nextBlock = index == blocks.length - 1 ? -1 : blocks[index + 1];
            disk.occupyBlock(currentBlock, restoredFile.getPath(), nextBlock, savedFile.getColorId());
        }
        restoredFile.setFirstBlockIndex(blocks[0]);
        restoredFile.setColorId(savedFile.getColorId());
    }

    private void promoteNewProcessesLocked() {
        for (int index = 0; index < pendingTasks.size(); index++) {
            ProcessControlBlock process = pendingTasks.get(index).getProcess();
            if (process.getState() == ProcessState.NEW) {
                process.setState(ProcessState.READY);
            }
        }
    }

    private ProcessControlBlock[] buildReadyProcessesLocked() {
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

    private SimulationTask findTaskByPidLocked(int pid) {
        for (int index = 0; index < pendingTasks.size(); index++) {
            SimulationTask task = pendingTasks.get(index);
            if (task.getProcess().getPid() == pid) {
                return task;
            }
        }
        return null;
    }

    private void removePendingTaskLocked(int pid) {
        for (int index = 0; index < pendingTasks.size(); index++) {
            if (pendingTasks.get(index).getProcess().getPid() == pid) {
                pendingTasks.removeAt(index);
                return;
            }
        }
    }

    private void logAwakenedProcessesLocked(SinglyLinkedList<ProcessControlBlock> awakenedProcesses) {
        for (int index = 0; index < awakenedProcesses.size(); index++) {
            ProcessControlBlock awakened = awakenedProcesses.get(index);
            appendEventLocked("[LOCK] PID " + awakened.getPid() + " vuelve a READY.");
        }
    }

    private void collectSaveEntriesLocked(
            FSNode node,
            SinglyLinkedList<SavedDirectory> directories,
            SinglyLinkedList<SavedFile> files
    ) {
        if (node.getType() == FSNodeType.FILE) {
            FileNode file = (FileNode) node;
            files.addLast(new SavedFile(
                    file.getPath(),
                    file.getOwner(),
                    file.getVisibility(),
                    file.getSizeInBlocks(),
                    allocationManager.getAllocatedBlocks(file),
                    file.getColorId(),
                    file.getIoPosition()
            ));
            return;
        }

        DirectoryNode directory = (DirectoryNode) node;
        if (!directory.isRoot()) {
            directories.addLast(new SavedDirectory(
                    directory.getPath(),
                    directory.getOwner(),
                    directory.getVisibility()
            ));
        }

        for (int index = 0; index < directory.getChildrenCount(); index++) {
            collectSaveEntriesLocked(directory.getChildAt(index), directories, files);
        }
    }

    private void collectVisibleFilesLocked(FSNode node, SinglyLinkedList<FileNode> files) {
        if (node.getType() == FSNodeType.FILE) {
            if (canSeeNodeLocked(node)) {
                files.addLast((FileNode) node);
            }
            return;
        }
        DirectoryNode directory = (DirectoryNode) node;
        for (int index = 0; index < directory.getChildrenCount(); index++) {
            collectVisibleFilesLocked(directory.getChildAt(index), files);
        }
    }

    private void collectAllFilesLocked(FSNode node, SinglyLinkedList<FileNode> files) {
        if (node.getType() == FSNodeType.FILE) {
            files.addLast((FileNode) node);
            return;
        }
        DirectoryNode directory = (DirectoryNode) node;
        for (int index = 0; index < directory.getChildrenCount(); index++) {
            collectAllFilesLocked(directory.getChildAt(index), files);
        }
    }

    private void copyVisibleChildrenLocked(DirectoryNode sourceDirectory, DirectoryNode targetDirectory) {
        for (int index = 0; index < sourceDirectory.getChildrenCount(); index++) {
            FSNode child = sourceDirectory.getChildAt(index);
            if (child.getType() == FSNodeType.FILE) {
                if (!canSeeNodeLocked(child)) {
                    continue;
                }
                targetDirectory.addChild(cloneFile((FileNode) child));
                continue;
            }

            DirectoryNode sourceChildDirectory = (DirectoryNode) child;
            DirectoryNode clonedDirectory = new DirectoryNode(
                    sourceChildDirectory.getName(),
                    sourceChildDirectory.getOwner(),
                    sourceChildDirectory.getVisibility()
            );
            boolean childVisible = canSeeNodeLocked(sourceChildDirectory);
            copyVisibleChildrenLocked(sourceChildDirectory, clonedDirectory);
            if (childVisible || clonedDirectory.getChildrenCount() > 0) {
                targetDirectory.addChild(clonedDirectory);
            }
        }
    }

    private FileNode cloneFile(FileNode sourceFile) {
        FileNode clone = new FileNode(
                sourceFile.getName(),
                sourceFile.getOwner(),
                sourceFile.getVisibility(),
                sourceFile.getSizeInBlocks()
        );
        clone.setFirstBlockIndex(sourceFile.getFirstBlockIndex());
        clone.setColorId(sourceFile.getColorId());
        clone.setIoPosition(sourceFile.getIoPosition());
        return clone;
    }

    private FSNode requireNodeLocked(String path) {
        FSNode node = fileSystemTree.findNode(path);
        if (node == null) {
            throw new IllegalArgumentException("No existe la ruta: " + path + ".");
        }
        return node;
    }

    private FileNode requireReadableFileLocked(String path) {
        FSNode node = requireNodeLocked(path);
        if (node.getType() != FSNodeType.FILE) {
            throw new IllegalArgumentException("La lectura del scheduler solo aplica a archivos.");
        }
        if (!canReadNodeLocked(node)) {
            throw new IllegalStateException("El usuario actual no tiene permiso de lectura sobre " + path + ".");
        }
        return (FileNode) node;
    }

    private FSNode requireModifiableNodeLocked(String path, String action) {
        FSNode node = requireNodeLocked(path);
        if (!canModifyNodeLocked(node)) {
            throw new IllegalStateException("El modo actual no permite " + action + ".");
        }
        return node;
    }

    private int resolveRequestedPositionLocked(FSNode targetNode) {
        if (targetNode.getType() == FSNodeType.FILE) {
            FileNode file = (FileNode) targetNode;
            if (file.getIoPosition() >= 0) {
                return file.getIoPosition();
            }
        }
        return currentHeadPosition;
    }

    private int findFirstFreeIoPositionLocked() {
        for (int position = 0; position <= MAX_IO_POSITION; position++) {
            if (!isIoPositionUsedLocked(position)) {
                return position;
            }
        }
        throw new IllegalStateException("No hay posiciones logicas de E/S disponibles.");
    }

    private boolean isIoPositionUsedLocked(int ioPosition) {
        SinglyLinkedList<FileNode> files = new SinglyLinkedList<>();
        collectAllFilesLocked(fileSystemTree.getRoot(), files);
        for (int index = 0; index < files.size(); index++) {
            if (files.get(index).getIoPosition() == ioPosition) {
                return true;
            }
        }
        return false;
    }

    private boolean canAdjustHeadPositionLocked() {
        return currentProcess == null && (schedulerPaused || pendingTasks.isEmpty());
    }

    private int normalizeRequestedPosition(int requestedPosition) {
        if (requestedPosition < 0) {
            return 0;
        }
        if (requestedPosition > MAX_IO_POSITION) {
            return MAX_IO_POSITION;
        }
        return requestedPosition;
    }

    private boolean canSeeNodeLocked(FSNode node) {
        if (node == null) {
            return false;
        }
        if (currentMode == UserMode.ADMINISTRADOR) {
            return true;
        }
        if (node.isRoot()) {
            return true;
        }
        if (node.getType() == FSNodeType.FILE) {
            return canReadNodeLocked(node);
        }
        String path = node.getPath();
        return "/system".equals(path)
                || "/users".equals(path)
                || ("/users/" + currentUser).equals(path)
                || currentUser.equals(node.getOwner())
                || node.getVisibility() == EntryVisibility.PUBLIC;
    }

    private boolean canReadNodeLocked(FSNode node) {
        if (node == null) {
            return false;
        }
        if (currentMode == UserMode.ADMINISTRADOR) {
            return true;
        }
        if (node.getType() != FSNodeType.FILE) {
            return canSeeNodeLocked(node);
        }
        return currentUser.equals(node.getOwner()) || node.getVisibility() == EntryVisibility.PUBLIC;
    }

    private boolean canModifyNodeLocked(FSNode node) {
        return currentMode == UserMode.ADMINISTRADOR && node != null;
    }

    private void clearSimulationStateLocked() {
        fileSystemTree.clear();
        disk.clear();
        lockManager.clear();
        journalManager.clear();
        pendingTasks.clear();
        processHistory.clear();
        eventLog.clear();
        completedRequestPositions.clear();
        nextPid = 1;
        currentTask = null;
        currentProcess = null;
        interruptRequested = false;
    }

    private void initializeBaseDirectoriesLocked() {
        fileSystemTree.createDirectory("/", "system", "system", EntryVisibility.SYSTEM);
        fileSystemTree.createDirectory("/", "users", "system", EntryVisibility.SYSTEM);
        fileSystemTree.createDirectory("/users", "daniel", "daniel", EntryVisibility.PRIVATE);
    }

    private void seedInitialStateLocked() {
        initializeBaseDirectoriesLocked();
        createSeedFileLocked("/system", "readme.txt", "system", EntryVisibility.PUBLIC, 1, 34);
        createSeedFileLocked("/system", "config.sys", "system", EntryVisibility.SYSTEM, 2, 95);
        createSeedFileLocked("/users/daniel", "notes.txt", "daniel", EntryVisibility.PRIVATE, 3, 62);
        appendEventLocked("[BOOT] Sistema inicial cargado.");
        appendEventLocked("[BOOT] Politica inicial: " + schedulingPolicy + ".");
        appendEventLocked("[BOOT] Worker del scheduler listo. Usa Iniciar para procesar la cola.");
    }

    private void createSeedFileLocked(
            String parentPath,
            String name,
            String owner,
            EntryVisibility visibility,
            int sizeInBlocks,
            int ioPosition
    ) {
        FileNode file = fileSystemTree.createFile(parentPath, name, owner, visibility, sizeInBlocks);
        file.setIoPosition(normalizeRequestedPosition(ioPosition));
        allocationManager.allocateFile(file);
    }

    private void ensureAdministratorLocked(String action) {
        if (currentMode != UserMode.ADMINISTRADOR) {
            throw new IllegalStateException("El modo usuario no permite " + action + ".");
        }
    }

    private void ensureNoRunningProcessLocked(String action) {
        if (currentProcess != null) {
            throw new IllegalStateException("Espera a que termine el proceso actual antes de " + action + ".");
        }
    }

    private void clearCurrentExecutionLocked() {
        currentTask = null;
        currentProcess = null;
    }

    private void appendEventLocked(String event) {
        eventLog.addLast(event);
        requestViewRefreshLocked();
    }

    private void requestViewRefreshLocked() {
        Runnable listener = viewRefreshListener;
        if (listener != null) {
            listener.run();
        }
    }

    private String getCurrentOwnerLocked() {
        if (currentMode == UserMode.ADMINISTRADOR) {
            return "admin";
        }
        return currentUser;
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

    private String parentPath(String path) {
        int separatorIndex = path.lastIndexOf('/');
        if (separatorIndex <= 0) {
            return "/";
        }
        return path.substring(0, separatorIndex);
    }

    private String nameFromPath(String path) {
        int separatorIndex = path.lastIndexOf('/');
        return path.substring(separatorIndex + 1);
    }

    private String[] copyStringListLocked(SinglyLinkedList<String> source) {
        String[] copy = new String[source.size()];
        for (int index = 0; index < source.size(); index++) {
            copy[index] = source.get(index);
        }
        return copy;
    }

    private SavedDirectory[] toDirectoryArrayLocked(SinglyLinkedList<SavedDirectory> directories) {
        SavedDirectory[] array = new SavedDirectory[directories.size()];
        for (int index = 0; index < directories.size(); index++) {
            array[index] = directories.get(index);
        }
        return array;
    }

    private SavedFile[] toFileArrayLocked(SinglyLinkedList<SavedFile> files) {
        SavedFile[] array = new SavedFile[files.size()];
        for (int index = 0; index < files.size(); index++) {
            array[index] = files.get(index);
        }
        return array;
    }

    private static final class TaskInterruptedException extends RuntimeException {

        private TaskInterruptedException(String message) {
            super(message);
        }
    }
}
