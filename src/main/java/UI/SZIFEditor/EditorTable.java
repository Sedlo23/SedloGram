package UI.SZIFEditor;

import UI.SZIFEditor.PacketCellEditor;
import UI.SZIFEditor.PacketCellRenderer;
import UI.SZIFEditor.TableExportAdapter;
import UI.SZIFEditor.TableRowData;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table for editing telegram data with auto-sizing capabilities
 */
public class EditorTable extends JTable implements EditorController.TableDataLoadListener  {
    public static final String[] COLUMN_NAMES = {
            "M_VERSION", "N_PIG", "N_TOTAL", "M_DUP", "M_MCOUNT",
            "NID_C", "NID_BG", "Q_LINK", "P1", "P2", "P3", "P4",
            "P5", "P6", "P7", "P8", "P9", "Notes"
    };

    private EditorController controller;
    private DefaultTableModel tableModel;
    private TableExportAdapter exportAdapter;
    private boolean skipAdjustment = false;

    public EditorTable(EditorController controller) {
        this.controller = controller;


        // Initialize table model
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        setModel(tableModel);

        // Add 200 empty rows
        for (int i = 0; i < 200; i++) {
            tableModel.addRow(new Object[COLUMN_NAMES.length]);
        }

        // Configure table appearance
        setRowHeight(40);



        PacketCellRenderer cellEditor = new PacketCellRenderer(controller);
        setDefaultRenderer(String.class, cellEditor);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        getTableHeader().setReorderingAllowed(false);

        // Configure selection model
        //setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellSelectionEnabled(true);

        // Set custom editor for packet cells
        for (int i = 8; i < 17; i++) {
            getColumnModel().getColumn(i).setCellEditor(new PacketCellEditor( controller));
        }
        setDefaultEditor(String.class, new AutoAdjustingCellEditor());

        // Add right-click context menu
        setupContextMenu();

        // Add copy/paste support
        exportAdapter = new TableExportAdapter(this);

        // Add listener to adjust row heights based on content
        getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (row >= 0) {
                    adjustRowHeight(row);
                }
            }
        });

        // Register for repository changes to refresh data
        controller.addRepositoryChangeListener(this::refreshAfterPacketChanges);
        controller.addRepositoryChangeListener(new Runnable() {
            @Override
            public void run() {
                cellEditor.clearRendererCache();
            }
        });
    }

    /**
     * Refresh table display after packet repository changes
     */
    private void refreshAfterPacketChanges() {
        // Repaint to refresh cell renderers
        repaint();

        // Adjust row heights to accommodate any changes in rendering
        SwingUtilities.invokeLater(this::adjustAllRowHeights);
    }

    private void setupContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem insertRowItem = new JMenuItem("Insert Row");
        insertRowItem.addActionListener(e -> {
            int selectedRow = getSelectedRow();
            if (selectedRow != -1) {
                tableModel.insertRow(selectedRow, new Object[COLUMN_NAMES.length]);
            }
        });

        JMenuItem deleteRowItem = new JMenuItem("Delete Row");
        deleteRowItem.addActionListener(e -> {
            int selectedRow = getSelectedRow();
            if (selectedRow != -1) {
                tableModel.removeRow(selectedRow);
            }
        });

        JMenuItem clearCellItem = new JMenuItem("Clear Cell");
        clearCellItem.addActionListener(e -> {
            int[] selectedRows = getSelectedRows();
            int[] selectedCols = getSelectedColumns();

            for (int row : selectedRows) {
                for (int col : selectedCols) {
                    setValueAt("", row, col);
                }
            }
        });

        popupMenu.add(insertRowItem);
        popupMenu.add(deleteRowItem);
        popupMenu.addSeparator();
        popupMenu.add(clearCellItem);

        setComponentPopupMenu(popupMenu);
    }

    public void setColumnVisible(int columnIndex, boolean visible) {
        TableColumn column = getColumnModel().getColumn(columnIndex);
        if (visible) {
            column.setMinWidth(15);
            column.setMaxWidth(250);
            column.setPreferredWidth(150);
        } else {
            column.setMinWidth(0);
            column.setMaxWidth(0);
            column.setPreferredWidth(0);
        }
    }

    public List<TableRowData> getTableData() {
        List<TableRowData> result = new ArrayList<>();

        for (int row = 0; row < getRowCount(); row++) {
            TableRowData rowData = new TableRowData();
            boolean hasData = false;

            for (int col = 0; col < getColumnCount(); col++) {
                String value = (String) getValueAt(row, col);
                rowData.setValue(col, value != null ? value : "");

                if (col < 8 && value != null && !value.isEmpty()) {
                    hasData = true;
                }
            }

            if (hasData) {
                result.add(rowData);
            }
        }

        return result;
    }

    /**
     * Sets whether to skip auto-adjustment (useful during editing)
     */
    public void setSkipAdjustment(boolean skip) {
        this.skipAdjustment = skip;
    }

    /**
     * Adjusts the row height based on cell content
     */
    public void adjustRowHeight(int row) {
        if (skipAdjustment) {
            return; // Skip adjustment if flag is set
        }

        int maxHeight = getRowHeight(); // Default height

        for (int col = 0; col < getColumnCount(); col++) {
            TableCellRenderer renderer = getCellRenderer(row, col);
            Component comp = prepareRenderer(renderer, row, col);
            int height = comp.getPreferredSize().height + 6;
            maxHeight = Math.max(maxHeight, height);
        }

        if (maxHeight != getRowHeight(row)) {
            setRowHeight(row, maxHeight);
        }
    }

    /**
     * Adjusts all row heights in the table
     */
    public void adjustAllRowHeights() {
        for (int row = 0; row < getRowCount(); row++) {
            String value = "";
            for (int col = 8; col < 17; col++) {
                Object cellValue = getValueAt(row, col);
                if (cellValue != null && !cellValue.toString().isEmpty()) {
                    value = cellValue.toString();
                    if (value.contains("[")) {
                        adjustRowHeight(row);
                        break;
                    }
                }
            }
        }
    }


    // Add this method to EditorTable class
    // Add this method to EditorTable class
    public List<TableRowData> getVisuallyFilledRows() {
        List<TableRowData> filledRows = new ArrayList<>();
        Map<Integer, String> lastNonEmptyValues = new HashMap<>();

        // First find the last row with any non-empty cell
        int lastNonEmptyRow = -1;
        for (int i = 0; i < getRowCount(); i++) {
            boolean hasData = false;
            for (int col = 0; col < getColumnCount(); col++) {
                Object value = getValueAt(i, col);
                if (value != null && !value.toString().isEmpty()) {
                    hasData = true;
                    break;
                }
            }
            if (hasData) {
                lastNonEmptyRow = i;
            }
        }

        // If no data found, return empty list
        if (lastNonEmptyRow == -1) {
            return filledRows;
        }

        // Now process only rows up to the last non-empty one
        for (int i = 0; i <= lastNonEmptyRow; i++) {
            boolean hasVisibleData = false;
            TableRowData tempRow = new TableRowData();

            // For standard columns (0-7), check if they would appear filled
            for (int col = 0; col < 8; col++) {
                Object value = getValueAt(i, col);
                String strValue = (value != null) ? value.toString() : "";

                if (!strValue.isEmpty()) {
                    // Actual data in this cell
                    tempRow.setValue(col, strValue);
                    lastNonEmptyValues.put(col, strValue);
                    hasVisibleData = true;
                } else if (lastNonEmptyValues.containsKey(col)) {
                    // No actual data, but would be filled by renderer
                    tempRow.setValue(col, lastNonEmptyValues.get(col));
                    hasVisibleData = true;
                }
            }

            // For packet columns (8-16), check if they have data
            for (int col = 8; col < 17; col++) {
                Object value = getValueAt(i, col);
                String strValue = (value != null) ? value.toString() : "";

                if (!strValue.isEmpty()) {
                    tempRow.setValue(col, strValue);
                    hasVisibleData = true;
                }
            }

            // Notes column
            Object notes = getValueAt(i, 17);
            if (notes != null && !notes.toString().isEmpty()) {
                tempRow.setValue(17, notes.toString());
            }

            // If this row would appear to have data in the UI, add it to visible rows
            if (hasVisibleData) {
                filledRows.add(tempRow);
            }
        }

        return filledRows;
    }
    /**
     * Adjusts column widths based on content
     */

    public void adjustColumnWidths() {
        for (int col = 0; col < getColumnCount(); col++) {
            TableColumn column = getColumnModel().getColumn(col);
            if (column.getWidth() > 0) { // Only if visible
                int maxWidth = getColumnName(col).length() * 10 + 5; // Header width

                for (int row = 0; row < getRowCount(); row++) {
                    TableCellRenderer renderer = getCellRenderer(row, col);
                    Component comp = prepareRenderer(renderer, row, col);
                    int width = comp.getPreferredSize().width + 10;
                    maxWidth = Math.max(maxWidth, width);
                }


                if (col>7 && col<17)
                    maxWidth = Math.max(maxWidth, 25);
                else
                {
                    maxWidth = Math.min(maxWidth, 100);
                }

                if (col==0)maxWidth =45;
                if (col==1)maxWidth =30;
                if (col==2)maxWidth =50;
                if (col==3)maxWidth =100;
                if (col==4)maxWidth =45;
                if (col==5)maxWidth =75;
                if (col==6)maxWidth =50;
                if (col==7)maxWidth =75;
                if (col==17)maxWidth =150;


                column.setPreferredWidth(maxWidth);
            }
        }

        // Also adjust row heights
        adjustAllRowHeights();
    }

    /**
     * Custom cell editor for automatic height adjustment
     */
    private class AutoAdjustingCellEditor extends DefaultCellEditor {
        private JTextArea textArea;
        private JScrollPane scrollPane;

        public AutoAdjustingCellEditor() {
            super(new JTextField());

            textArea = new JTextArea();
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setFont(new Font("Arial", Font.PLAIN, 12));

            scrollPane = new JScrollPane(textArea);
            scrollPane.setBorder(null);

            // Listen for key events to adjust size
            textArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        int preferredHeight = textArea.getPreferredSize().height;
                        int rowHeight = Math.max(40, preferredHeight + 10);
                        scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth(), rowHeight));
                        scrollPane.revalidate();
                    });
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            textArea.setText(value != null ? value.toString() : "");

            // Calculate initial height
            int preferredHeight = textArea.getPreferredSize().height;
            int rowHeight = Math.max(40, preferredHeight + 10);
            scrollPane.setPreferredSize(new Dimension(300, rowHeight));

            return scrollPane;
        }

        @Override
        public Object getCellEditorValue() {
            return textArea.getText();
        }

        @Override
        public boolean stopCellEditing() {
            boolean result = super.stopCellEditing();
            if (result) {
                // Adjust row height after editing completes
                SwingUtilities.invokeLater(() -> {
                    int editingRow = EditorTable.this.getEditingRow();
                    if (editingRow >= 0) {
                        adjustRowHeight(editingRow);
                    }
                });
            }
            return result;
        }
    }

    /**
     * Loads data into the table from an external source
     */
    public void onTableDataLoaded(List<TableRowData> rows, boolean[] columnVisibility) {
        logDebug("onTableDataLoaded called with " + rows.size() + " rows");

        // First clear existing data
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                tableModel.setValueAt("", i, j);
            }
        }

        // Set column visibility if provided
        if (columnVisibility != null && columnVisibility.length == COLUMN_NAMES.length) {
            for (int i = 0; i < columnVisibility.length; i++) {
                setColumnVisible(i, columnVisibility[i]);
            }
        }

        // Load the data rows at their specific indices
        for (int i = 0; i < rows.size(); i++) {
            TableRowData rowData = rows.get(i);
            if (rowData == null) {
                logDebug("Row " + i + " is null, skipping");
                continue;
            }

            // Make sure we have enough rows
            while (i >= tableModel.getRowCount()) {
                tableModel.addRow(new Object[COLUMN_NAMES.length]);
            }

            // Set the values for this row
            for (int j = 0; j < COLUMN_NAMES.length; j++) {
                String value = rowData.getValue(j);
                if (value != null && !value.isEmpty()) {
                    logDebug("Setting value at [" + i + "," + j + "]: " + value);
                    tableModel.setValueAt(value, i, j);
                }
            }
        }

        logDebug("Finished loading " + rows.size() + " rows into table");

        // Adjust all row heights and column widths
        SwingUtilities.invokeLater(() -> {
            adjustAllRowHeights();
            adjustColumnWidths();
        });
    }

    private void logDebug(String message) {
        System.out.println("[EditorTable] " + message);
    }
}
