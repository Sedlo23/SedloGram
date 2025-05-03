package UI.SZIFEditor;

import UI.FileManager.FileManager;

import tools.ui.TlgTemp;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main application panel containing tabs
 */
class MainPanel extends JPanel {
    private EditorController controller;
    private JTabbedPane tabbedPane;
    private EditorTable editorTable;
    private PacketEditorPanel packetEditorPanel;
    private TelegramGeneratorPanel generatorPanel;
    private FileManager fileManager;

    public MainPanel(EditorController controller, DefaultListModel<TlgTemp> telegramList2, FileManager fileManager) {
        this.controller = controller;
        this.fileManager = fileManager;
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        // Create editor table panel
        editorTable = new EditorTable(controller);



        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(editorTable), BorderLayout.CENTER);

        // Register the table as a data load listener
        controller.addTableDataLoadListener(editorTable);

        // Create control panel for buttons
        JPanel tableControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Add button to adjust column widths
        JButton adjustButton = new JButton("Adjust Column Widths");
        adjustButton.addActionListener(e -> editorTable.adjustColumnWidths());
        tableControlPanel.add(adjustButton);

        // Add button to save table data
        JButton saveButton = new JButton("Save Table Data");
        saveButton.addActionListener(e -> saveTableData());
        tableControlPanel.add(saveButton);

        // Add button to load table data
        JButton loadButton = new JButton("Load Table Data");
        loadButton.addActionListener(e -> loadTableData());
        tableControlPanel.add(loadButton);

        tablePanel.add(tableControlPanel, BorderLayout.NORTH);

        // Create packet editor panel
        packetEditorPanel = new PacketEditorPanel(controller);

        // Create telegram generator panel
        generatorPanel = new TelegramGeneratorPanel(controller, editorTable, telegramList2);

        // Add tabs
        tabbedPane.addTab("Table Editor", tablePanel);
        tabbedPane.addTab("Packet Templates", packetEditorPanel);
        tabbedPane.addTab("Generator", generatorPanel);

        // Add listener to adjust column widths when switching to table tab
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                SwingUtilities.invokeLater(() -> editorTable.adjustColumnWidths());
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Toggles column visibility in editor table
     */
    public void toggleColumnVisibility(int columnIndex, boolean visible) {
        editorTable.setColumnVisible(columnIndex, visible);

        // Adjust column widths after toggling visibility
        SwingUtilities.invokeLater(() -> editorTable.adjustColumnWidths());
    }

