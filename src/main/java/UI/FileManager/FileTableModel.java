package UI.FileManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A modern {@link javax.swing.table.TableModel} for displaying an array of {@link File} objects,
 * including icons and file metadata with enhanced formatting and system colors.
 */
class FileTableModel extends AbstractTableModel {

    private static final Logger LOG = LogManager.getLogger(FileTableModel.class);

    /**
     * The array of {@link File} objects displayed in the table.
     */
    private File[] files;

    /**
     * Provides file-related system services, such as icon retrieval and display name.
     */
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    /**
     * Date formatter for last modified column.
     */
    private final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    /**
     * Column names for the modern table display.
     */
    private final String[] columns = {
            "",           // 0 - Icon
            "Název",      // 1 - File name
            "Cesta",      // 2 - Path
            "Velikost",   // 3 - Size
            "Změněno",    // 4 - Last modified
            "Typ"         // 5 - File type
    };

    /**
     * Default constructor initializing with an empty array of files.
     */
    FileTableModel() {
        this(new File[0]);
    }

    /**
     * Constructs the model with a specific array of files.
     *
     * @param files the files to display in the table
     */
    FileTableModel(File[] files) {
        this.files = files != null ? files : new File[0];
    }

    /**
     * Returns the number of table columns.
     *
     * @return the total number of columns
     */
    @Override
    public int getColumnCount() {
        return columns.length;
    }

    /**
     * Returns the number of rows.
     *
     * @return the total number of rows
     */
    @Override
    public int getRowCount() {
        return files.length;
    }

    /**
     * Gets the column name for the specified index.
     *
     * @param column the column index
     * @return the name of the column
     */
    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    /**
     * Returns the class type for the specified column for proper rendering.
     *
     * @param column the column index
     * @return the {@link Class} of data in that column
     */
    @Override
    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case 0 -> ImageIcon.class;  // icon
            case 3 -> Long.class;       // file size
            case 4 -> Date.class;       // last modified
            default -> String.class;
        };
    }

    /**
     * Retrieves the value at the specified row and column with modern formatting.
     *
     * @param row    the row index
     * @param column the column index
     * @return the appropriate value for that cell
     */
    @Override
    public Object getValueAt(int row, int column) {
        if (row < 0 || row >= files.length) {
            return null;
        }

        File file = files[row];
        return switch (column) {
            case 0 -> getFileIcon(file);
            case 1 -> getDisplayName(file);
            case 2 -> file.getPath();
            case 3 -> file.length();
            case 4 -> new Date(file.lastModified());
            case 5 -> getFileType(file);
            default -> "";
        };
    }

    /**
     * Gets the system icon for a file with error handling.
     *
     * @param file the file to get icon for
     * @return the system icon or a default icon
     */
    private Icon getFileIcon(File file) {
        try {
            return fileSystemView.getSystemIcon(file);
        } catch (Exception e) {
            LOG.warn("Nepodařilo se získat ikonu pro soubor: {}", file.getName());
            // Return a default icon or null
            return UIManager.getIcon("FileView.fileIcon");
        }
    }

    /**
     * Gets the display name for a file with error handling.
     *
     * @param file the file to get display name for
     * @return the display name
     */
    private String getDisplayName(File file) {
        try {
            String displayName = fileSystemView.getSystemDisplayName(file);
            return displayName != null && !displayName.trim().isEmpty() ?
                    displayName : file.getName();
        } catch (Exception e) {
            LOG.warn("Nepodařilo se získat display name pro soubor: {}", file.getName());
            return file.getName();
        }
    }

    /**
     * Gets a user-friendly file type description.
     *
     * @param file the file to get type for
     * @return the file type description
     */
    private String getFileType(File file) {
        if (file.isDirectory()) {
            return "📁 Složka";
        }

        String name = file.getName().toLowerCase();
        if (name.endsWith(".tlg")) {
            return "🚄 TLG Telegram";
        } else if (name.endsWith(".pdf")) {
            return "📄 PDF Dokument";
        } else if (name.endsWith(".txt")) {
            return "📝 Textový soubor";
        } else if (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif")) {
            return "🖼️ Obrázek";
        } else if (name.endsWith(".zip") || name.endsWith(".rar") ||
                name.endsWith(".7z")) {
            return "🗜️ Archiv";
        } else {
            // Try to get system type description
            try {
                String systemType = fileSystemView.getSystemTypeDescription(file);
                return systemType != null && !systemType.trim().isEmpty() ?
                        systemType : "📄 Soubor";
            } catch (Exception e) {
                return "📄 Soubor";
            }
        }
    }

    /**
     * Formats file size into human-readable format.
     *
     * @param size size in bytes
     * @return formatted size string
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Returns the file at a given row index.
     *
     * @param row the row index
     * @return the file at that row
     * @throws ArrayIndexOutOfBoundsException if the row is out of range
     */
    public File getFile(int row) {
        if (row < 0 || row >= files.length) {
            throw new ArrayIndexOutOfBoundsException("Row index out of range: " + row);
        }
        return files[row];
    }

    /**
     * Updates the entire table model with a new array of files.
     *
     * @param files the new array of {@link File} objects
     */
    public void setFiles(File[] files) {
        this.files = files != null ? files : new File[0];
        fireTableDataChanged();
        LOG.debug("Table model updated with {} files", this.files.length);
    }

    /**
     * Adds a single file to the model.
     *
     * @param file the file to add
     */
    public void addFile(File file) {
        if (file == null) return;

        File[] newFiles = new File[files.length + 1];
        System.arraycopy(files, 0, newFiles, 0, files.length);
        newFiles[files.length] = file;

        this.files = newFiles;
        fireTableRowsInserted(files.length - 1, files.length - 1);
    }

    /**
     * Removes a file from the model.
     *
     * @param file the file to remove
     * @return true if the file was found and removed
     */
    public boolean removeFile(File file) {
        if (file == null) return false;

        for (int i = 0; i < files.length; i++) {
            if (files[i].equals(file)) {
                File[] newFiles = new File[files.length - 1];
                System.arraycopy(files, 0, newFiles, 0, i);
                System.arraycopy(files, i + 1, newFiles, i, files.length - i - 1);

                this.files = newFiles;
                fireTableRowsDeleted(i, i);
                return true;
            }
        }
        return false;
    }

    /**
     * Clears all files from the model.
     */
    public void clear() {
        int oldSize = files.length;
        this.files = new File[0];
        if (oldSize > 0) {
            fireTableRowsDeleted(0, oldSize - 1);
        }
    }

    /**
     * Returns the number of files in the model.
     *
     * @return the number of files
     */
    public int getFileCount() {
        return files.length;
    }

    /**
     * Checks if the model is empty.
     *
     * @return true if no files are present
     */
    public boolean isEmpty() {
        return files.length == 0;
    }
}