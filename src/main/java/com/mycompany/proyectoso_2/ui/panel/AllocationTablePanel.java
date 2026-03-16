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

    public AllocationTablePanel() {
        tableModel = new AllocationTableModel();
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Tabla de asignacion"));

        JTable allocationTable = new JTable(tableModel);
        allocationTable.setFillsViewportHeight(true);

        add(new JScrollPane(allocationTable), BorderLayout.CENTER);
        add(new JLabel("La tabla se actualizara con cada operacion CRUD."),
                BorderLayout.SOUTH);
    }
}
