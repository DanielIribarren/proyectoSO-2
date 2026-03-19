package com.mycompany.proyectoso_2.ui.panel;

import com.mycompany.proyectoso_2.ui.model.AllocationTableModel;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class AllocationTablePanel extends JPanel {

    private final AllocationTableModel tableModel;
    private final JLabel footerLabel;

    public AllocationTablePanel() {
        tableModel = new AllocationTableModel();
        footerLabel = new JLabel("La tabla se actualizara con cada operacion CRUD.");
        initializePanel();
    }

    public void setRows(Object[][] rows) {
        tableModel.setRows(rows);
        footerLabel.setText("Archivos asignados: " + rows.length + ".");
    }

    private void initializePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Tabla de asignacion"));

        JTable allocationTable = new JTable(tableModel);
        allocationTable.setFillsViewportHeight(true);

        add(new JScrollPane(allocationTable), BorderLayout.CENTER);
        add(footerLabel, BorderLayout.SOUTH);
    }
}
