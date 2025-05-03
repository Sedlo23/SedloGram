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
        UIManager.put("TabbedPane.tabsOverlapBorder", false);
        UIManager.put("TabbedPane.contentOpaque", true);
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 2, 0, 2));
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(1, 1, 1, 1));
        UIManager.put("TabbedPane.tabInsets", new Insets(1, 4, 1, 4));

    }

    //////////////////////////////////////////////////////////////////////////////
    //                             DOCK BUILDERS
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a very simple "Zdroj" dock panel.
     *
     * @return a {@link SimplePanel} for the "Zdroj" section of the UI.
     */
    public SimplePanel buildZdrojDock() {
        SimplePanel zdrojDock = new SimplePanel("Zdroj", "one", null);

        // Example usage: add or layout components in this panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        zdrojDock.add(panel);
        return zdrojDock;
    }

    /**
     * Builds the "Editor Paketu" dock panel, which provides an interface to create,
     * configure, and reorder telegrams (each with multiple packets).
     *
     * @return the constructed "Editor Paketu" dock panel.
     */
    public SimplePanel buildEditDock() {
        SimplePanel editDock = new SimplePanel("Editor Paketu", "two", null);
        DnDTabbedPane tabbedPane = new DnDTabbedPane();

        ListModel<TlgTemp> listModel = telegramList.getModel();

        // Lambda for rebuilding the top-level DnDTabbedPane whenever the list changes.
        Runnable rebuildTabs = () -> {
            tabbedPane.removeAll();

            // 1) Build a main tab for each TlgTemp in the list.
            for (int i = 0; i < ((DefaultListModel<TlgTemp>) listModel).size(); i++) {
                TlgTemp telegramEntry = listModel.getElementAt(i);

                // Create internal tabbed pane for IPackets in this telegram
                JTabbedPane packetTabbedPane = new JTabbedPane();
                packetTabbedPane.setTabPlacement(JTabbedPane.LEFT);

                JProgressBar totalLengthProgressBar = new JProgressBar();
                JButton refreshButton = new JButton("Aktualizovat");

                // 1.1) Rebuilds the IPacket tabs inside a single telegram
                Runnable updatePackets = () -> {
                    // Remove all tabs except the "Přidat packet" placeholder
                    for (int idx = packetTabbedPane.getTabCount() - 1; idx >= 0; idx--) {
                        if (!"Přidat packet".equals(packetTabbedPane.getTitleAt(idx))) {
                            packetTabbedPane.removeTabAt(idx);
                        }
                    }

                    // Add a tab for each IPacket in the telegram's model
                    for (int z = 0; z < telegramEntry.defaultListModel.getSize(); z++) {
                        IPacket packet = telegramEntry.defaultListModel.get(z);
                        ((Packet) packet).setjProgressBar(refreshButton); // link to refresh button

                        Component packetComponent = packet.getPacketComponent();
                        packetTabbedPane.addTab(packet.toString(), packetComponent);

                        // Build a custom tab header with reorder arrows and a close button
                        JPanel tabHeader = buildTabHeader(packetTabbedPane, telegramEntry.defaultListModel,
                                packet, packetComponent, refreshButton,telegramEntry.defaultListModel.get(0));

                        // Attach the custom header to the newly created tab
                        int lastIndex = packetTabbedPane.getTabCount() - 1;
                        packetTabbedPane.setTabComponentAt(lastIndex, tabHeader);



                    }
                };



                // 1.2) Initialize or refresh the internal packet tabs
                updatePackets.run();

                // 1.3) Create a placeholder tab for "Přidat packet"
                packetTabbedPane.addTab("Přidat packet", new JPanel());

                buildAddPacketTabHeader(packetTabbedPane, telegramEntry.defaultListModel,
                        totalLengthProgressBar, refreshButton);

                // 1.4) Finally, add this newly built packetTabbedPane to the main top-level tab
                tabbedPane.addTab(
                        telegramEntry.toString().split("\\[")[0],
                        packetTabbedPane
                );

                // Build and attach a custom header for the entire telegram (tab).
                JPanel telegramTabHeader = buildTelegramTabHeader(
                        tabbedPane, (DefaultListModel<TlgTemp>) listModel, telegramEntry
                );
                int lastIndex = tabbedPane.getTabCount() - 1;

                tabbedPane.setTabComponentAt(lastIndex, telegramTabHeader);
            }

            // 2) Build the "Přidat nový telegram" tab
            tabbedPane.addTab("", new JPanel());
            buildAddTelegramTabHeader(tabbedPane);

            // Disable the final "add new telegram" tab so it cannot be selected
            tabbedPane.setEnabledAt(tabbedPane.getTabCount() - 1, false);
        };



        // Attach rebuild logic to the telegram list model
        listModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e)    { rebuildTabs.run(); }
            @Override
            public void intervalRemoved(ListDataEvent e)  { rebuildTabs.run(); }
            @Override
            public void contentsChanged(ListDataEvent e)  { rebuildTabs.run(); }
        });

        // Initial build
        rebuildTabs.run();

        editDock.add(tabbedPane);
        return editDock;
    }

    /**
     * Constructs the "Logy" dock panel with a text pane for log output
     * and a combo box to filter the minimum log level.
     *
     * @return a {@link SimplePanel} containing the logging area.
     */
    public SimplePanel buildTelegDock() {
        SimplePanel telegDock = new SimplePanel("Logy", "three", null, new ArrayList<>());


        // Where you previously created and set up your JTextPane:
        JScrollPane logScrollPane = VirtualLogListAppender.createLogListComponent();


// Set up the level combo box (same as before)
        JComboBox<Level> levelCombo = new JComboBox<>(new Level[]{
                Level.OFF, Level.FATAL, Level.ERROR, Level.WARN,
                Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL
        });
        levelCombo.setSelectedItem(Level.INFO); // Default level
        VirtualLogListAppender.setLevelCombo(levelCombo);


        // Listen for changes in the combo box and update visible levels
        levelCombo.addActionListener(e -> {
            Level selectedLevel = (Level) levelCombo.getSelectedItem();
            if (selectedLevel == null) return;
            Set<Level> newLevels = new HashSet<>();

            for (Level levelOption : new Level[]{
                    Level.ALL, Level.TRACE, Level.DEBUG, Level.INFO,
                    Level.WARN, Level.ERROR, Level.FATAL, Level.OFF
            }) {
                if (levelOption.intLevel() >= selectedLevel.intLevel()) {
                    newLevels.add(levelOption);
                }
            }
            VirtualLogListAppender.setVisibleLevels(newLevels);
        });

        // A small panel to hold the label + combo
        JPanel topPanel = new JPanel();
        topPanel.add(levelCombo);

        // Assemble the final log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.add(topPanel, BorderLayout.NORTH);
        logPanel.add(logScrollPane, BorderLayout.CENTER);


        telegDock.add(logPanel);
        return telegDock;
    }

    /**
     * Builds a "Grafické zobrazení" dock (currently empty).
     *
     * @return the constructed dock panel for future UI features.
     */
    public SimplePanel buildGrafDock()
    {

        SimplePanel graphDock= new SimplePanel("Grafické zobrazení", "four", null, new ArrayList<>());
        graphPanel = new JPanel();
        graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));
        graphDock.add(graphPanel);


        return graphDock;
    }

    /**
     * Builds a "Dekatické vyjádření" dock (currently empty).
     *
     * @return an empty dock panel for future expansions.
     */
    public SimplePanel buildDecDock() {

        JPanel diffPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));

        diffPanel.add(new EditorFrame((DefaultListModel<TlgTemp>) telegramList.getModel(),files).getContentPanel(), "grow");
        
        SimplePanel binDock = new SimplePanel("Input File", "seven", null, new ArrayList<>());

        binDock.add(diffPanel);

        return binDock;
    }

    /**
     * Builds an "Input file" or "BinDock" panel (currently minimal example).
     *
     * @return a {@link SimplePanel} for binary file inputs or similar data.
     */
    public SimplePanel buildBinDock() {


        JPanel diffPanel = new JPanel(new MigLayout("fill", "[200!][grow]", "[grow][]"));

        JTabbedPane tabbedPane = new JTabbedPane();

        JScrollPane listScrollPane = new JScrollPane(telegramList);
        JScrollPane tableScrollPane = new JScrollPane(tabbedPane);
        JButton jButton = new JButton("Porovnat");

        // List takes all extra vertical space (pushy, growy)
        diffPanel.add(listScrollPane, "cell 0 0, grow, pushy");
        // Button takes minimal height, no extra vertical space
        diffPanel.add(jButton, "cell 0 1, growx");
        // Table spans both rows vertically on the right
        diffPanel.add(tableScrollPane, "cell 1 0 1 2, grow");

        jButton.addActionListener(e -> {


            String baseString = "";

            tabbedPane.removeAll();

            for (int i =0; i < telegramList.getSelectedValue().defaultListModel.getSize(); i++)
            {
                IPacket packet = telegramList.getSelectedValue().defaultListModel.get(i);
                baseString += packet.getSimpleView()+"\n";

            }


            for (int i =0; i < telegramList.getModel().getSize(); i++)
            {
                if (i==telegramList.getSelectedIndex())
                    continue;

                TlgTemp packet = ((DefaultListModel<TlgTemp>)telegramList.getModel()).get(i);
                String compareString = "";

                for (int ii =0; ii < packet.defaultListModel.getSize(); ii++)
                {
                    IPacket packet2 = packet.defaultListModel.get(ii);
                    compareString += packet2.getSimpleView() +"\n";
                }


                tabbedPane.addTab(packet.toString(),createHtmlDiffTable(baseString,compareString));

            }



        });



        SimplePanel binDock = new SimplePanel("Porovnání", "AAAABC", null, new ArrayList<>());
        binDock.add(diffPanel);

        return binDock;
    }

    /**
     * Builds a "Soubory" dock containing a file navigation component.
     * This allows the user to open or manage saved telegram files.
     *
     * @return the constructed "Soubory" dock panel.
     */
    public SimplePanel buildMapDock() {
        files = new FileManager();
        Component fileTreeComponent = files.getGui(telegramList);

        ArrayList<JMenu> menuList = new ArrayList<>();
        SimplePanel mapDock = new SimplePanel("Soubory", "eight", null, menuList);

        mapDock.add(fileTreeComponent);
        return mapDock;
    }

    //////////////////////////////////////////////////////////////////////////////
    //                           HELPER METHODS
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Moves an existing tab from one index to another in both the {@code JTabbedPane}
     * and the associated {@code DefaultListModel}.
     *
     * @param tabbedPane   the {@link JTabbedPane} containing the tabs
     * @param packetModel  the {@link DefaultListModel} holding IPacket objects
     * @param fromIndex    the original index of the tab
     * @param toIndex      the new index of the tab
     */
    private void moveTab(JTabbedPane tabbedPane, DefaultListModel<IPacket> packetModel,
                         int fromIndex, int toIndex) {

        // 1) Reorder the JTabbedPane
        Component comp   = tabbedPane.getComponentAt(fromIndex);
        Component header = tabbedPane.getTabComponentAt(fromIndex);
        String title     = tabbedPane.getTitleAt(fromIndex);
        Icon icon        = tabbedPane.getIconAt(fromIndex);
        String tooltip   = tabbedPane.getToolTipTextAt(fromIndex);
        boolean enabled  = tabbedPane.isEnabledAt(fromIndex);

        tabbedPane.removeTabAt(fromIndex);
        tabbedPane.insertTab(title, icon, comp, tooltip, toIndex);
        tabbedPane.setTabComponentAt(toIndex, header);
        tabbedPane.setEnabledAt(toIndex, enabled);
        tabbedPane.setSelectedIndex(toIndex);

        // 2) Reorder the DefaultListModel if indexes are valid
        if (fromIndex >= 0 && fromIndex < packetModel.size()
                && toIndex >= 0 && toIndex < packetModel.size()) {
            IPacket movingItem = packetModel.get(fromIndex);
            packetModel.removeElementAt(fromIndex);
            packetModel.add(toIndex, movingItem);
        }
    }

    /**
     * Builds a custom tab header for a single {@link IPacket} tab. This header
     * includes up/down reorder arrows and a close button to remove the packet.
     *
     * @param packetTabbedPane the {@link JTabbedPane} for the packets
     * @param packetModel      the model storing the {@link IPacket}s
     * @param packet           the current {@link IPacket} this tab represents
     * @param packetComponent  the UI component for this packet
     * @param refreshButton    a button that triggers recalculation of the packet lengths
     * @param iPacket
     * @return a {@link JPanel} to be set as the custom tab header
     */
    private JPanel buildTabHeader(JTabbedPane packetTabbedPane,
                                  DefaultListModel<IPacket> packetModel,
                                  IPacket packet,
                                  Component packetComponent,
                                  JButton refreshButton, IPacket iPacket)
    {

        int version = 0;

        if (iPacket instanceof PH)
        {
            version = (((PH) iPacket).getM_version().getDecValue());

        }


        JPanel tabHeader = new JPanel(new BorderLayout(5, 0));
        tabHeader.setOpaque(false);



        // Create reorder arrow panel
        JPanel arrowPanel = new JPanel(new BorderLayout());
        arrowPanel.setOpaque(false);

        JButton upButton = createArrowButton("⋀");
        JButton downButton = createArrowButton("⋁");

        // Move tab up
        upButton.addActionListener(e -> {
            int index = packetTabbedPane.indexOfComponent(packetComponent);
            if (index > 0) {
                // If the next item is P0 or P200 or PH, do not move
                if (packetModel.get(index - 1) instanceof P0
                        || packetModel.get(index - 1) instanceof P200
                        || packetModel.get(index - 1) instanceof PH) {
                    return;
                }
                moveTab(packetTabbedPane, packetModel, index, index - 1);
            }
        });

        // Move tab down
        downButton.addActionListener(e -> {
            int index = packetTabbedPane.indexOfComponent(packetComponent);
            if (index < packetTabbedPane.getTabCount() - 1) {
                if (packetModel.get(index + 1) instanceof P255) {
                    return;
                }
                moveTab(packetTabbedPane, packetModel, index, index + 1);
            }
        });

        arrowPanel.add(upButton, BorderLayout.NORTH);
        arrowPanel.add(downButton, BorderLayout.WEST);
        tabHeader.add(arrowPanel, BorderLayout.WEST);

        // Build the center label(s)
        JPanel titlePanel = buildPacketTitlePanel(packet);

        tabHeader.add(titlePanel, BorderLayout.CENTER);

        // Right side: a close button
        JButton closeButton = createCloseButton("×");
        closeButton.setFont(closeButton.getFont().deriveFont(18f));
        closeButton.addActionListener(e -> {
            // Remove packet from the model
            packetModel.removeElement(packet);
            packetTabbedPane.remove(packetComponent);
            // Force update of the parent's progress bar if desired
            refreshButton.doClick();
        });

        // Disable the close button if PH or P255
        if (packet instanceof PH || packet instanceof P255) {
            closeButton.setEnabled(false);
            closeButton.setText(" ");
        }

        // Disable arrow buttons if PH, P0, P200, P255
        if (packet instanceof PH || packet instanceof P200
                || packet instanceof P0 || packet instanceof P255) {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            upButton.setText("");
            downButton.setText("");
        }

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closePanel.setOpaque(false);
        closePanel.add(closeButton);

        tabHeader.add(closePanel, BorderLayout.EAST);

        // Listener for selecting the tab on mouse press anywhere in the header
        tabHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                packetTabbedPane.setSelectedIndex(packetTabbedPane.indexOfTabComponent(tabHeader));

                graphPanel.removeAll();

                graphPanel.add(packet.getGraphicalVisualization()==null?new JPanel():packet.getGraphicalVisualization());
            }
        });

        return tabHeader;
    }

    /**
     * Creates a small arrow button with minimal styling for up/down moves.
     *
     * @param text the text (arrow character) of the button
     * @return a {@link JButton} with hover and style settings
     */
    private JButton createArrowButton(String text) {
        JButton arrowButton = new JButton(text);
        arrowButton.setOpaque(false);
        arrowButton.setContentAreaFilled(false);
        arrowButton.setBorderPainted(false);
        arrowButton.setFocusable(false);
        arrowButton.setMinimumSize(new Dimension(30, 15));
        arrowButton.setPreferredSize(new Dimension(30, 15));
        arrowButton.setMaximumSize(new Dimension(30, 15));

        arrowButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                arrowButton.setForeground(new JTextArea().getSelectedTextColor());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                arrowButton.setForeground(new JButton().getForeground());
            }
        });
        return arrowButton;
    }

    /**
     * Creates a close button with minimal styling to remove a tab or packet.
     *
     * @param text the label text (usually "x")
     * @return a {@link JButton} styled for closing
     */
    private JButton createCloseButton(String text) {
        JButton closeButton = new JButton(text);
        closeButton.setOpaque(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusable(false);

        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(new JTextArea().getSelectedTextColor());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(new JButton().getForeground());
            }
        });
        return closeButton;
    }

    /**
     * Builds a panel that displays a {@link IPacket}'s title in a monospaced format.
     *
     * @param packet the packet whose title needs processing
     * @return a {@link JPanel} suitable for placing in a tab header
     */
    private JPanel buildPacketTitlePanel(IPacket packet) {
        // Parse the packet's text for display
        String rawTitle = packet.toString().replace("</html>", "");
        String[] parts = rawTitle.split(":");

        // Create section labels
        JLabel leftLabel   = new JLabel(parts.length > 0 ? parts[0].trim() : "");
        JLabel centerLabel = new JLabel(parts.length > 1 ? parts[1] : "");
        JLabel rightLabel  = new JLabel(parts.length > 2 ? parts[2].trim() : "");

        // Remove underscores in left label
        int underscoreIndex = leftLabel.getText().indexOf('_');
        if (underscoreIndex != -1) {
            leftLabel.setText(leftLabel.getText().substring(0, underscoreIndex));
        }

        // Add vertical bars
        leftLabel.setText(StringHelper.padLeft(leftLabel.getText(),4, ' ') + "");
        rightLabel.setText("" + rightLabel.getText());

        // Align center and right labels
        centerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rightLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Put them in a small border layout
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        leftLabel.setOpaque(false);
        centerLabel.setOpaque(false);
        rightLabel.setOpaque(false);

        titlePanel.add(leftLabel, BorderLayout.WEST);
        titlePanel.add(centerLabel, BorderLayout.CENTER);
        titlePanel.add(rightLabel, BorderLayout.EAST);

        // Set sizing
        titlePanel.setMinimumSize(new Dimension(250, 15));
        titlePanel.setPreferredSize(new Dimension(250, 15));
        titlePanel.setMaximumSize(new Dimension(250, 15));

        // Monospaced font
        Font monoFont = new Font("Monospaced", Font.PLAIN, 12);
        leftLabel.setFont(monoFont);
       // centerLabel.setFont(monoFont);
        rightLabel.setFont(monoFont);

        if (packet instanceof PH phPacket) {
            // Link the middle label to the PH packet if needed
            phPacket.setjLabel1(centerLabel);
        }

        return titlePanel;
    }

    /**
     * Builds the custom tab header that appears in the "Přidat packet" tab,
     * containing a combo box to select new packet types, a progress bar, and
     * a button to insert the chosen packet into the telegram.
     *
     * @param packetTabbedPane   the parent {@link JTabbedPane} for the IPackets
     * @param packetModel        the model containing {@link IPacket} objects
     * @param totalLengthBar     a progress bar indicating total packet length
     * @param refreshButton      a button triggering re-check of total lengths
     */
    private void buildAddPacketTabHeader(JTabbedPane packetTabbedPane,
                                         DefaultListModel<IPacket> packetModel,
                                         JProgressBar totalLengthBar,
                                         JButton refreshButton) {
        int lastIndex = packetTabbedPane.getTabCount() - 1;

        // Container for everything in the "Přidat packet" tab header
        JPanel addPacketHeader = new JPanel(new BorderLayout(5, 0));
        addPacketHeader.setOpaque(false);

        addPacketHeader.setBorder(BorderFactory.createEtchedBorder());

// With this custom border implementation:
        addPacketHeader.setBorder(new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();

                // Use the component's background color to determine border colors
                Color background = c.getBackground();
                Color highlight = background.brighter();
                Color shadow = background.darker();

                // Draw top etched line
                g2d.setColor(shadow);
                g2d.drawLine(x, y, x + width - 1, y);
                g2d.setColor(highlight);
                g2d.drawLine(x, y + 1, x + width - 1, y + 1);

                // Draw right etched line
                g2d.setColor(shadow);
                g2d.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
                g2d.setColor(highlight);
                g2d.drawLine(x + width - 2, y, x + width - 2, y + height - 1);

                g2d.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                // Return zero insets to avoid adding extra space
                return new Insets(5, 5, 5, 5);
            }

            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                insets.top = 5;
                insets.left = 5;
                insets.bottom = 5;
                insets.right = 5;
                return insets;
            }
        });

        // 1) Combo box for selecting which packet to add
        JComboBox<Packet> comboBox = new JComboBox<>();
        GUIHelper.initNewPlist(comboBox);

        // Custom cell renderer for the combo box
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            // Build a panel with a single label
            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(true);

            JLabel centerLabel = new JLabel();
            centerLabel.setHorizontalAlignment(SwingConstants.CENTER);
            centerLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));

            if (value != null) {
                String rawValue = value.toString().replace("</html>", "");
                String[] parts = rawValue.split(":");
                String first = (parts.length > 0) ? parts[0].trim() : "";
                String third = (parts.length > 2) ? parts[2].trim() : "";
                centerLabel.setText(first + third);
            } else {
                centerLabel.setText("");
            }

            Color bg = isSelected ? UIManager.getColor("List.selectionBackground") :
                    UIManager.getColor("List.background");
            Color fg = isSelected ? UIManager.getColor("List.selectionForeground") :
                    UIManager.getColor("List.foreground");

            panel.setBackground(bg);
            centerLabel.setForeground(fg);

            panel.add(centerLabel, BorderLayout.CENTER);
            return panel;
        });

        // 2) A label and combo
        addPacketHeader.add(new JLabel("Přidat Packet: "), BorderLayout.WEST);
        addPacketHeader.add(comboBox, BorderLayout.CENTER);

        // 3) Button to add the selected packet
        JButton plusButton = new JButton("+");

        plusButton.setFocusable(false);
        plusButton.addActionListener(e -> {
            IPacket selected = (IPacket) comboBox.getSelectedItem();
            // Disallow adding PH
            if (selected instanceof PH) return;

            int position = 1;
            // If first index is P0 or P200, skip second index
            if (packetModel.get(1) instanceof P0 || packetModel.get(1) instanceof P200) {
                position = 2;
            }

            packetModel.add(position, selected);
            // Rebuild the internal packet tabs
            rebuildPacketTabs(packetTabbedPane, packetModel);

            // Reorder the "Přidat packet" tab to the end
            moveAddPacketTabToEnd(packetTabbedPane);

            // Trigger refresh
            refreshButton.doClick();
        });

        // 4) Put plus button to the right
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(plusButton);
        addPacketHeader.add(rightPanel, BorderLayout.EAST);

        // 5) Add layout for bottom portion: progress bar, encode buttons, etc.
        JPanel addPacketContainer = new JPanel(new MigLayout("", "[]5[]", "[]5[]"));
        addPacketContainer.setOpaque(false);

        addPacketContainer.add(addPacketHeader, "newline");

        // Add totalLengthBar + tie it to refreshButton
        addPacketContainer.add(totalLengthBar, "newline,growx");
        totalLengthBar.setFont(new Font("Monospaced", Font.BOLD, 14));

        refreshButton.addActionListener(e -> {
            // Recompute total bits
            int totalBits = 0;
            for (int i = 0; i < packetModel.getSize(); i++) {
                totalBits += packetModel.getElementAt(i).getBinData().length();
            }

            // 210 for short, 830 for long
            String type = (totalBits <= 210) ? "Krátký" : "Dlouhý";
            totalLengthBar.setMaximum((totalBits <= 210) ? 210 : 830);

            if (totalBits > 830) {
                totalLengthBar.setString("ERROR ( " + totalBits + " )");
            } else {
                totalLengthBar.setString("OK ( " + totalBits + " ) [" + type + "]");
            }

            totalLengthBar.setStringPainted(true);
            totalLengthBar.setValue(totalBits);
        });
        refreshButton.doClick();

        // 7) "Encode" button and progress bar
        JButton encodeBtn = new JButton("Zakódovat jeden telegram");
        JButton encodeBtn2 = new JButton("Zakódovat a vytvořit BG (2x B)");
        addPacketContainer.add(encodeBtn2, "newline,growx");
        addPacketContainer.add(encodeBtn, "newline,growx");

        JProgressBar encodingProgressBar = new JProgressBar();
        encodingProgressBar.setFont(new Font("Monospaced", Font.PLAIN, 14));
        addPacketContainer.add(encodingProgressBar, "newline,growx");
        final Logger LOG = LogManager.getLogger(encodeBtn.getClass());

        encodeBtn.addActionListener(e -> {
            encodingProgressBar.setIndeterminate(true);
            encodingProgressBar.setString("Kodovaní telegramu");
            encodingProgressBar.setStringPainted(true);

            SwingWorker<Void, Void> encoderWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground()
                {

                    String s ="";

                    LOG.info("Sestavování telegramu");

                    for (int i =0;i<packetModel.getSize(); i++)
                    {
                        LOG.debug("Přidávám: "+packetModel.getElementAt(i).toString() + "; Hex: " + ArithmeticalFunctions.bin2Hex(packetModel.getElementAt(i).getBinData()));
                        LOG.trace(packetModel.getElementAt(i).getSimpleView());

                        s+= packetModel.get(i).getBinData();
                    }

                                    int targetLength = (s.length() <= 210) ? 210 : 830;
                                    LOG.trace("Velikost tlg:"+targetLength);

                                    StringBuilder sb = new StringBuilder(s);
                                    // Append '1' until the string reaches the target length
                                    while (sb.length() < targetLength) {
                                        sb.append('1');
                                    }
                                    sb.append('0');
                                    sb.append('0');
                                    s= sb.toString();

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

                                        if (selectedFile == null)
                                        {

                                            LOG.warn("Není zvolena složka pro ukládaní tlg");
                                            LOG.error("Telegram neuložen");
                                            return null;
                                        }
                                         LOG.info("Cesta pro uložení: "+selectedFile.getAbsolutePath());



                                        int ver = 0;

                                         String filename = selectedFile.toString();

                                         filename += "/"+name+"_v"+StringHelper.padLeft(String.valueOf(ver),3, '0')+".tlg";

                                        File file = new File(filename);

                                        while (file.exists())
                                        {
                                            ver++;
                                            filename = selectedFile.toString();
                                            filename += "/"+name+"_v"+StringHelper.padLeft(String.valueOf(ver),3, '0')+".tlg";
                                            file = new File(filename);

                                        }

                                         LOG.debug("Název: "+filename);

                                        final FileWriter myWriter = null;

                                        try {

                                            final File outputFile = file.getAbsoluteFile();
                                            String lastTelegram = tlg;
                                            LOG.info("Ukládám: "+outputFile.getAbsolutePath());
                                            ArrayList<Byte> tmp = new ArrayList<Byte>();

                                            int index = 0;

                                            for (int i = 0; i < lastTelegram.length() - 1; i += 2) {

                                                final char c = (char) ArithmeticalFunctions.bin2Dec(ArithmeticalFunctions.hex2Bin(lastTelegram.charAt(i) + "" + lastTelegram.charAt(i + 1)));

                                                tmp.add((byte) c);

                                                index++;

                                            }
                                            try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                                                // Step 2: Initialize a byte array of the same size as the ArrayList
                                                byte[] byteArray = new byte[tmp.size()];
                                                // Step 3: Convert each Byte object to a byte primitive
                                                for (int i = 0; i < tmp.size(); i++) {
                                                    byteArray[i] = tmp.get(i);
                                                }
                                                outputStream.write(byteArray);
                                            }

                                        } catch (final IOException ex)
                                        {
                                            LOG.error(ex.getMessage());
                                        }


                                    }



                    return null;
                }

                @Override
                protected void done() {
                    encodingProgressBar.setIndeterminate(false);
                    encodingProgressBar.setString("Hotovo");
                    encodingProgressBar.setValue(encodingProgressBar.getMaximum());
                }
            };
            encoderWorker.execute();
        });

        encodeBtn2.addActionListener(e -> {
            encodingProgressBar.setIndeterminate(true);
            encodingProgressBar.setString("Kodovaní telegramu");
            encodingProgressBar.setStringPainted(true);

            SwingWorker<Void, Void> encoderWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground()
                {

                   PH phX = (PH) packetModel.get(0);

                    for (int ii =0;ii<2;ii++)
                   {

                       PH ph1 = (PH) phX.deepCopy();

                       ph1.getQ_updown() .setBinValue(phX.getQ_updown() .getBinValue());
                       ph1.getM_version().setBinValue(phX.getM_version().getBinValue());
                       ph1.getQ_media()  .setBinValue(phX.getQ_media()  .getBinValue());
                       ph1.getNid_bg()   .setBinValue(phX.getNid_bg()   .getBinValue());
                       ph1.getM_mcount() .setBinValue(phX.getM_mcount() .getBinValue());
                       ph1.getNid_c()    .setBinValue(phX.getNid_c()    .getBinValue());


                       ph1.getM_dup().setBinValue(String.valueOf(ArithmeticalFunctions.dec2XBin(String.valueOf(ii+1),2)));
                       ph1.getN_pig().setBinValue(String.valueOf(ii));
                       ph1.getN_total().setBinValue("1");


                    String s ="";

                    s+= ph1.getBinData();

                    LOG.info("Sestavování telegramu");

                    for (int i =1;i<packetModel.getSize(); i++)
                    {
                        LOG.debug("Přidávám: "+packetModel.getElementAt(i).toString() + "; Hex: " + ArithmeticalFunctions.bin2Hex(packetModel.getElementAt(i).getBinData()));
                        LOG.trace(packetModel.getElementAt(i).getSimpleView());

                        s+= packetModel.get(i).getBinData();
                    }

                    int targetLength = (s.length() <= 210) ? 210 : 830;
                    LOG.trace("Velikost tlg:"+targetLength);

                    StringBuilder sb = new StringBuilder(s);
                    // Append '1' until the string reaches the target length
                    while (sb.length() < targetLength) {
                        sb.append('1');
                    }
                    sb.append('0');
                    sb.append('0');
                    s= sb.toString();

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

                        if (selectedFile == null)
                        {

                            LOG.warn("Není zvolena složka pro ukládaní tlg");
                            LOG.error("Telegram neuložen");
                            return null;
                        }
                        LOG.info("Cesta pro uložení: "+selectedFile.getAbsolutePath());



                        int ver = 0;

                        String filename = selectedFile.toString();

                        filename += "/"+name+"_v"+StringHelper.padLeft(String.valueOf(ver),3, '0')+".tlg";

                        File file = new File(filename);

                        while (file.exists())
                        {
                            ver++;
                            filename = selectedFile.toString();
                            filename += "/"+name+"_v"+StringHelper.padLeft(String.valueOf(ver),3, '0')+".tlg";
                            file = new File(filename);

                        }

                        LOG.debug("Název: "+filename);

                        final FileWriter myWriter = null;

                        try {

                            final File outputFile = file.getAbsoluteFile();
                            String lastTelegram = tlg;
                            LOG.info("Ukládám: "+outputFile.getAbsolutePath());
                            ArrayList<Byte> tmp = new ArrayList<Byte>();

                            int index = 0;

                            for (int i = 0; i < lastTelegram.length() - 1; i += 2) {

                                final char c = (char) ArithmeticalFunctions.bin2Dec(ArithmeticalFunctions.hex2Bin(lastTelegram.charAt(i) + "" + lastTelegram.charAt(i + 1)));

                                tmp.add((byte) c);

                                index++;

                            }
                            try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                                // Step 2: Initialize a byte array of the same size as the ArrayList
                                byte[] byteArray = new byte[tmp.size()];
                                // Step 3: Convert each Byte object to a byte primitive
                                for (int i = 0; i < tmp.size(); i++) {
                                    byteArray[i] = tmp.get(i);
                                }
                                outputStream.write(byteArray);
                            }

                        } catch (final IOException ex)
                        {
                            LOG.error(ex.getMessage());
                        }

                    }
                    }



                    return null;
                }

                @Override
                protected void done() {
                    encodingProgressBar.setIndeterminate(false);
                    encodingProgressBar.setString("Hotovo");
                    encodingProgressBar.setValue(encodingProgressBar.getMaximum());
                }
            };
            encoderWorker.execute();
        });

        // 8) Place the container in the "Přidat packet" tab header
        packetTabbedPane.setTabComponentAt(lastIndex, addPacketContainer);
        packetTabbedPane.setEnabledAt(lastIndex, false); // do not allow direct selection
    }

    /**
     * Helper that re-triggers the logic for building all packet tabs in a single telegram.
     *
     * @param packetTabbedPane the parent {@link JTabbedPane} for this telegram
     * @param packetModel      the {@link DefaultListModel} of IPackets
     */
    private void rebuildPacketTabs(JTabbedPane packetTabbedPane, DefaultListModel<IPacket> packetModel) {
        // Remove all tabs except "Přidat packet"
        for (int idx = packetTabbedPane.getTabCount() - 1; idx >= 0; idx--) {
            if (!"Přidat packet".equals(packetTabbedPane.getTitleAt(idx))) {
                packetTabbedPane.removeTabAt(idx);
            }
        }

        // Re-add each packet in the model
        for (int z = 0; z < packetModel.size(); z++) {
            IPacket packet = packetModel.get(z);
            Component packetComponent = packet.getPacketComponent();
            packetTabbedPane.addTab(packet.toString(), packetComponent);

            // Build the custom header
            JPanel tabHeader = buildTabHeader(packetTabbedPane, packetModel, packet, packetComponent, new JButton(),packetModel.get(0));
            int lastIndex = packetTabbedPane.getTabCount() - 1;
            packetTabbedPane.setTabComponentAt(lastIndex, tabHeader);
        }
    }

    /**
     * Moves the "Přidat packet" tab to the end of the tab list, keeping it out of the main packet ordering.
     *
     * @param packetTabbedPane the {@link JTabbedPane} containing the packet tabs
     */
    private void moveAddPacketTabToEnd(JTabbedPane packetTabbedPane) {
        Component comp   = packetTabbedPane.getComponentAt(0);
        Component header = packetTabbedPane.getTabComponentAt(0);
        String title     = packetTabbedPane.getTitleAt(0);
        Icon icon        = packetTabbedPane.getIconAt(0);
        String tooltip   = packetTabbedPane.getToolTipTextAt(0);
        boolean enabled  = packetTabbedPane.isEnabledAt(0);

        int lastIndex = packetTabbedPane.getTabCount() - 1;
        packetTabbedPane.removeTabAt(0);
        packetTabbedPane.insertTab(title, icon, comp, tooltip, lastIndex);
        packetTabbedPane.setTabComponentAt(lastIndex, header);
        packetTabbedPane.setEnabledAt(lastIndex, enabled);
        packetTabbedPane.setSelectedIndex(lastIndex);
    }

    /**
     * Builds and attaches a custom header for an entire telegram tab.
     * Includes a title label (linked to {@link PH}) and a close button
     * that removes the entire telegram from the list.
     *
     * @param topTabbedPane  the main DnDTabbedPane that holds all telegrams
     * @param listModel      the {@link DefaultListModel} storing all {@link TlgTemp} telegram objects
     * @param telegramEntry  the current telegram data instance
     * @return a {@link JPanel} representing the custom tab header
     */
    private JPanel buildTelegramTabHeader(DnDTabbedPane topTabbedPane,
                                          DefaultListModel<TlgTemp> listModel,
                                          TlgTemp telegramEntry)
    {
        JPanel mainTabHeader = new JPanel(new BorderLayout(0, 0));
        mainTabHeader.setOpaque(false);
        // Replace this line:
        mainTabHeader.setBorder(BorderFactory.createEtchedBorder());

// With this custom border implementation:
        mainTabHeader.setBorder(new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();

                // Use the component's background color to determine border colors
                Color background = c.getBackground();
                Color highlight = background.brighter();
                Color shadow = background.darker();

                // Draw top etched line
                g2d.setColor(shadow);
                g2d.drawLine(x, y, x + width - 1, y);
                g2d.setColor(highlight);
                g2d.drawLine(x, y + 1, x + width - 1, y + 1);

                // Draw right etched line
                g2d.setColor(shadow);
                g2d.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
                g2d.setColor(highlight);
                g2d.drawLine(x + width - 2, y, x + width - 2, y + height - 1);

                g2d.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                // Return zero insets to avoid adding extra space
                return new Insets(0, 0, 0, 0);
            }

            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                insets.top = 0;
                insets.left = 0;
                insets.bottom = 0;
                insets.right = 0;
                return insets;
            }
        });


        JLabel mainLabel = new JLabel(String.valueOf(telegramEntry));
        mainLabel.setOpaque(false);
        mainTabHeader.add(mainLabel, BorderLayout.CENTER);

        // If the first IPacket is PH, link it to the main label if needed
        if (!telegramEntry.defaultListModel.isEmpty() &&
                telegramEntry.defaultListModel.get(0) instanceof PH ph)
        {
            ph.setjLabel(mainLabel);

        }

        JButton closeButton = createCloseButton("×");
        closeButton.addActionListener(e -> {
            int tabIndex = topTabbedPane.indexOfTabComponent(mainTabHeader);
            if (tabIndex != -1) {
                topTabbedPane.remove(tabIndex);
                listModel.removeElement(telegramEntry);
            }
        });
        closeButton.setFont(closeButton.getFont().deriveFont(18f));
        // Mouse hover effects
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(new JTextArea().getSelectedTextColor());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(new JButton().getForeground());
            }
        });

        JButton copyButton = createCloseButton("⧉");

        copyButton.setFont(copyButton.getFont().deriveFont(14f));
        copyButton.addActionListener(e -> {
            ((DefaultListModel<TlgTemp>) telegramList.getModel()).add(
                    0, new TlgTemp("",
                            ArithmeticalFunctions.bin2Hex(telegramEntry.getTlg()) )
            );
        });

        // Mouse hover effects
        copyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                copyButton.setForeground(new JTextArea().getSelectedTextColor());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                copyButton.setForeground(new JButton().getForeground());
            }
        });

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));



        rightSide.setOpaque(false);
        rightSide.add(copyButton);
        rightSide.add(closeButton);

        mainTabHeader.add(rightSide, BorderLayout.EAST);
        return mainTabHeader;
    }

    /**
     * Builds and attaches a custom "Přidat nový telegram" tab to the end of the top-level
     * {@link DnDTabbedPane}.
     *
     * @param topTabbedPane the main {@link DnDTabbedPane} used for entire telegrams
     */
    private void buildAddTelegramTabHeader(DnDTabbedPane topTabbedPane) {
        int lastIndex = topTabbedPane.getTabCount() - 1;

        JPanel addTelegramHeader = new JPanel(new BorderLayout(0, 0));
        addTelegramHeader.setOpaque(false);

        JLabel emptyTitle = new JLabel("");
        addTelegramHeader.add(emptyTitle, BorderLayout.CENTER);

        // Add button to create a new empty telegram
        JButton addTelegramButton = createCloseButton("+");

        addTelegramButton.setFont(addTelegramButton.getFont().deriveFont(16f));
        addTelegramButton.setFocusable(false);
        addTelegramButton.addActionListener(e -> {
            ((DefaultListModel<TlgTemp>) telegramList.getModel()).add(
                    0, new TlgTemp("", "911372C07001FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")
            );
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
