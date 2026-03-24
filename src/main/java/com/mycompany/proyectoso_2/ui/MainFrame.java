package com.mycompany.proyectoso_2.ui;

import com.mycompany.proyectoso_2.filesystem.EntryVisibility;
import com.mycompany.proyectoso_2.filesystem.FSNode;
import com.mycompany.proyectoso_2.filesystem.FSNodeType;
import com.mycompany.proyectoso_2.model.SchedulingPolicy;
import com.mycompany.proyectoso_2.model.UserMode;
import com.mycompany.proyectoso_2.process.ProcessState;
import com.mycompany.proyectoso_2.simulation.SimulationController;
import com.mycompany.proyectoso_2.ui.panel.AllocationTablePanel;
import com.mycompany.proyectoso_2.ui.panel.DiskPanel;
import com.mycompany.proyectoso_2.ui.panel.FileExplorerPanel;
import com.mycompany.proyectoso_2.ui.panel.LogPanel;
import com.mycompany.proyectoso_2.ui.panel.ProcessPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private final SimulationController controller;
    private final FileExplorerPanel fileExplorerPanel;
    private final DiskPanel diskPanel;
    private final AllocationTablePanel allocationTablePanel;
    private final ProcessPanel processPanel;
    private final LogPanel eventLogPanel;
    private final LogPanel journalLogPanel;
    private final JComboBox<UserMode> modeComboBox;
    private final JComboBox<SchedulingPolicy> policyComboBox;
    private final JSpinner headSpinner;
    private final JTextField currentUserField;
    private final JButton createFileButton;
    private final JButton createDirectoryButton;
    private final JButton readButton;
    private final JButton renameButton;
    private final JButton deleteButton;
    private final JButton simulateFailureButton;
    private final JButton startSchedulerButton;
    private final JButton pauseSchedulerButton;
    private final JButton resumeSchedulerButton;
    private final JButton interruptButton;
    private final JButton applyUserButton;
    private final JButton loadJsonButton;
    private final JButton saveJsonButton;
    private final JButton loadTestCaseButton;
    private boolean refreshQueued;
    private boolean refreshRequestedWhileQueued;

    public MainFrame() {
        super("Proyecto 2 - Simulador de Sistema de Archivos");
        controller = new SimulationController();
        fileExplorerPanel = new FileExplorerPanel();
        diskPanel = new DiskPanel();
        allocationTablePanel = new AllocationTablePanel();
        processPanel = new ProcessPanel();
        eventLogPanel = new LogPanel("Eventos del sistema");
        journalLogPanel = new LogPanel("Journal");
        modeComboBox = new JComboBox<>(UserMode.values());
        policyComboBox = new JComboBox<>(SchedulingPolicy.values());
        headSpinner = new JSpinner(new SpinnerNumberModel(12, 0, 199, 1));
        currentUserField = new JTextField("daniel", 8);
        createFileButton = createActionButton("Crear archivo", this::handleCreateFile);
        createDirectoryButton = createActionButton("Crear directorio", this::handleCreateDirectory);
        readButton = createActionButton("Leer seleccionado", this::handleReadNode);
        renameButton = createActionButton("Renombrar", this::handleRenameNode);
        deleteButton = createActionButton("Eliminar", this::handleDeleteNode);
        simulateFailureButton = createActionButton("Simular fallo", this::handleSimulateFailure);
        startSchedulerButton = createActionButton("Iniciar", controller::startScheduler);
        pauseSchedulerButton = createActionButton("Pausar", controller::pauseScheduler);
        resumeSchedulerButton = createActionButton("Reanudar", controller::resumeScheduler);
        interruptButton = createActionButton("Interrumpir actual", controller::interruptCurrentProcess);
        applyUserButton = createActionButton("Aplicar usuario", this::handleApplyUser);
        loadJsonButton = createActionButton("Cargar JSON", this::handleLoadJson);
        saveJsonButton = createActionButton("Guardar JSON", this::handleSaveJson);
        loadTestCaseButton = createActionButton("Cargar caso de prueba", this::handleLoadTestCase);
        refreshQueued = false;
        refreshRequestedWhileQueued = false;
        initializeFrame();
    }

    private void initializeFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1380, 860));
        setPreferredSize(new Dimension(1440, 900));
        setLayout(new BorderLayout(12, 12));

        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        fileExplorerPanel.setSelectionListener(selection -> updateSelectionTitle());

        contentPanel.add(buildTopBar(), BorderLayout.NORTH);
        contentPanel.add(buildCenterArea(), BorderLayout.CENTER);
        contentPanel.add(buildBottomArea(), BorderLayout.SOUTH);

        setContentPane(contentPanel);
        controller.setViewRefreshListener(this::scheduleRefreshView);
        wireControls();
        refreshView();
        pack();
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                controller.shutdown();
            }
        });
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topBar.setBorder(BorderFactory.createTitledBorder("Controles de simulacion"));

        topBar.add(new JLabel("Modo"));
        topBar.add(modeComboBox);
        topBar.add(new JLabel("Politica"));
        topBar.add(policyComboBox);
        topBar.add(new JLabel("Cabezal"));
        topBar.add(headSpinner);
        topBar.add(new JLabel("Usuario"));
        topBar.add(currentUserField);
        topBar.add(applyUserButton);
        topBar.add(startSchedulerButton);
        topBar.add(pauseSchedulerButton);
        topBar.add(resumeSchedulerButton);
        topBar.add(interruptButton);
        topBar.add(createFileButton);
        topBar.add(createDirectoryButton);
        topBar.add(readButton);
        topBar.add(renameButton);
        topBar.add(deleteButton);
        topBar.add(simulateFailureButton);
        topBar.add(loadTestCaseButton);
        topBar.add(loadJsonButton);
        topBar.add(saveJsonButton);

        return topBar;
    }

    private JSplitPane buildCenterArea() {
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.22);

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplitPane.setResizeWeight(0.58);

        JSplitPane sideSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sideSplitPane.setResizeWeight(0.5);

        sideSplitPane.setTopComponent(allocationTablePanel);
        sideSplitPane.setBottomComponent(processPanel);

        rightSplitPane.setLeftComponent(diskPanel);
        rightSplitPane.setRightComponent(sideSplitPane);

        mainSplitPane.setLeftComponent(fileExplorerPanel);
        mainSplitPane.setRightComponent(rightSplitPane);

        mainSplitPane.setDividerLocation(320);
        rightSplitPane.setDividerLocation(760);
        sideSplitPane.setDividerLocation(250);
        return mainSplitPane;
    }

    private JTabbedPane buildBottomArea() {
        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.setPreferredSize(new Dimension(1000, 220));
        bottomTabs.addTab("Eventos", eventLogPanel);
        bottomTabs.addTab("Journal", journalLogPanel);
        return bottomTabs;
    }

    private void wireControls() {
        modeComboBox.addActionListener(event -> {
            controller.setCurrentMode((UserMode) modeComboBox.getSelectedItem());
            refreshView();
        });
        policyComboBox.addActionListener(event -> {
            controller.setSchedulingPolicy((SchedulingPolicy) policyComboBox.getSelectedItem());
            refreshView();
        });
        headSpinner.addChangeListener(event -> {
            try {
                controller.setCurrentHeadPosition((Integer) headSpinner.getValue());
                refreshView();
            } catch (RuntimeException exception) {
                showError(exception.getMessage());
            }
        });
    }

    private JButton createActionButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.addActionListener(event -> {
            try {
                action.run();
            } catch (RuntimeException exception) {
                refreshView();
                showError(exception.getMessage());
            }
        });
        return button;
    }

    private void handleCreateFile() {
        JTextField nameField = new JTextField();
        JTextField sizeField = new JTextField("1");
        JComboBox<EntryVisibility> visibilityComboBox =
                new JComboBox<>(new EntryVisibility[]{EntryVisibility.PRIVATE, EntryVisibility.PUBLIC});

        if (!showFormDialog("Crear archivo", "Nombre", nameField, "Tamano (bloques)", sizeField,
                "Visibilidad", visibilityComboBox)) {
            return;
        }

        controller.createFile(
                resolveSelectedDirectoryPath(),
                nameField.getText().trim(),
                parsePositiveInteger(sizeField.getText(), "El tamano del archivo"),
                (EntryVisibility) visibilityComboBox.getSelectedItem()
        );
        refreshView();
    }

    private void handleCreateDirectory() {
        JTextField nameField = new JTextField();
        JComboBox<EntryVisibility> visibilityComboBox =
                new JComboBox<>(new EntryVisibility[]{EntryVisibility.PRIVATE, EntryVisibility.PUBLIC, EntryVisibility.SYSTEM});

        if (!showFormDialog("Crear directorio", "Nombre", nameField, "Visibilidad", visibilityComboBox)) {
            return;
        }

        controller.createDirectory(
                resolveSelectedDirectoryPath(),
                nameField.getText().trim(),
                (EntryVisibility) visibilityComboBox.getSelectedItem()
        );
        refreshView();
    }

    private void handleReadNode() {
        String selectedPath = requireSelectionPath();
        controller.queueRead(selectedPath);
        refreshView();
    }

    private void handleRenameNode() {
        String selectedPath = requireSelectionPath();
        JTextField nameField = new JTextField();
        if (!showFormDialog("Renombrar nodo", "Nuevo nombre", nameField)) {
            return;
        }

        controller.renameNode(selectedPath, nameField.getText().trim());
        refreshView();
        fileExplorerPanel.selectPath(buildPath(parentPath(selectedPath), nameField.getText().trim()));
    }

    private void handleDeleteNode() {
        String selectedPath = requireSelectionPath();
        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Se eliminara " + selectedPath + ".",
                "Confirmar eliminacion",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirmation != JOptionPane.OK_OPTION) {
            return;
        }

        controller.deleteNode(selectedPath);
        refreshView();
        fileExplorerPanel.selectPath(parentPath(selectedPath));
    }

    private void handleSimulateFailure() {
        JTextField nameField = new JTextField("recovery.tmp");
        JTextField sizeField = new JTextField("2");

        if (!showFormDialog("Simular fallo en CREATE", "Nombre", nameField,
                "Tamano (bloques)", sizeField)) {
            return;
        }

        controller.simulateFailedCreate(
                resolveSelectedDirectoryPath(),
                nameField.getText().trim(),
                parsePositiveInteger(sizeField.getText(), "El tamano del archivo")
        );
        refreshView();
    }

    private void handleApplyUser() {
        controller.setCurrentUser(currentUserField.getText().trim());
        refreshView();
    }

    private void handleLoadJson() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos JSON", "json"));
        fileChooser.setSelectedFile(new java.io.File("simulator-state.json"));
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        executeJsonLoad(fileChooser);
    }

    private void handleSaveJson() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos JSON", "json"));
        fileChooser.setSelectedFile(new java.io.File("simulator-state.json"));
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        executeJsonSave(fileChooser);
    }

    private void handleLoadTestCase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos JSON", "json"));
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            controller.loadTestCase(fileChooser.getSelectedFile().toPath());
            refreshView();
        } catch (IOException exception) {
            showError("No se pudo cargar el caso de prueba: " + exception.getMessage());
        }
    }

    private boolean showFormDialog(String title, Object... components) {
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        for (Object component : components) {
            if (component instanceof String label) {
                formPanel.add(new JLabel(label));
            } else if (component instanceof java.awt.Component awtComponent) {
                formPanel.add(awtComponent);
            }
        }
        return JOptionPane.showConfirmDialog(
                this,
                formPanel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        ) == JOptionPane.OK_OPTION;
    }

    private void scheduleRefreshView() {
        synchronized (this) {
            if (refreshQueued) {
                refreshRequestedWhileQueued = true;
                return;
            }
            refreshQueued = true;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                refreshView();
            } finally {
                boolean scheduleAgain;
                synchronized (MainFrame.this) {
                    scheduleAgain = refreshRequestedWhileQueued;
                    refreshQueued = false;
                    refreshRequestedWhileQueued = false;
                }
                if (scheduleAgain) {
                    scheduleRefreshView();
                }
            }
        });
    }

    private void refreshView() {
        String selectedPath = fileExplorerPanel.getSelectedPath();
        fileExplorerPanel.setFileSystem(controller.buildVisibleTreeSnapshot());
        fileExplorerPanel.selectPath(selectedPath);
        diskPanel.setDiskState(controller.getDisk(), controller.getCurrentHeadPosition());
        allocationTablePanel.setRows(controller.buildAllocationRows());
        processPanel.setRows(controller.buildProcessRows());
        processPanel.setLockDescription(controller.buildLockDescription());
        processPanel.setMetrics(
                controller.countProcessesByState(ProcessState.READY),
                controller.countProcessesByState(ProcessState.BLOCKED),
                controller.countProcessesByState(ProcessState.NEW),
                controller.countProcessesByState(ProcessState.TERMINATED)
        );
        eventLogPanel.setLines(controller.buildEventLogLines());
        journalLogPanel.setLines(controller.buildJournalLines());
        if (modeComboBox.getSelectedItem() != controller.getCurrentMode()) {
            modeComboBox.setSelectedItem(controller.getCurrentMode());
        }
        if (policyComboBox.getSelectedItem() != controller.getSchedulingPolicy()) {
            policyComboBox.setSelectedItem(controller.getSchedulingPolicy());
        }
        if ((Integer) headSpinner.getValue() != controller.getCurrentHeadPosition()) {
            headSpinner.setValue(controller.getCurrentHeadPosition());
        }
        if (!currentUserField.getText().equals(controller.getCurrentUser())) {
            currentUserField.setText(controller.getCurrentUser());
        }
        updateActionAvailability();
        updateSelectionTitle();
    }

    private void updateActionAvailability() {
        boolean adminMode = controller.getCurrentMode() == UserMode.ADMINISTRADOR;
        FSNode selectedNode = resolveActualSelectedNode();
        createFileButton.setEnabled(adminMode);
        createDirectoryButton.setEnabled(adminMode);
        renameButton.setEnabled(adminMode);
        deleteButton.setEnabled(adminMode);
        simulateFailureButton.setEnabled(adminMode);
        readButton.setEnabled(selectedNode != null
                && selectedNode.getType() == FSNodeType.FILE
                && controller.canReadNode(selectedNode));
    }

    private void updateSelectionTitle() {
        String selectedPath = fileExplorerPanel.getSelectedPath();
        setTitle("Proyecto 2 - Simulador de Sistema de Archivos | "
                + controller.getCurrentMode()
                + " | usuario " + controller.getCurrentUser()
                + " | " + selectedPath);
    }

    private String resolveSelectedDirectoryPath() {
        FSNode selectedNode = resolveActualSelectedNode();
        if (selectedNode == null) {
            return "/";
        }
        if (selectedNode.getType() == FSNodeType.DIRECTORY) {
            return selectedNode.getPath();
        }
        return parentPath(selectedNode.getPath());
    }

    private FSNode resolveActualSelectedNode() {
        String selectedPath = fileExplorerPanel.getSelectedPath();
        return controller.getFileSystemTree().findNode(selectedPath);
    }

    private String requireSelectionPath() {
        String selectedPath = fileExplorerPanel.getSelectedPath();
        if (selectedPath == null || "/".equals(selectedPath)) {
            throw new IllegalStateException("Selecciona primero un nodo distinto de la raiz.");
        }
        return selectedPath;
    }

    private int parsePositiveInteger(String rawValue, String fieldName) {
        try {
            int parsedValue = Integer.parseInt(rawValue.trim());
            if (parsedValue <= 0) {
                throw new IllegalArgumentException(fieldName + " debe ser mayor a cero.");
            }
            return parsedValue;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " debe ser numerico.");
        }
    }

    private String parentPath(String path) {
        int separatorIndex = path.lastIndexOf('/');
        if (separatorIndex <= 0) {
            return "/";
        }
        return path.substring(0, separatorIndex);
    }

    private String buildPath(String parentPath, String name) {
        if ("/".equals(parentPath)) {
            return parentPath + name;
        }
        return parentPath + "/" + name;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Operacion invalida",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void executeJsonSave(JFileChooser fileChooser) {
        try {
            controller.saveToJson(ensureJsonExtension(fileChooser.getSelectedFile()).toPath());
            refreshView();
        } catch (IOException exception) {
            showError("No se pudo guardar el JSON: " + exception.getMessage());
        }
    }

    private void executeJsonLoad(JFileChooser fileChooser) {
        try {
            controller.loadFromJson(fileChooser.getSelectedFile().toPath());
            refreshView();
        } catch (IOException exception) {
            showError("No se pudo cargar el JSON: " + exception.getMessage());
        }
    }

    private java.io.File ensureJsonExtension(java.io.File selectedFile) {
        if (selectedFile.getName().endsWith(".json")) {
            return selectedFile;
        }
        if (selectedFile.getParentFile() == null) {
            return new java.io.File(selectedFile.getName() + ".json");
        }
        return new java.io.File(selectedFile.getParentFile(), selectedFile.getName() + ".json");
    }
}
