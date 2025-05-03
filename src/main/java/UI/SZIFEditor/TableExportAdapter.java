package UI.SZIFEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * Adapter for Excel-like copy/paste/delete operations
 */
class TableExportAdapter implements ActionListener {
    private JTable table;
    private Clipboard clipboard;

    public TableExportAdapter(JTable table) {
        this.table = table;
        this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // Register keyboard shortcuts
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK);
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK);
        KeyStroke selectAll = KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK);

        table.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
        table.registerKeyboardAction(this, "Paste", paste, JComponent.WHEN_FOCUSED);
        table.registerKeyboardAction(this, "Delete", delete, JComponent.WHEN_FOCUSED);
        table.registerKeyboardAction(this, "Cut", cut, JComponent.WHEN_FOCUSED);
        table.registerKeyboardAction(this, "SelectAll", selectAll, JComponent.WHEN_FOCUSED);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Copy":
                copyToClipboard();
                break;
            case "Paste":
                pasteFromClipboard();
                break;
            case "Delete":
                deleteSelection();
                break;
            case "Cut":
                cutSelection();
                break;
            case "SelectAll":
                table.selectAll();
                break;
        }
    }

    private void copyToClipboard() {
        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();

        if (rows.length == 0 || cols.length == 0) {
            return;
        }

        // Build data string without requiring contiguous selection
        StringBuilder sb = new StringBuilder();

        // Get min and max rows/columns to create a rectangular selection
        int minRow = getMinValue(rows);
        int maxRow = getMaxValue(rows);
        int minCol = getMinValue(cols);
        int maxCol = getMaxValue(cols);

        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                // Check if the cell is actually selected
                boolean cellSelected = isCellSelected(i, j, rows, cols);
                Object value = cellSelected ? table.getValueAt(i, j) : "";
                sb.append(value != null ? value : "");
                if (j < maxCol) {
                    sb.append("\t");
                }
            }
            sb.append("\n");
        }

        // Copy to clipboard
        StringSelection selection = new StringSelection(sb.toString());
        clipboard.setContents(selection, selection);
    }

    private void pasteFromClipboard() {
        int startRow = table.getSelectedRow();
        int startCol = table.getSelectedColumn();

        if (startRow < 0 || startCol < 0) {
            return;
        }

        try {
            String content = (String) clipboard.getContents(this)
                    .getTransferData(DataFlavor.stringFlavor);

            // Split into rows and columns
            String[] rows = content.split("\n");
            for (int i = 0; i < rows.length; i++) {
                String[] cells = rows[i].split("\t");
                for (int j = 0; j < cells.length; j++) {
                    int targetRow = startRow + i;
                    int targetCol = startCol + j;

                    if (targetRow < table.getRowCount() &&
                            targetCol < table.getColumnCount()) {
                        table.setValueAt(cells[j], targetRow, targetCol);
                    }
                }
            }

            // Adjust row heights after paste
            if (table instanceof EditorTable) {
                for (int i = 0; i < rows.length; i++) {
                    int targetRow = startRow + i;
                    if (targetRow < table.getRowCount()) {
                        ((EditorTable) table).adjustRowHeight(targetRow);
                    }
                }
            }

            // Excel-like behavior: select the pasted range
            selectPastedRange(startRow, startCol, rows.length,
                    rows.length > 0 ? rows[0].split("\t").length : 0);

        } catch (UnsupportedFlavorException | IOException ex) {
            JOptionPane.showMessageDialog(table, "Error pasting data: " + ex.getMessage());
        }
    }

    private void deleteSelection() {
        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();

        if (rows.length == 0 || cols.length == 0) {
            return;
        }

        // Clear all selected cells
        for (int row : rows) {
            for (int col : cols) {
                table.setValueAt("", row, col);
            }
        }

        // Adjust row heights if needed
        if (table instanceof EditorTable) {
            for (int row : rows) {
                ((EditorTable) table).adjustRowHeight(row);
            }
        }
    }

    private void cutSelection() {
        copyToClipboard();
        deleteSelection();
    }

    private int getMinValue(int[] array) {
        int min = Integer.MAX_VALUE;
        for (int value : array) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    private int getMaxValue(int[] array) {
        int max = Integer.MIN_VALUE;
        for (int value : array) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private boolean isCellSelected(int row, int col, int[] selectedRows, int[] selectedCols) {
        boolean rowSelected = false;
        boolean colSelected = false;

        for (int r : selectedRows) {
            if (r == row) {
                rowSelected = true;
                break;
            }
        }

        for (int c : selectedCols) {
            if (c == col) {
                colSelected = true;
                break;
            }
        }

        return rowSelected && colSelected;
    }

    private void selectPastedRange(int startRow, int startCol, int rowCount, int colCount) {
        if (rowCount <= 0 || colCount <= 0) {
            return;
        }

        // Calculate end indices (inclusive)
        int endRow = Math.min(startRow + rowCount - 1, table.getRowCount() - 1);
        int endCol = Math.min(startCol + colCount - 1, table.getColumnCount() - 1);

        // Select the range
        table.setRowSelectionInterval(startRow, endRow);
        table.setColumnSelectionInterval(startCol, endCol);
    }
}