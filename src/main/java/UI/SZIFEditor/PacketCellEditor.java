// PacketCellEditor.java
package UI.SZIFEditor;

import packets.Var.Variables;
import tools.ui.InputJCombobox;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.EventObject;
import java.util.List;

/**
 * Custom cell editor for packet cells that displays inline editing
 * with an HTML-like appearance similar to the renderer
 */
class PacketCellEditor extends AbstractCellEditor implements TableCellEditor {
    private EditorController controller;
    private JPanel editorPanel;
    private JTable table;
    private int row;
    private int column;
    private String[] paramValues = new String[10];
    private int originalRowHeight;
    private JTextArea notesArea;
    private Map<JComboBox<String>, Integer> comboBoxParamMap = new HashMap<>();
    private Map<JCheckBox, Integer> templateCheckMap = new HashMap<>();
    private Map<JComboBox<String>, Integer> templateComboMap = new HashMap<>();
    private PacketTemplate selectedPacket;
    private int originalColumnWidth;
    private boolean isCurrentlyEditing = false;

    public static final String[] searchPackages = {
            "packets.Var.A",
            "packets.Var.D",
            "packets.Var.G",
            "packets.Var.L",
            "packets.Var.M",
            "packets.Var.N",
            "packets.Var.NID",
            "packets.Var.Q",
            "packets.Var.T",
            "packets.Var.V",
            "packets.Var.X"
    };

    public Class<?> findClassByName(String name) {
        for (int i = 0; i < searchPackages.length; i++) {
            try {
                return Class.forName(searchPackages[i] + "." + name);
            } catch (ClassNotFoundException e) {
                // Continue searching
            }
        }
        return null;
    }

    public PacketCellEditor(EditorController controller) {
        this.controller = controller;
        Arrays.fill(paramValues, "0");
        // Register for changes in the packet repository
        controller.addRepositoryChangeListener(this::refreshPacketsModel);
    }

    private void refreshPacketsModel() {
        // No need to do anything specific here as we recreate the UI each time
    }

