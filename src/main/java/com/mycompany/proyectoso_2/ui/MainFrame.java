package com.mycompany.proyectoso_2.ui;

import com.mycompany.proyectoso_2.model.SchedulingPolicy;
import com.mycompany.proyectoso_2.model.UserMode;
import com.mycompany.proyectoso_2.ui.panel.AllocationTablePanel;
import com.mycompany.proyectoso_2.ui.panel.DiskPanel;
import com.mycompany.proyectoso_2.ui.panel.FileExplorerPanel;
import com.mycompany.proyectoso_2.ui.panel.LogPanel;
import com.mycompany.proyectoso_2.ui.panel.ProcessPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

    private final FileExplorerPanel fileExplorerPanel;
    private final DiskPanel diskPanel;
    private final AllocationTablePanel allocationTablePanel;
    private final ProcessPanel processPanel;
    private final LogPanel eventLogPanel;
    private final LogPanel journalLogPanel;

    public MainFrame() {
        super("Proyecto 2 - Simulador de Sistema de Archivos");
        fileExplorerPanel = new FileExplorerPanel();
        diskPanel = new DiskPanel();
        allocationTablePanel = new AllocationTablePanel();
        processPanel = new ProcessPanel();
        eventLogPanel = new LogPanel("Eventos del sistema");
        journalLogPanel = new LogPanel("Journal");
        initializeFrame();
    }

    private void initializeFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1380, 860));
        setPreferredSize(new Dimension(1440, 900));
        setLayout(new BorderLayout(12, 12));

        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        contentPanel.add(buildTopBar(), BorderLayout.NORTH);
        contentPanel.add(buildCenterArea(), BorderLayout.CENTER);
        contentPanel.add(buildBottomArea(), BorderLayout.SOUTH);

        setContentPane(contentPanel);
        pack();
        setLocationRelativeTo(null);
        seedLogs();
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topBar.setBorder(BorderFactory.createTitledBorder("Controles de simulacion"));

        JComboBox<UserMode> modeComboBox = new JComboBox<>(UserMode.values());
        JComboBox<SchedulingPolicy> policyComboBox =
                new JComboBox<>(SchedulingPolicy.values());
        JSpinner headSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 999, 1));

        topBar.add(new JLabel("Modo"));
        topBar.add(modeComboBox);
        topBar.add(new JLabel("Politica"));
        topBar.add(policyComboBox);
        topBar.add(new JLabel("Cabezal inicial"));
        topBar.add(headSpinner);
        topBar.add(createActionButton("Crear archivo"));
        topBar.add(createActionButton("Crear directorio"));
        topBar.add(createActionButton("Renombrar"));
        topBar.add(createActionButton("Eliminar"));
        topBar.add(createActionButton("Simular fallo"));
        topBar.add(createActionButton("Cargar JSON"));
        topBar.add(createActionButton("Guardar JSON"));

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

    private JButton createActionButton(String label) {
        JButton button = new JButton(label);
        button.addActionListener(event -> JOptionPane.showMessageDialog(
                this,
                "La accion \"" + label + "\" se conectara en las siguientes ramas.",
                "Base del proyecto",
                JOptionPane.INFORMATION_MESSAGE));
        return button;
    }

    private void seedLogs() {
        eventLogPanel.appendLine("[BOOT] Interfaz base inicializada.");
        eventLogPanel.appendLine("[BOOT] Layout listo para integrar el simulador.");
        journalLogPanel.appendLine("[BOOT] Journal listo para registrar transacciones.");
    }
}
