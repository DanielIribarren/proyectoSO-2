package com.mycompany.proyectoso_2.filesystem;

import com.mycompany.proyectoso_2.structures.SinglyLinkedList;

public class DirectoryNode extends FSNode {

    private final SinglyLinkedList<FSNode> children;

    public DirectoryNode(String name, String owner, EntryVisibility visibility) {
        super(name, owner, visibility);
        children = new SinglyLinkedList<>();
    }

    @Override
    public FSNodeType getType() {
        return FSNodeType.DIRECTORY;
    }

    public int getChildrenCount() {
        return children.size();
    }

    public SinglyLinkedList<FSNode> getChildren() {
        return children;
    }

    public void addChild(FSNode child) {
        validateChild(child);
        child.setParent(this);
        children.addLast(child);
    }

    public FSNode getChildAt(int index) {
        return children.get(index);
    }

    public FSNode removeChildAt(int index) {
        FSNode removedNode = children.removeAt(index);
        removedNode.setParent(null);
        return removedNode;
    }

    public int indexOfChild(String childName) {
        for (int index = 0; index < children.size(); index++) {
            FSNode current = children.get(index);
            if (current.getName().equals(childName)) {
                return index;
            }
        }
        return -1;
    }

    private void validateChild(FSNode child) {
        if (child == null) {
            throw new IllegalArgumentException("El hijo no puede ser nulo.");
        }
        if (child == this) {
            throw new IllegalArgumentException("Un directorio no puede ser hijo de si mismo.");
        }
        if (indexOfChild(child.getName()) >= 0) {
            throw new IllegalArgumentException(
                    "Ya existe un nodo con nombre " + child.getName() + " en este directorio."
            );
        }
    }
}
