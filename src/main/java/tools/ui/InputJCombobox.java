package tools.ui;

import static tools.string.StringHelper.isNumeric;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A specialized document for enabling auto-completion behavior
 * within a {@link JComboBox}.
 */
public class InputJCombobox extends PlainDocument {

    private static final long serialVersionUID = 1L;

    // Static map to store country flags - loaded only once
    private static final Map<String, ImageIcon> countryFlags = new HashMap<>();

    // Flag to track if icons have been loaded
    private static boolean iconsLoaded = false;

    /**
     * The target combo box that uses this document for auto-completion.
     */
    private final JComboBox<String> comboBox;

    /**
     * The underlying combo box model used to retrieve items.
     */
    private final ComboBoxModel<String> comboBoxModel;

    /**
     * The text editor component of the combo box.
     */
    private JTextComponent textEditor;

    /**
     * Flag used to indicate that we are in the middle of a programmatic selection.
     */
    private boolean selecting = false;

    /**
     * Whether the popup should be hidden when focus is lost.
     */
    private boolean hidePopupOnFocusLoss;

    /**
     * Tracks if the BACKSPACE key was pressed.
     */
    private boolean hitBackspace = false;

    /**
     * Tracks if BACKSPACE was pressed while a text selection was active.
     */
    private boolean hitBackspaceOnSelection;

    /**
     * The original ComboBox editor
     */
    private ComboBoxEditor originalEditor;

