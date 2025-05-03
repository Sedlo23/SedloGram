// PacketCellRenderer.java
package UI.SZIFEditor;

import UI.SZIFEditor.VariableRegistry;
import net.miginfocom.swing.MigLayout;
import packets.Var.Variables;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Enhanced cell renderer with HTML support and note visualization
 * that only shows non-fixed values (referenced parameters)
 */
class PacketCellRenderer extends DefaultTableCellRenderer {
    private static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12);
    private EditorController controller;

    public PacketCellRenderer(EditorController controller) {
        this.controller = controller;

        setFont(DEFAULT_FONT);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);




        Border leftBorder = BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK);
        setBorder(leftBorder);


        boolean hasDataBelow = false;
        boolean hasDataAtLevel = false;
        for (int r = row + 1; r < table.getRowCount(); r++) {
            for (int c = 0; c < table.getColumnCount(); c++) {
                Object cellValue = table.getValueAt(r, c);
                if (cellValue != null && !cellValue.toString().isEmpty()) {
                    hasDataBelow = true;
                    break;
                }
            }
            if (hasDataBelow) break;
        }

        for (int c = 0; c < table.getColumnCount(); c++) {
            Object cellValue = table.getValueAt(row, c);
            if (cellValue != null && !cellValue.toString().isEmpty()) {
                hasDataAtLevel = true;
                break;
            }
        }

        // Handle standard column values (0-7)
        if (column < 8) {
            String stringValue = (value == null) ? "" : value.toString();
            return renderStandardColumn(stringValue, column, isSelected, row, table, false, hasDataBelow, hasDataAtLevel);
        }

        // Handle empty values for non-standard columns
        if (value == null || value.toString().isEmpty())
        {
            JLabel label = new JLabel();
            label.setBackground(isSelected?table.getSelectionBackground():table.getBackground());
            label.setOpaque(true);

            label.setBorder(leftBorder);

            return label;
        }

        String stringValue = value.toString();

        // Handle packet columns (8-16)
        if (column >= 8 && column < 17) {
            if (stringValue.contains("[")) {
                return renderPacketColumn(stringValue, isSelected);
            }
        }

        if (column == 17 && !stringValue.isEmpty())
        {
            // Determine if we're using dark theme
            boolean isDarkTheme = isDarkTheme(table);
            Color bgColor = isDarkTheme ? new Color(51, 51, 0) : new Color(255, 255, 204);
            Color borderColor = isDarkTheme ? new Color(102, 102, 102) : new Color(230, 230, 230);
            Color textColor = isDarkTheme ? new Color(255, 255, 204) : new Color(0, 0, 0);

            StringBuilder html = new StringBuilder();
            {
                html.append("<div style='margin-top:5px;padding:3px;background-color:" + colorToHex(bgColor) + ";");
                html.append("border:1px solid " + colorToHex(borderColor) + ";font-size:8px;color:" + colorToHex(textColor) + ";'>");
                html.append(" ").append(stringValue).append("</div>");
            }

            setText("<html>" + html.toString() + "</html>");
        }

        return component;
    }

    private Component renderStandardColumn(String value, int column, boolean isSelected, int currentRow, JTable table, boolean isref, boolean hasDataBelow, boolean hasDataAtLevel) {
        boolean isDarkTheme = isDarkTheme(table);

        // Create a JPanel instead of using a JLabel directly
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);

        // Set the background color based on selection
        if (isSelected) {
            panel.setBackground(table.getSelectionBackground());
            setBackground(table.getSelectionBackground());
        } else {
            panel.setBackground(table.getBackground());
            setBackground(table.getBackground());
        }

        if (!hasDataBelow && isref && !hasDataAtLevel) {
            Border leftBorder = BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK);
            JLabel label = new JLabel();
            label.setBackground(isSelected?table.getSelectionBackground():table.getBackground());
            label.setOpaque(true);

            label.setBorder(leftBorder);
            return label;

        }

        // Check if the value is empty
        if (value.isEmpty()) {
            // Find the closest upper non-empty cell in the same column
            if (table != null) {
                for (int row = currentRow - 1; row >= 0; row--) {
                    Object cellValue = table.getValueAt(row, column);
                    if (cellValue != null && !cellValue.toString().isEmpty() && isNumeric(cellValue.toString())) {
                        return renderStandardColumn(cellValue.toString(), column, isSelected, row, table, true, hasDataBelow, hasDataAtLevel);
                    }
                }
            }

            Border leftBorder = BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK);
            JLabel label = new JLabel();
            label.setOpaque(true);

            label.setBorder(leftBorder);
            label.setBackground(isSelected?table.getSelectionBackground():table.getBackground());
            return label;        }

        if (!isNumeric(value)) {
            JLabel label = new JLabel("???");
            label.setOpaque(true);
            label.setBackground(panel.getBackground());
            label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            panel.add(label, BorderLayout.CENTER);
            Border leftBorder = BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK);
            label.setBackground(isSelected?table.getSelectionBackground():table.getBackground());
            label.setOpaque(true);
            label.setBorder(leftBorder);
            return panel;
        }

        int index = Integer.parseInt(value);

        // Get the appropriate variable name based on column
        String varName = EditorTable.COLUMN_NAMES[column];
        java.util.List<String> values = VariableRegistry.getValuesForVariable(varName);

        if (values != null && index < values.size()) {
            Color captionBg = isDarkTheme ? new Color(60, 60, 60) : new Color(224, 224, 224);
            Color captionBgReverse = !isDarkTheme ? new Color(60, 60, 60) : new Color(224, 224, 224);

            Color captionText = isDarkTheme ? new Color(220, 220, 220) : new Color(0, 0, 0);

            String key = "bold";

            if (isref) {
                captionBg = isDarkTheme ? new Color(45, 45, 45) : new Color(235, 235, 235);
                captionText = isDarkTheme ? new Color(180, 180, 180) : new Color(70, 70, 70);
                key = "italic";
            }

            // Create a JLabel with HTML content
            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setBackground(panel.getBackground());
            Border leftBorder = BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK);
            label.setBackground(isSelected?table.getSelectionBackground():table.getBackground());
            label.setOpaque(true);

            label.setBorder(leftBorder);

            StringBuilder html = new StringBuilder();
            html.append("<html>");
            html.append("<table style='width:100%;border-collapse:collapse;");
            html.append("font-family:Arial,sans-serif;font-size:8px;'>");
            html.append("<caption style='font-weight:"+key+";background-color:" + colorToHex(captionBg) + ";color:" + colorToHex(captionText) + ";padding:3px;'>");
            html
                    .append(values.get(index).replace(" ", "_") +"&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp")
                    .append("</caption>");
            html.append("</table>");
            html.append("</html>");

            label.setText(html.toString());
            label.setOpaque(true);

            label.setBorder(leftBorder);

            panel.add(label, BorderLayout.NORTH);


        } else {
            JLabel label = new JLabel("???");

            label.setOpaque(true);
            label.setBackground(panel.getBackground());
            label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            panel.add(label, BorderLayout.CENTER);
        }

        return panel;
    }  // Add these fields to the class that contains renderPacketColumn

    private long cacheLastClearedTime = System.currentTimeMillis();
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes
    // Add these fields to the class
    private Map<String, Component> rendererCache = new HashMap<>();


    // Cache key class to include selection state
    private static class RendererCacheKey {
        private final String value;
        private final boolean isSelected;
        private final String Color;

        public RendererCacheKey(String value, boolean isSelected, String color) {
            this.value = value;
            this.isSelected = isSelected;
            Color = color;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RendererCacheKey that = (RendererCacheKey) o;
            return isSelected == that.isSelected &&
                    Objects.equals(value, that.value) &&
                    Objects.equals(Color, that.Color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, isSelected, Color);
        }
    }

    private Component renderPacketColumn(String value, boolean isSelected) {
        // Create cache key
        RendererCacheKey key = new RendererCacheKey(value, isSelected, value);

        // Check cache first
        if (rendererCache.containsKey(key.toString())) {
            return rendererCache.get(key.toString());
        }

        // Extract packet name and parameters
        int bracketIndex = value.indexOf('[');
        if (bracketIndex <= 0) {
            return this;
        }

        String packetName = value.substring(0, bracketIndex);

        // Extract notes if any
        String notes = "";
        int bracketEndIndex = value.indexOf(']');
        if (bracketEndIndex > 0 && bracketEndIndex < value.length() - 1) {
            notes = value.substring(bracketEndIndex + 1);
        }

        // Find packet in repository
        for (PacketTemplate packetTemplate : controller.getPackets()) {
            if (packetTemplate.getName().equals(packetName)) {
                Component renderer = createHtmlRenderer(packetTemplate, value, notes, isSelected);

                // Cache the result
                rendererCache.put(key.toString(), renderer);

                // Periodically clear cache to prevent memory leaks
                checkAndClearCacheIfNeeded();

                return renderer;
            }
        }

        return this;
    }

    private Component createHtmlRenderer(PacketTemplate packetTemplate, String value, String notes, boolean isSelected) {
        // No need to cache this separately since renderPacketColumn already caches the result

        // Extract parameter values
        String[] params = parseParameters(value);

        // Create HTML content
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setFont(DEFAULT_FONT);

        // Generate HTML table with notes, showing only non-fixed values
        String html = buildHtmlTable(packetTemplate, params, notes, editorPane);
        editorPane.setText(html);

        // Set background color
        if (isSelected) {
            editorPane.setBackground(UIManager.getColor("Table.selectionBackground"));
            editorPane.setForeground(UIManager.getColor("Table.selectionForeground"));
        } else {
            editorPane.setBackground(UIManager.getColor("Table.background"));
            editorPane.setForeground(UIManager.getColor("Table.foreground"));
        }

        boolean isDarkTheme = isDarkTheme(new JPanel());
        Color captionBgReverse = !isDarkTheme ? new Color(60, 60, 60) : new Color(224, 224, 224);
        Border leftBorder = BorderFactory.createMatteBorder(1, 1, 0, 0, captionBgReverse);
        editorPane.setBorder(leftBorder);

        // Create a panel to hold the HTML content
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(editorPane, BorderLayout.NORTH);
        panel.setBackground(editorPane.getBackground());

        // Let's force sizing calculation
        Dimension prefSize = editorPane.getPreferredSize();
        panel.setPreferredSize(new Dimension(Math.min(prefSize.width, 300), prefSize.height));

        return panel;
    }

    /**
     * Clears the renderer cache periodically to prevent memory issues
     */
    private void checkAndClearCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - cacheLastClearedTime > CACHE_EXPIRY_MS) {
            rendererCache.clear();
            cacheLastClearedTime = currentTime;
        }
    }

    /**
     * Call this method when packet templates are modified or UI settings change
     */
    public void clearRendererCache() {
        rendererCache.clear();
        cacheLastClearedTime = System.currentTimeMillis();
    }

    private String buildHtmlTable(PacketTemplate packetTemplate, String[] params, String notes, Component component) {
        boolean isDarkTheme = isDarkTheme(component);

        // Define theme-sensitive colors
        Color captionBg = isDarkTheme ? new Color(60, 60, 60) : new Color(224, 224, 224);
        Color captionText = isDarkTheme ? new Color(220, 220, 220) : Color.BLACK;
        Color borderColor = isDarkTheme ? new Color(80, 80, 80) : new Color(204, 204, 204);
        Color notesBg = isDarkTheme ? new Color(51, 51, 0) : new Color(255, 255, 204);
        Color notesBorder = isDarkTheme ? new Color(102, 102, 102) : new Color(230, 230, 230);
        Color notesText = isDarkTheme ? new Color(220, 220, 220) : Color.BLACK;
        Color templateColor = isDarkTheme ? new Color(102, 178, 102) : new Color(0, 102, 0);
        Color defaultText = isDarkTheme ? new Color(220, 220, 220) : Color.BLACK;

        StringBuilder html = new StringBuilder();
        html.append("<html><table style='width:100%;border-collapse:collapse;");
        html.append("font-family:Arial,sans-serif;font-size:8px;'>");
        html.append("<caption style='font-weight:bold;background-color:" + colorToHex(captionBg) + ";color:" + colorToHex(captionText) + ";padding:3px;'>");
        html.append(packetTemplate.getName()).append("</caption>");

        boolean hasNonFixedValues = false;

        // First, check if there are any non-fixed values to display
        for (PacketVariable var : packetTemplate.getVariables()) {
            if (var.getValue().startsWith("-")) {
                hasNonFixedValues = true;
                break;
            }
        }

        // First, check if there are any non-fixed values to display
        for (PacketVariable var : packetTemplate.getVariables()) {
            if (var.forceShow) {
                hasNonFixedValues = true;
                break;
            }
        }

        if (!hasNonFixedValues) {
            // If there are no non-fixed values, show a simplified display
            html.append("<tr><td style='padding:5px;text-align:center;font-style:italic;color:" + colorToHex(defaultText) + ";'>");
            html.append("Default packet configuration");
            html.append("</td></tr>");
        }
        else {
            // Only add the variables that have parameter references
            for (PacketVariable var : packetTemplate.getVariables()) {
                if (var.getValue().startsWith("-")) {
                    // This is a reference to a parameter (non-fixed value)
                    int paramIndex = Math.abs(Integer.parseInt(var.getValue())) - 1;
                    if (paramIndex >= 0 && paramIndex < params.length) {
                        String paramValue = params[paramIndex];

                        html.append("<tr>");
                        html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(defaultText) + ";'>")
                                .append(var.getName()).append("</td>");

                        // Check if it's a template reference (starts with TEMPLATE:)
                        if (paramValue != null && paramValue.startsWith("TEMPLATE:")) {
                            String[] parts = paramValue.split(":", 3);
                            if (parts.length == 3) {
                                String templateName = parts[1];
                                String variableName = parts[2];

                                html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(templateColor) + ";font-style:italic;'>")
                                        .append(variableName + " (" + templateName + ")").append("</td>");
                            } else {
                                html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(defaultText) + ";'>")
                                        .append(paramValue).append("</td>");
                            }
                        } else if (isNumeric(paramValue)) {
                            // Check if it's a numeric value for lookup
                            int valueIndex = Integer.parseInt(paramValue);
                            List<String> values = VariableRegistry.getValuesForVariable(var.getName());

                            if (values != null && valueIndex < values.size()) {
                                String value = values.get(valueIndex);
                                html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(defaultText) + ";'>")
                                        .append(value).append("</td>");
                            } else {
                                String value = paramValue;
                                if (isNumeric(value) && findClassByName(var.getName()) != null) {
                                    Object obj = null;
                                    try {
                                        obj = (findClassByName(var.getName())).newInstance();
                                    } catch (InstantiationException a) {
                                        throw new RuntimeException(a);
                                    } catch (IllegalAccessException r) {
                                        throw new RuntimeException(r);
                                    }

                                    if (obj instanceof Variables) {
                                        value = ((Variables) obj).getCombo().get(Integer.parseInt(value));
                                    }
                                }

                                html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(defaultText) + ";'>")
                                        .append(value).append("</td>");
                            }
                        } else {
                            html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(defaultText) + ";'>")
                                    .append(paramValue).append("</td>");
                        }

                        html.append("</tr>");
                    }
                }

                if (var.forceShow && !var.getValue().startsWith("-")) {
                    html.append("<tr>");
                    html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(defaultText) + ";'>")
                            .append(var.getName()).append("</td>");


                    String paramValue = var.getValue();


                    // Check if it's a template reference (starts with TEMPLATE:)
                    if (paramValue != null && paramValue.startsWith("TEMPLATE:")) {
                        String[] parts = paramValue.split(":", 3);
                        if (parts.length == 3) {
                            String templateName = parts[1];
                            String variableName = parts[2];

                            html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(templateColor) + ";font-style:italic;'>")
                                    .append(variableName + " (" + templateName + ")").append("</td>");
                        } else {
                            html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(defaultText) + ";'>")
                                    .append(paramValue).append("</td>");
                        }
                    } else if (isNumeric(paramValue)) {
                        // Check if it's a numeric value for lookup
                        int valueIndex = Integer.parseInt(paramValue);
                        List<String> values = VariableRegistry.getValuesForVariable(var.getName());

                        if (values != null && valueIndex < values.size()) {
                            String value = values.get(valueIndex);
                            html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(defaultText) + ";'>")
                                    .append(value).append("</td>");
                        } else {
                            String value = paramValue;
                            if (isNumeric(value) && findClassByName(var.getName()) != null) {
                                Object obj = null;
                                try {
                                    obj = (findClassByName(var.getName())).newInstance();
                                } catch (InstantiationException a) {
                                    throw new RuntimeException(a);
                                } catch (IllegalAccessException r) {
                                    throw new RuntimeException(r);
                                }

                                if (obj instanceof Variables) {
                                    value = ((Variables) obj).getCombo().get(Integer.parseInt(value));
                                }
                            }

                            html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(defaultText) + ";'>")
                                    .append(value).append("</td>");
                        }
                    } else {
                        html.append("<td style='padding:2px 5px;border:1px solid " + colorToHex(borderColor) + ";color:" + colorToHex(defaultText) + ";'>")
                                .append(paramValue).append("</td>");
                    }




                    html.append("</tr>");
                }
            }
        }

        html.append("</table>");

        // Add notes if present
        if (notes != null && !notes.trim().isEmpty()) {
            html.append("<div style='margin-top:5px;padding:3px;background-color:" + colorToHex(notesBg) + ";");
            html.append("border:1px solid " + colorToHex(notesBorder) + ";font-size:8px;color:" + colorToHex(notesText) + ";'>");
            html.append("").append(notes).append("</div>");
        }

        html.append("</html>");
        return html.toString();
    }

    private String[] parseParameters(String value) {
        String[] params = new String[10];
        Arrays.fill(params, "0");

        int startIndex = value.indexOf('[');
        int endIndex = value.indexOf(']');

        if (startIndex >= 0 && endIndex > startIndex) {
            String paramsStr = value.substring(startIndex + 1, endIndex);
            String[] paramsArray = paramsStr.split(";");

            for (int i = 0; i < Math.min(paramsArray.length, params.length); i++) {
                params[i] = paramsArray[i];
            }
        }

        return params;
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
                // Ignore and continue searching
            }
        }

        return null;
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

    /**
     * Converts a Color to its hex string representation
     */
    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}