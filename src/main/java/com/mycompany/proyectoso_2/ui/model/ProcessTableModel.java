package com.mycompany.proyectoso_2.ui.model;

import javax.swing.table.AbstractTableModel;

public class ProcessTableModel extends AbstractTableModel {

    private final String[] columns = {"PID", "Operacion", "Objetivo", "Estado"};
    private Object[][] rows = {
        {1, "READ", "/sistema/readme.txt", "READY"},
        {2, "UPDATE", "/config/config.sys", "BLOCKED"},
        {3, "DELETE", "/tmp/cache.bin", "NEW"}
    };

    @Override
    public int getRowCount() {
        return rows.length;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows[rowIndex][columnIndex];
    }

    public void setRows(Object[][] newRows) {
        if (newRows == null) {
            rows = new Object[0][columns.length];
            fireTableDataChanged();
            return;
        }
        rows = copyRows(newRows);
        fireTableDataChanged();
    }

    private Object[][] copyRows(Object[][] source) {
        Object[][] copy = new Object[source.length][];
        for (int rowIndex = 0; rowIndex < source.length; rowIndex++) {
            copy[rowIndex] = new Object[source[rowIndex].length];
            for (int columnIndex = 0; columnIndex < source[rowIndex].length; columnIndex++) {
                copy[rowIndex][columnIndex] = source[rowIndex][columnIndex];
            }
        }
        return copy;
    }
}
