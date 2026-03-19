package com.mycompany.proyectoso_2.persistence;

import com.mycompany.proyectoso_2.model.SchedulingPolicy;
import com.mycompany.proyectoso_2.model.UserMode;

public class SimulationSaveData {

    private final UserMode userMode;
    private final SchedulingPolicy schedulingPolicy;
    private final int headPosition;
    private final SavedDirectory[] directories;
    private final SavedFile[] files;

    public SimulationSaveData(
            UserMode userMode,
            SchedulingPolicy schedulingPolicy,
            int headPosition,
            SavedDirectory[] directories,
            SavedFile[] files
    ) {
        if (userMode == null) {
            throw new IllegalArgumentException("El modo de usuario es obligatorio.");
        }
        if (schedulingPolicy == null) {
            throw new IllegalArgumentException("La politica es obligatoria.");
        }
        this.userMode = userMode;
        this.schedulingPolicy = schedulingPolicy;
        this.headPosition = headPosition;
        this.directories = copyDirectories(directories);
        this.files = copyFiles(files);
    }

    public UserMode getUserMode() {
        return userMode;
    }

    public SchedulingPolicy getSchedulingPolicy() {
        return schedulingPolicy;
    }

    public int getHeadPosition() {
        return headPosition;
    }

    public SavedDirectory[] getDirectories() {
        return copyDirectories(directories);
    }

    public SavedFile[] getFiles() {
        return copyFiles(files);
    }

    private SavedDirectory[] copyDirectories(SavedDirectory[] source) {
        if (source == null) {
            return new SavedDirectory[0];
        }
        SavedDirectory[] copy = new SavedDirectory[source.length];
        for (int index = 0; index < source.length; index++) {
            copy[index] = source[index];
        }
        return copy;
    }

    private SavedFile[] copyFiles(SavedFile[] source) {
        if (source == null) {
            return new SavedFile[0];
        }
        SavedFile[] copy = new SavedFile[source.length];
        for (int index = 0; index < source.length; index++) {
            copy[index] = source[index];
        }
        return copy;
    }
}
