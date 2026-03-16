package com.mycompany.proyectoso_2.model;

public enum UserMode {
    ADMINISTRADOR("Administrador"),
    USUARIO("Usuario");

    private final String label;

    UserMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
