package UI.DockingPanes;

import ModernDocking.internal.DockingProperties;
import ModernDocking.ui.DockingHeaderUI;
import ModernDocking.ui.HeaderController;
import ModernDocking.ui.HeaderModel;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class HeaderCustomUI extends JPanel implements DockingHeaderUI, AncestorListener {

    private final HeaderController headerController;
    private final HeaderModel headerModel;

    protected final JLabel titleLabel = new JLabel();
    protected final JButton pinButton = new JButton();
    protected final JButton windowButton = new JButton();
    protected final JButton maximizeButton = new JButton();
    protected final JButton closeButton = new JButton();

    private final List<JButton> customOptionButtons = new ArrayList<>();

    private static final int BUTTON_SIZE = 32;
    private static final int TITLE_FONT_SIZE = 13;

    // Colors for auto-hide (pin) button styling
    private static final Color AUTO_HIDE_BG = new Color(248, 249, 250);
    private static final Color AUTO_HIDE_HOVER = new Color(230, 230, 230);
    private static final Color AUTO_HIDE_ACTIVE = new Color(0, 123, 255, 30);
    private static final Color AUTO_HIDE_BORDER = new Color(220, 220, 220);

    private boolean initialized = false;

    public HeaderCustomUI(HeaderController headerController, HeaderModel headerModel) {
        this.headerController = headerController;
        this.headerModel = headerModel;
        setOpaque(true);

        JComponent component = (JComponent) headerModel.dockable;
        component.addAncestorListener(this);
    }

    private void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        setupModernStyling();
        setupTitleLabel();
        setupActionButtons();
        setupLayout();
        setupThemeListener();
    }

    private void setupModernStyling() {
        setBorder(createModernHeaderBorder());

        Color bgColor = DockingProperties.getTitlebarBackgroundColor();
        if (bgColor == null) {
            bgColor = UIManager.getColor("Panel.background");
        }
        setBackground(bgColor);

        setMinimumSize(new Dimension(0, 40));
        setPreferredSize(new Dimension(0, 40));
    }

    private AbstractBorder createModernHeaderBorder() {
        return new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color borderColor = DockingProperties.getTitlebarBorderColor();
                if (borderColor == null) {
                    borderColor = new Color(230, 230, 230);
                }

                if (DockingProperties.isTitlebarBorderEnabled()) {
                    g2d.setColor(borderColor);
                    g2d.setStroke(new BasicStroke(DockingProperties.getTitlebarBorderSize()));
                    g2d.drawLine(x, y + height - 1, x + width, y + height - 1);
                }
                g2d.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(8, 12, 8, 8);
            }
        };
    }

    private void setupTitleLabel() {
        titleLabel.setText(headerModel.titleText());

        Font titleFont = UIManager.getFont("Label.font");
        if (titleFont == null) {
            titleFont = new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE);
        } else {
            titleFont = titleFont.deriveFont(Font.BOLD, (float) TITLE_FONT_SIZE);
        }
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
    }

    private void setupActionButtons() {
        setupPinButton();
        setupWindowButton();
        setupMaximizeButton();
        setupCloseButton();

        List<JButton> allButtons = List.of(pinButton, windowButton, maximizeButton, closeButton);
        allButtons.forEach(this::applyModernButtonStyling);

        // Apply special styling to the pin button (auto-hide button)
        applyAutoHideButtonStyling(pinButton);
    }

    private void setupPinButton() {
        // Enhanced pin button with better auto-hide visualization
        updatePinButtonAppearance();
        pinButton.setToolTipText("Toggle Auto-Hide");
        pinButton.addActionListener(e -> {
            if (headerModel.isUnpinned()) {
                headerController.pinDockable();
            } else {
                headerController.unpinDockable();
            }
            update();
        });
    }

    private void updatePinButtonAppearance() {
        if (headerModel.isUnpinned()) {
            pinButton.setText("📌"); // Pinned state
            pinButton.setToolTipText("Pin Panel");
        } else {
            pinButton.setText("📍"); // Unpinned (auto-hide) state
            pinButton.setToolTipText("Auto-Hide Panel");
        }
    }

    private void setupWindowButton() {
        windowButton.setText("⧉");
        windowButton.setToolTipText("Float in New Window");
        windowButton.addActionListener(e -> headerController.newWindow());
    }

    private void setupMaximizeButton() {
        maximizeButton.setText("⬜");
        maximizeButton.setToolTipText("Maximize/Restore");
        maximizeButton.addActionListener(e -> {
            if (headerModel.isMaximized()) {
                headerController.minimize();
                maximizeButton.setText("⬜");
                maximizeButton.setToolTipText("Maximize");
            } else {
                headerController.maximize();
                maximizeButton.setText("🗗");
                maximizeButton.setToolTipText("Restore");
            }
        });
    }

    private void setupCloseButton() {
        closeButton.setText("✕");
        closeButton.setToolTipText("Close");
        closeButton.addActionListener(e -> headerController.close());
    }

    private void applyModernButtonStyling(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        Font buttonFont = UIManager.getFont("Button.font");
        if (buttonFont == null) {
            buttonFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        }
        button.setFont(buttonFont);

        Color foregroundColor = UIManager.getColor("Button.foreground");
        if (foregroundColor == null) {
            foregroundColor = new Color(100, 100, 100);
        }
        button.setForeground(foregroundColor);

        Dimension buttonSize = new Dimension(BUTTON_SIZE, BUTTON_SIZE);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void applyAutoHideButtonStyling(JButton button) {
        // Special styling for the auto-hide (pin) button
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setOpaque(true);
                button.setBackground(AUTO_HIDE_HOVER);
                button.setBorder(BorderFactory.createLineBorder(AUTO_HIDE_BORDER, 1));
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setOpaque(false);
                button.setBorder(null);
                button.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(AUTO_HIDE_ACTIVE);
                button.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.contains(e.getPoint())) {
                    button.setBackground(AUTO_HIDE_HOVER);
                } else {
                    button.setOpaque(false);
                    button.setBorder(null);
                }
                button.repaint();
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        add(titlePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        buttonPanel.setOpaque(false);

        customOptionButtons.forEach(buttonPanel::add);

        if (headerModel.isPinnedAllowed()) {
            buttonPanel.add(pinButton);
        }

        if (headerModel.isFloatingAllowed()) {
            buttonPanel.add(windowButton);
        }

        if (headerModel.isMaximizeAllowed()) {
            buttonPanel.add(maximizeButton);
        }

        if (headerModel.isCloseAllowed()) {
            buttonPanel.add(closeButton);
        }

        add(buttonPanel, BorderLayout.EAST);
    }

    private void setupThemeListener() {
        UIManager.addPropertyChangeListener(e -> {
            if ("lookAndFeel".equals(e.getPropertyName()) ||
                    e.getPropertyName().startsWith("ModernDocking.titlebar")) {
                SwingUtilities.invokeLater(this::updateTheme);
            }
        });
    }

    private void updateTheme() {
        Color newBg = DockingProperties.getTitlebarBackgroundColor();
        if (newBg == null) {
            newBg = UIManager.getColor("Panel.background");
        }
        setBackground(newBg);

        titleLabel.setForeground(UIManager.getColor("Label.foreground"));

        Color buttonFg = UIManager.getColor("Button.foreground");
        if (buttonFg != null) {
            pinButton.setForeground(buttonFg);
            windowButton.setForeground(buttonFg);
            maximizeButton.setForeground(buttonFg);
            closeButton.setForeground(buttonFg);
        }

        repaint();
    }

    public void update() {
        if (headerModel.isPinnedAllowed()) {
            updatePinButtonAppearance();
        }

        if (headerModel.isMaximizeAllowed()) {
            if (headerModel.isMaximized()) {
                maximizeButton.setText("🗗");
                maximizeButton.setToolTipText("Restore");
            } else {
                maximizeButton.setText("⬜");
                maximizeButton.setToolTipText("Maximize");
            }
        }

        titleLabel.setText(headerModel.titleText());
        repaint();
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
        init();
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        // No action needed
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
        // No action needed
    }
}