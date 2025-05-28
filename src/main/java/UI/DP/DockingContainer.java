package UI.DP;

// DockingContainer.java
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class DockingContainer extends JPanel {
    private JSplitPane mainSplitPane;
    private Map<DockingZone, JPanel> dockingZones;
    private List<DockingPanel> dockedPanels;

    public enum DockingZone {
        LEFT, RIGHT, TOP, BOTTOM, CENTER
    }

    public DockingContainer() {
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));
        dockedPanels = new ArrayList<>();
        dockingZones = new HashMap<>();
        initializeLayout();
    }

    private void initializeLayout() {
        // Create docking zones
        JPanel leftZone = createZonePanel();
        JPanel rightZone = createZonePanel();
        JPanel topZone = createZonePanel();
        JPanel bottomZone = createZonePanel();
        JPanel centerZone = createZonePanel();

        dockingZones.put(DockingZone.LEFT, leftZone);
        dockingZones.put(DockingZone.RIGHT, rightZone);
        dockingZones.put(DockingZone.TOP, topZone);
        dockingZones.put(DockingZone.BOTTOM, bottomZone);
        dockingZones.put(DockingZone.CENTER, centerZone);

        // Create split panes for layout
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplit.setLeftComponent(leftZone);
        horizontalSplit.setRightComponent(centerZone);
        horizontalSplit.setDividerLocation(200);
        horizontalSplit.setDividerSize(5);
        horizontalSplit.setContinuousLayout(true);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplit.setLeftComponent(horizontalSplit);
        rightSplit.setRightComponent(rightZone);
        rightSplit.setDividerLocation(600);
        rightSplit.setDividerSize(5);
        rightSplit.setContinuousLayout(true);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplit.setTopComponent(topZone);
        verticalSplit.setBottomComponent(rightSplit);
        verticalSplit.setDividerLocation(100);
        verticalSplit.setDividerSize(5);
        verticalSplit.setContinuousLayout(true);

        JSplitPane bottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomSplit.setTopComponent(verticalSplit);
        bottomSplit.setBottomComponent(bottomZone);
        bottomSplit.setDividerLocation(500);
        bottomSplit.setDividerSize(5);
        bottomSplit.setContinuousLayout(true);

        add(bottomSplit, BorderLayout.CENTER);

        // Style the split panes
        styleSplitPane(horizontalSplit);
        styleSplitPane(rightSplit);
        styleSplitPane(verticalSplit);
        styleSplitPane(bottomSplit);
    }

    private JPanel createZonePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(37, 37, 38));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setMinimumSize(new Dimension(100, 100));
        return panel;
    }

    private void styleSplitPane(JSplitPane splitPane) {
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(new Color(60, 63, 65));
                        g.fillRect(0, 0, getSize().width, getSize().height);

                        // Draw grip
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

                        g2d.setColor(new Color(100, 100, 100));
                        int centerX = getWidth() / 2;
                        int centerY = getHeight() / 2;

                        if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
                            for (int i = -10; i <= 10; i += 5) {
                                g2d.fillOval(centerX - 1, centerY + i - 1, 2, 2);
                            }
                        } else {
                            for (int i = -10; i <= 10; i += 5) {
                                g2d.fillOval(centerX + i - 1, centerY - 1, 2, 2);
                            }
                        }
                    }
                };
            }
        });

        splitPane.setBorder(null);
        splitPane.setBackground(new Color(30, 30, 30));
    }

    public void addPanel(DockingPanel panel) {
        addPanel(panel, DockingZone.CENTER);
    }

    public void addPanel(DockingPanel panel, DockingZone zone) {
        JPanel zonePanel = dockingZones.get(zone);
        if (zonePanel != null) {
            panel.setParentContainer(this);
            zonePanel.add(panel);
            zonePanel.revalidate();
            zonePanel.repaint();
            dockedPanels.add(panel);

            // Animate addition
            animatePanelAddition(panel);
        }
    }

    private void animatePanelAddition(DockingPanel panel) {
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 0));

        Timer animator = new Timer(10, null);
        animator.addActionListener(new ActionListener() {
            int height = 0;
            int targetHeight = 200;
            int step = 10;

            @Override
            public void actionPerformed(ActionEvent e) {
                height += step;
                if (height >= targetHeight) {
                    height = targetHeight;
                    animator.stop();
                }

                panel.setPreferredSize(new Dimension(
                        panel.getPreferredSize().width, height));
                panel.revalidate();
            }
        });
        animator.start();
    }

    void checkDockingZones(DockingPanel panel, Point screenPoint) {
        for (Map.Entry<DockingZone, JPanel> entry : dockingZones.entrySet()) {
            JPanel zonePanel = entry.getValue();
            Point zoneLoc = zonePanel.getLocationOnScreen();
            Rectangle zoneBounds = new Rectangle(
                    zoneLoc.x, zoneLoc.y,
                    zonePanel.getWidth(), zonePanel.getHeight()
            );

            if (zoneBounds.contains(screenPoint)) {
                highlightZone(zonePanel, true);

                // Dock after a short delay
                Timer dockTimer = new Timer(500, e -> {
                    addPanel(panel, entry.getKey());
                    highlightZone(zonePanel, false);
                    panel.dock();
                });
                dockTimer.setRepeats(false);
                dockTimer.start();

                return;
            }
        }
    }

    private void highlightZone(JPanel zone, boolean highlight) {
        if (highlight) {
            zone.setBorder(BorderFactory.createLineBorder(
                    new Color(0, 122, 204), 2));
        } else {
            zone.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
    }
}