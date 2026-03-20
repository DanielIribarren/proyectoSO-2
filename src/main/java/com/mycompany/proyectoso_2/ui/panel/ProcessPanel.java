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
    private final JTextArea lockArea;
    private final JLabel readyLabel;
    private final JLabel blockedLabel;
    private final JLabel newLabel;
    private final JLabel terminatedLabel;

    public ProcessPanel() {
        tableModel = new ProcessTableModel();
        lockArea = new JTextArea();
        readyLabel = new JLabel("Procesos listos: 0");
        blockedLabel = new JLabel("Procesos bloqueados: 0");
        newLabel = new JLabel("Procesos nuevos: 0");
        terminatedLabel = new JLabel("Procesos terminados: 0");
        initializePanel();
    }

    public void setRows(Object[][] rows) {
        tableModel.setRows(rows);
    }

    public void setLockDescription(String description) {
        lockArea.setText("Locks activos:\n" + description);
    }

    public void setMetrics(int readyCount, int blockedCount, int newCount, int terminatedCount) {
        readyLabel.setText("Procesos listos: " + readyCount);
        blockedLabel.setText("Procesos bloqueados: " + blockedCount);
        newLabel.setText("Procesos nuevos: " + newCount);
        terminatedLabel.setText("Procesos terminados: " + terminatedCount);
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
        lockArea.setEditable(false);
        lockArea.setRows(5);
        lockArea.setText("Locks activos:\nSin locks activos.");

        JPanel metricsPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        metricsPanel.add(readyLabel);
        metricsPanel.add(blockedLabel);
        metricsPanel.add(newLabel);
        metricsPanel.add(terminatedLabel);

        infoPanel.add(metricsPanel, BorderLayout.NORTH);
        infoPanel.add(new JScrollPane(lockArea), BorderLayout.CENTER);
        return infoPanel;
    }
}
