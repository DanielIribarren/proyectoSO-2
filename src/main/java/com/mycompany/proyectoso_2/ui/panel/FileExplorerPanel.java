package com.mycompany.proyectoso_2.ui.panel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

public class FileExplorerPanel extends JPanel {

    public FileExplorerPanel() {
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Estructura del sistema de archivos"));

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("/");
        DefaultMutableTreeNode systemNode = new DefaultMutableTreeNode("system");
        DefaultMutableTreeNode usersNode = new DefaultMutableTreeNode("users");
        DefaultMutableTreeNode configNode = new DefaultMutableTreeNode("config.sys");
        DefaultMutableTreeNode readmeNode = new DefaultMutableTreeNode("readme.txt");
        DefaultMutableTreeNode danielNode = new DefaultMutableTreeNode("daniel");
        DefaultMutableTreeNode notesNode = new DefaultMutableTreeNode("notes.txt");

        systemNode.add(readmeNode);
        systemNode.add(configNode);
        danielNode.add(notesNode);
        usersNode.add(danielNode);
        rootNode.add(systemNode);
        rootNode.add(usersNode);

        JTree fileTree = new JTree(rootNode);
        fileTree.setRootVisible(true);

        add(new JScrollPane(fileTree), BorderLayout.CENTER);
        add(buildDetailsPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildDetailsPanel() {
        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        detailsPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        detailsPanel.add(new JLabel("Seleccion actual: readme.txt"));
        detailsPanel.add(new JLabel("Tipo: Archivo"));
        detailsPanel.add(new JLabel("Dueno: system"));
        detailsPanel.add(new JLabel("Tamano: 1 bloque"));
        return detailsPanel;
    }
}