    /**
     * Determines if the current UI is using a dark theme
     */
    private boolean isDarkTheme(Component component) {
        Color bg = component.getBackground();
        // Average the RGB components to get a brightness value
        int brightness = (bg.getRed() + bg.getGreen() + bg.getBlue()) / 3;
        // Consider it a dark theme if average brightness is less than 128
        return brightness < 128;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        if (isCurrentlyEditing) {
            // If we're already editing, just return the component without re-expanding
            return editorPanel;
        }



        isCurrentlyEditing = true;

        this.table = table;
        this.row = row;
        this.column = column;
        comboBoxParamMap.clear();
        templateCheckMap.clear();
        templateComboMap.clear();

        // Store original dimensions
        this.originalRowHeight = table.getRowHeight(row);
        this.originalColumnWidth = table.getColumnModel().getColumn(column).getWidth();

        // Parse current value
        String currentValue = (String) value;
        paramValues = parseParameters(currentValue);

        // Parse the packet name from the value
        String packetName = "";
        if (currentValue != null && !currentValue.isEmpty()) {
            int bracketIndex = currentValue.indexOf('[');
            if (bracketIndex > 0) {
                packetName = currentValue.substring(0, bracketIndex);
            }
        }

        // Find the packet template
        selectedPacket = null;
        for (PacketTemplate pt : controller.getPackets()) {
            if (pt.getName().equals(packetName)) {
                selectedPacket = pt;
                break;
            }
        }

        boolean isDarkTheme = isDarkTheme(table);

        // Create the main editor panel
        editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBorder(BorderFactory.createLineBorder(isDarkTheme ? Color.DARK_GRAY : Color.GRAY));
        editorPanel.setBackground(isDarkTheme ? new Color(50, 50, 50) : Color.WHITE);

        if (selectedPacket == null) {
            // If no packet selected, show a combo box to select one
            JComboBox<PacketTemplate> packetComboBox = new JComboBox<>();
            expandCellForEditing(200);

            for (PacketTemplate pt : controller.getPackets()) {
                packetComboBox.addItem(pt);
            }

            JPanel selectionPanel = new JPanel(new BorderLayout());
            selectionPanel.setBackground(isDarkTheme ? new Color(60, 60, 60) : Color.WHITE);
            JLabel selectLabel = new JLabel("");
            selectLabel.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
            selectionPanel.add(selectLabel, BorderLayout.WEST);
            selectionPanel.add(packetComboBox, BorderLayout.CENTER);

            JButton applyButton = new JButton("Apply");
            applyButton.addActionListener(e -> {
                selectedPacket = (PacketTemplate) packetComboBox.getSelectedItem();
                // Calculate preferred width for editor
                int preferredWidth = calculatePreferredWidth();

                // Expand cell for editing (directly, not using invokeLater)
                expandCellForEditing(preferredWidth);
                stopCellEditing();
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(isDarkTheme ? new Color(60, 60, 60) : Color.WHITE);
            buttonPanel.add(applyButton);
            selectionPanel.setPreferredSize(new Dimension(120, Math.min(200, selectionPanel.getPreferredSize().height)));

            editorPanel.add(selectionPanel, BorderLayout.NORTH);
            editorPanel.add(buttonPanel, BorderLayout.SOUTH);
        } else {
            // Create HTML-like panel structure
            JPanel htmlStructure = new JPanel();
            htmlStructure.setLayout(new BoxLayout(htmlStructure, BoxLayout.Y_AXIS));
            htmlStructure.setBackground(isDarkTheme ? new Color(50, 50, 50) : Color.WHITE);

            // Header panel with table caption styling
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(isDarkTheme ? new Color(60, 60, 60) : new Color(224, 224, 224));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            JLabel headerLabel = new JLabel(selectedPacket.getName());
            headerLabel.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
            headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));
            headerPanel.add(headerLabel, BorderLayout.CENTER);

            // Variable panels with combo boxes
            JPanel variablesPanel = new JPanel();
            variablesPanel.setLayout(new BoxLayout(variablesPanel, BoxLayout.Y_AXIS));
            variablesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            variablesPanel.setBackground(isDarkTheme ? new Color(50, 50, 50) : Color.WHITE);

            boolean hasNonFixedValues = false;

            // Check if there are non-fixed values
            for (PacketVariable var : selectedPacket.getVariables()) {
                if (var.getValue().startsWith("-")) {
                    hasNonFixedValues = true;
                    break;
                }
            }

            if (!hasNonFixedValues) {
                // If no non-fixed values, show simplified display
                JPanel defaultPanel = new JPanel();
                defaultPanel.setBackground(isDarkTheme ? new Color(50, 50, 50) : Color.WHITE);
                JLabel defaultLabel = new JLabel("Default packet configuration");
                defaultLabel.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
                defaultLabel.setFont(defaultLabel.getFont().deriveFont(Font.ITALIC));
                defaultPanel.add(defaultLabel);
                variablesPanel.add(defaultPanel);
            } else {
                // Create HTML-like table rows with combo boxes
                for (PacketVariable var : selectedPacket.getVariables()) {
                    if (var.getValue().startsWith("-")) {
                        // This is a reference to a parameter
                        int paramIndex = Math.abs(Integer.parseInt(var.getValue())) - 1;
                        if (paramIndex >= 0 && paramIndex < paramValues.length) {
                            // Create row panel that looks like an HTML table row
                            JPanel rowPanel = new JPanel(new BorderLayout(5, 0));
                            rowPanel.setBackground(isDarkTheme ? new Color(50, 50, 50) : Color.WHITE);
                            rowPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                                    isDarkTheme ? new Color(80, 80, 80) : Color.LIGHT_GRAY));
                            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                            // Variable name cell
                            JLabel nameLabel = new JLabel(var.getName());
                            nameLabel.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
                            nameLabel.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(
                                            isDarkTheme ? new Color(80, 80, 80) : new Color(204, 204, 204), 1),
                                    BorderFactory.createEmptyBorder(2, 5, 2, 5)
                            ));
                            nameLabel.setPreferredSize(new Dimension(120, 25));

                            // Create a panel for the value options (regular combobox and template options)
                            JPanel valuePanel = new JPanel(new BorderLayout());
                            valuePanel.setBackground(isDarkTheme ? new Color(50, 50, 50) : Color.WHITE);

                            // Create combo box for value selection
                            JComboBox<String> valueCombo = new JComboBox<>();
                            valueCombo.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(
                                            isDarkTheme ? new Color(80, 80, 80) : new Color(204, 204, 204), 1),
                                    BorderFactory.createEmptyBorder(2, 5, 2, 5)
                            ));

