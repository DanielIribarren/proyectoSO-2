package com.mycompany.proyectoso_2.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DiskPanel extends JPanel {

    private static final int BLOCK_COUNT = 64;
    private static final int GRID_COLUMNS = 8;
    private final int[] blockOwners;
    private final Color[] palette;

    public DiskPanel() {
        blockOwners = createSampleBlockOwners();
        palette = new Color[]{
            new Color(234, 234, 234),
            new Color(100, 149, 237),
            new Color(87, 166, 74),
            new Color(230, 157, 67),
            new Color(197, 90, 107)
        };
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Disco simulado"));
        add(new JLabel("Vista inicial del disco para integrar asignacion encadenada."),
                BorderLayout.SOUTH);
        setPreferredSize(new Dimension(760, 560));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        drawBlocks(graphics);
    }

    private void drawBlocks(Graphics graphics) {
        int availableWidth = getWidth() - 40;
        int availableHeight = getHeight() - 70;
        int blockWidth = Math.max(58, availableWidth / GRID_COLUMNS);
        int rows = (BLOCK_COUNT + GRID_COLUMNS - 1) / GRID_COLUMNS;
        int blockHeight = Math.max(42, availableHeight / rows);
        int startX = 20;
        int startY = 30;
        FontMetrics fontMetrics = graphics.getFontMetrics();

        for (int blockIndex = 0; blockIndex < BLOCK_COUNT; blockIndex++) {
            int column = blockIndex % GRID_COLUMNS;
            int row = blockIndex / GRID_COLUMNS;
            int x = startX + column * blockWidth;
            int y = startY + row * blockHeight;
            Color blockColor = palette[blockOwners[blockIndex] % palette.length];

            graphics.setColor(blockColor);
            graphics.fillRoundRect(x, y, blockWidth - 10, blockHeight - 10, 12, 12);
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawRoundRect(x, y, blockWidth - 10, blockHeight - 10, 12, 12);

            String label = Integer.toString(blockIndex);
            int textX = x + (blockWidth - 10 - fontMetrics.stringWidth(label)) / 2;
            int textY = y + (blockHeight - 10 + fontMetrics.getAscent()) / 2 - 4;
            graphics.drawString(label, textX, textY);
        }
    }

    private int[] createSampleBlockOwners() {
        int[] owners = new int[BLOCK_COUNT];
        for (int blockIndex = 0; blockIndex < BLOCK_COUNT; blockIndex++) {
            if (blockIndex >= 11 && blockIndex <= 12) {
                owners[blockIndex] = 1;
            } else if (blockIndex == 34) {
                owners[blockIndex] = 2;
            } else if (blockIndex >= 48 && blockIndex <= 51) {
                owners[blockIndex] = 3;
            } else if (blockIndex >= 56 && blockIndex <= 60) {
                owners[blockIndex] = 4;
            }
        }
        return owners;
    }
}
