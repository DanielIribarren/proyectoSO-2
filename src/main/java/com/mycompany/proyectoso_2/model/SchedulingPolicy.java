package com.mycompany.proyectoso_2.model;

public enum SchedulingPolicy {
    FIFO("FIFO"),
    SSTF("SSTF"),
    SCAN("SCAN"),
    C_SCAN("C-SCAN");

    private final String label;

    SchedulingPolicy(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
