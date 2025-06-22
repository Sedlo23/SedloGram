package UI.DockingPanes;

import ModernDocking.Dockable;
import ModernDocking.RootDockingPanel;
import ModernDocking.internal.DockableToolbar;
import ModernDocking.internal.DockableWrapper;
import ModernDocking.internal.DockedUnpinnedPanel;
import ModernDocking.internal.DockingInternal;
import ModernDocking.util.CombinedIcon;
import ModernDocking.util.RotatedIcon;
import ModernDocking.util.TextIcon;
import ModernDocking.util.UnselectableButtonGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DockableToolbarCostume extends DockableToolbar implements ComponentListener {

    // Our own tracking since parent fields are private
    private static class Entry {
        private final Dockable dockable;
        private final JToggleButton button;
        private final DockedUnpinnedPanel panel;

        private Entry(Dockable dockable, JToggleButton button, DockedUnpinnedPanel panel) {
            this.dockable = dockable;
            this.button = button;
            this.panel = panel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(dockable, entry.dockable);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dockable);
        }
    }

    private final List<Entry> customDockables = new ArrayList<>();
    private final UnselectableButtonGroup customButtonGroup = new UnselectableButtonGroup();

    RootDockingPanel root;
    Window window;

    public DockableToolbarCostume(Window window, RootDockingPanel root, Location location) {
        super(window, root, location);
        this.root = root;
        this.window = window;

        // Apply FlatLaf styling to the toolbar container
        applyFlatLafToolbarStyling();
    }

    /**
     * Apply FlatLaf color scheme to the toolbar container
     */
    private void applyFlatLafToolbarStyling() {
        // Use FlatLaf's existing colors
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createLineBorder(
                UIManager.getColor("Component.borderColor") != null ?
                        UIManager.getColor("Component.borderColor") :
                        UIManager.getColor("TextField.borderColor"), 1));
    }

    @Override
    public void addDockable(Dockable dockable) {
        if (!hasCustomDockable(dockable)) {
            DockableWrapper wrapper = DockingInternal.getWrapper(dockable);

            // Create the toggle button with FlatLaf styling
            JToggleButton button = createFlatLafStyledToggleButton(dockable, wrapper);

            DockedUnpinnedPanel panel = new DockedUnpinnedPanel(dockable, root, this);
            wrapper.setWindow(window);

            // Update all the buttons and panels
            button.addActionListener(e -> updateCustomButtons());

            // Add to our custom tracking
            customButtonGroup.add(button);
            customDockables.add(new Entry(dockable, button, panel));

            JLayeredPane layeredPane;
            if (window instanceof JFrame) {
                layeredPane = ((JFrame) window).getLayeredPane();
            } else {
                layeredPane = ((JDialog) window).getLayeredPane();
            }

            layeredPane.add(panel, root.getPinningLayer());

            // Call parent's addDockable to maintain compatibility
            try {
                // First add to parent's tracking using reflection
                addToParentTracking(dockable, button, panel);
            } catch (Exception e) {
                System.err.println("Warning: Could not add to parent tracking: " + e.getMessage());
            }

            createCustomContents();
        }
    }

    private void addToParentTracking(Dockable dockable, JToggleButton button, DockedUnpinnedPanel panel) throws Exception {
        // Use reflection to access parent's private fields
        Field dockablesField = DockableToolbar.class.getDeclaredField("dockables");
        dockablesField.setAccessible(true);
        List parentDockables = (List) dockablesField.get(this);

        Field buttonGroupField = DockableToolbar.class.getDeclaredField("buttonGroup");
        buttonGroupField.setAccessible(true);
        UnselectableButtonGroup parentButtonGroup = (UnselectableButtonGroup) buttonGroupField.get(this);

        // Create parent's Entry object using reflection
        Class<?> entryClass = null;
        for (Class<?> declaredClass : DockableToolbar.class.getDeclaredClasses()) {
            if (declaredClass.getSimpleName().equals("Entry")) {
                entryClass = declaredClass;
                break;
            }
        }

        if (entryClass != null) {
            Object parentEntry = entryClass.getDeclaredConstructor(Dockable.class, JToggleButton.class, DockedUnpinnedPanel.class)
                    .newInstance(dockable, button, panel);
            parentDockables.add(parentEntry);
            parentButtonGroup.add(button);
        }
    }

    private boolean hasCustomDockable(Dockable dockable) {
        return customDockables.stream()
                .anyMatch(entry -> entry.dockable.equals(dockable));
    }

    /**
     * Creates a toggle button styled with FlatLaf colors
     */
    private JToggleButton createFlatLafStyledToggleButton(Dockable dockable, DockableWrapper wrapper) {
        JToggleButton button = new JToggleButton();

        // Apply FlatLaf styling first
        applyFlatLafButtonStyling(button);

        // Set icon and text based on orientation
        button.setIcon(dockable.getIcon());

        if (isVertical()) {
            TextIcon textIcon = new TextIcon(button, dockable.getTabText(), TextIcon.Layout.HORIZONTAL);
            RotatedIcon rotatedIcon = new RotatedIcon(textIcon,
                    getDockedLocation() == Location.WEST ? RotatedIcon.Rotate.UP : RotatedIcon.Rotate.DOWN);

            if (wrapper.getDockable().getIcon() != null) {
                button.setIcon(new CombinedIcon(wrapper.getDockable().getIcon(), rotatedIcon));
            } else {
                button.setIcon(rotatedIcon);
            }

            Insets insets = UIManager.getInsets("Button.margin");
            Insets margin = new Insets(insets.left, insets.top, insets.left, insets.top);
            button.setMargin(margin);
        } else {
            button.setText(dockable.getTabText());
        }

        return button;
    }

    /**
     * Apply FlatLaf styling to auto-hide toggle buttons
     */
    private void applyFlatLafButtonStyling(JToggleButton button) {
        // Use FlatLaf's existing color scheme
        Color buttonBg = UIManager.getColor("Button.background");
        Color buttonFg = UIManager.getColor("Button.foreground");
        Color borderColor = UIManager.getColor("Component.borderColor");
        Color hoverBg = UIManager.getColor("Button.hoverBackground");
        Color selectedBg = UIManager.getColor("Button.selectedBackground");
        Color focusColor = UIManager.getColor("Component.focusColor");

        // Fallbacks if colors are null
        if (buttonBg == null) buttonBg = UIManager.getColor("Panel.background");
        if (buttonFg == null) buttonFg = UIManager.getColor("Label.foreground");
        if (borderColor == null) borderColor = UIManager.getColor("TextField.borderColor");
        if (hoverBg == null) hoverBg = UIManager.getColor("Button.background").brighter();
        if (selectedBg == null) selectedBg = UIManager.getColor("Button.background").darker();
        if (focusColor == null) focusColor = UIManager.getColor("Component.focusedBorderColor");

        // Apply FlatLaf colors
        button.setBackground(buttonBg);
        button.setForeground(buttonFg);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Use FlatLaf font
        Font buttonFont = UIManager.getFont("Button.font");
        if (buttonFont != null) {
            button.setFont(buttonFont.deriveFont(Font.BOLD, buttonFont.getSize() *1.2f));
        }

        // Store colors for hover effects
        final Color finalButtonBg = buttonBg;
        final Color finalHoverBg = hoverBg;
        final Color finalSelectedBg = selectedBg;
        final Color finalBorderColor = borderColor;
        final Color finalFocusColor = focusColor;

        // Add FlatLaf-style hover and selection effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.isSelected()) {
                    button.setBackground(finalHoverBg);
                    button.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(finalFocusColor != null ? finalFocusColor : finalBorderColor, 1),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)
                    ));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.isSelected()) {
                    button.setBackground(finalButtonBg);
                    button.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(finalBorderColor, 1),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)
                    ));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                updateButtonSelectionStyle(button);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                updateButtonSelectionStyle(button);
            }
        });

        // Add selection change listener for selected state styling
        button.addChangeListener(e -> updateButtonSelectionStyle(button));

        System.out.println("✅ Styled auto-hide tab with FlatLaf colors: " +
                (button.getText().isEmpty() ? "Icon Button" : button.getText()));
    }

    /**
     * Update button appearance based on selection state using FlatLaf colors
     */
    private void updateButtonSelectionStyle(JToggleButton button) {
        Color selectedBg = UIManager.getColor("Button.selectedBackground");
        Color selectedFg = UIManager.getColor("Button.selectedForeground");
        Color borderColor = UIManager.getColor("Component.borderColor");
        Color focusColor = UIManager.getColor("Component.focusedBorderColor");
        Color buttonBg = UIManager.getColor("Button.background");
        Color buttonFg = UIManager.getColor("Button.foreground");

        // Fallbacks
        if (selectedBg == null) selectedBg = UIManager.getColor("List.selectionBackground");
        if (selectedFg == null) selectedFg = UIManager.getColor("List.selectionForeground");
        if (borderColor == null) borderColor = UIManager.getColor("TextField.borderColor");
        if (focusColor == null) focusColor = UIManager.getColor("Component.focusedBorderColor");
        if (buttonBg == null) buttonBg = UIManager.getColor("Panel.background");
        if (buttonFg == null) buttonFg = UIManager.getColor("Label.foreground");

        if (button.isSelected()) {
            button.setBackground(selectedBg);
            button.setForeground(selectedFg);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(focusColor != null ? focusColor : borderColor, 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
        } else {
            button.setBackground(buttonBg);
            button.setForeground(buttonFg);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        }
    }

    /**
     * Update our custom buttons
     */
    private void updateCustomButtons() {
        for (Entry entry : customDockables) {
            entry.panel.setVisible(customButtonGroup.getSelection() == entry.button.getModel());
        }

        // Also try to call parent's updateButtons if possible
        try {
            Method updateButtonsMethod = DockableToolbar.class.getDeclaredMethod("updateButtons");
            updateButtonsMethod.setAccessible(true);
            updateButtonsMethod.invoke(this);
        } catch (Exception e) {
            // Ignore if we can't access parent method
        }

        // Update styling for all buttons after state change
        for (Entry entry : customDockables) {
            updateButtonSelectionStyle(entry.button);
        }
    }

    /**
     * Create contents using our custom tracking
     */
    private void createCustomContents() {
        removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        for (Entry entry : customDockables) {
            add(entry.button, gbc);

            if (isVertical()) {
                gbc.gridy++;
            } else {
                gbc.gridx++;
            }
        }

        if (isVertical()) {
            gbc.weighty = 1.0;
        } else {
            gbc.weightx = 1.0;
        }
        add(new JLabel(""), gbc);

        revalidate();
        repaint();
    }

    @Override
    public void removeDockable(Dockable dockable) {
        Entry toRemove = null;
        for (Entry entry : customDockables) {
            if (entry.dockable == dockable) {
                toRemove = entry;

                JLayeredPane layeredPane;
                if (window instanceof JFrame) {
                    layeredPane = ((JFrame) window).getLayeredPane();
                } else {
                    layeredPane = ((JDialog) window).getLayeredPane();
                }
                layeredPane.remove(entry.panel);
                break;
            }
        }

        if (toRemove != null) {
            customDockables.remove(toRemove);
            createCustomContents();
        }

        super.removeDockable(dockable);
    }

    @Override
    public boolean hasDockable(Dockable dockable) {
        return hasCustomDockable(dockable) || super.hasDockable(dockable);
    }

    @Override
    public boolean shouldDisplay() {
        return customDockables.size() > 0 || super.shouldDisplay();
    }

    @Override
    public void hideAll() {
        customButtonGroup.setSelected(customButtonGroup.getSelection(), false);
        updateCustomButtons();
        super.hideAll();
    }

    @Override
    public List<String> getPersistentIDs() {
        List<String> ids = new ArrayList<>();
        for (Entry entry : customDockables) {
            ids.add(entry.dockable.getPersistentID());
        }
        ids.addAll(super.getPersistentIDs());
        return ids;
    }

    /**
     * Update colors when theme changes
     */
    public void updateFlatLafColors() {
        applyFlatLafToolbarStyling();
        for (Entry entry : customDockables) {
            applyFlatLafButtonStyling(entry.button);
        }
        repaint();
    }
}