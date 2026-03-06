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
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);

            // Làm cho bảng JTable trông thoáng hơn
            UIManager.put("Table.rowHeight", 35);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("TableHeader.background", new Color(240, 240, 240));

        } catch (Exception ignored) {}
    }
}