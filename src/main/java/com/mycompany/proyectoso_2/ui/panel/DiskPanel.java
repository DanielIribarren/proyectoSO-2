package com.mycompany.proyectoso_2.ui.panel;

import com.mycompany.proyectoso_2.disk.DiskBlock;
import com.mycompany.proyectoso_2.disk.SimulatedDisk;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DiskPanel extends JPanel {

    private static final int GRID_COLUMNS = 8;

    private final JLabel summaryLabel;
    private SimulatedDisk disk;
    private int headPosition;

    public DiskPanel() {
        summaryLabel = new JLabel("Disco sin inicializar.");
        initializePanel();
    }

    public void setDiskState(SimulatedDisk disk, int headPosition) {
        this.disk = disk;
        this.headPosition = headPosition;
        if (disk == null) {
            summaryLabel.setText("Disco sin inicializar.");
        } else {
            summaryLabel.setText("Cabezal en " + headPosition
                    + " | libres: " + disk.countFreeBlocks()
                    + " | ocupados: " + disk.countUsedBlocks());
        }
        repaint();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Disco simulado"));
        add(summaryLabel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(760, 560));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (disk == null) {
            return;
        }
        drawBlocks(graphics);
    }

    private void drawBlocks(Graphics graphics) {
        int totalBlocks = disk.getTotalBlocks();
        int availableWidth = getWidth() - 40;
        int availableHeight = getHeight() - 70;
        int blockWidth = Math.max(58, availableWidth / GRID_COLUMNS);
        int rows = (totalBlocks + GRID_COLUMNS - 1) / GRID_COLUMNS;
        int blockHeight = Math.max(42, availableHeight / rows);
        int startX = 20;
        int startY = 30;
        FontMetrics fontMetrics = graphics.getFontMetrics();

        for (int blockIndex = 0; blockIndex < totalBlocks; blockIndex++) {
            int column = blockIndex % GRID_COLUMNS;
            int row = blockIndex / GRID_COLUMNS;
            int x = startX + column * blockWidth;
            int y = startY + row * blockHeight;
            DiskBlock block = disk.getBlock(blockIndex);

            graphics.setColor(resolveBlockColor(block));
            graphics.fillRoundRect(x, y, blockWidth - 10, blockHeight - 10, 12, 12);
            graphics.setColor(blockIndex == headPosition ? new Color(166, 35, 35) : Color.DARK_GRAY);
            graphics.drawRoundRect(x, y, blockWidth - 10, blockHeight - 10, 12, 12);

            String label = block.getIndex() + describeNextBlock(block);
            int textX = x + 8;
            int textY = y + (blockHeight - 10 + fontMetrics.getAscent()) / 2 - 4;
            graphics.drawString(label, textX, textY);

            if (blockIndex == headPosition) {
                graphics.setColor(new Color(166, 35, 35));
                graphics.fillOval(x + blockWidth - 24, y + 6, 10, 10);
            }
        }
    }

    private Color resolveBlockColor(DiskBlock block) {
        if (block.isFree()) {
            return new Color(232, 232, 232);
        }
        int colorId = block.getColorId();
        int red = 70 + (colorId * 53) % 150;
        int green = 90 + (colorId * 83) % 130;
        int blue = 110 + (colorId * 37) % 120;
        return new Color(red, green, blue);
    }

    private String describeNextBlock(DiskBlock block) {
        if (block.isFree()) {
            return "";
        }
        return "->" + block.getNextBlockIndex();
    }
}
