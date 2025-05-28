package UI.DP;

// ModernMenuBar.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ModernMenuBar extends JMenuBar {

    public ModernMenuBar() {
        setBackground(new Color(45, 45, 48));
        setBorder(BorderFactory.createEmptyBorder());

        // File Menu
        JMenu fileMenu = createStyledMenu("File");
        fileMenu.add(createStyledMenuItem("New Panel", this::createNewPanel));
        fileMenu.add(createStyledMenuItem("Open", null));
        fileMenu.addSeparator();
        fileMenu.add(createStyledMenuItem("Exit", () -> System.exit(0)));

        // View Menu
        JMenu viewMenu = createStyledMenu("View");
        viewMenu.add(createStyledMenuItem("Reset Layout", this::resetLayout));
        viewMenu.add(createStyledMenuItem("Toggle Dark Mode", null));

        // Window Menu
        JMenu windowMenu = createStyledMenu("Window");
        windowMenu.add(createStyledMenuItem("Cascade", null));
        windowMenu.add(createStyledMenuItem("Tile", null));

        add(fileMenu);
        add(viewMenu);
        add(windowMenu);
    }

    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (isSelected()) {
                    g.setColor(new Color(62, 62, 66));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };

        menu.setForeground(Color.WHITE);
        menu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        menu.setBorderPainted(false);
        menu.setOpaque(true);

        // Custom popup menu
        menu.getPopupMenu().setBorder(BorderFactory.createLineBorder(
                new Color(67, 67, 70)));
        menu.getPopupMenu().setBackground(new Color(45, 45, 48));

        return menu;
    }

    private JMenuItem createStyledMenuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(new Color(62, 62, 66));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };

        item.setForeground(Color.WHITE);
        item.setBackground(new Color(45, 45, 48));
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.setBorderPainted(false);

        if (action != null) {
            item.addActionListener(e -> action.run());
        }

        return item;
    }

    private void createNewPanel() {
        // Implementation would create a new panel
    }

    private void resetLayout() {
        // Implementation would reset the layout
    }
}