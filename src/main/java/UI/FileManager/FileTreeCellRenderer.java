package UI.FileManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom {@link javax.swing.tree.TreeCellRenderer} for rendering {@link File} objects
 * within a {@link JTree}. This renderer caches system icons and display names to improve
 * performance when displaying file nodes.
 */
class FileTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final Logger LOG = LogManager.getLogger(FileTreeCellRenderer.class);

    /** Cache for file icons */
    private final Map<File, Icon> iconCache = new HashMap<>();

    /** Cache for file display names */
    private final Map<File, String> displayNameCache = new HashMap<>();

    /** Provides system file details (icons, display names, etc.) */
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    /** Label used for rendering each tree node */
    private final JLabel label;

    /** Colors for selected and non-selected states */
    private final Color backgroundSelectionColor = UIManager.getColor("Tree.selectionBackground");
    private final Color textSelectionColor = UIManager.getColor("Tree.selectionForeground");
    private final Color backgroundNonSelectionColor = UIManager.getColor("Tree.textBackground");
    private final Color textNonSelectionColor = UIManager.getColor("Tree.textForeground");

    /**
     * Constructs a new {@code FileTreeCellRenderer} that uses a {@link JLabel}
     * to render file nodes in a {@link JTree}.
     */
    FileTreeCellRenderer() {
        label = new JLabel();
        label.setOpaque(true);
    }

    /**
     * Returns a component configured to display the specified value in the tree.
     * This method retrieves file icons and display names from caches or computes them
     * using the {@link FileSystemView} if not already cached.
     *
     * @param tree     the {@link JTree} we're painting
     * @param value    the value to be rendered; expected to be a {@link DefaultMutableTreeNode}
     *                 whose user object is a {@link File}
     * @param selected whether the node is selected
     * @param expanded whether the node is expanded
     * @param leaf     whether the node is a leaf node
     * @param row      the row index
     * @param hasFocus whether the node has focus
     * @return a {@link Component} configured for rendering
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        

        if (!(value instanceof DefaultMutableTreeNode node) ||
                !(node.getUserObject() instanceof File file)) {
            return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }

        // Retrieve or compute the icon for the file
        Icon icon = iconCache.computeIfAbsent(file, f -> {
            try {
                return fileSystemView.getSystemIcon(f);
            } catch (Exception e) {
                
                return null;
            }
        });
        label.setIcon(icon);

        // Retrieve or compute the display name for the file
        String displayName = displayNameCache.computeIfAbsent(file, f -> fileSystemView.getSystemDisplayName(f));
        label.setText(displayName);

        // Set the label's colors based on selection state
        if (selected) {
            label.setBackground(backgroundSelectionColor);
            label.setForeground(textSelectionColor);
        } else {
            label.setBackground(backgroundNonSelectionColor);
            label.setForeground(textNonSelectionColor);
        }

        // Some Look-and-Feels might require the label to be non-opaque.
        label.setOpaque(false);

        return label;
    }
}
