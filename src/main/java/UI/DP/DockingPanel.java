package UI.DP;

// DockingPanel.java
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class DockingPanel extends JPanel {
    private String title;
    private JPanel titleBar;
    private JPanel contentPanel;
    private boolean isDocked = true;
    private boolean isFloating = false;
    private JFrame floatingFrame;
    private DockingContainer parentContainer;

    public DockingPanel(String title) {
        this.title = title;
        setLayout(new BorderLayout());
        initializeTitleBar();
        initializeContentPanel();
        setupDragAndDrop();
    }

    private void initializeTitleBar() {
        titleBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Modern gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(60, 63, 65),
                        0, getHeight(), new Color(43, 43, 43)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        titleBar.setPreferredSize(new Dimension(0, 30));
        titleBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        buttonPanel.setOpaque(false);

        // Dock/Undock button
        JButton dockButton = createTitleBarButton("⬜");
        dockButton.addActionListener(e -> toggleDocking());

        // Close button
        JButton closeButton = createTitleBarButton("✕");
        closeButton.addActionListener(e -> closePanel());

        buttonPanel.add(dockButton);
        buttonPanel.add(closeButton);

        titleBar.add(titleLabel, BorderLayout.WEST);
        titleBar.add(buttonPanel, BorderLayout.EAST);

        add(titleBar, BorderLayout.NORTH);
    }

    private JButton createTitleBarButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    g.setColor(new Color(80, 80, 80));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };

        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(20, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void initializeContentPanel() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(43, 43, 43));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(contentPanel, BorderLayout.CENTER);
    }

    private void setupDragAndDrop() {
        MouseAdapter dragListener = new MouseAdapter() {
            private Point dragStart;
            private Point originalLocation;

            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                if (isFloating && floatingFrame != null) {
                    originalLocation = floatingFrame.getLocation();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart == null) return;

                if (!isFloating) {
                    // Start floating when dragged
                    Point screenPoint = e.getLocationOnScreen();
                    startFloating(screenPoint);
                } else if (floatingFrame != null) {
                    // Move floating window
                    Point current = e.getLocationOnScreen();
                    int xDiff = current.x - dragStart.x;
                    int yDiff = current.y - dragStart.y;

                    floatingFrame.setLocation(
                            originalLocation.x + xDiff - dragStart.x,
                            originalLocation.y + yDiff - dragStart.y
                    );
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isFloating && parentContainer != null) {
                    Point releasePoint = e.getLocationOnScreen();
                    parentContainer.checkDockingZones(DockingPanel.this, releasePoint);
                }
                dragStart = null;
            }
        };

        titleBar.addMouseListener(dragListener);
        titleBar.addMouseMotionListener(dragListener);
    }

    public void setContent(Component component) {
        contentPanel.removeAll();
        contentPanel.add(component, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void toggleDocking() {
        if (isFloating) {
            dock();
        } else {
            Point screenCenter = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration()
                    .getBounds()
                    .getLocation();
            screenCenter.translate(400, 300);
            startFloating(screenCenter);
        }
    }

    void startFloating(Point location) {
        if (isFloating) return;

        isFloating = true;

        // Create floating frame
        floatingFrame = new JFrame(title);
        floatingFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        floatingFrame.setUndecorated(true);
        floatingFrame.setSize(getSize());
        floatingFrame.setLocation(location);

        // Remove from parent and add to frame
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
            parent.revalidate();
            parent.repaint();
        }

        floatingFrame.add(this);

        // Animate appearance
        animateFloatingFrame(true);

        floatingFrame.setVisible(true);
    }

    void dock() {
        if (!isFloating || parentContainer == null) return;

        isFloating = false;

        // Animate disappearance
        animateFloatingFrame(false);

        Timer timer = new Timer(200, e -> {
            if (floatingFrame != null) {
                floatingFrame.remove(this);
                floatingFrame.dispose();
                floatingFrame = null;
            }

            parentContainer.addPanel(this);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void animateFloatingFrame(boolean appearing) {
        if (floatingFrame == null) return;

        float startOpacity = appearing ? 0.0f : 1.0f;
        float endOpacity = appearing ? 1.0f : 0.0f;

        Timer animator = new Timer(10, null);
        animator.addActionListener(new ActionListener() {
            float opacity = startOpacity;
            float step = (endOpacity - startOpacity) / 20;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += step;

                if ((appearing && opacity >= endOpacity) ||
                        (!appearing && opacity <= endOpacity)) {
                    opacity = endOpacity;
                    animator.stop();
                }

                floatingFrame.setOpacity(opacity);
            }
        });
        animator.start();
    }

    public void closePanel() {
        if (isFloating && floatingFrame != null) {
            floatingFrame.dispose();
        } else if (getParent() != null) {
            Container parent = getParent();
            parent.remove(this);
            parent.revalidate();
            parent.repaint();
        }
    }

    void setParentContainer(DockingContainer container) {
        this.parentContainer = container;
    }
}