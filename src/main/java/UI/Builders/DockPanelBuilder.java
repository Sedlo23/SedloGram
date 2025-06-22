package UI.Builders;

import Encoding.TelegramDecoder;
import Encoding.TelegramEncoder;
import UI.DnDTabbedPane.DnDTabbedPane;
import UI.DockingPanes.SimplePanel;
import UI.FileManager.FileManager;
import UI.SZIFEditor.EditorFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.crypto.ArithmeticalFunctions;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.Level;
import packets.Interfaces.IPacket;
import packets.TrackToTrain.*;
import tools.ui.GUIHelper;
import tools.string.StringHelper;
import tools.ui.TlgTemp;
import tools.ui.VirtualLogListAppender;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import static tools.string.StringHelper.createHtmlDiffTable;

/**
 * The {@code DockPanelBuilder} class is responsible for creating and managing a set
 * of {@link SimplePanel} dock panels that compose the UI. Each dock panel has unique
 * functionality, such as packet editing, logging, file management, and so forth.
 */
public class DockPanelBuilder {

    /**
     * A {@link JList} for holding custom telegram data objects ({@link TlgTemp}).
     * This list supplies source data for the "Editor Paketu" dock.
     */
    private final JList<TlgTemp> telegramList;

    /**
     * Manages file interactions for the "Soubory" dock panel.
     */
    private FileManager files;

    private JPanel graphPanel;

