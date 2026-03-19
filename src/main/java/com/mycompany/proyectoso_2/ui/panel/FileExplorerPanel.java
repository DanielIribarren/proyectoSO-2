package com.mycompany.proyectoso_2.ui.panel;

import com.mycompany.proyectoso_2.filesystem.DirectoryNode;
import com.mycompany.proyectoso_2.filesystem.EntryVisibility;
import com.mycompany.proyectoso_2.filesystem.FSNode;
import com.mycompany.proyectoso_2.filesystem.FSNodeType;
import com.mycompany.proyectoso_2.filesystem.FileNode;
import com.mycompany.proyectoso_2.filesystem.FileSystemTree;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class FileExplorerPanel extends JPanel {

    private final DefaultMutableTreeNode rootNode;
    private final DefaultTreeModel treeModel;
    private final JTree fileTree;
    private final JLabel selectedPathLabel;
    private final JLabel typeLabel;
    private final JLabel ownerLabel;
    private final JLabel visibilityLabel;
    private final JLabel sizeLabel;
    private Consumer<String> selectionListener;

    public FileExplorerPanel() {
        rootNode = new DefaultMutableTreeNode("/");
        treeModel = new DefaultTreeModel(rootNode);
        fileTree = new JTree(treeModel);
        selectedPathLabel = new JLabel("Seleccion actual: /");
        typeLabel = new JLabel("Tipo: Directorio");
        ownerLabel = new JLabel("Dueno: system");
        visibilityLabel = new JLabel("Visibilidad: SYSTEM");
        sizeLabel = new JLabel("Tamano: -");
        initializePanel();
    }

    public void setFileSystem(FileSystemTree fileSystemTree) {
        String selectedPath = getSelectedPath();
        rootNode.removeAllChildren();
        rootNode.setUserObject(fileSystemTree.getRoot());
        rebuildTree(fileSystemTree.getRoot(), rootNode);
        treeModel.reload();
        expandRoot();

        if (selectedPath != null) {
            selectPath(selectedPath);
        } else {
            updateDetails(fileSystemTree.getRoot());
        }
    }

    public String getSelectedPath() {
        TreePath selectionPath = fileTree.getSelectionPath();
        if (selectionPath == null) {
            return "/";
        }
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        Object userObject = selectedNode.getUserObject();
        if (userObject instanceof FSNode fsNode) {
            return fsNode.getPath();
        }
        return "/";
    }

    public void selectPath(String targetPath) {
        DefaultMutableTreeNode matchingNode = findTreeNode(rootNode, targetPath);
        if (matchingNode == null) {
            fileTree.clearSelection();
            return;
        }

        TreePath selectionPath = new TreePath(matchingNode.getPath());
        fileTree.setSelectionPath(selectionPath);
        fileTree.scrollPathToVisible(selectionPath);
    }

    public void setSelectionListener(Consumer<String> selectionListener) {
        this.selectionListener = selectionListener;
    }

    private void initializePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Estructura del sistema de archivos"));

        fileTree.setRootVisible(true);
        fileTree.addTreeSelectionListener(this::handleSelectionChange);

        add(new JScrollPane(fileTree), BorderLayout.CENTER);
        add(buildDetailsPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildDetailsPanel() {
        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        detailsPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        detailsPanel.add(selectedPathLabel);
        detailsPanel.add(typeLabel);
        detailsPanel.add(ownerLabel);
        detailsPanel.add(visibilityLabel);
        detailsPanel.add(sizeLabel);
        return detailsPanel;
    }

    private void handleSelectionChange(TreeSelectionEvent event) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        Object userObject = selectedNode.getUserObject();
        if (userObject instanceof FSNode fsNode) {
            updateDetails(fsNode);
            if (selectionListener != null) {
                selectionListener.accept(fsNode.getPath());
            }
        }
    }

    private void updateDetails(FSNode node) {
        selectedPathLabel.setText("Seleccion actual: " + node.getPath());
        typeLabel.setText("Tipo: " + describeType(node.getType()));
        ownerLabel.setText("Dueno: " + node.getOwner());
        visibilityLabel.setText("Visibilidad: " + describeVisibility(node.getVisibility()));
        sizeLabel.setText("Tamano: " + describeSize(node));
    }

    private void rebuildTree(FSNode currentNode, DefaultMutableTreeNode treeNode) {
        if (currentNode.getType() != FSNodeType.DIRECTORY) {
            return;
        }

        DirectoryNode directory = (DirectoryNode) currentNode;
        for (int index = 0; index < directory.getChildrenCount(); index++) {
            FSNode child = directory.getChildAt(index);
            DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(child);
            treeNode.add(childTreeNode);
            rebuildTree(child, childTreeNode);
        }
    }

    private DefaultMutableTreeNode findTreeNode(DefaultMutableTreeNode treeNode, String targetPath) {
        Object userObject = treeNode.getUserObject();
        if (userObject instanceof FSNode fsNode && fsNode.getPath().equals(targetPath)) {
            return treeNode;
        }

        for (int index = 0; index < treeNode.getChildCount(); index++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) treeNode.getChildAt(index);
            DefaultMutableTreeNode matchingNode = findTreeNode(childNode, targetPath);
            if (matchingNode != null) {
                return matchingNode;
            }
        }
        return null;
    }

    private void expandRoot() {
        TreeNode rootTreeNode = (TreeNode) treeModel.getRoot();
        fileTree.expandPath(new TreePath(((DefaultMutableTreeNode) rootTreeNode).getPath()));
    }

    private String describeType(FSNodeType nodeType) {
        if (nodeType == FSNodeType.FILE) {
            return "Archivo";
        }
        return "Directorio";
    }

    private String describeVisibility(EntryVisibility visibility) {
        return switch (visibility) {
            case PRIVATE -> "PRIVATE";
            case PUBLIC -> "PUBLIC";
            case SYSTEM -> "SYSTEM";
        };
    }

    private String describeSize(FSNode node) {
        if (node.getType() != FSNodeType.FILE) {
            return "-";
        }
        FileNode file = (FileNode) node;
        return file.getSizeInBlocks() + " bloques";
    }
}
