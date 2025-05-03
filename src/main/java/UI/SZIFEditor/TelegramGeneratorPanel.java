package UI.SZIFEditor;

import tools.ui.TlgTemp;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Panel for generating telegrams
 */
class TelegramGeneratorPanel extends JPanel {
    private EditorController controller;
    private EditorTable editorTable;
    private JList<TelegramData> telegramList;
    private DefaultListModel<TelegramData> listModel;
    private JProgressBar progressBar;
    private JButton generateButton;
    private JButton saveButton;

    public TelegramGeneratorPanel(EditorController controller, EditorTable editorTable, DefaultListModel<TlgTemp> telegramList2) {
        this.controller = controller;
        this.editorTable = editorTable;
        setLayout(new BorderLayout());

        // Create UI components
        listModel = new DefaultListModel<>();
        telegramList = new JList<>(listModel);
        telegramList.setCellRenderer(new TelegramListCellRenderer());

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        generateButton = new JButton("Generate Telegrams");
        saveButton = new JButton("Save Telegrams");
        JButton exportButton = new JButton("Export Selected to editor");

        generateButton.addActionListener(e -> generateTelegrams());

        saveButton.addActionListener(e -> saveTelegrams());

        exportButton.addActionListener(e -> {
            ((DefaultListModel<TlgTemp>) telegramList2).add(
                    0, new TlgTemp("",
                            (telegramList.getSelectedValue().getEncodedData()) )
            );
        });


        // Layout components
        JPanel controlPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(generateButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(exportButton);

        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(progressBar, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(telegramList), BorderLayout.CENTER);
    }

    // In TelegramGeneratorPanel.generateTelegrams() method
    private void generateTelegrams() {
        generateButton.setEnabled(false);
        progressBar.setIndeterminate(true);

        // Create worker thread to avoid UI freeze
        SwingWorker<java.util.List<TelegramData>, Integer> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<TelegramData> doInBackground() {
                // Use the new method to get visually filled rows
                java.util.List<TableRowData> rows = editorTable.getVisuallyFilledRows();
                return controller.generateTelegrams(rows);
            }

            @Override
            protected void done() {
                try {
                    List<TelegramData> telegrams = get();
                    listModel.clear();
                    for (TelegramData telegram : telegrams) {
                        listModel.addElement(telegram);
                    }

                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    generateButton.setEnabled(true);
                } catch (Exception e) {
                    progressBar.setIndeterminate(false);
                    generateButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void saveTelegrams() {


        if (telegramList.getModel().getSize() == 0) {
            JOptionPane.showMessageDialog(this, "No telegrams to save.",
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Directory to Save Telegrams");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File directory = fileChooser.getSelectedFile();

            for (int i = 0; i < listModel.getSize(); i++) {
                TelegramData telegram = listModel.getElementAt(i);

                String filename = telegram.getName() + ".tlg";

                File outputFile = new File(directory, filename);

                controller.saveTelegramToFile(telegram, outputFile);

            }

            JOptionPane.showMessageDialog(this, "Telegrams saved successfully.");
        }
    }

    private static class TelegramListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof TelegramData) {
                TelegramData telegram = (TelegramData) value;
                setText(telegram.toString());
            }
            return c;
        }
    }
}
