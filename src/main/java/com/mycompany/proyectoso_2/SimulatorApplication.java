package com.mycompany.proyectoso_2;

import com.mycompany.proyectoso_2.ui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class SimulatorApplication {

    private SimulatorApplication() {
    }

    public static void main(String[] args) {
        installSystemLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }

    private static void installSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            System.err.println("No se pudo aplicar el look and feel del sistema: "
                    + exception.getMessage());
        }
    }
}