    /**
     * Constructs a new {@code DockPanelBuilder} and initializes an empty {@link JList}
     * for holding {@code TlgTemp} objects.
     */
    public DockPanelBuilder() {
        telegramList = new JList<>();
        telegramList.setModel(new DefaultListModel<>());

        // Modern FlatLaf styling using system properties
        UIManager.put("TabbedPane.tabsOverlapBorder", false);
        UIManager.put("TabbedPane.contentOpaque", true);
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(4, 8, 4, 8));
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(2, 2, 2, 2));
        UIManager.put("TabbedPane.tabInsets", new Insets(8, 16, 8, 16));
    }

    /**
     * Ensures PH packets are always first in the packet model
     */
    private void ensurePHFirst(DefaultListModel<IPacket> packetModel) {
        if (packetModel.getSize() == 0) return;

        // Find all PH packets and their positions
        java.util.List<IPacket> phPackets = new ArrayList<>();
        java.util.List<IPacket> otherPackets = new ArrayList<>();

        for (int i = 0; i < packetModel.getSize(); i++) {
            IPacket packet = packetModel.get(i);
            if (packet instanceof PH) {
                phPackets.add(packet);
            } else {
                otherPackets.add(packet);
            }
        }

        // Only reorganize if PH packets are not already first
        if (!phPackets.isEmpty() && !(packetModel.get(0) instanceof PH)) {
            // Clear the model
            packetModel.clear();

            // Add PH packets first
            for (IPacket ph : phPackets) {
                packetModel.addElement(ph);
            }

            // Add other packets after
            for (IPacket other : otherPackets) {
                packetModel.addElement(other);
            }
        }
    }

    /**
     * Gets packets from model in correct order (PH first) regardless of model order
     */
    private java.util.List<IPacket> getPacketsInCorrectOrder(DefaultListModel<IPacket> packetModel) {
        java.util.List<IPacket> allPackets = new ArrayList<>();
        for (int i = 0; i < packetModel.getSize(); i++) {
            allPackets.add(packetModel.get(i));
        }

        // Separate PH and non-PH packets
        java.util.List<IPacket> phPackets = new ArrayList<>();
        java.util.List<IPacket> otherPackets = new ArrayList<>();

        for (IPacket packet : allPackets) {
            if (packet instanceof PH) {
                phPackets.add(packet);
            } else {
                otherPackets.add(packet);
            }
        }

        // Return PH first, then others
        java.util.List<IPacket> result = new ArrayList<>();
        result.addAll(phPackets);
        result.addAll(otherPackets);
        return result;
    }

    //////////////////////////////////////////////////////////////////////////////
    //                             DOCK BUILDERS
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a very simple "Zdroj" dock panel.
     */
    public SimplePanel buildZdrojDock() {
        SimplePanel zdrojDock = new SimplePanel("Zdroj", "one", null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(UIManager.getColor("Panel.background"));

        zdrojDock.add(panel);
        return zdrojDock;
    }

    /**
     * Builds the "Editor Paketu" dock panel with DnD support.
     */
    public SimplePanel buildEditDock() {
        SimplePanel editDock = new SimplePanel("Editor Paketu", "two", null);
        DnDTabbedPane tabbedPane = new DnDTabbedPane();
        setupPacketTypeChecker(tabbedPane);

        // Modern styling for tabbed pane
        tabbedPane.putClientProperty("JTabbedPane.tabType", "card");
        tabbedPane.putClientProperty("JTabbedPane.showTabSeparators", true);

        ListModel<TlgTemp> listModel = telegramList.getModel();

        // Lambda for rebuilding the top-level DnDTabbedPane whenever the list changes.
        Runnable rebuildTabs = () -> {
            tabbedPane.removeAll();

            // 1) Build a main tab for each TlgTemp in the list.
            for (int i = 0; i < ((DefaultListModel<TlgTemp>) listModel).size(); i++) {
                TlgTemp telegramEntry = listModel.getElementAt(i);

                // **FIX: Ensure PH is first before building UI**
                ensurePHFirst(telegramEntry.defaultListModel);

                // Create internal tabbed pane for IPackets in this telegram
                DnDTabbedPane packetTabbedPane = new DnDTabbedPane();
                setupPacketTypeChecker(packetTabbedPane);
                setupPacketModelUpdater(packetTabbedPane, telegramEntry.defaultListModel);

                packetTabbedPane.setTabPlacement(JTabbedPane.LEFT);
                packetTabbedPane.putClientProperty("JTabbedPane.tabType", "card");

                JProgressBar totalLengthProgressBar = createModernProgressBar();
                JButton refreshButton = createModernButton("🔄 Aktualizovat", "accent");

                // 1.1) Rebuilds the IPacket tabs inside a single telegram
                Runnable updatePackets = () -> {
                    // **FIX: Ensure PH is first in model before updating UI**
                    ensurePHFirst(telegramEntry.defaultListModel);

                    // **FIX: Store the "Přidat packet" tab info before removal**
                    Component addPacketComponent = null;
                    Component addPacketTabComponent = null;
                    int addPacketIndex = -1;

                    // Find and store the "Přidat packet" tab
                    for (int idx = 0; idx < packetTabbedPane.getTabCount(); idx++) {
                        if ("Přidat packet".equals(packetTabbedPane.getTitleAt(idx))) {
                            addPacketComponent = packetTabbedPane.getComponentAt(idx);
                            addPacketTabComponent = packetTabbedPane.getTabComponentAt(idx);
                            addPacketIndex = idx;
                            break;
                        }
                    }

                    // Remove all tabs except the "Přidat packet" placeholder
                    for (int idx = packetTabbedPane.getTabCount() - 1; idx >= 0; idx--) {
                        if (!"Přidat packet".equals(packetTabbedPane.getTitleAt(idx))) {
                            packetTabbedPane.removeTabAt(idx);
                        }
                    }

                    // **FIX: Get packets in correct order (PH first)**
                    java.util.List<IPacket> correctOrderPackets = getPacketsInCorrectOrder(telegramEntry.defaultListModel);

                    // Add tabs for packets in correct order
                    for (IPacket packet : correctOrderPackets) {
                        ((Packet) packet).setjProgressBar(refreshButton);

                        Component packetComponent = packet.getPacketComponent();

                        // **FIX: Always insert at the end (before "Přidat packet" tab)**
                        int insertIndex = packetTabbedPane.getTabCount();
                        if (insertIndex > 0) {
                            // Check if last tab is "Přidat packet"
                            String lastTitle = packetTabbedPane.getTitleAt(insertIndex - 1);
                            if ("Přidat packet".equals(lastTitle)) {
                                insertIndex = insertIndex - 1; // Insert before "Přidat packet"
                            }
                        }

                        packetTabbedPane.insertTab(packet.toString(), null, packetComponent, null, insertIndex);

                        // Build a custom tab header (NO arrows - just close button)
                        // **FIX: Always use the first PH packet from correct order list for reference**
                        IPacket firstPH = correctOrderPackets.stream()
                                .filter(p -> p instanceof PH)
                                .findFirst()
                                .orElse(correctOrderPackets.isEmpty() ? null : correctOrderPackets.get(0));

                        JPanel tabHeader = buildModernTabHeader(packetTabbedPane, telegramEntry.defaultListModel,
                                packet, packetComponent, refreshButton, firstPH);

                        tabHeader.setOpaque(false);

                        // Attach the custom header to the newly inserted tab
                        packetTabbedPane.setTabComponentAt(insertIndex, tabHeader);
                    }

                    // **FIX: Ensure "Přidat packet" tab is still at the end and enabled properly**
                    for (int idx = 0; idx < packetTabbedPane.getTabCount(); idx++) {
                        if ("Přidat packet".equals(packetTabbedPane.getTitleAt(idx))) {
                            packetTabbedPane.setEnabledAt(idx, false); // Keep it disabled for selection
                            break;
                        }
                    }
                };

                // 1.2) Initialize or refresh the internal packet tabs
                updatePackets.run();

                // 1.3) Create a placeholder tab for "Přidat packet" - ensure it's always last
                if (packetTabbedPane.getTabCount() == 0 ||
                        !"Přidat packet".equals(packetTabbedPane.getTitleAt(packetTabbedPane.getTabCount() - 1))) {
                    packetTabbedPane.addTab("Přidat packet", new JPanel());
                }

                buildModernAddPacketTabHeader(packetTabbedPane, telegramEntry.defaultListModel,
                        totalLengthProgressBar, refreshButton, updatePackets);

                // 1.4) Finally, add this newly built packetTabbedPane to the main top-level tab
                tabbedPane.addTab(
                        telegramEntry.toString().split("\\[")[0],
                        packetTabbedPane
                );

                // Build and attach a custom header for the entire telegram (tab).
                JPanel telegramTabHeader = buildModernTelegramTabHeader(
                        tabbedPane, (DefaultListModel<TlgTemp>) listModel, telegramEntry
                );
                int lastIndex = tabbedPane.getTabCount() - 1;

                tabbedPane.setTabComponentAt(lastIndex, telegramTabHeader);
            }

            // 2) Build the "Přidat nový telegram" tab
            tabbedPane.addTab("", new JPanel());
            buildModernAddTelegramTabHeader(tabbedPane);

            // Disable the final "add new telegram" tab so it cannot be selected
            tabbedPane.setEnabledAt(tabbedPane.getTabCount() - 1, false);
        };

        // Attach rebuild logic to the telegram list model
        listModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                // **FIX: Ensure PH packets are first in newly added telegrams**
                SwingUtilities.invokeLater(() -> {
                    for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                        if (i < listModel.getSize()) {
                            TlgTemp telegram = listModel.getElementAt(i);
                            ensurePHFirst(telegram.defaultListModel);
                        }
                    }
                    rebuildTabs.run();
                });
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
                SwingUtilities.invokeLater(rebuildTabs);
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                // **FIX: Ensure PH packets are first when telegrams are modified**
                SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < listModel.getSize(); i++) {
                        TlgTemp telegram = listModel.getElementAt(i);
                        ensurePHFirst(telegram.defaultListModel);
                    }
                    rebuildTabs.run();
                });
            }
        });

        // Initial build
        rebuildTabs.run();

        editDock.add(tabbedPane);
        return editDock;
    }

    /**
     * Sets up the model updater for DnD operations
     */
    private void setupPacketModelUpdater(DnDTabbedPane packetTabbedPane, DefaultListModel<IPacket> packetModel) {
        packetTabbedPane.putClientProperty("packetModel", packetModel);
    }

    /**
     * Sets up packet type checking for the DnDTabbedPane
     */
    private void setupPacketTypeChecker(DnDTabbedPane tabbedPane) {
        tabbedPane.setPacketTypeChecker(new DnDTabbedPane.PacketTypeChecker() {
            @Override
            public boolean isPH(JTabbedPane tabbedPane, int index) {
                if (index < 0 || index >= tabbedPane.getTabCount()) return false;
                String title = tabbedPane.getTitleAt(index);
                return title != null && title.startsWith("PH");
            }

            @Override
            public boolean isP0orP200(JTabbedPane tabbedPane, int index) {
                if (index < 0 || index >= tabbedPane.getTabCount()) return false;
                String title = tabbedPane.getTitleAt(index);
                return title != null && (title.startsWith("P0_") || title.startsWith("P0 ") ||
                        title.equals("P0") || title.startsWith("P200"));
            }

            @Override
            public boolean isP255(JTabbedPane tabbedPane, int index) {
                if (index < 0 || index >= tabbedPane.getTabCount()) return false;
                String title = tabbedPane.getTitleAt(index);
                return title != null && title.startsWith("P255");
            }

            @Override
            public boolean isAddPacketTab(JTabbedPane tabbedPane, int index) {
                if (index < 0 || index >= tabbedPane.getTabCount()) return false;
                String title = tabbedPane.getTitleAt(index);
                return title != null && title.equals("Přidat packet");
            }
        });
    }

    private JPanel buildModernTabHeader(JTabbedPane packetTabbedPane,
                                        DefaultListModel<IPacket> packetModel,
                                        IPacket packet,
                                        Component packetComponent,
                                        JButton refreshButton, IPacket iPacket) {

        JPanel tabHeader = new JPanel(new MigLayout("insets 0", "[270!][20!]", "[20!]"));
        tabHeader.setOpaque(false);
        tabHeader.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        // Build the center label(s) - NO ARROW BUTTONS
        JPanel titlePanel = buildModernPacketTitlePanel(packet);
        tabHeader.add(titlePanel);

        // Right side: a modern close button
        JButton closeButton = createModernCloseButton("✕");
        closeButton.addActionListener(e -> {
            packetModel.removeElement(packet);
            packetTabbedPane.remove(packetComponent);
            refreshButton.doClick();
        });

        // Disable the close button if PH or P255
        if (packet instanceof PH || packet instanceof P255) {
            closeButton.setEnabled(false);
            closeButton.setVisible(false);
        }

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closePanel.setOpaque(false);
        closePanel.add(closeButton);

        tabHeader.add(closePanel);

        // **FIX: Add proper mouse event dispatching for DnD support**
        MouseAdapter dndMouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) {
                    // Dispatch mouse event to the tabbedPane for DnD
                    packetTabbedPane.dispatchEvent(SwingUtilities.convertMouseEvent(
                            e.getComponent(), e, packetTabbedPane));

                    // Select the tab
                    packetTabbedPane.setSelectedIndex(packetTabbedPane.indexOfTabComponent(tabHeader));

                    // Update graph panel
                    if (graphPanel != null) {
                        graphPanel.removeAll();
                        graphPanel.add(packet.getGraphicalVisualization() == null ?
                                new JPanel() : packet.getGraphicalVisualization());
                        graphPanel.revalidate();
                        graphPanel.repaint();
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // Dispatch drag events to the tabbedPane for DnD
                packetTabbedPane.dispatchEvent(SwingUtilities.convertMouseEvent(
                        e.getComponent(), e, packetTabbedPane));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                Color hoverColor = UIManager.getColor("Button.hoverBackground");
                if (hoverColor == null) {
                    hoverColor = UIManager.getColor("List.selectionBackground");
                }
                if (hoverColor != null) {
                    tabHeader.setBackground(hoverColor);
                    tabHeader.setOpaque(false);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tabHeader.setOpaque(false);
            }
        };

        // Add mouse listeners to the tab header for DnD support
        tabHeader.addMouseListener(dndMouseAdapter);
        tabHeader.addMouseMotionListener(dndMouseAdapter);

        // Also add to the title panel to ensure events are captured
        titlePanel.addMouseListener(dndMouseAdapter);
        titlePanel.addMouseMotionListener(dndMouseAdapter);

        return tabHeader;
    }
    /**
     * Constructs the "Logy" dock panel with a text pane for log output
     * and a combo box to filter the minimum log level.
     */
    public SimplePanel buildTelegDock() {
        SimplePanel telegDock = new SimplePanel("Logy", "three", null, new ArrayList<>());

        JScrollPane logScrollPane = VirtualLogListAppender.createLogListComponent();

        // Modern styling for scroll pane
        logScrollPane.setBorder(createModernBorder());
        logScrollPane.setBackground(UIManager.getColor("ScrollPane.background"));


        // A modern panel to hold the label + combo





        // Assemble the final log panel
        JPanel logPanel = new JPanel(new BorderLayout(0, 8));
        logPanel.setBackground(UIManager.getColor("Panel.background"));
        logPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        logPanel.add(logScrollPane, BorderLayout.CENTER);

        telegDock.add(logPanel);
        return telegDock;
    }

    /**
     * Builds a "Grafické zobrazení" dock.
     */
    public SimplePanel buildGrafDock() {
        SimplePanel graphDock = new SimplePanel("Grafické zobrazení", "four", null, new ArrayList<>());
        graphPanel = new JPanel();
        graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));
        graphPanel.setBackground(UIManager.getColor("Panel.background"));
        graphPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        graphDock.add(graphPanel);

        return graphDock;
    }

    /**
     * Builds a "Dekatické vyjádření" dock.
     */
    public SimplePanel buildDecDock() {
        JPanel diffPanel = new JPanel(new MigLayout("fill, insets 16", "[grow]", "[grow]"));
        diffPanel.setBackground(UIManager.getColor("Panel.background"));

        diffPanel.add(new EditorFrame((DefaultListModel<TlgTemp>) telegramList.getModel(), files).getContentPanel(), "grow");

        SimplePanel binDock = new SimplePanel("Input File", "seven", null, new ArrayList<>());
        binDock.add(diffPanel);

        return binDock;
    }

    /**
     * Builds an "Input file" or "BinDock" panel.
     */
    public SimplePanel buildBinDock() {
        JPanel diffPanel = new JPanel(new MigLayout("fill, insets 16", "[220!][grow]", "[grow][]"));
        diffPanel.setBackground(UIManager.getColor("Panel.background"));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("JTabbedPane.tabType", "card");

        JScrollPane listScrollPane = new JScrollPane(telegramList);
        JScrollPane tableScrollPane = new JScrollPane(tabbedPane);
        JButton jButton = createModernButton("🔄 Porovnat", "accent");

        // Style scroll panes
        listScrollPane.setBorder(createModernBorder());
        tableScrollPane.setBorder(createModernBorder());

        diffPanel.add(listScrollPane, "cell 0 0, grow, pushy");
        diffPanel.add(jButton, "cell 0 1, growx, gaptop 8");
        diffPanel.add(tableScrollPane, "cell 1 0 1 2, grow");

        jButton.addActionListener(e -> {
            String baseString = "";
            tabbedPane.removeAll();

            for (int i = 0; i < telegramList.getSelectedValue().defaultListModel.getSize(); i++) {
                IPacket packet = telegramList.getSelectedValue().defaultListModel.get(i);
                baseString += packet.getSimpleView() + "\n";
            }

            for (int i = 0; i < telegramList.getModel().getSize(); i++) {
                if (i == telegramList.getSelectedIndex())
                    continue;

                TlgTemp packet = ((DefaultListModel<TlgTemp>) telegramList.getModel()).get(i);
                String compareString = "";

                for (int ii = 0; ii < packet.defaultListModel.getSize(); ii++) {
                    IPacket packet2 = packet.defaultListModel.get(ii);
                    compareString += packet2.getSimpleView() + "\n";
                }

                tabbedPane.addTab(packet.toString(), createHtmlDiffTable(baseString, compareString));
            }
        });

        SimplePanel binDock = new SimplePanel("Porovnání", "AAAABC", null, new ArrayList<>());
        binDock.add(diffPanel);

        return binDock;
    }

    /**
     * Builds a "Soubory" dock containing a file navigation component.
     */
    public SimplePanel buildMapDock() {
        files = new FileManager();
        Component fileTreeComponent = files.getGui(telegramList);

        ArrayList<JMenu> menuList = new ArrayList<>();
        SimplePanel mapDock = new SimplePanel("Soubory", "eight", null, menuList);

        // Wrap in a panel with modern styling
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIManager.getColor("Panel.background"));
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        wrapper.add(fileTreeComponent, BorderLayout.CENTER);

        mapDock.add(wrapper);
        return mapDock;
    }

    //////////////////////////////////////////////////////////////////////////////
    //                           MODERN STYLING METHODS
    //////////////////////////////////////////////////////////////////////////////

    private JButton createModernButton(String text, String colorType) {
        JButton button = new JButton(text);

        // Use system colors based on type
        Color backgroundColor, foregroundColor;
        switch (colorType) {
            case "accent":
                backgroundColor = UIManager.getColor("Component.accentColor");
                if (backgroundColor == null) backgroundColor = UIManager.getColor("Button.default.background");
                foregroundColor = UIManager.getColor("Button.default.foreground");
                if (foregroundColor == null) foregroundColor = Color.BLACK;
                break;
            case "success":
                backgroundColor = UIManager.getColor("Actions.Green");
                if (backgroundColor == null) backgroundColor = UIManager.getColor("Component.accentColor");
                foregroundColor = Color.BLACK;
                break;
            case "warning":
                backgroundColor = UIManager.getColor("Actions.Yellow");
                if (backgroundColor == null) backgroundColor = UIManager.getColor("Component.warningFocusColor");
                foregroundColor = Color.BLACK;
                break;
            case "danger":
                backgroundColor = UIManager.getColor("Actions.Red");
                if (backgroundColor == null) backgroundColor = UIManager.getColor("Component.errorFocusColor");
                foregroundColor = Color.BLACK;
                break;
            default:
                backgroundColor = UIManager.getColor("Button.background");
                foregroundColor = UIManager.getColor("Button.foreground");
        }

        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effects using system colors
        Color finalBackgroundColor = backgroundColor;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Color hoverColor = UIManager.getColor("Button.hoverBackground");
                if (hoverColor == null) {
                    // Fallback: darken the current color
                    hoverColor = finalBackgroundColor.darker();
                }
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(finalBackgroundColor);
            }
        });

        return button;
    }

    private JProgressBar createModernProgressBar() {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setFont(UIManager.getFont("ProgressBar.font"));
        progressBar.setFont(progressBar.getFont().deriveFont(20f));
        if (progressBar.getFont() == null) {
            progressBar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        }
        progressBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        progressBar.setForeground(UIManager.getColor("ProgressBar.foreground"));
        progressBar.setBackground(UIManager.getColor("ProgressBar.background"));
        return progressBar;
    }

    private void styleModernComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(UIManager.getFont("ComboBox.font"));
        if (comboBox.getFont() == null) {
            comboBox.setFont(comboBox.getFont().deriveFont(13f));
        }
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                createModernBorder(),
                BorderFactory.createEmptyBorder(2, 2, 2, 22)
        ));
        comboBox.setBackground(UIManager.getColor("ComboBox.background"));
        comboBox.setForeground(UIManager.getColor("ComboBox.foreground"));
    }

    private AbstractBorder createModernBorder() {
        return new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color borderColor = UIManager.getColor("Component.borderColor");
                if (borderColor == null) {
                    borderColor = UIManager.getColor("Panel.border");
                }
                if (borderColor == null) {
                    borderColor = new Color(200, 200, 200);
                }

                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(x, y, width - 1, height - 1, 8, 8);
                g2d.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 1, 1, 1);
            }
        };
    }

    private AbstractBorder createModernCardBorder() {
        return new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Drop shadow effect using system colors
                Color shadowColor = UIManager.getColor("Component.shadowColor");
                if (shadowColor == null) {
                    shadowColor = new Color(0, 0, 0, 20);
                }
                g2d.setColor(shadowColor);
                g2d.fillRoundRect(x + 2, y + 2, width - 2, height - 2, 12, 12);

                // Main border using system colors
                Color borderColor = UIManager.getColor("Component.borderColor");
                if (borderColor == null) {
                    borderColor = UIManager.getColor("Panel.border");
                }
                if (borderColor == null) {
                    borderColor = new Color(200, 200, 200);
                }

                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(x, y, width - 3, height - 3, 12, 12);
                g2d.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(8, 8, 10, 10);
            }
        };
    }

    /**
     * Creates a modern close button with better styling using system colors.
     */
    private JButton createModernCloseButton(String text) {
        JButton closeButton = new JButton(text);
        closeButton.setOpaque(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusable(false);
        closeButton.setFont(closeButton.getFont().deriveFont(Font.BOLD, 14f));
        closeButton.setPreferredSize(new Dimension(24, 24));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setForeground(UIManager.getColor("Button.foreground"));

        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Color errorColor = UIManager.getColor("Actions.Red");
                if (errorColor == null) {
                    errorColor = UIManager.getColor("Component.errorFocusColor");
                }
                if (errorColor == null) {
                    errorColor = Color.RED.darker();
                }
                closeButton.setForeground(errorColor);

                // Create a lighter version of the error color for background
                Color hoverBg = new Color(errorColor.getRed(), errorColor.getGreen(), errorColor.getBlue(), 30);
                closeButton.setContentAreaFilled(true);
                closeButton.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(UIManager.getColor("Button.foreground"));
                closeButton.setContentAreaFilled(false);
            }
        });
        return closeButton;
    }

    /**
     * Builds a modern panel that displays a {@link IPacket}'s title using system fonts.
     */
    private JPanel buildModernPacketTitlePanel(IPacket packet) {
        String rawTitle = packet.toString().replace("</html>", "");
        String[] parts = rawTitle.split(":");

        JLabel leftLabel = new JLabel(parts.length > 0 ? parts[0].trim() : "");
        JLabel centerLabel = new JLabel(parts.length > 1 ? parts[1] : "");
        JLabel rightLabel = new JLabel(parts.length > 2 ? parts[2].trim() : "");

        // Remove underscores in left label
        int underscoreIndex = leftLabel.getText().indexOf('_');
        if (underscoreIndex != -1) {
            leftLabel.setText(leftLabel.getText().substring(0, underscoreIndex));
        }

        leftLabel.setText(StringHelper.padLeft(leftLabel.getText(), 4, ' '));

        leftLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        leftLabel.setIcon(packet.getIcon());

        centerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rightLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel titlePanel = new JPanel(new BorderLayout(4, 0));
        titlePanel.setOpaque(false);

        titlePanel.add(leftLabel, BorderLayout.WEST);
        titlePanel.add(centerLabel, BorderLayout.CENTER);
        titlePanel.add(rightLabel, BorderLayout.EAST);

        titlePanel.setPreferredSize(new Dimension(280, 20));

        // Use system fonts
        Font defaultFont = UIManager.getFont("Label.font");
        Font monoFont = UIManager.getFont("TextArea.font"); // Usually monospaced
        if (monoFont == null) {
            monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 11);
        }
        if (defaultFont == null) {
            defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }

        leftLabel.setFont(monoFont);
        rightLabel.setFont(monoFont);
        centerLabel.setFont(defaultFont);

        // Use system colors
        Color foregroundColor = UIManager.getColor("Label.foreground");
        leftLabel.setForeground(foregroundColor);
        centerLabel.setForeground(foregroundColor);
        rightLabel.setForeground(foregroundColor);

        if (packet instanceof PH phPacket) {
            phPacket.setjLabel1(centerLabel);
        }

        return titlePanel;
    }

    /**
     * Builds the modern custom tab header for the "Přidat packet" tab using system colors.
     */
    private void buildModernAddPacketTabHeader(JTabbedPane packetTabbedPane,
                                               DefaultListModel<IPacket> packetModel,
                                               JProgressBar totalLengthBar,
                                               JButton refreshButton,
                                               Runnable updatePackets) {
        int lastIndex = packetTabbedPane.getTabCount() - 1;

        JPanel addPacketHeader = new JPanel(new BorderLayout(12, 0));
        addPacketHeader.setOpaque(true);
        addPacketHeader.setBackground(UIManager.getColor("Panel.background"));
        addPacketHeader.setBorder(createModernCardBorder());

        // 1) Combo box for selecting which packet to add
        JComboBox<Packet> comboBox = new JComboBox<>();
        GUIHelper.initNewPlist(comboBox);
        styleModernComboBox(comboBox);

        // Custom cell renderer for the combo box using system colors
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(true);

            JLabel centerLabel = new JLabel();
            centerLabel.setHorizontalAlignment(SwingConstants.CENTER);

            Font monoFont = UIManager.getFont("TextArea.font");
            if (monoFont == null) {
                monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 13);
            }
            centerLabel.setFont(monoFont);
            centerLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

            if (value != null) {
                String rawValue = value.toString().replace("</html>", "");
                String[] parts = rawValue.split(":");
                String first = (parts.length > 0) ? parts[0].trim() : "";
                String third = (parts.length > 2) ? parts[2].trim() : "";
                centerLabel.setText(first + third);
            } else {
                centerLabel.setText("");
            }

            Color bg = isSelected ?
                    UIManager.getColor("List.selectionBackground") :
                    UIManager.getColor("List.background");
            Color fg = isSelected ?
                    UIManager.getColor("List.selectionForeground") :
                    UIManager.getColor("List.foreground");

            panel.setBackground(bg);
            centerLabel.setForeground(fg);
            panel.add(centerLabel, BorderLayout.CENTER);
            return panel;
        });

        // 2) Modern label and combo
        JLabel addLabel = new JLabel("Přidat Packet:");
        addLabel.setFont(addLabel.getFont().deriveFont(Font.BOLD, 14f));
        addLabel.setForeground(UIManager.getColor("Label.foreground"));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(addLabel);

        addPacketHeader.add(leftPanel, BorderLayout.WEST);
        addPacketHeader.add(comboBox, BorderLayout.CENTER);

        // 3) Modern add button
        JButton plusButton = createModernButton("＋", "success");
        plusButton.setPreferredSize(new Dimension(40, 32));

        plusButton.addActionListener(e -> {
            IPacket selected = (IPacket) comboBox.getSelectedItem();
            if (selected instanceof PH) return;

            // **FIX: Add after PH packets but before other packets**
            int position = 0;
            for (int i = 0; i < packetModel.getSize(); i++) {
                if (!(packetModel.get(i) instanceof PH)) {
                    position = i;
                    break;
                }
                position = i + 1; // If all are PH, add at the end
            }

            packetModel.add(position, selected);

            // **FIX: Ensure PH packets are still first after adding**
            ensurePHFirst(packetModel);

            updatePackets.run(); // This rebuilds the tabs automatically!
            refreshButton.doClick();
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(plusButton);
        addPacketHeader.add(rightPanel, BorderLayout.EAST);

        // 4) Container with modern layout
        JPanel addPacketContainer = new JPanel(new MigLayout("fillx, insets 1", "[]", "[]8[]8[]8[]8[]"));
        addPacketContainer.setOpaque(true);
        addPacketContainer.setBackground(UIManager.getColor("Panel.background"));

        addPacketContainer.add(addPacketHeader, "growx, wrap");

        // Add modern progress bar
        addPacketContainer.add(totalLengthBar, "growx, wrap");

        refreshButton.addActionListener(e -> {
            int totalBits = 0;
            for (int i = 0; i < packetModel.getSize(); i++) {
                totalBits += packetModel.getElementAt(i).getBinData().length();
            }

            String type = (totalBits <= 210) ? "Krátký" : "Dlouhý";
            totalLengthBar.setMaximum((totalBits <= 210) ? 210 : 830);

            if (totalBits > 830) {
                totalLengthBar.setString("❌ ERROR ( " + totalBits + " )");
                Color errorColor = UIManager.getColor("Actions.Red");
                if (errorColor == null) errorColor = Color.RED;
                totalLengthBar.setForeground(errorColor);
            } else {
                totalLengthBar.setString("✅ OK ( " + totalBits + " ) [" + type + "]");
                Color successColor = UIManager.getColor("Actions.Green");
                if (successColor == null) successColor = Color.GREEN.darker();
                totalLengthBar.setForeground(successColor);
            }

            totalLengthBar.setStringPainted(true);
            totalLengthBar.setValue(totalBits);
        });
        refreshButton.doClick();

        // 7) Modern encode buttons
        JButton encodeBtn = createModernButton("🔒 Zakódovat jeden telegram", "accent");
        JButton encodeBtn2 = createModernButton("🔄 Zakódovat a vytvořit BG (2x B)", "warning");

        addPacketContainer.add(encodeBtn2, "growx, wrap");
        addPacketContainer.add(encodeBtn, "growx, wrap");

        JProgressBar encodingProgressBar = createModernProgressBar();
        encodingProgressBar.setString("Připraven k enkódování");
        encodingProgressBar.setStringPainted(true);
        addPacketContainer.add(encodingProgressBar, "growx, wrap");

        final Logger LOG = LogManager.getLogger(encodeBtn.getClass());

        encodeBtn.addActionListener(e -> {
            encodingProgressBar.setIndeterminate(true);
            encodingProgressBar.setString("🔄 Kódování telegramu...");
            encodingProgressBar.setStringPainted(true);

            SwingWorker<Void, Void> encoderWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    String s = "";

                    LOG.info("Sestavování telegramu");

                    // **FIX: Use packets in correct order**
                    java.util.List<IPacket> orderedPackets = getPacketsInCorrectOrder(packetModel);
                    for (IPacket packet : orderedPackets) {
                        LOG.debug("Přidávám: " + packet.toString() + "; Hex: " + ArithmeticalFunctions.bin2Hex(packet.getBinData()));
                        LOG.trace(packet.getSimpleView());
                        s += packet.getBinData();
                    }

                    int targetLength = (s.length() <= 210) ? 210 : 830;
                    LOG.trace("Velikost tlg:" + targetLength);

                    StringBuilder sb = new StringBuilder(s);
                    while (sb.length() < targetLength) {
                        sb.append('1');
                    }
                    sb.append('0');
                    sb.append('0');
                    s = sb.toString();

                    String tlg = TelegramEncoder.encode((s));
                    String as = TelegramDecoder.decodeTelegram(tlg);
                    PH ph = new PH(new String[]{s});

                    String name =
                            String.valueOf(ph.getNid_c().getDecValue()) + "_" +
                                    String.valueOf(ph.getNid_bg().getDecValue()) + "_" +
                                    String.valueOf(ph.getN_pig().getDecValue()) + "_" +
                                    String.valueOf(ph.getM_mcount().getDecValue());

                    {
                        File selectedFile = files.getCurrentFolder();

                        if (selectedFile == null) {
                            LOG.warn("Není zvolena složka pro ukládaní tlg");
                            LOG.error("Telegram neuložen");
                            return null;
                        }
                        LOG.info("Cesta pro uložení: " + selectedFile.getAbsolutePath());

                        int ver = 0;
                        String filename = selectedFile.toString();
                        filename += "/" + name + "_v" + StringHelper.padLeft(String.valueOf(ver), 3, '0') + ".tlg";
                        File file = new File(filename);

                        while (file.exists()) {
                            ver++;
                            filename = selectedFile.toString();
                            filename += "/" + name + "_v" + StringHelper.padLeft(String.valueOf(ver), 3, '0') + ".tlg";
                            file = new File(filename);
                        }

                        LOG.debug("Název: " + filename);

                        try {
                            final File outputFile = file.getAbsoluteFile();
                            String lastTelegram = tlg;
                            LOG.info("Ukládám: " + outputFile.getAbsolutePath());
                            ArrayList<Byte> tmp = new ArrayList<Byte>();

                            for (int i = 0; i < lastTelegram.length() - 1; i += 2) {
                                final char c = (char) ArithmeticalFunctions.bin2Dec(ArithmeticalFunctions.hex2Bin(lastTelegram.charAt(i) + "" + lastTelegram.charAt(i + 1)));
                                tmp.add((byte) c);
                            }

                            try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                                byte[] byteArray = new byte[tmp.size()];
                                for (int i = 0; i < tmp.size(); i++) {
                                    byteArray[i] = tmp.get(i);
                                }
                                outputStream.write(byteArray);
                            }

                        } catch (final IOException ex) {
                            LOG.error(ex.getMessage());
                        }
                    }

                    return null;
                }

                @Override
                protected void done() {
                    encodingProgressBar.setIndeterminate(false);
                    encodingProgressBar.setString("✅ Hotovo!");
                    encodingProgressBar.setValue(encodingProgressBar.getMaximum());

                    Color successColor = UIManager.getColor("Actions.Green");
                    if (successColor == null) successColor = Color.GREEN.darker();
                    encodingProgressBar.setForeground(successColor);
                }
            };
            encoderWorker.execute();
        });

        encodeBtn2.addActionListener(e -> {
            encodingProgressBar.setIndeterminate(true);
            encodingProgressBar.setString("🔄 Kódování telegramu...");
            encodingProgressBar.setStringPainted(true);

            SwingWorker<Void, Void> encoderWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    // **FIX: Get first PH packet in correct order**
                    java.util.List<IPacket> orderedPackets = getPacketsInCorrectOrder(packetModel);
                    PH phX = (PH) orderedPackets.stream().filter(p -> p instanceof PH).findFirst().orElse(null);

                    if (phX == null) return null;

                    for (int ii = 0; ii < 2; ii++) {
                        PH ph1 = (PH) phX.deepCopy();

                        ph1.getQ_updown().setBinValue(phX.getQ_updown().getBinValue());
                        ph1.getM_version().setBinValue(phX.getM_version().getBinValue());
                        ph1.getQ_media().setBinValue(phX.getQ_media().getBinValue());
                        ph1.getNid_bg().setBinValue(phX.getNid_bg().getBinValue());
                        ph1.getM_mcount().setBinValue(phX.getM_mcount().getBinValue());
                        ph1.getNid_c().setBinValue(phX.getNid_c().getBinValue());

                        ph1.getM_dup().setBinValue(String.valueOf(ArithmeticalFunctions.dec2XBin(String.valueOf(ii + 1), 2)));
                        ph1.getN_pig().setBinValue(String.valueOf(ii));
                        ph1.getN_total().setBinValue("1");

                        String s = "";
                        s += ph1.getBinData();

                        LOG.info("Sestavování telegramu");

                        for (IPacket packet : orderedPackets) {
                            if (packet instanceof PH) continue; // Skip PH, we already added our modified one
                            LOG.debug("Přidávám: " + packet.toString() + "; Hex: " + ArithmeticalFunctions.bin2Hex(packet.getBinData()));
                            LOG.trace(packet.getSimpleView());
                            s += packet.getBinData();
                        }

                        int targetLength = (s.length() <= 210) ? 210 : 830;
                        LOG.trace("Velikost tlg:" + targetLength);

                        StringBuilder sb = new StringBuilder(s);
                        while (sb.length() < targetLength) {
                            sb.append('1');
                        }
                        sb.append('0');
                        sb.append('0');
                        s = sb.toString();

                        String tlg = TelegramEncoder.encode((s));
                        String as = TelegramDecoder.decodeTelegram(tlg);
                        PH ph = new PH(new String[]{s});

                        String name =
                                String.valueOf(ph.getNid_c().getDecValue()) + "_" +
                                        String.valueOf(ph.getNid_bg().getDecValue()) + "_" +
                                        String.valueOf(ph.getN_pig().getDecValue()) + "_" +
                                        String.valueOf(ph.getM_mcount().getDecValue());

                        {
                            File selectedFile = files.getCurrentFolder();

                            if (selectedFile == null) {
                                LOG.warn("Není zvolena složka pro ukládaní tlg");
                                LOG.error("Telegram neuložen");
                                return null;
                            }
                            LOG.info("Cesta pro uložení: " + selectedFile.getAbsolutePath());

                            int ver = 0;
                            String filename = selectedFile.toString();
                            filename += "/" + name + "_v" + StringHelper.padLeft(String.valueOf(ver), 3, '0') + ".tlg";
                            File file = new File(filename);

                            while (file.exists()) {
                                ver++;
                                filename = selectedFile.toString();
                                filename += "/" + name + "_v" + StringHelper.padLeft(String.valueOf(ver), 3, '0') + ".tlg";
                                file = new File(filename);
                            }

                            LOG.debug("Název: " + filename);

                            try {
                                final File outputFile = file.getAbsoluteFile();
                                String lastTelegram = tlg;
                                LOG.info("Ukládám: " + outputFile.getAbsolutePath());
                                ArrayList<Byte> tmp = new ArrayList<Byte>();

                                for (int i = 0; i < lastTelegram.length() - 1; i += 2) {
                                    final char c = (char) ArithmeticalFunctions.bin2Dec(ArithmeticalFunctions.hex2Bin(lastTelegram.charAt(i) + "" + lastTelegram.charAt(i + 1)));
                                    tmp.add((byte) c);
                                }

                                try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                                    byte[] byteArray = new byte[tmp.size()];
                                    for (int i = 0; i < tmp.size(); i++) {
                                        byteArray[i] = tmp.get(i);
                                    }
                                    outputStream.write(byteArray);
                                }

                            } catch (final IOException ex) {
                                LOG.error(ex.getMessage());
                            }
                        }
                    }

                    return null;
                }

                @Override
                protected void done() {
                    encodingProgressBar.setIndeterminate(false);
                    encodingProgressBar.setString("✅ Hotovo!");
                    encodingProgressBar.setValue(encodingProgressBar.getMaximum());

                    Color successColor = UIManager.getColor("Actions.Green");
                    if (successColor == null) successColor = Color.GREEN.darker();
                    encodingProgressBar.setForeground(successColor);
                }
            };
            encoderWorker.execute();
        });

        packetTabbedPane.setTabComponentAt(lastIndex, addPacketContainer);

        packetTabbedPane.setEnabledAt(lastIndex, false);
    }

    private JPanel buildModernTelegramTabHeader(DnDTabbedPane topTabbedPane,
                                                DefaultListModel<TlgTemp> listModel,
                                                TlgTemp telegramEntry) {
        JPanel mainTabHeader = new JPanel(new BorderLayout(8, 0));
        mainTabHeader.setOpaque(true);
        mainTabHeader.setBackground(UIManager.getColor("Panel.background"));
        mainTabHeader.setBorder(BorderFactory.createCompoundBorder(
                createModernBorder(),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel mainLabel = new JLabel(String.valueOf(telegramEntry));
        mainLabel.setOpaque(false);
        mainLabel.setFont(mainLabel.getFont().deriveFont(Font.BOLD, 13f));
        mainLabel.setForeground(UIManager.getColor("Label.foreground"));
        mainTabHeader.add(mainLabel, BorderLayout.CENTER);

        // **FIX: Always use the first PH packet after ensuring order**
        ensurePHFirst(telegramEntry.defaultListModel);
        if (!telegramEntry.defaultListModel.isEmpty() &&
                telegramEntry.defaultListModel.get(0) instanceof PH ph) {
            ph.setjLabel(mainLabel);
        }

        JButton closeButton = createModernCloseButton("✕");
        closeButton.addActionListener(e -> {
            int tabIndex = topTabbedPane.indexOfTabComponent(mainTabHeader);
            if (tabIndex != -1) {
                topTabbedPane.remove(tabIndex);
                listModel.removeElement(telegramEntry);
            }
        });

        JButton copyButton = createModernCloseButton("📋");
        copyButton.setFont(copyButton.getFont().deriveFont(12f));
        copyButton.addActionListener(e -> {
            TlgTemp newTelegram = new TlgTemp("", ArithmeticalFunctions.bin2Hex(telegramEntry.getTlg()));
            // **FIX: Ensure PH is first immediately after creation**
            ensurePHFirst(newTelegram.defaultListModel);
            ((DefaultListModel<TlgTemp>) telegramList.getModel()).add(0, newTelegram);
        });

        copyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Color accentColor = UIManager.getColor("Component.accentColor");
                if (accentColor == null) {
                    accentColor = UIManager.getColor("List.selectionBackground");
                }
                if (accentColor != null) {
                    copyButton.setForeground(accentColor);
                    Color hoverBg = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30);
                    copyButton.setContentAreaFilled(true);
                    copyButton.setBackground(hoverBg);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                copyButton.setForeground(UIManager.getColor("Button.foreground"));
                copyButton.setContentAreaFilled(false);
            }
        });

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightSide.setOpaque(false);
        rightSide.add(copyButton);
        rightSide.add(closeButton);

        mainTabHeader.add(rightSide, BorderLayout.EAST);

        // **FIX: Add DnD support for telegram tab headers**
        MouseAdapter telegramDndMouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) {
                    topTabbedPane.dispatchEvent(SwingUtilities.convertMouseEvent(
                            e.getComponent(), e, topTabbedPane));

                    int tabIndex = topTabbedPane.indexOfTabComponent(mainTabHeader);
                    if (tabIndex != -1) {
                        topTabbedPane.setSelectedIndex(tabIndex);
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                topTabbedPane.dispatchEvent(SwingUtilities.convertMouseEvent(
                        e.getComponent(), e, topTabbedPane));
            }
        };

        mainTabHeader.addMouseListener(telegramDndMouseAdapter);
        mainTabHeader.addMouseMotionListener(telegramDndMouseAdapter);
        mainLabel.addMouseListener(telegramDndMouseAdapter);
        mainLabel.addMouseMotionListener(telegramDndMouseAdapter);

        return mainTabHeader;
    }

    /**
     * Builds and attaches a modern custom "Přidat nový telegram" tab using system colors.
     */
    private void buildModernAddTelegramTabHeader(DnDTabbedPane topTabbedPane) {
        int lastIndex = topTabbedPane.getTabCount() - 1;

        JPanel addTelegramHeader = new JPanel(new BorderLayout(8, 0));
        addTelegramHeader.setOpaque(true);
        addTelegramHeader.setBackground(UIManager.getColor("Panel.background"));
        addTelegramHeader.setBorder(BorderFactory.createCompoundBorder(
                createModernBorder(),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel addLabel = new JLabel("📝 Nový telegram");
        addLabel.setFont(addLabel.getFont().deriveFont(Font.BOLD, 13f));
        addLabel.setForeground(UIManager.getColor("Label.foreground"));
        addTelegramHeader.add(addLabel, BorderLayout.CENTER);

        JButton addTelegramButton = createModernButton("＋", "success");
        addTelegramButton.setPreferredSize(new Dimension(30, 30));
        addTelegramButton.setFont(addTelegramButton.getFont().deriveFont(10f));

        addTelegramButton.addActionListener(e -> {
            TlgTemp newTelegram = new TlgTemp("", "911372C07001FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
            // **FIX: Ensure PH is first immediately after creation**
            SwingUtilities.invokeLater(() -> {
                ensurePHFirst(newTelegram.defaultListModel);
                ((DefaultListModel<TlgTemp>) telegramList.getModel()).add(0, newTelegram);
            });
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(addTelegramButton);
        addTelegramHeader.add(rightPanel, BorderLayout.EAST);

        topTabbedPane.setTabComponentAt(lastIndex, addTelegramHeader);
    }

    //////////////////////////////////////////////////////////////////////////////
    //                               INNER CLASSES
    //////////////////////////////////////////////////////////////////////////////

    /**
     * A custom output stream that writes data to a {@link JTextArea}.
     * The text area automatically scrolls to the end upon each write.
     */
    public class CustomOutputStream extends OutputStream {
        private final JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            textArea.append(String.valueOf((char) b));
            textArea.setCaretPosition(textArea.getDocument().getLength());
            textArea.update(textArea.getGraphics());
        }
    }
}