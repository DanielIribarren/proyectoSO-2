package com.mycompany.proyectoso_2.filesystem;

public abstract class FSNode {

    private String name;
    private String owner;
    private EntryVisibility visibility;
    private DirectoryNode parent;

    protected FSNode(String name, String owner, EntryVisibility visibility) {
        validateName(name);
        this.name = name;
        this.owner = owner;
        this.visibility = visibility;
    }

    public abstract FSNodeType getType();

    public String getName() {
        return name;
    }

    public void rename(String newName) {
        validateName(newName);
        name = newName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public EntryVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(EntryVisibility visibility) {
        this.visibility = visibility;
    }

    public DirectoryNode getParent() {
        return parent;
    }

    public void setParent(DirectoryNode parent) {
        this.parent = parent;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public String getPath() {
        if (parent == null) {
            return name;
        }
        if ("/".equals(parent.getPath())) {
            return parent.getPath() + name;
        }
        return parent.getPath() + "/" + name;
    }

    private void validateName(String candidateName) {
        if (candidateName == null || candidateName.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio.");
        }
    }
}
