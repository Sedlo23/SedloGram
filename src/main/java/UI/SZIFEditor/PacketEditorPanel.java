package UI.SZIFEditor;

import UI.SZIFEditor.TableExportAdapter;
import net.miginfocom.swing.MigLayout;
import packets.TrackToTrain.Packet;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static tools.ui.GUIHelper.initNewPlist;

/**
 * Panel for editing packet templates
 */
class PacketEditorPanel extends JPanel {
    private EditorController controller;
    private JList<PacketTemplate> packetList;
    private DefaultListModel<PacketTemplate> listModel;
    private JTable variablesTable;
    private DefaultTableModel tableModel;
    private JTextField packetNameField;
    private JButton saveButton;
    private JButton deleteButton;
    private JButton importButton;
    private JCheckBox isTemplateCheck;

    Packet packet;

    public PacketEditorPanel(EditorController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // Create packet list
        listModel = new DefaultListModel<>();
        updatePacketList();

        packetList = new JList<>(listModel);
        packetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        packetList.setCellRenderer(new PacketListCellRenderer());
        packetList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                PacketTemplate selected = packetList.getSelectedValue();
                if (selected != null) {
                    displayPacketDetails(selected);
                }
            }
        });

        // Create variables table with custom model for checkbox column
        String[] columnNames = {"Variable Name", "Size (bits)", "Value", "Force Show"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Return Boolean.class for the Force Show column to use a checkbox
                return columnIndex == 3 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Don't allow editing Force Show if the row is incomplete
                if (column == 3) {
                    // Check if the row has content in the first three columns
                    for (int i = 0; i < 3; i++) {
                        Object value = getValueAt(row, i);
                        if (value == null || value.toString().trim().isEmpty()) {
                            return false;
                        }
                    }
                }
                return true;
            }
        };

        variablesTable = new JTable(tableModel);

        // Add copy/paste support
        TableExportAdapter exportAdapter = new TableExportAdapter(variablesTable);

        // Add custom renderer for ForceShow column
        variablesTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            private final JCheckBox checkbox = new JCheckBox();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                // Check if this row has content
                boolean hasContent = true;
                for (int i = 0; i < 3; i++) {
                    Object cellValue = table.getValueAt(row, i);
                    if (cellValue == null || cellValue.toString().trim().isEmpty()) {
                        hasContent = false;
                        break;
                    }
                }

                if (!hasContent) {
                    // Empty cell for incomplete rows
                    JLabel label = new JLabel();
                    label.setOpaque(true);
                    if (isSelected) {
                        label.setBackground(table.getSelectionBackground());
                    } else {
                        label.setBackground(table.getBackground());
                    }
                    return label;
                }

                // Use checkbox for complete rows
                checkbox.setSelected(Boolean.TRUE.equals(value));
                checkbox.setHorizontalAlignment(JLabel.CENTER);
                checkbox.setOpaque(true);

                if (isSelected) {
                    checkbox.setBackground(table.getSelectionBackground());
                    checkbox.setForeground(table.getSelectionForeground());
                } else {
                    checkbox.setBackground(table.getBackground());
                    checkbox.setForeground(table.getForeground());
                }

                return checkbox;
            }
        });

        // Add 50 empty rows
        for (int i = 0; i < 1000; i++) {
            tableModel.addRow(new Object[]{"", "", "", Boolean.FALSE});
        }

        // Create packet name field and buttons
        packetNameField = new JTextField(20);
        saveButton = new JButton("Save Packet");
        deleteButton = new JButton("Delete Packet");
        importButton = new JButton("Import Packet");
        isTemplateCheck = new JCheckBox("Is Template");

        isTemplateCheck.addActionListener(e -> {
            // If checked, update packetNameField to include "vzor" if it doesn't already
            if (isTemplateCheck.isSelected() && !packetNameField.getText().toLowerCase().contains("vzor")) {
                packetNameField.setText(packetNameField.getText() + " vzor");
            }

            // Update table headers based on template selection
            updateTableHeadersForTemplate(isTemplateCheck.isSelected());
        });

        saveButton.addActionListener(e -> savePacket());
        deleteButton.addActionListener(e -> deletePacket());

        // Layout components
        JPanel controlPanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanelLeft.add(new JLabel("Packet Name:"));
        controlPanelLeft.add(packetNameField);
        controlPanelLeft.add(isTemplateCheck);
        controlPanelLeft.add(saveButton);
        controlPanelLeft.add(deleteButton);

        JButton jButton = new JButton("Add new packet");

        jButton.addActionListener(e ->
        {
            for (int i =0;i<tableModel.getRowCount();i++)
            {
                tableModel.setValueAt(""     ,i, 0);
                tableModel.setValueAt(""     ,i, 1);
                tableModel.setValueAt(""     ,i, 2);
                tableModel.setValueAt(Boolean.FALSE, i, 3);
                packetNameField.setText("Nový Packet");
            }
        });

        variablesTable.setCellSelectionEnabled(true);

        controlPanelLeft.add(jButton);

        JPanel controlPanelRight = new JPanel(new FlowLayout(FlowLayout.LEFT));

        controlPanelRight.add(importButton);

        JComboBox<Packet> comboBox = new JComboBox<Packet>();
        initNewPlist(comboBox);
        controlPanelRight.add(comboBox);

        JPanel jPanel = new JPanel(new BorderLayout());

        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jPanel.removeAll();
                jPanel.add(((Packet)comboBox.getSelectedItem()).getPacketComponent());
            }
        });

        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Packet)comboBox.getSelectedItem()).getBinData();
                String packetString = ((Packet)comboBox.getSelectedItem()).getSimpleView();

                String[] lines = packetString.split("\n");

                for (String line : lines) {
                    // Using regex to extract the co ponents
                    // Group 1: name, Group 2: size, Group 3: value
                    String regex = "(\\w+)\\s+\\((\\d+)\\)\\s+=(.+)";
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
                    java.util.regex.Matcher matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        String name = matcher.group(1);
                        String size = matcher.group(2);
                        String value = matcher.group(3);
                        Boolean forceShow = name.contains("NID_PACKET");

                        Object[] newRowData = {name, size, value, forceShow};
                        addToFirstFreeRow(variablesTable, newRowData);
                    }
                }
            }
        });

        JPanel controlPanel = new JPanel(new MigLayout("insets 0, fillx", "[left]push[right]"));

        controlPanel.add(controlPanelLeft, "left");
        controlPanel.add(controlPanelRight, "right");

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(controlPanel, BorderLayout.NORTH);

        rightPanel.add(new JScrollPane(variablesTable), BorderLayout.CENTER);
        rightPanel.add(new JScrollPane(jPanel), BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(packetList), rightPanel);
        splitPane.setDividerLocation(200);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Updates the table headers based on template mode
     */
    private void updateTableHeadersForTemplate(boolean isTemplate) {
        JTableHeader header = variablesTable.getTableHeader();
        if (isTemplate) {
            // Custom header for template packets
            variablesTable.getColumnModel().getColumn(2).setHeaderValue("Template Name");
            variablesTable.getColumnModel().getColumn(1).setHeaderValue("Actual Value");
        } else {
            // Standard header
            variablesTable.getColumnModel().getColumn(2).setHeaderValue("Value");
            variablesTable.getColumnModel().getColumn(1).setHeaderValue("Size (bits)");
        }
        header.repaint();
    }

    private void updatePacketList() {
        listModel.clear();
        for (PacketTemplate packetTemplate : controller.getPackets()) {
            listModel.addElement(packetTemplate);
        }
        for (PacketTemplate template : controller.getTemplatePackets()) {
            listModel.addElement(template);
        }
    }

    private void displayPacketDetails(PacketTemplate packetTemplate) {
        packetNameField.setText(packetTemplate.getName());
        isTemplateCheck.setSelected(packetTemplate.isTemplate());

        // Update the table header based on whether this is a template
        updateTableHeadersForTemplate(packetTemplate.isTemplate());

        // Clear table
        tableModel.setRowCount(0);

        // Add variables
        for (PacketVariable var : packetTemplate.getVariables()) {
            tableModel.addRow(new Object[]{
                    var.getName(),
                    var.getSize(),
                    var.getValue(),
                    var.forceShow
            });
        }

        // Add empty rows
        while (tableModel.getRowCount() < 1000) {
            tableModel.addRow(new Object[]{"", "", "", Boolean.FALSE});
        }
    }

    private void savePacket() {
        String packetName = packetNameField.getText().trim();
        if (packetName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a packet name",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isTemplate = isTemplateCheck.isSelected();
        PacketTemplate packetTemplate = new PacketTemplate(packetName, isTemplate);

        // Add variables from table
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object nameObj = tableModel.getValueAt(i, 0);
            Object sizeObj = tableModel.getValueAt(i, 1);
            Object valueObj = tableModel.getValueAt(i, 2);
            Object forceShowObj = tableModel.getValueAt(i, 3);

            if (nameObj != null && !nameObj.toString().isEmpty() &&
                    sizeObj != null && !sizeObj.toString().isEmpty() &&
                    valueObj != null && !valueObj.toString().isEmpty()) {

                String name = nameObj.toString();
                String value = valueObj.toString();
                String sizeStr = sizeObj.toString();
                boolean forceShow = false;

                // Check if forceShow is a Boolean
                if (forceShowObj instanceof Boolean) {
                    forceShow = (Boolean)forceShowObj;
                } else if (forceShowObj != null) {
                    // Try to parse as string
                    forceShow = Boolean.parseBoolean(forceShowObj.toString());
                }

                // Default forceShow to true for NID_PACKET fields
                if (name.contains("NID_PACKET")) {
                    forceShow = true;
                }

                try {
                    // For templates, the "size" field contains the actual value, and "value" contains the display name
                    if (isTemplate) {
                        int actualValue = Integer.parseInt(sizeStr.trim());
                        packetTemplate.addVariable(new PacketVariable(name, actualValue, value, forceShow));
                    } else {
                        int size = Integer.parseInt(sizeStr.trim());
                        packetTemplate.addVariable(new PacketVariable(name, size, value, forceShow));
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid rows
                    JOptionPane.showMessageDialog(this,
                            "Invalid number in row " + (i+1) + ": " + sizeStr,
                            "Input Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

        controller.savePacket(packetTemplate);
        updatePacketList();
    }

    private void deletePacket() {
        PacketTemplate selected = packetList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete packet: " + selected.getName() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                controller.deletePacket(selected);
                updatePacketList();
                packetNameField.setText("");
                isTemplateCheck.setSelected(false);

                // Reset table headers to standard
                updateTableHeadersForTemplate(false);

                tableModel.setRowCount(0);

                // Add empty rows
                while (tableModel.getRowCount() < 1000) {
                    tableModel.addRow(new Object[]{"", "", "", Boolean.FALSE});
                }
            }
        }
    }

    public void addToFirstFreeRow(JTable table, Object[] rowData) {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        // Check if model has correct number of columns
        if (tableModel.getColumnCount() != 4) {
            System.err.println("Table model doesn't have 4 columns");
            return;
        }

        // First approach: Look for first null or empty row
        int rowCount = tableModel.getRowCount();
        boolean rowFound = false;

        for (int row = 0; row < rowCount; row++) {
            // Check if this row is empty (all cells are null or empty strings)
            boolean isEmpty = true;
            for (int col = 0; col < 3; col++) { // Only check first 3 columns
                Object value = tableModel.getValueAt(row, col);
                if (value != null && !value.toString().trim().isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }

            if (isEmpty) {
                // Found an empty row, update it
                for (int col = 0; col < rowData.length; col++) {
                    tableModel.setValueAt(rowData[col], row, col);
                }
                rowFound = true;
                break;
            }
        }

        // If no empty row was found, add a new row
        if (!rowFound) {
            tableModel.addRow(rowData);
        }

        // Notify the table that data has changed
        tableModel.fireTableDataChanged();
    }

    private static class PacketListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);




            if (value instanceof PacketTemplate)
            {
                PacketTemplate packetTemplate = (PacketTemplate) value;
                setText(packetTemplate.getName());
                if (packetTemplate.isTemplate()) {
                    setFont(getFont().deriveFont(Font.ITALIC));
                } else {
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
            }
            return c;
        }
    }


}