                            // Get possible values
                            List<String> possibleValues = VariableRegistry.getValuesForVariable(var.getName());
                            if (possibleValues != null && !possibleValues.isEmpty()) {
                                for (String val : possibleValues) {
                                    valueCombo.addItem(val);
                                }
                            } else if (findClassByName(var.getName()) != null) {
                                // Try to get values from class if available
                                try {
                                    Object obj = findClassByName(var.getName()).newInstance();
                                    if (obj instanceof Variables) {
                                        String[] values = ((Variables) obj).getCombo().toArray(new String[0]);
                                        valueCombo.setModel(new DefaultComboBoxModel<>(values));
                                        new InputJCombobox(valueCombo);
                                    }
                                } catch (Exception ex) {
                                    // Fallback to default values
                                    for (int i = 0; i < 16; i++) {
                                        valueCombo.addItem(String.valueOf(i));
                                    }
                                }
                            } else {
                                // Default values
                                for (int i = 0; i < 16; i++) {
                                    valueCombo.addItem(String.valueOf(i));
                                }
                            }

                            // Set current value if numeric
                            String currentParamValue = paramValues[paramIndex];
                            boolean isNumericValue = isNumeric(currentParamValue);

                            if (isNumericValue) {
                                int index = Integer.parseInt(currentParamValue);
                                if (index >= 0 && index < valueCombo.getItemCount()) {
                                    valueCombo.setSelectedIndex(index);
                                }
                            }

                            // Track which parameter this combo box controls
                            comboBoxParamMap.put(valueCombo, paramIndex);

                            // Add listener to update parameter value when selection changes
                            valueCombo.addActionListener(e -> {
                                Integer pIndex = comboBoxParamMap.get(valueCombo);
                                if (pIndex != null) {
                                    paramValues[pIndex] = String.valueOf(valueCombo.getSelectedIndex());
                                }
                            });

                            valuePanel.add(valueCombo, BorderLayout.CENTER);

                            // Add template support
                            setupTemplateSupport(var, valuePanel, paramIndex, currentParamValue, valueCombo, isDarkTheme);

                            // Add to the row panel
                            rowPanel.add(nameLabel, BorderLayout.WEST);
                            rowPanel.add(valuePanel, BorderLayout.CENTER);