    /**
     * Constructs a new {@code InputJCombobox} associated with the given combo box.
     *
     * @param comboBox the combo box to be made auto-completable
     */
    public InputJCombobox(final JComboBox<String> comboBox) {
        this.comboBox = Objects.requireNonNull(comboBox, "comboBox cannot be null");
        this.comboBoxModel = comboBox.getModel();

        // Ensure the combo box is editable
        comboBox.setEditable(true);

        // Store the original editor
        originalEditor = comboBox.getEditor();

        // Get the text component
        Component comp = originalEditor.getEditorComponent();
        if (comp instanceof JTextComponent) {
            textEditor = (JTextComponent) comp;
            textEditor.setDocument(this);
        } else {
            // Try to look through the component hierarchy to find a text component
            textEditor = findTextComponentIn(comp);
            if (textEditor != null) {
                textEditor.setDocument(this);
            } else {
                System.err.println("Could not find a text component in the editor");
                return;
            }
        }

        // Load country flags if not already loaded
        if (!iconsLoaded) {
            loadCountryFlags();
            iconsLoaded = true;
        }

        // Replace the editor with our custom editor
        setupCustomEditor();

        // Set preferred sizing for consistency
        comboBox.setPreferredSize(new Dimension(165, 25));
        comboBox.setMinimumSize(new Dimension(165, 25));
        comboBox.setMaximumSize(new Dimension(165, 25));

        // Add a listener to highlight text after selection changes
        comboBox.addActionListener(e -> {
            if (!selecting) {
                highlightCompletedText(0);
            }
        });

        // Key listener to manage special handling of BACKSPACE and DELETE
        textEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Show popup if typed while combo is displayable
                if (comboBox.isDisplayable()) {
                    comboBox.setPopupVisible(true);
                }
                hitBackspace = false;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_BACK_SPACE -> {
                        hitBackspace = true;
                        hitBackspaceOnSelection = (textEditor.getSelectionStart() != textEditor.getSelectionEnd());
                    }
                    case KeyEvent.VK_DELETE -> {
                        // Consume DELETE to prevent unwanted text removal
                        e.consume();
                        comboBox.getToolkit().beep();
                    }
                    default -> {
                        // no-op
                    }
                }
            }
        });

        // Focus listener to highlight all text on focus gain
        textEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                highlightCompletedText(0);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (hidePopupOnFocusLoss) {
                    comboBox.setPopupVisible(false);
                }
            }
        });

        // Initialize the text with the combo's currently selected item
        Object selected = comboBox.getSelectedItem();
        if (selected != null) {
            setText(selected.toString());
        }
        highlightCompletedText(0);

        // Adjust the maximum row count based on items (excluding "NOT_USED")
        int visibleItemCount = 0;
        int maxIndex = 0;
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            if (item != null && !item.contains("NOT_USED")) {
                visibleItemCount++;
            }
            maxIndex = i; // track maximum available index
            if (visibleItemCount > 10) {
                break;
            }
        }
        comboBox.setMaximumRowCount(Math.max(maxIndex, 10));

        // Set a custom renderer for the dropdown list
        comboBox.setRenderer(new CountryFlagRenderer());
    }

    /**
     * Find a JTextComponent inside a component
     */
    private JTextComponent findTextComponentIn(Component comp) {
        if (comp instanceof JTextComponent) {
            return (JTextComponent) comp;
        } else if (comp instanceof Container) {
            Container container = (Container) comp;
            for (Component child : container.getComponents()) {
                JTextComponent textComp = findTextComponentIn(child);
                if (textComp != null) {
                    return textComp;
                }
            }
        }
        return null;
    }

    /**
     * Set up a custom editor with a flag
     */
    private void setupCustomEditor() {
        comboBox.setEditor(new ComboBoxEditor() {
            // A panel to hold the flag and text editor
            private final JPanel panel = new JPanel(new BorderLayout(5, 0));

            // Label for the flag
            private final JLabel flagLabel = new JLabel();

            {
                // Initialize the panel
                panel.setOpaque(false);
                flagLabel.setPreferredSize(new Dimension(20, 12));
                panel.add(flagLabel, BorderLayout.WEST);
                panel.add(textEditor, BorderLayout.CENTER);

                // Update the flag when initialized
                updateFlag();
            }

            @Override
            public Component getEditorComponent() {
                return panel;
            }

            @Override
            public void setItem(Object item) {
                originalEditor.setItem(item);
                updateFlag();
            }

            @Override
            public Object getItem() {
                return originalEditor.getItem();
            }

            @Override
            public void selectAll() {
                originalEditor.selectAll();
            }

            @Override
            public void addActionListener(ActionListener l) {
                originalEditor.addActionListener(l);
            }

            @Override
            public void removeActionListener(ActionListener l) {
                originalEditor.removeActionListener(l);
            }

            /**
             * Update the flag icon based on the selected item
             */
            private void updateFlag() {
                Object item = getItem();
                if (item != null) {
                    String text = item.toString();
                    if (!text.equals("NOT_USED")) {
                        // Try to match any of our country codes within the text
                        for (Map.Entry<String, ImageIcon> entry : countryFlags.entrySet()) {
                            String countryCode = entry.getKey();

                            boolean b = false;

                            for (String s:text.split(" "))
                            {
                                if (s.compareTo(countryCode) == 0) {b = true;}
                            }

                            if (text.compareTo(countryCode) == 0) {b = true;}



                            if (b) {
                                ImageIcon icon = entry.getValue();
                                if (icon != null) {
                                    flagLabel.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
                                    flagLabel.setIcon(icon);
                                    flagLabel.setVisible(true);
                                    return;
                                }
                            }
                        }
                    }
                }

                // No valid flag to show
                flagLabel.setIcon(null);
                flagLabel.setVisible(false);
            }
        });
    }

    /**
     * Renderer for displaying country flags in the dropdown
     */
    public class CountryFlagRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        /**
         * A panel with zero preferred size, used to hide certain items.
         */
        private final JPanel hiddenPanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(0, 0);
            }
        };

        @Override
        public Component getListCellRendererComponent(JList<?> list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {

            // Default rendering (colors, selection, etc.)
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                return label;
            }

            String text = value.toString();

            // Hide "NOT_USED" entries entirely
            if (text.contains("NOT_USED")) {
                return hiddenPanel;
            }

            // Dim out lines containing '?'
            if (text.contains("?")) {
                label.setForeground(new JTextArea().getDisabledTextColor());
            }

            // If not numeric, prefix the label with the current index
            if (!isNumeric(text)) {
                label.setText(index + " - " + text);
            }

            // Add flag icon if applicable
            if (value != null && value.toString().length() > 0) {
                if (!text.equals("NOT_USED")) {
                    // Check for any country code in the text
                    for (Map.Entry<String, ImageIcon> entry : countryFlags.entrySet()) {
                        String countryCode = entry.getKey();
                        Boolean b = false;

                        for (String s:text.split(" "))
                        {
                            if (s.compareTo(countryCode) == 0) {b = true;}
                        }
                        if (text.compareTo(countryCode) == 0) {b = true;}
                        if (b) {
                            ImageIcon icon = entry.getValue();
                            if (icon != null) {
                                label.setIcon(icon);
                                label.setIconTextGap(4);
                                break;  // Use the first match
                            }
                        }
                    }
                }
            }

            return label;
        }
    }

    private static boolean isDarkColor(Color color) {
        // Calculate perceived brightness using common formula
        double brightness = (0.299 * color.getRed() +
                0.587 * color.getGreen() +
                0.114 * color.getBlue()) / 255;

        // If brightness is less than 0.5, consider it dark
        return brightness < 0.5;
    }

    /**
     * Loads country flags with properly scaled dimensions
     * This method is called only once for the class
     */
    private static void loadCountryFlags() {
        try {
            // Default text height if no component is available
            int textHeight = 16;

            // Function to load and scale an image
            java.util.function.Function<String, ImageIcon> loadAndScaleIcon = (path) -> {
                ImageIcon originalIcon = new ImageIcon(GUIHelper.class.getClassLoader().getResource(path));
                Image originalImage = originalIcon.getImage();

                // Calculate width to maintain aspect ratio
                int originalWidth = originalIcon.getIconWidth();
                int originalHeight = originalIcon.getIconHeight();

                // Avoid division by zero
                if (originalHeight <= 0) return originalIcon;

                // Calculate new width to maintain aspect ratio
                int newWidth = (int) ((double) originalWidth / originalHeight * (textHeight));

                // Scale the image
                Image scaledImage = originalImage.getScaledInstance(newWidth, textHeight, Image.SCALE_SMOOTH);

                BufferedImage bufferedImage;

                if (scaledImage instanceof BufferedImage) {
                    bufferedImage = (BufferedImage) scaledImage;
                } else {
                    // Create a buffered image with transparency
                    bufferedImage = new BufferedImage(
                            originalWidth,
                            originalHeight,
                            BufferedImage.TYPE_INT_ARGB);

                    // Draw the image on to the buffered image
                    Graphics2D g2d = bufferedImage.createGraphics();
                    g2d.drawImage(originalImage, 0, 0, null);
                    g2d.dispose();
                }

                // Create a new transparent image
                BufferedImage transparentImage = new BufferedImage(
                        bufferedImage.getWidth(),
                        bufferedImage.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);

                // Loop through each pixel
                // Get the background color of the JLabel
                Color backgroundColor = new JLabel().getBackground();

// Determine if the background is dark
                boolean isDarkBackground = isDarkColor(backgroundColor);

                boolean isCountry =
                                 path.contains("flags/pt.png") ||
                                 path.contains("flags/lu.png") ||
                                 path.contains("flags/de.png") ||
                                 path.contains("flags/fr.png") ||
                                 path.contains("flags/be.png") ||
                                 path.contains("flags/it.png") ||
                                 path.contains("flags/fi.png") ||
                                 path.contains("flags/pl.png") ||
                                 path.contains("flags/dk.png") ||
                                 path.contains("flags/es.png") ||
                                 path.contains("flags/at.png") ||
                                 path.contains("flags/ie.png") ||
                                 path.contains("flags/cz.png") ||
                                 path.contains("flags/england.png");



                for (int x = 0; x < bufferedImage.getWidth(); x++)
                {
                    for (int y = 0; y < bufferedImage.getHeight(); y++) {
                        int rgb = bufferedImage.getRGB(x, y);
                        Color color = new Color(rgb, true);



                        {
                            // For light backgrounds, make white pixels transparent
                            if (color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255 && !isCountry) {
                                // Make it transparent
                                transparentImage.setRGB(x, y, new Color(255, 255, 255, 0).getRGB());
                            } else {
                                // Keep the original color
                                transparentImage.setRGB(x, y, rgb);
                            }



                            if (isDarkBackground && !isCountry) {

                                 rgb = transparentImage.getRGB(x, y);
                                 color = new Color(rgb, true);

                                 // For dark backgrounds, make black pixels white

                                if (color.getRed() <= 1 && color.getGreen() <= 1 && color.getBlue() <= 1 && color.getAlpha() != 0)
                                {
                                    // Make it white
                                    transparentImage.setRGB(x, y, Color.WHITE.getRGB());
                                } else {
                                    // Keep the original color
                                    transparentImage.setRGB(x, y, rgb);
                                }
                            }


                        }
                    }
                }

                scaledImage = transparentImage.getScaledInstance(newWidth, textHeight, Image.SCALE_SMOOTH);


                return new ImageIcon(scaledImage);
            };

            // Map country codes to their scaled flag icons
            countryFlags.put("ENG", loadAndScaleIcon.apply("flags/england.png"));
            countryFlags.put("POR", loadAndScaleIcon.apply("flags/pt.png"));
            countryFlags.put("LUX", loadAndScaleIcon.apply("flags/lu.png"));
            countryFlags.put("GER", loadAndScaleIcon.apply("flags/de.png"));
            countryFlags.put("FRA", loadAndScaleIcon.apply("flags/fr.png"));
            countryFlags.put("BEL", loadAndScaleIcon.apply("flags/be.png"));
            countryFlags.put("ITA", loadAndScaleIcon.apply("flags/it.png"));
            countryFlags.put("FIN", loadAndScaleIcon.apply("flags/fi.png"));
            countryFlags.put("POL", loadAndScaleIcon.apply("flags/pl.png"));
            countryFlags.put("DEN", loadAndScaleIcon.apply("flags/dk.png"));
            countryFlags.put("SPA", loadAndScaleIcon.apply("flags/es.png"));
            countryFlags.put("AUS", loadAndScaleIcon.apply("flags/at.png"));
            countryFlags.put("IRE", loadAndScaleIcon.apply("flags/ie.png"));
            countryFlags.put("CZE", loadAndScaleIcon.apply("flags/cz.png"));

            countryFlags.put("Level 0", loadAndScaleIcon.apply("flags/LE_01.png"));
            countryFlags.put("Level NTC", loadAndScaleIcon.apply("flags/LE_02.png"));
            countryFlags.put("PZB", loadAndScaleIcon.apply("flags/LE_02a.png"));
            countryFlags.put("LZB", loadAndScaleIcon.apply("flags/LE_02a.png"));
            countryFlags.put("Level 1", loadAndScaleIcon.apply("flags/LE_03.png"));
            countryFlags.put("Level 2", loadAndScaleIcon.apply("flags/LE_04.png"));

            countryFlags.put("Full Supervision", loadAndScaleIcon.apply("flags/MO_11.png"));
            countryFlags.put("On Sight", loadAndScaleIcon.apply("flags/MO_07.png"));
            countryFlags.put("Staff Responsible", loadAndScaleIcon.apply("flags/MO_09.png"));
            countryFlags.put("Unfitted", loadAndScaleIcon.apply("flags/MO_16.png"));
            countryFlags.put("Stand By", loadAndScaleIcon.apply("flags/MO_13.png"));
            countryFlags.put("Trip", loadAndScaleIcon.apply("flags/MO_04.png"));
            countryFlags.put("Post Trip", loadAndScaleIcon.apply("flags/MO_06.png"));
            countryFlags.put("Limited Supervision", loadAndScaleIcon.apply("flags/LS_01.png"));

            countryFlags.put("Nominální", loadAndScaleIcon.apply("flags/right-arrow.png"));
            countryFlags.put("Reverzní", loadAndScaleIcon.apply("flags/back-arrow.png"));
            countryFlags.put("Oba", loadAndScaleIcon.apply("flags/stretch.png"));

            countryFlags.put("Ano", loadAndScaleIcon.apply("flags/check.png"));
            countryFlags.put("Je", loadAndScaleIcon.apply("flags/check.png"));
            countryFlags.put("Vždy", loadAndScaleIcon.apply("flags/check.png"));
            countryFlags.put("Zapnout", loadAndScaleIcon.apply("flags/check.png"));
            countryFlags.put("Nastavit", loadAndScaleIcon.apply("flags/check.png"));

            countryFlags.put("Ne", loadAndScaleIcon.apply("flags/close.png"));
            countryFlags.put("Není", loadAndScaleIcon.apply("flags/close.png"));
            countryFlags.put("Bez zobrazení", loadAndScaleIcon.apply("flags/close.png"));
            countryFlags.put("Zastavit", loadAndScaleIcon.apply("flags/close.png"));
            countryFlags.put("Nikdy", loadAndScaleIcon.apply("flags/close.png"));
            countryFlags.put("Vypnout", loadAndScaleIcon.apply("flags/close.png"));
            countryFlags.put("Zrušit", loadAndScaleIcon.apply("flags/close.png"));

            countryFlags.put("Stoupaní", loadAndScaleIcon.apply("flags/up-arrow.png"));
            countryFlags.put("Klesání", loadAndScaleIcon.apply("flags/down-arrow.png"));

            countryFlags.put("LS", loadAndScaleIcon.apply("flags/ls.png"));

            countryFlags.put("1x BG", loadAndScaleIcon.apply("flags/1.png"));
            countryFlags.put("2x BG", loadAndScaleIcon.apply("flags/2.png"));
            countryFlags.put("3x BG", loadAndScaleIcon.apply("flags/3.png"));


            countryFlags.put("Balíza", loadAndScaleIcon.apply("flags/bal.png"));

            countryFlags.put("Čelo", loadAndScaleIcon.apply("flags/vlakcelo.png"));
            countryFlags.put("Konec", loadAndScaleIcon.apply("flags/vlakend.png"));


        } catch (Exception e) {
            System.err.println("Error loading flag resources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Allow updating the icon size when needed (e.g., for different look and feels)
     */
    public static void updateIconSize(int newHeight) {
        if (!countryFlags.isEmpty()) {
            try {
                // Function to rescale an existing icon
                java.util.function.BiFunction<ImageIcon, Integer, ImageIcon> rescaleIcon = (icon, height) -> {
                    if (icon == null) return null;

                    int originalWidth = icon.getIconWidth();
                    int originalHeight = icon.getIconHeight();

                    if (originalHeight <= 0) return icon;

                    int newWidth = (int) ((double) originalWidth / originalHeight * height);
                    Image scaledImage = icon.getImage().getScaledInstance(newWidth, height, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaledImage);
                };

                // Rescale all icons
                for (String key : countryFlags.keySet()) {
                    ImageIcon originalIcon = countryFlags.get(key);
                    countryFlags.put(key, rescaleIcon.apply(originalIcon, newHeight));
                }
            } catch (Exception e) {
                System.err.println("Error rescaling flag resources: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Overridden to handle special behavior on BACKSPACE
     */
    @Override
    public void remove(int offs, int len) throws BadLocationException {
        // If no text editor or we're programmatically setting a selection, don't perform the remove logic
        if (textEditor == null || selecting) {
            return;
        }

        if (hitBackspace) {
            if (offs > 0) {
                // If user was removing a highlighted range, offset might need adjusting
                if (hitBackspaceOnSelection) {
                    offs--;
                }
            } else {
                // Beep to indicate no further removal is possible at index 0
                comboBox.getToolkit().beep();
            }
            highlightCompletedText(offs);
        } else {
            // Normal removal
            super.remove(offs, len);
        }
    }

    /**
     * Overridden to insert user-typed text and update the auto-completion selection.
     */
    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (textEditor == null || selecting) {
            // If the insertion was triggered during a programmatic update, do nothing
            return;
        }

        super.insertString(offs, str, a);

        // Attempt to find an item matching the typed text
        Object item = lookupItem(getText(0, getLength()));
        if (item != null) {
            setSelectedItem(item);
        } else {
            // Revert if no item matches
            item = comboBox.getSelectedItem();
            offs = offs - str.length();
            comboBox.getToolkit().beep();
        }

        // Ensure the editor text is up-to-date and highlight the remainder
        setText(item.toString());
        highlightCompletedText(offs + str.length());
    }

    /**
     * Helper method to replace the entire text contents of this document.
     */
    private void setText(String text) {
        try {
            super.remove(0, getLength());
            super.insertString(0, text, null);
        } catch (BadLocationException e) {
            throw new RuntimeException("Failed to set text in document: " + e.getMessage(), e);
        }
    }

    /**
     * Highlights the completed text portion
     */
    private void highlightCompletedText(int start) {
        if (textEditor == null) return;

        try {
            textEditor.setCaretPosition(getLength());
            textEditor.moveCaretPosition(start);
        } catch (Exception e) {
            // Generally safe to ignore or log if needed
        }
    }

    /**
     * Selects the given item in the combo box without triggering unwanted events.
     */
    private void setSelectedItem(Object item) {
        selecting = true;
        comboBoxModel.setSelectedItem(item);
        selecting = false;
    }

    /**
     * Searches for an item matching the given pattern
     */
    private Object lookupItem(String pattern) {
        Object selectedItem = comboBoxModel.getSelectedItem();
        if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern)) {
            return selectedItem;
        }

        // Otherwise, search through the model
        for (int i = 0, n = comboBoxModel.getSize(); i < n; i++) {
            Object currentItem = comboBoxModel.getElementAt(i);
            if (currentItem != null
                    && startsWithIgnoreCase(currentItem.toString(), pattern)) {
                return currentItem;
            }
        }
        return null;
    }

    /**
     * Checks if one string starts with another, ignoring case.
     */
    private boolean startsWithIgnoreCase(String str1, String str2) {
        return str1.toUpperCase().startsWith(str2.toUpperCase());
    }
}