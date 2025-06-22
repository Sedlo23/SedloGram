package UI.DockingPanes;

import ModernDocking.internal.DockingProperties;
import javax.swing.*;
import java.awt.*;

public class ModernDockingCustomization {

    public static void setupDockingTheme() {
        // Use the available DockingProperties keys to style related components

        // Titlebar styling (affects the header where auto-hide buttons might be)
        UIManager.put("ModernDocking.titlebar.background.color", new Color(248, 249, 250));
        UIManager.put("ModernDocking.titlebar.border.color", new Color(220, 220, 220));
        UIManager.put("ModernDocking.titlebar.border.enabled", true);
        UIManager.put("ModernDocking.titlebar.border.size", 1);

        // Handle styling (for drag handles)
        UIManager.put("ModernDocking.handles.background", new Color(245, 245, 245));
        UIManager.put("ModernDocking.handles.background.border", new Color(220, 220, 220));
        UIManager.put("ModernDocking.handles.outline", new Color(180, 180, 180));
        UIManager.put("ModernDocking.handles.fill", new Color(255, 255, 255));

        // Overlay styling (for docking indicators)
        UIManager.put("ModernDocking.overlay.color", new Color(0, 123, 255, 75));
        UIManager.put("ModernDocking.overlay.border.color", new Color(0, 123, 255));
        UIManager.put("ModernDocking.overlay.alpha", 75);

        // Custom properties for auto-hide buttons (if ModernDocking supports them)
        UIManager.put("ModernDocking.autoHide.background", new Color(248, 249, 250));
        UIManager.put("ModernDocking.autoHide.foreground", new Color(33, 37, 41));
        UIManager.put("ModernDocking.autoHide.border", new Color(220, 220, 220));
        UIManager.put("ModernDocking.autoHide.selectedBackground", new Color(0, 123, 255, 30));
        UIManager.put("ModernDocking.autoHide.hoverBackground", new Color(230, 230, 230));
    }
}