                            // Add row to variables panel
                            variablesPanel.add(rowPanel);
                        }
                    }

                    // Handle force show variables
                    if (var.forceShow) {
                        JPanel rowPanel = new JPanel(new BorderLayout(5, 0));
                        rowPanel.setBackground(isDarkTheme ? new Color(50, 50, 50) : Color.WHITE);
                        rowPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                                isDarkTheme ? new Color(80, 80, 80) : Color.LIGHT_GRAY));
                        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                        // Variable name cell
                        JLabel nameLabel = new JLabel(var.getName());
                        nameLabel.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
                        nameLabel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(
                                        isDarkTheme ? new Color(80, 80, 80) : new Color(204, 204, 204), 1),
                                BorderFactory.createEmptyBorder(2, 5, 2, 5)
                        ));
                        nameLabel.setPreferredSize(new Dimension(120, 25));

                        // Fixed value label
                        JLabel valueLabel = new JLabel(var.getValue());
                        valueLabel.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
                        valueLabel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(
                                        isDarkTheme ? new Color(80, 80, 80) : new Color(204, 204, 204), 1),
                                BorderFactory.createEmptyBorder(2, 5, 2, 5)
                        ));

                        rowPanel.add(nameLabel, BorderLayout.WEST);
                        rowPanel.add(valueLabel, BorderLayout.CENTER);

                        variablesPanel.add(rowPanel);
                    }
                }
            }

            // Create notes area
            notesArea = new JTextArea(2, 20);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            if (isDarkTheme) {
                notesArea.setBackground(new Color(51, 51, 0));
                notesArea.setForeground(Color.WHITE);
                notesArea.setCaretColor(Color.WHITE);
            } else {
                notesArea.setBackground(new Color(255, 255, 204));
                notesArea.setForeground(Color.BLACK);
            }

            // Extract notes if any
            if (currentValue != null) {
                int bracketEnd = currentValue.indexOf(']');
                if (bracketEnd >= 0 && bracketEnd + 1 < currentValue.length()) {
                    notesArea.setText(currentValue.substring(bracketEnd + 1));
                }
            }

            // Style notes panel with HTML-like background color
            JPanel notesPanel = new JPanel(new BorderLayout());
            notesPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(
                            isDarkTheme ? new Color(80, 80, 80) : Color.GRAY),
                    "Notes",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    null,
                    isDarkTheme ? Color.WHITE : Color.BLACK
            ));
            notesPanel.setBackground(isDarkTheme ? new Color(51, 51, 0) : new Color(255, 255, 204));

            JScrollPane notesScrollPane = new JScrollPane(notesArea);
            if (isDarkTheme) {
                notesScrollPane.getViewport().setBackground(new Color(51, 51, 0));
                notesScrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
            }
            notesPanel.add(notesScrollPane, BorderLayout.CENTER);

            // Add control buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(isDarkTheme ? new Color(50, 50, 50) : Color.WHITE);
            JButton okButton = new JButton("Apply");
            JButton cancelButton = new JButton("Cancel");

            okButton.addActionListener(e -> stopCellEditing());
            cancelButton.addActionListener(e -> cancelCellEditing());

            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            // Assemble the components in HTML-like structure
            htmlStructure.add(headerPanel);

            // Add variables in a scroll pane if there are many
            JScrollPane varScrollPane = new JScrollPane(variablesPanel);
            varScrollPane.setBorder(null);
            varScrollPane.setPreferredSize(new Dimension(120, Math.min(200, variablesPanel.getPreferredSize().height)));
            if (isDarkTheme) {
                varScrollPane.getViewport().setBackground(new Color(50, 50, 50));
            }

            htmlStructure.add(varScrollPane);
            htmlStructure.add(notesPanel);
            htmlStructure.add(buttonPanel);

            // Add to main panel
            editorPanel.add(htmlStructure, BorderLayout.CENTER);
        }

        // Calculate preferred width for editor
        int preferredWidth = calculatePreferredWidth();

        // Expand cell for editing (directly, not using invokeLater)
        expandCellForEditing(preferredWidth);

        return editorPanel;
    }

    private void setupTemplateSupport(PacketVariable var, JPanel valuePanel, int paramIndex, String currentValue, JComboBox<String> valueCombo, boolean isDarkTheme) {
        // Find all template values that match this variable
        List<PacketTemplate> templates = controller.getTemplatePackets();
        if (templates.isEmpty()) return;

        // Create template interface components
        JCheckBox useTemplateCheck = new JCheckBox("");
        if (isDarkTheme) {
            useTemplateCheck.setBackground(new Color(50, 50, 50));
            useTemplateCheck.setForeground(Color.WHITE);
        }

        // Create combo with all available templates for this variable
        JComboBox<String> templateCombo = new JComboBox<>();
        if (isDarkTheme) {
            templateCombo.setBackground(new Color(60, 60, 60));
            templateCombo.setForeground(Color.WHITE);
        }

        Map<String, String> templateValues = new HashMap<>();

        for (PacketTemplate template : templates) {
            for (PacketVariable templateVar : template.getVariables()) {
                if (templateVar.getName().equals(var.getName())) {
                    String displayName = template.getName() + ": " + templateVar.getValue();
                    templateCombo.addItem(displayName);
                    // Create a special format: TEMPLATE:TemplateName:VariableName
                    String refValue = "TEMPLATE:" + template.getName() + ":" + templateVar.getValue();
                    templateValues.put(displayName, refValue);
                }
            }
        }

        // If no matching templates, don't add template support
        if (templateCombo.getItemCount() == 0) return;

        // Check if current value is a template reference
        boolean usingTemplate = currentValue != null && currentValue.startsWith("TEMPLATE:");
        useTemplateCheck.setSelected(usingTemplate);

        // Create a card layout panel to switch between comboboxes
        JPanel cardPanel = new JPanel(new CardLayout());
        cardPanel.setBackground(isDarkTheme ? new Color(50, 50, 50) : Color.WHITE);
        cardPanel.add(valueCombo, "standard");
        cardPanel.add(templateCombo, "template");

        // Set initial card
        CardLayout cardLayout = (CardLayout)cardPanel.getLayout();
        cardLayout.show(cardPanel, usingTemplate ? "template" : "standard");

        // Try to select correct template if already using one
        if (usingTemplate) {
            for (int i = 0; i < templateCombo.getItemCount(); i++) {
                String option = templateCombo.getItemAt(i);
                String refValue = templateValues.get(option);
                if (refValue != null && refValue.equals(currentValue)) {
                    templateCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // When template checkbox changes, toggle visibility and update value
        useTemplateCheck.addActionListener(e -> {
            boolean useTemplate = useTemplateCheck.isSelected();

            // Show appropriate card
            cardLayout.show(cardPanel, useTemplate ? "template" : "standard");

            if (useTemplate) {
                // Get selected template option
                String selected = (String)templateCombo.getSelectedItem();
                if (selected != null) {
                    // Store the template reference value
                    String refValue = templateValues.get(selected);
                    if (refValue != null) {
                        paramValues[paramIndex] = refValue;
                        System.out.println("Template selected: " + selected + " => " + refValue);
                    }
                }
            } else {
                // Store standard value
                paramValues[paramIndex] = String.valueOf(valueCombo.getSelectedIndex());
                System.out.println("Standard value selected: " + valueCombo.getSelectedIndex());
            }
        });

        // When template selection changes, update parameter value
        templateCombo.addActionListener(e -> {
            if (useTemplateCheck.isSelected()) {
                String selected = (String)templateCombo.getSelectedItem();
                if (selected != null) {
                    String refValue = templateValues.get(selected);
                    if (refValue != null) {
                        paramValues[paramIndex] = refValue;
                        System.out.println("Template changed to: " + selected + " => " + refValue);
                    }
                }
            }
        });

        // Store references for when cell editing finishes
        templateCheckMap.put(useTemplateCheck, paramIndex);
        templateComboMap.put(templateCombo, paramIndex);

        // Create the layout for the value panel
        valuePanel.removeAll(); // Remove existing components
        valuePanel.setLayout(new BorderLayout());
        valuePanel.add(cardPanel, BorderLayout.CENTER);

        templateCombo.setPreferredSize(new Dimension(25, templateCombo.getPreferredSize().height));

        // Add checkbox to the East position
        JPanel checkboxPanel = new JPanel(new BorderLayout());
        checkboxPanel.setBackground(isDarkTheme ? new Color(50, 50, 50) : Color.WHITE);
        checkboxPanel.add(useTemplateCheck, BorderLayout.CENTER);
        valuePanel.add(checkboxPanel, BorderLayout.EAST);
    }

    @Override
    public Object getCellEditorValue() {
        if (selectedPacket == null) {
            return "";
        }

        // Build result string
        StringBuilder result = new StringBuilder();
        result.append(selectedPacket.getName());
        result.append("[");

        for (String value : paramValues) {
            result.append(value).append(";");
        }

        result.append("]");

        if (notesArea != null) {
            result.append(notesArea.getText());
        }

        return result.toString();
    }

    // Helper class to store template info in combo box
    private static class TemplateOption {
        public final String displayName;
        public final int value;

        public TemplateOption(String displayName, int value) {
            this.displayName = displayName;
            this.value = value;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private void updateParamValueFromTemplate(JComboBox<String> templateCombo, int paramIndex) {
        if (templateCombo.getSelectedItem() == null) return;

        String selectedItem = templateCombo.getSelectedItem().toString();
        String[] parts = selectedItem.split(": ", 2);
        if (parts.length != 2) return;

        String templateName = parts[0];
        String valueName = parts[1];

        for (PacketTemplate template : controller.getTemplatePackets()) {
            if (template.getName().equals(templateName)) {
                for (PacketVariable var : template.getVariables()) {
                    if (var.getValue().equals(valueName)) {
                        // Store the actual size value
                        paramValues[paramIndex] = String.valueOf(var.getSize());
                        return;
                    }
                }
            }
        }
    }

    private int calculatePreferredWidth() {
        // Calculate based on content - use a reasonable minimum
        int minWidth = 75;

        // If we have a selected packet, calculate based on its components
        if (selectedPacket != null) {
            // Get longest variable name length
            int maxNameLength = 0;
            for (PacketVariable var : selectedPacket.getVariables()) {
                maxNameLength = Math.max(maxNameLength, var.getName().length());
            }

            // Calculate width based on name length plus space for combo box
            int calculatedWidth = Math.max(minWidth, (maxNameLength * 8) + 200);

            // Cap at a reasonable maximum
            return Math.min(calculatedWidth, 350); // Increased from 300 to accommodate template controls
        }

        return minWidth;
    }

    private void expandCellForEditing(int preferredWidth) {
        // Calculate preferred height based on content
        int preferredHeight = 0;

        // If we have a selected packet, calculate based on its variables
        if (selectedPacket != null) {
            // Base height for header, buttons, and padding
            int baseHeight = 100;

            // Add height for each variable (approximately 30px per variable)
            int variableCount = 0;
            for (PacketVariable var : selectedPacket.getVariables()) {
                // Count only variables that will be shown in the editor
                if (var.getValue().startsWith("-") || var.forceShow) {
                    variableCount++;
                }
            }

            // Add height for notes area
            int notesHeight = 60;

            // Calculate total height
            preferredHeight = baseHeight + (variableCount * 30) + notesHeight;
        } else {
            // Default height for packet selection dialog
            preferredHeight = 75;
        }

        // Cap at maximum height
        int editorHeight = Math.min(preferredHeight, 400);

        // Immediately set dimensions before displaying editor
        table.setRowHeight(row, editorHeight);

        // Make sure column is wide enough
        TableColumn column = table.getColumnModel().getColumn(this.column);
        if (column.getWidth() < preferredWidth) {
            int oldWidth = column.getWidth();
            column.setPreferredWidth(preferredWidth);
            column.setWidth(preferredWidth);
        }

        // If this is an EditorTable, tell it not to auto-adjust while editing
        if (table instanceof EditorTable) {
            ((EditorTable) table).setSkipAdjustment(true);
        }

        // Force immediate update
        table.revalidate();
        table.repaint();
    }

    private void restoreCellDimensions() {
        // First re-enable auto-adjustment if applicable
        if (table instanceof EditorTable) {
            ((EditorTable) table).setSkipAdjustment(false);
        }

        // Let the table handle its own adjustment after a brief delay
        SwingUtilities.invokeLater(() -> {
            if (table instanceof EditorTable) {
                ((EditorTable) table).adjustRowHeight(row);
                ((EditorTable) table).adjustColumnWidths();
            } else {
                // Fall back to original height if not our custom table
                table.setRowHeight(row, originalRowHeight);
            }
        });
    }

    private String[] parseParameters(String value) {
        String[] params = new String[10];
        Arrays.fill(params, "0");

        if (value != null && !value.isEmpty()) {
            int bracketStart = value.indexOf('[');
            int bracketEnd = value.indexOf(']');

            if (bracketStart > 0 && bracketEnd > bracketStart) {
                String paramsStr = value.substring(bracketStart + 1, bracketEnd);
                String[] paramsArray = paramsStr.split(";");

                for (int i = 0; i < Math.min(paramsArray.length, params.length); i++) {
                    params[i] = paramsArray[i];
                }
            }
        }

        return params;
    }

    @Override
    public boolean stopCellEditing() {
        boolean result = super.stopCellEditing();
        // Restore dimensions after editing
        restoreCellDimensions();
        isCurrentlyEditing = false;
        return result;
    }

    @Override
    public void cancelCellEditing() {
        super.cancelCellEditing();
        // Restore dimensions if editing is canceled
        restoreCellDimensions();
        isCurrentlyEditing = false;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        // Only respond to single-clicks for editing
        if (e instanceof MouseEvent) {
            return ((MouseEvent) e).getClickCount() > 1;
        }
        return true; // Allow keyboard-initiated editing as well
    }

    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}