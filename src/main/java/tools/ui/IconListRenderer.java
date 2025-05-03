package tools.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A custom list cell renderer that applies a single shared icon to each cell in a list.
 *
 * <p>
 * Usage example:
 * <pre>{@code
 * JList<String> myList = new JList<>(new String[] {"Item A", "Item B", "Item C"});
 * IconListRenderer renderer = new IconListRenderer(myIcon);
 * myList.setCellRenderer(renderer);
 * }</pre>
 * Each list cell will display the specified icon alongside its text.
 */
public class IconListRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    /**
     * The icon to be shown for each item in the list.
     */
    private final Icon icon;

    /**
     * Constructs an {@code IconListRenderer} with the specified icon.
     *
     * @param icon the icon to display next to every list item
     */
    public IconListRenderer(Icon icon) {
        this.icon = icon;
    }

    /**
     * Returns a label component used to paint each cell in the list, ensuring that the given icon is set.
     *
     * @param list         the {@link JList} that is asking the renderer to draw
     * @param value        the value of the cell to be rendered
     * @param index        the cell's index
     * @param isSelected   indicates whether the cell is selected
     * @param cellHasFocus indicates whether the cell has the focus
     * @return a component (specifically a {@link JLabel}) that renders the cell, including the icon
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,boolean isSelected,
                                                  boolean cellHasFocus) {
        // Let the superclass handle default label text/colors/selection, etc.
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Always set the custom icon
        label.setIcon(icon);

        return label;
    }
}