    /**
     * Saves table data to file
     */
    private void saveTableData() {
        File currentFolder = fileManager.getCurrentFolder();
        if (currentFolder != null && currentFolder.isDirectory()) {
            // Create a default filename based on current date/time
            String defaultFilename = "table_data_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".stbl";

            // Prompt for filename while showing the default
            String filename = JOptionPane.showInputDialog(
                    this,
                    "Enter filename to save table data:",
                    defaultFilename
            );

            if (filename != null && !filename.trim().isEmpty()) {
                // Add extension if needed
                if (!filename.toLowerCase().endsWith(".stbl")) {
                    filename += ".stbl";
                }

                // Create the file in the current folder
                File saveFile = new File(currentFolder, filename);
                saveAllDataToFile(saveFile);
            }
        } else {
            // If no current folder is selected, prompt for one
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Folder to Save Table Data");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = fileChooser.getSelectedFile();

                // Create a default filename
                String defaultFilename = "table_data_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".stbl";

                // Prompt for filename
                String filename = JOptionPane.showInputDialog(
                        this,
                        "Enter filename to save table data:",
                        defaultFilename
                );

                if (filename != null && !filename.trim().isEmpty()) {
                    // Add extension if needed
                    if (!filename.toLowerCase().endsWith(".stbl")) {
                        filename += ".stbl";
                    }

                    // Create the file in the selected folder
                    File saveFile = new File(selectedFolder, filename);
                    saveAllDataToFile(saveFile);
                }
            }
        }
    }

    /**
     * Loads table data from file
     */
    private void loadTableData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Table Data File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                return f.getName().toLowerCase().endsWith(".stbl");
            }

            @Override
            public String getDescription() {
                return "SZIF Table Files (*.stbl)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            controller.loadDataFromFile(selectedFile);
        }
    }

    /**
     * Saves both table data and packet editor data to a custom file format
     * @param file The file to save to
     */
    private void saveAllDataToFile(File file) {
        try {
            // Create a StringBuilder to build our file content
            StringBuilder fileContent = new StringBuilder();

            // Add file header with version
            fileContent.append("SZIF_TABLE_DATA_v1.0\n");

            // Save column visibility states
            fileContent.append("COLUMNS:");
            for (int i = 0; i < EditorTable.COLUMN_NAMES.length; i++) {
                boolean isVisible = editorTable.getColumnModel().getColumn(i).getWidth() > 0;
                fileContent.append(isVisible ? "1" : "0");
                if (i < EditorTable.COLUMN_NAMES.length - 1) {
                    fileContent.append(",");
                }
            }
            fileContent.append("\n");

            // Main editor table data section
            fileContent.append("EDITOR_TABLE_START\n");

            // Save only the actual data in the table (not rendered values)
            // For each row in the table
            for (int rowIndex = 0; rowIndex < editorTable.getRowCount(); rowIndex++) {
                boolean hasData = false;
                StringBuilder rowStr = new StringBuilder();

                // First add the row index
                rowStr.append(rowIndex).append(":");

                // Then add each column value
                for (int col = 0; col < EditorTable.COLUMN_NAMES.length; col++) {
                    Object cellValue = editorTable.getValueAt(rowIndex, col);
                    String value = (cellValue != null) ? cellValue.toString() : "";

                    // Check if this cell has data
                    if (!value.trim().isEmpty()) {
                        hasData = true;
                    }

                    // Escape any delimiter characters in the value
                    value = value.replace("\\", "\\\\").replace("|", "\\|");
                    rowStr.append(value);

                    if (col < EditorTable.COLUMN_NAMES.length - 1) {
                        rowStr.append("|");
                    }
                }

                // Only write rows that have actual data
                if (hasData) {
                    fileContent.append(rowStr).append("\n");
                }
            }

            fileContent.append("EDITOR_TABLE_END\n");

            // Save Packet Editor data
            fileContent.append("PACKET_TEMPLATES_START\n");

// Get all packet templates from the controller
            List<PacketTemplate> packetTemplates = controller.getPackets();
            List<PacketTemplate> templatePackets = controller.getTemplatePackets();
            List<PacketTemplate> allTemplates = new ArrayList<>();
            allTemplates.addAll(packetTemplates);
            allTemplates.addAll(templatePackets);

            // Save each packet template
            for (PacketTemplate template : allTemplates) {
                // Begin template
                fileContent.append("TEMPLATE_START\n");

                // Template properties
                fileContent.append("NAME:").append(escapeValue(template.getName())).append("\n");
                fileContent.append("IS_TEMPLATE:").append(template.isTemplate() ? "1" : "0").append("\n");

                // Template variables
                fileContent.append("VARIABLES_START\n");
                for (PacketVariable var : template.getVariables()) {
                    StringBuilder varStr = new StringBuilder();
                    varStr.append(escapeValue(var.getName())).append("|");
                    varStr.append(var.getSize()).append("|");
                    varStr.append(escapeValue(var.getValue())).append("|");
                    varStr.append(var.forceShow ? "1" : "0");
                    fileContent.append(varStr).append("\n");
                }
                fileContent.append("VARIABLES_END\n");

                // End template
                fileContent.append("TEMPLATE_END\n");
            }

            fileContent.append("PACKET_TEMPLATES_END\n");

            // Write to file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(fileContent.toString());
            }

            // Show success message
            JOptionPane.showMessageDialog(this,
                    "All data saved successfully to " + file.getName(),
                    "Save Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving data: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Escapes special characters in values
     */
    private String escapeValue(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\n", "\\n")
                .replace(":", "\\:");
    }
}