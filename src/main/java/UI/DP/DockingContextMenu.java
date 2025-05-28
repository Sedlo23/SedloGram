package UI.DP;

// DockingContextMenu.java
import javax.swing.*;
import java.awt.*;

public class DockingContextMenu extends JPopupMenu {

    public DockingContextMenu(DockingPanel panel) {
        setBackground(new Color(45, 45, 48));
        setBorder(BorderFactory.createLineBorder(new Color(67, 67, 70)));

        add(createMenuItem("Float", () -> panel.startFloating(
                MouseInfo.getPointerInfo().getLocation())));
        add(createMenuItem("Dock Left", () -> movePanelToZone(
                panel, DockingContainer.DockingZone.LEFT)));
        add(createMenuItem("Dock Right", () -> movePanelToZone(
                panel, DockingContainer.DockingZone.RIGHT)));
        add(createMenuItem("Dock Top", () -> movePanelToZone(
                panel, DockingContainer.DockingZone.TOP)));
        add(createMenuItem("Dock Bottom", () -> movePanelToZone(
                panel, DockingContainer.DockingZone.BOTTOM)));
        addSeparator();
        add(createMenuItem("Close", panel::closePanel));
    }

    private JMenuItem createMenuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.setForeground(Color.WHITE);
        item.setBackground(new Color(45, 45, 48));
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.addActionListener(e -> action.run());
        return item;
    }

    private void movePanelToZone(DockingPanel panel, DockingContainer.DockingZone zone) {
        // Implementation to move panel to specific zone
    }
}