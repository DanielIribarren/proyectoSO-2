package com.mycompany.proyectoso_2.ui.panel;

import com.mycompany.proyectoso_2.ui.model.ProcessTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

public class ProcessPanel extends JPanel {

    private final ProcessTableModel tableModel;

    public ProcessPanel() {
        tableModel = new ProcessTableModel();
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Procesos y locks"));

        JTable processTable = new JTable(tableModel);
        processTable.setFillsViewportHeight(true);

        add(new JScrollPane(processTable), BorderLayout.CENTER);
        add(buildLockInfoPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildLockInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout(6, 6));
        JTextArea lockArea = new JTextArea();
        lockArea.setEditable(false);
        lockArea.setRows(5);
        lockArea.setText("Locks activos:\n"
                + "- /config/config.sys -> EXCLUSIVE por PID 2\n"
                + "- /system/readme.txt -> SHARED por PID 1");

        JPanel metricsPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        metricsPanel.add(new JLabel("Procesos listos: 1"));
        metricsPanel.add(new JLabel("Procesos bloqueados: 1"));
        metricsPanel.add(new JLabel("Procesos nuevos: 1"));

        infoPanel.add(metricsPanel, BorderLayout.NORTH);
        infoPanel.add(new JScrollPane(lockArea), BorderLayout.CENTER);
        return infoPanel;
    }
}
