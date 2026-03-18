package com.mycompany.proyectoso_2.ui.panel;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogPanel extends JPanel {

    private final JTextArea textArea;

    public LogPanel(String title) {
        textArea = new JTextArea();
        initializePanel(title);
    }

    private void initializePanel(String title) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(title));

        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    public void appendLine(String line) {
        if (!textArea.getText().isEmpty()) {
            textArea.append(System.lineSeparator());
        }
        textArea.append(line);
    }
}
