package UI.SZIFEditor;

import UI.FileManager.FileManager;
import tools.ui.TlgTemp;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window frame
 */
public class EditorFrame extends JPanel {
    private EditorController controller;
    private MainPanel mainPanel;
    private JTextArea consoleArea;
    JPanel contentPanel;

    public EditorFrame(DefaultListModel<TlgTemp> telegramList2, FileManager fileManager) {



        consoleArea = new JTextArea(5, 50);
        consoleArea.setEditable(false);

        controller = new EditorController( consoleArea);
        mainPanel = new MainPanel(controller, telegramList2,fileManager);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(mainPanel, BorderLayout.CENTER);

        setupMenu();
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exportItem = new JMenuItem("Export Telegram");

        openItem.addActionListener(e -> controller.openFile());
        saveItem.addActionListener(e -> controller.saveFile());
        exportItem.addActionListener(e -> controller.exportTelegram());

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exportItem);

        JMenu viewMenu = new JMenu("View");

        // Add column visibility menu items
        String[] columnNames = EditorTable.COLUMN_NAMES;
        for (int i = 0; i < columnNames.length; i++) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(columnNames[i]);
            final int colIndex = i;
            item.setSelected(true);
            item.addActionListener(e -> mainPanel.toggleColumnVisibility(colIndex, item.isSelected()));
            viewMenu.add(item);
        }

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

    }
}
