package UI.DockingPanes;

import ModernDocking.internal.DockingProperties;
import ModernDocking.ui.DockingHeaderUI;
import ModernDocking.ui.HeaderController;
import ModernDocking.ui.HeaderModel;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A modern header UI for dockable components. This class builds a header panel
 * that displays a title and action buttons with a flat, minimal design.
 * It implements {@link DockingHeaderUI} and {@link AncestorListener} to initialize its
 * layout once added to a container.
 */
public class HeaderCustomUI extends JPanel implements DockingHeaderUI, AncestorListener {

    private final HeaderController headerController;
    private final HeaderModel headerModel;

    protected final JLabel titleLabel = new JLabel();
    protected final JButton pinButton = new JButton();
    protected final JButton windowButton = new JButton();
    protected final JButton maximizeButton = new JButton();
    protected final JButton closeButton = new JButton();

    private final List<JButton> customOptionButtons = new ArrayList<>();

    private static final Color HOVER_COLOR = new Color(220, 220, 220);
    private static final int BUTTON_SIZE = 28;
    private static final int ICON_SIZE = 16;

    private boolean initialized = false;

    /**
     * Constructs a new modern HeaderCustomUI using the provided controller and model.
     *
     * @param headerController the controller responsible for header actions
     * @param headerModel      the model containing header data and state
     */
    public HeaderCustomUI(HeaderController headerController, HeaderModel headerModel) {
        this.headerController = headerController;
        this.headerModel = headerModel;
        setOpaque(true);

        // Add AncestorListener to initialize once component is added to a container
        JComponent component = (JComponent) headerModel.dockable;
        component.addAncestorListener(this);
    }

    /**
     * Performs one-time initialization of the header UI components.
     */
    private void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        // Set modern flat look with subtle border
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        // Configure background color
        Color bgColor = DockingProperties.getTitlebarBackgroundColor();
        setBackground(bgColor);

        // Configure title label
        titleLabel.setText(headerModel.titleText());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // Create action buttons with modern icons
        setupActionButtons();

        // Handle custom options
        if (headerModel.hasMoreOptions()) {
            createCustomOptionButtons();
        }

        // Configure layout
        setupLayout();

        // Add listener for Look-and-Feel changes
        UIManager.addPropertyChangeListener(e -> {
            if ("lookAndFeel".equals(e.getPropertyName())
                    || "ModernDocking.titlebar.background".equals(e.getPropertyName())) {
                Color newBg = DockingProperties.getTitlebarBackgroundColor();
                SwingUtilities.invokeLater(() -> setBackground(newBg));
            }
        });
    }

    /**
     * Sets up the action buttons with modern symbols and tooltips.
     */
    private void setupActionButtons() {
        // Pin/Unpin button
        pinButton.setText("📌"); // Unicode pin
        pinButton.setToolTipText("Toggle Pin State");
        pinButton.addActionListener(e -> {
            if (headerModel.isUnpinned()) {
                headerController.pinDockable();
            } else {
                headerController.unpinDockable();
            }
            update();
        });

        // Window button (float into separate window)
        windowButton.setText("⬆"); // Unicode up arrow
        windowButton.setToolTipText("Float in New Window");
        windowButton.addActionListener(e -> headerController.newWindow());

        // Maximize/Restore button
        maximizeButton.setText("⛶"); // Unicode maximize
        maximizeButton.setToolTipText("Maximize/Restore");
        maximizeButton.addActionListener(e -> {
            if (headerModel.isMaximized()) {
                headerController.minimize();
                maximizeButton.setText("⛶");
            } else {
                headerController.maximize();
                maximizeButton.setText("⬇");
            }
        });

        // Close button
        closeButton.setText("✕"); // Unicode X
        closeButton.setToolTipText("Close");
        closeButton.addActionListener(e -> headerController.close());

        // Apply modern button styling to all buttons
        List<JButton> allButtons = List.of(pinButton, windowButton, maximizeButton, closeButton);
        for (JButton button : allButtons) {
            styleModernButton(button);
        }
    }

    /**
     * Creates buttons for any custom options provided by the header model.
     */
    private void createCustomOptionButtons() {
        final JButton[] optionButton = {null};

        // Create a custom action listener to handle model options
    /*    headerModel.addMoreOptionsProvider(actions -> {
            if (actions != null && !actions.isEmpty()) {
                for (Action action : actions) {
                    JButton button = new JButton();

                    // Set button icon/text from action
                    if (action.getValue(Action.SMALL_ICON) != null) {
                        button.setIcon((Icon)action.getValue(Action.SMALL_ICON));
                    } else if (action.getValue(Action.NAME) != null) {
                        button.setText(action.getValue(Action.NAME).toString());
                    }

                    // Set tooltip if available
                    if (action.getValue(Action.SHORT_DESCRIPTION) != null) {
                        button.setToolTipText(action.getValue(Action.SHORT_DESCRIPTION).toString());
                    }

                    button.addActionListener(action);
                    styleModernButton(button);
                    customOptionButtons.add(button);
                }
            }
        });
   */

    }

    /**
     * Applies modern styling to a button.
     *
     * @param button the button to style
     */
    private void styleModernButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFont(button.getFont().deriveFont(13f));
        button.setForeground(new Color(80, 80, 80));

        // Set preferred size for consistent button dimensions
        button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        button.setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        button.setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setOpaque(true);
                button.setBackground(HOVER_COLOR);

                // Special case for close button - red hover
                if (button == closeButton) {
                    button.setBackground(new Color(232, 17, 35));
                    button.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setOpaque(false);
                button.setBackground(getBackground());

                // Reset foreground color for close button
                if (button == closeButton) {
                    button.setForeground(new Color(80, 80, 80));
                }
            }
        });
    }

    /**
     * Sets up the layout of the header components.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());

        // Add title with some padding
        add(titleLabel, BorderLayout.CENTER);

        // Create button panel for right-aligned buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);

        // Add custom option buttons first
        for (JButton button : customOptionButtons) {
            buttonPanel.add(button);
        }

        // Add built-in action buttons
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

    /**
     * Updates the UI elements based on the current state of the header model.
     */
    public void update() {
        // Update pin button state
        if (headerModel.isPinnedAllowed()) {
            pinButton.setText(headerModel.isUnpinned() ? "📌" : "👁️");
            pinButton.setToolTipText(headerModel.isUnpinned() ? "Pin" : "Unpin");
        }

        // Update maximize button state
        if (headerModel.isMaximizeAllowed()) {
            maximizeButton.setText(headerModel.isMaximized() ? "⬇" : "⛶");
        }

        // Update title
        titleLabel.setText(headerModel.titleText());
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