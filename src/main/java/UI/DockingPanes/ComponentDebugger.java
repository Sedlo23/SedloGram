package UI.DockingPanes;

import javax.swing.*;
import java.awt.*;

public class ComponentDebugger {

    public static void debugComponents(JFrame mainFrame) {
        Timer debugTimer = new Timer(3000, e -> {
            SwingUtilities.invokeLater(() -> {
                System.out.println("=== DEBUG: Looking for auto-hide components ===");
                debugComponentsRecursively(mainFrame, 0);
            });
        });
        debugTimer.start();
    }

    private static void debugComponentsRecursively(Container container, int depth) {
        String indent = "  ".repeat(depth);
        Component[] components = container.getComponents();

        for (Component component : components) {
            String info = String.format("%s%s: %s",
                    indent,
                    component.getClass().getSimpleName(),
                    getComponentInfo(component)
            );

            // Look for potential auto-hide related components
            String className = component.getClass().getSimpleName().toLowerCase();
            if (className.contains("auto") || className.contains("hide") ||
                    className.contains("pin") || className.contains("side") ||
                    className.contains("tab") || className.contains("bar")) {

                System.out.println(">>> POTENTIAL AUTO-HIDE: " + info);
            }

            if (component instanceof JButton button) {
                String text = button.getText();
                String tooltip = button.getToolTipText();
                if ((text != null && (text.contains("📌") || text.contains("📍"))) ||
                        (tooltip != null && tooltip.toLowerCase().contains("pin"))) {
                    System.out.println(">>> FOUND PIN BUTTON: " + info);
                }
            }

            if (component instanceof Container && depth < 10) {
                debugComponentsRecursively((Container) component, depth + 1);
            }
        }
    }

    private static String getComponentInfo(Component component) {
        StringBuilder info = new StringBuilder();

        if (component instanceof JButton button) {
            info.append("text='").append(button.getText()).append("'");
            if (button.getToolTipText() != null) {
                info.append(", tooltip='").append(button.getToolTipText()).append("'");
            }
        } else if (component instanceof JLabel label) {
            info.append("text='").append(label.getText()).append("'");
        }

        if (component.getName() != null) {
            info.append(", name='").append(component.getName()).append("'");
        }

        return info.toString();
    }
}