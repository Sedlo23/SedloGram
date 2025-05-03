package tools.ui;

import tools.string.StringHelper;

import javax.swing.*;
import java.awt.*;

/**
 * A custom {@link ListCellRenderer} that splits the string representation of each item
 * into three parts (left, center, right) separated by colons, then displays them in a
 * monospaced, bordered layout. Useful for presenting structured text data in a modern style.
 *
 * @param <T> the type of the items in the list
 */
public class ModernListCellRenderer<T> implements ListCellRenderer<T> {

    /**
     * Default constructor.
     */
    public ModernListCellRenderer() {
        // No initialization needed
    }

    /**
     * Returns the component used for drawing the cell. The string representation of the value
     * is split by colons into three parts which are displayed in a panel with a monospaced font.
     *
     * @param list         the JList we're painting
     * @param value        the value returned by list.getModel().getElementAt(index)
     * @param index        the cell's index
     * @param isSelected   true if the specified cell was selected
     * @param cellHasFocus true if the cell has focus
     * @return the component used for drawing the cell
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends T> list,
                                                  T value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        // Create labels for each portion of the split text.
        String text = value.toString().replace("</html>", "");
        String[] parts = text.split(":");

        JLabel leftLabel = new JLabel(parts.length > 0 ? parts[0].replace(" ", "") : "");
        JLabel centerLabel = new JLabel(parts.length > 1 ? parts[1] : "");
        JLabel rightLabel = new JLabel(parts.length > 2 ? parts[2].replace(" ", "") : "");

        // Remove underscore and pad left label
        int underscoreIndex = leftLabel.getText().indexOf('_');
        if (underscoreIndex != -1) {
            leftLabel.setText(leftLabel.getText().substring(0, underscoreIndex));
        }
        // Pad left label to ensure consistent width
        leftLabel.setText(StringHelper.padLeft(leftLabel.getText(), 4, ' ') + "  ");
        rightLabel.setText(" | " + rightLabel.getText());

        // Set horizontal alignment for center and right labels
        centerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rightLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Create a panel with BorderLayout to hold the three labels
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(leftLabel, BorderLayout.WEST);
        panel.add(centerLabel, BorderLayout.CENTER);
        panel.add(rightLabel, BorderLayout.EAST);

        // Use a monospaced font for consistent alignment
        Font monoFont = new Font("Monospaced", Font.PLAIN, 14);
        leftLabel.setFont(monoFont);
        centerLabel.setFont(monoFont);
        rightLabel.setFont(monoFont);

        // Retrieve current Look and Feel colors
        Color backgroundColor = UIManager.getColor("List.background");
        Color selectionBackground = UIManager.getColor("List.selectionBackground");
        Color foregroundColor = UIManager.getColor("List.foreground");
        Color selectionForeground = UIManager.getColor("List.selectionForeground");

        // Set background and foreground based on selection state
        panel.setBackground(isSelected ? selectionBackground : backgroundColor);
        leftLabel.setForeground(isSelected ? selectionForeground : foregroundColor);
        centerLabel.setForeground(isSelected ? selectionForeground : foregroundColor);
        rightLabel.setForeground(isSelected ? selectionForeground : foregroundColor);
        panel.setForeground(isSelected ? selectionForeground : foregroundColor);

        return panel;
    }
}
