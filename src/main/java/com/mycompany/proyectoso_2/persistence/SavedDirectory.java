package com.mycompany.proyectoso_2.persistence;

import com.mycompany.proyectoso_2.filesystem.EntryVisibility;

public class SavedDirectory {

    private final String path;
    private final String owner;
    private final EntryVisibility visibility;

    public SavedDirectory(String path, String owner, EntryVisibility visibility) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("La ruta del directorio es obligatoria.");
        }
        this.path = path;
        this.owner = owner;
        this.visibility = visibility;
    }

    public String getPath() {
        return path;
    }

    public String getOwner() {
        return owner;
    }

    public EntryVisibility getVisibility() {
        return visibility;
    }
}
