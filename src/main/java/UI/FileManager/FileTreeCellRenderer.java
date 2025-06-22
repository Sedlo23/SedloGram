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
 * A modern custom {@link javax.swing.tree.TreeCellRenderer} for rendering {@link File} objects
 * within a {@link JTree} with FlatLaf styling and improved performance through caching.
 */
class FileTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final Logger LOG = LogManager.getLogger(FileTreeCellRenderer.class);

    /** Cache for file icons to improve performance */
    private final Map<String, Icon> iconCache = new HashMap<>();

    /** Cache for file display names to improve performance */
    private final Map<String, String> displayNameCache = new HashMap<>();

    /** Provides system file details (icons, display names, etc.) */
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    /** Custom label for modern rendering */
    private final JLabel renderLabel;

    /** Modern colors from UIManager */
    private final Color backgroundSelectionColor;
    private final Color textSelectionColor;
    private final Color backgroundNonSelectionColor;
    private final Color textNonSelectionColor;
    private final Color borderColor;

    /** Modern font for file names */
    private final Font fileFont;

    /**
     * Constructs a new modern {@code FileTreeCellRenderer}.
     */
    FileTreeCellRenderer() {
        // Initialize custom label
        renderLabel = new JLabel();
        renderLabel.setOpaque(false);

        // Initialize modern colors with fallbacks
        backgroundSelectionColor = getUIColor("Tree.selectionBackground", new Color(0, 123, 255, 50));
        textSelectionColor = getUIColor("Tree.selectionForeground", Color.BLACK);
        backgroundNonSelectionColor = getUIColor("Tree.background", Color.WHITE);
        textNonSelectionColor = getUIColor("Tree.foreground", Color.BLACK);
        borderColor = getUIColor("Component.borderColor", new Color(220, 220, 220));

        // Initialize modern font
        Font systemFont = UIManager.getFont("Tree.font");
        if (systemFont != null) {
            fileFont = systemFont.deriveFont(Font.PLAIN, 12f);
        } else {
            fileFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
    }

    /**
     * Safely gets a color from UIManager with fallback.
     */
    private Color getUIColor(String key, Color fallback) {
        Color color = UIManager.getColor(key);
        return color != null ? color : fallback;
    }

    /**
     * Returns a component configured to display the specified value in the tree
     * with modern styling and improved performance through caching.
     *
     * @param tree     the {@link JTree} we're painting
     * @param value    the value to be rendered
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

        // Check if this is a file node
        if (!(value instanceof DefaultMutableTreeNode node) ||
                !(node.getUserObject() instanceof File file)) {
            return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }

        // Configure the custom label
        setupLabelForFile(file, selected);

        return renderLabel;
    }

    /**
     * Sets up the render label for a specific file with modern styling.
     *
     * @param file     the file to render
     * @param selected whether the node is selected
     */
    private void setupLabelForFile(File file, boolean selected) {
        // Get cached or compute icon
        Icon icon = getFileIcon(file);
        renderLabel.setIcon(icon);

        // Get cached or compute display name
        String displayName = getFileDisplayName(file);
        renderLabel.setText(displayName);

        // Apply modern styling
        renderLabel.setFont(fileFont);
        renderLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        // Set colors based on selection state
        if (selected) {
            renderLabel.setForeground(textSelectionColor);
            renderLabel.setOpaque(true);
            renderLabel.setBackground(backgroundSelectionColor);
        } else {
            renderLabel.setForeground(textNonSelectionColor);
            renderLabel.setOpaque(false);
        }

        // Add special styling for different file types
        applyFileTypeSpecificStyling(file, selected);
    }

    /**
     * Gets the icon for a file from cache or computes it.
     *
     * @param file the file to get icon for
     * @return the file icon
     */
    private Icon getFileIcon(File file) {
        String cacheKey = file.getAbsolutePath();

        return iconCache.computeIfAbsent(cacheKey, key -> {
            try {
                Icon systemIcon = fileSystemView.getSystemIcon(file);

                // If no system icon, provide type-specific icons
                if (systemIcon == null) {
                    return getDefaultIconForFileType(file);
                }

                return systemIcon;
            } catch (Exception e) {
                LOG.warn("Chyba při získávání ikony pro soubor: {}", file.getName());
                return getDefaultIconForFileType(file);
            }
        });
    }

    /**
     * Gets the display name for a file from cache or computes it.
     *
     * @param file the file to get display name for
     * @return the display name
     */
    private String getFileDisplayName(File file) {
        String cacheKey = file.getAbsolutePath();

        return displayNameCache.computeIfAbsent(cacheKey, key -> {
            try {
                String systemName = fileSystemView.getSystemDisplayName(file);
                return (systemName != null && !systemName.trim().isEmpty()) ?
                        systemName : file.getName();
            } catch (Exception e) {
                LOG.warn("Chyba při získávání display name pro soubor: {}", file.getName());
                return file.getName();
            }
        });
    }

    /**
     * Provides default icons for different file types when system icons are unavailable.
     *
     * @param file the file to get default icon for
     * @return a default icon
     */
    private Icon getDefaultIconForFileType(File file) {
        if (file.isDirectory()) {
            return UIManager.getIcon("FileView.directoryIcon");
        }

        String name = file.getName().toLowerCase();
        if (name.endsWith(".tlg")) {
            // Could create a custom icon for TLG files
            return UIManager.getIcon("FileView.fileIcon");
        } else if (name.endsWith(".pdf")) {
            return UIManager.getIcon("FileView.fileIcon");
        } else {
            return UIManager.getIcon("FileView.fileIcon");
        }
    }

    /**
     * Applies file type specific styling (like different colors for different file types).
     *
     * @param file     the file being rendered
     * @param selected whether the node is selected
     */
    private void applyFileTypeSpecificStyling(File file, boolean selected) {
        if (selected) {
            return; // Don't apply type-specific styling for selected items
        }

        String fileName = file.getName().toLowerCase();

        if (file.isDirectory()) {
            // Directories could have a different color
            Color directoryColor = getUIColor("Tree.textForeground", textNonSelectionColor);
            renderLabel.setForeground(directoryColor);
        } else if (fileName.endsWith(".tlg")) {
            // TLG files could have a special color
            Color tlgColor = getUIColor("Component.accentColor", new Color(0, 123, 255));
            if (tlgColor != null) {
                renderLabel.setForeground(tlgColor);
            }
        } else if (fileName.endsWith(".pdf")) {
            // PDF files could have their own color
            Color pdfColor = getUIColor("Actions.Red", new Color(220, 53, 69));
            if (pdfColor != null) {
                renderLabel.setForeground(pdfColor);
            }
        }
    }

    /**
     * Clears the caches to free memory. Call this when the tree is being disposed
     * or when memory usage becomes a concern.
     */
    public void clearCaches() {
        iconCache.clear();
        displayNameCache.clear();
        LOG.debug("FileTreeCellRenderer caches cleared");
    }

    /**
     * Gets the current cache size for monitoring purposes.
     *
     * @return the total number of cached items
     */
    public int getCacheSize() {
        return iconCache.size() + displayNameCache.size();
    }
}