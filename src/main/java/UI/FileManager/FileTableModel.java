package UI.FileManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import java.io.File;

/**
 * A {@link javax.swing.table.TableModel} for displaying an array of {@link File} objects,
 * including icons and file metadata such as name, path, size, and last-modified time (or MD4).
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
     * Column names for the table display. Adjust or rename as necessary to match your desired
     * file attributes (e.g., MD4, timestamps).
     */
    private final String[] columns = {
            "Icon",     // 0
            "Soubor",   // 1
            "Cesta",    // 2
            "Velikost", // 3
            "MD4"       // 4 (currently storing lastModified as a placeholder)
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
        this.files = files;
    }

    /**
     * Returns the number of table columns, corresponding to the length of {@link #columns}.
     *
     * @return the total number of columns
     */
    @Override
    public int getColumnCount() {
        return columns.length;
    }

    /**
     * Returns the number of rows, which is the number of {@link File} objects in the model.
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
     * Returns the class type for the specified column, which affects how the table will
     * render the values.
     *
     * @param column the column index
     * @return the {@link Class} of data in that column
     */
    @Override
    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case 0 -> ImageIcon.class; // icon
            case 3 -> Long.class;      // file length
            case 4 -> Long.class;      // lastModified time or MD4
            default -> String.class;
        };
    }

    /**
     * Retrieves the value at the specified row and column. This method drives how the table displays
     * icons and file attributes.
     *
     * @param row    the row index
     * @param column the column index
     * @return the appropriate value for that cell
     */
    @Override
    public Object getValueAt(int row, int column) {
        File file = files[row];
        return switch (column) {
            case 0 -> fileSystemView.getSystemIcon(file);
            case 1 -> fileSystemView.getSystemDisplayName(file);
            case 2 -> file.getPath();
            case 3 -> file.length();
            case 4 -> file.lastModified(); // Currently storing lastModified time. Replace with MD4, if needed.
            default -> {
                
                yield "";
            }
        };
    }

    /**
     * Returns the file at a given row index. Useful for retrieving a full {@link File} object
     * from the table model.
     *
     * @param row the row index
     * @return the file at that row
     * @throws ArrayIndexOutOfBoundsException if the row is out of range
     */
    public File getFile(int row) {
        return files[row];
    }

    /**
     * Updates the entire table model with a new array of files, firing a data change event
     * so the UI can refresh.
     *
     * @param files the new array of {@link File} objects
     */
    public void setFiles(File[] files) {
        
        this.files = files;
        fireTableDataChanged();
    }
}
