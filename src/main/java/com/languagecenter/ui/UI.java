package com.languagecenter.ui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;
import java.awt.*;

public final class UI {
    private UI() {}

    public static void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());

            // Tinh chỉnh Style toàn cục
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);

            // Cấu hình JTable hiện đại
            UIManager.put("Table.rowHeight", 40);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("Table.selectionBackground", new Color(41, 128, 185, 40));
            UIManager.put("Table.selectionForeground", Color.BLACK);
            UIManager.put("TableHeader.background", new Color(245, 245, 245));
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));

        } catch (Exception ignored) {}
    }
}