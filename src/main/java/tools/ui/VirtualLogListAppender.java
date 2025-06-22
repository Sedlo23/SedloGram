package tools.ui;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * A modern, highly optimized Log4j2 Appender with sleek UI design and non-blocking operations.
 */
@Plugin(
        name = "AsyncTextArea",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE
)
public class VirtualLogListAppender extends AbstractAppender {

    /**
     * Represents a single log entry with all needed metadata
     */
    public static class LogEntry {
        private final String formattedMessage;
        private final String rawMessage;
        private final Level level;
        private final long timestamp;
        private final String loggerName;

        public LogEntry(String formattedMessage, String rawMessage, Level level, long timestamp, String loggerName) {
            this.formattedMessage = formattedMessage;
            this.rawMessage = rawMessage;
            this.level = level;
            this.timestamp = timestamp;
            this.loggerName = loggerName;
        }

        public String getFormattedMessage() { return formattedMessage; }
        public String getRawMessage() { return rawMessage; }
        public Level getLevel() { return level; }
        public long getTimestamp() { return timestamp; }
        public String getLoggerName() { return loggerName; }
    }

    /**
     * Compact, modern card-style renderer for log entries
     */
    private static class ModernLogCellRenderer extends JPanel implements ListCellRenderer<LogEntry> {
        private final JLabel iconLabel;
        private final JLabel messageLabel;
        private final JLabel timestampLabel;
        private final JLabel levelLabel;
        private final SimpleDateFormat timeFormat;

        public ModernLogCellRenderer() {
            setLayout(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12)); // Reduced padding

            timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

            // Left side: Icon + Level (more compact)
            JPanel leftPanel = new JPanel(new BorderLayout(6, 0));
            leftPanel.setOpaque(false);

            iconLabel = new JLabel();
            iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12)); // Smaller icon
            iconLabel.setPreferredSize(new Dimension(16, 16));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

            levelLabel = new JLabel();
            levelLabel.setFont(UIManager.getFont("Label.font"));
            if (levelLabel.getFont() != null) {
                levelLabel.setFont(levelLabel.getFont().deriveFont(Font.BOLD, 10f)); // Smaller font
            }
            levelLabel.setPreferredSize(new Dimension(45, 16));

            leftPanel.add(iconLabel, BorderLayout.WEST);
            leftPanel.add(levelLabel, BorderLayout.CENTER);

            // Center: Message (more compact)
            messageLabel = new JLabel();
            Font monoFont = UIManager.getFont("TextArea.font");
            if (monoFont == null) {
                monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 11); // Smaller font
            } else {
                monoFont = monoFont.deriveFont(11f);
            }
            messageLabel.setFont(monoFont);

            // Right side: Timestamp (more compact)
            timestampLabel = new JLabel();
            timestampLabel.setFont(UIManager.getFont("Label.font"));
            if (timestampLabel.getFont() != null) {
                timestampLabel.setFont(timestampLabel.getFont().deriveFont(9f)); // Smaller font
            }
            timestampLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            timestampLabel.setPreferredSize(new Dimension(70, 16));

            add(leftPanel, BorderLayout.WEST);
            add(messageLabel, BorderLayout.CENTER);
            add(timestampLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends LogEntry> list, LogEntry entry,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            // Set message and timestamp
            messageLabel.setText(truncateMessage(entry.getFormattedMessage(), 150));
            timestampLabel.setText(timeFormat.format(new Date(entry.getTimestamp())));

            // Configure level and icon based on log level
            configureLevelDisplay(entry.getLevel());

            // Apply modern styling
            applyModernStyling(list, isSelected, index % 2 == 0);

            return this;
        }

        private void configureLevelDisplay(Level level) {
            String levelName = level.getStandardLevel().toString();
            levelLabel.setText(levelName);

            // Modern icons and colors for each level
            switch (levelName) {
                case "FATAL" -> {
                    iconLabel.setText("💀");
                    Color fatalColor = UIManager.getColor("Actions.Red");
                    if (fatalColor == null) fatalColor = new Color(220, 53, 69);
                    levelLabel.setForeground(fatalColor);
                    iconLabel.setForeground(fatalColor);
                }
                case "ERROR" -> {
                    iconLabel.setText("❌");
                    Color errorColor = UIManager.getColor("Actions.Red");
                    if (errorColor == null) errorColor = new Color(220, 53, 69);
                    levelLabel.setForeground(errorColor);
                    iconLabel.setForeground(errorColor);
                }
                case "WARN" -> {
                    iconLabel.setText("⚠️");
                    Color warnColor = UIManager.getColor("Actions.Yellow");
                    if (warnColor == null) warnColor = new Color(255, 193, 7);
                    levelLabel.setForeground(warnColor);
                    iconLabel.setForeground(warnColor);
                }
                case "INFO" -> {
                    iconLabel.setText("ℹ️");
                    Color infoColor = UIManager.getColor("Component.accentColor");
                    if (infoColor == null) infoColor = new Color(13, 110, 253);
                    levelLabel.setForeground(infoColor);
                    iconLabel.setForeground(infoColor);
                }
                case "DEBUG" -> {
                    iconLabel.setText("🔧");
                    Color debugColor = UIManager.getColor("Actions.Green");
                    if (debugColor == null) debugColor = new Color(25, 135, 84);
                    levelLabel.setForeground(debugColor);
                    iconLabel.setForeground(debugColor);
                }
                case "TRACE" -> {
                    iconLabel.setText("🔍");
                    Color traceColor = UIManager.getColor("Component.borderColor");
                    if (traceColor == null) traceColor = new Color(108, 117, 125);
                    levelLabel.setForeground(traceColor);
                    iconLabel.setForeground(traceColor);
                }
                default -> {
                    iconLabel.setText("📝");
                    levelLabel.setForeground(UIManager.getColor("Label.foreground"));
                    iconLabel.setForeground(UIManager.getColor("Label.foreground"));
                }
            }
        }

        private void applyModernStyling(JList<?> list, boolean isSelected, boolean isEvenRow) {
            Color backgroundColor;
            Color textColor = UIManager.getColor("Label.foreground");

            if (isSelected) {
                backgroundColor = UIManager.getColor("List.selectionBackground");
                textColor = UIManager.getColor("List.selectionForeground");
            } else {
                backgroundColor = UIManager.getColor("List.background");

                // Subtle alternating row colors for better readability
                if (isEvenRow) {
                    Color altColor = UIManager.getColor("Table.alternateRowColor");
                    if (altColor != null) {
                        backgroundColor = altColor;
                    }
                }
            }

            setBackground(backgroundColor);
            messageLabel.setForeground(textColor);
            timestampLabel.setForeground(
                    isSelected ? textColor : UIManager.getColor("Label.disabledForeground")
            );

            // Minimal border for compact design
            setBorder(createCompactCellBorder(isSelected));
        }

        private AbstractBorder createCompactCellBorder(boolean isSelected) {
            return new AbstractBorder() {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (isSelected) {
                        // Thin accent border for selected items
                        Color accentColor = UIManager.getColor("Component.accentColor");
                        if (accentColor == null) accentColor = new Color(13, 110, 253);
                        g2d.setColor(accentColor);
                        g2d.setStroke(new BasicStroke(1f));
                        g2d.drawRoundRect(x, y, width - 1, height - 1, 4, 4);
                    } else {
                        // Very subtle separator line
                        Color borderColor = UIManager.getColor("Component.borderColor");
                        if (borderColor == null) borderColor = new Color(0, 0, 0, 10);
                        g2d.setColor(borderColor);
                        g2d.drawLine(x + 12, y + height - 1, x + width - 12, y + height - 1);
                    }
                    g2d.dispose();
                }

                @Override
                public Insets getBorderInsets(Component c) {
                    return new Insets(1, 0, 1, 0);
                }
            };
        }

        private String truncateMessage(String message, int maxLength) {
            if (message.length() <= maxLength) {
                return message;
            }
            return message.substring(0, maxLength - 3) + "...";
        }
    }

    /**
     * Fixed filter panel that stays at the top
     */
    private static class ModernFilterPanel extends JPanel {
        private final JTextField searchField;
        private final JComboBox<Level> levelCombo;
        private final JButton clearButton;
        private final JButton exportButton;
        private final JLabel statusLabel;

        public ModernFilterPanel() {
            setLayout(new BorderLayout(8, 0));
            setBackground(UIManager.getColor("Panel.background"));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // Reduced padding

            // Left side: Search functionality
            JPanel searchPanel = createModernSearchPanel();

            // Center: Level filtering
            JPanel levelPanel = createModernLevelPanel();

            // Right side: Actions and status
            JPanel actionsPanel = createModernActionsPanel();

            add(searchPanel, BorderLayout.WEST);
            add(levelPanel, BorderLayout.CENTER);
            add(actionsPanel, BorderLayout.EAST);

            // Initialize components
            searchField = (JTextField) ((JPanel) searchPanel.getComponent(1)).getComponent(0);
            levelCombo = (JComboBox<Level>) ((JPanel) levelPanel.getComponent(1)).getComponent(0);
            clearButton = (JButton) ((JPanel) actionsPanel.getComponent(0)).getComponent(0);
            exportButton = (JButton) ((JPanel) actionsPanel.getComponent(0)).getComponent(1);
            statusLabel = (JLabel) actionsPanel.getComponent(1);
        }

        private JPanel createModernSearchPanel() {
            JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
            searchPanel.setOpaque(false);

            JLabel searchIcon = new JLabel("🔍");
            searchIcon.setFont(searchIcon.getFont().deriveFont(12f));
            searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

            JPanel searchFieldPanel = new JPanel(new BorderLayout());
            JTextField searchField = new JTextField(18); // Slightly smaller
            searchField.setFont(UIManager.getFont("TextField.font"));
            searchField.setBorder(BorderFactory.createCompoundBorder(
                    createModernFieldBorder(),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10) // Reduced padding
            ));
            searchField.setBackground(UIManager.getColor("TextField.background"));
            searchField.setForeground(UIManager.getColor("TextField.foreground"));

            // Placeholder text effect
            searchField.putClientProperty("JTextField.placeholderText", "Search logs...");

            searchFieldPanel.add(searchField, BorderLayout.CENTER);

            searchPanel.add(searchIcon, BorderLayout.WEST);
            searchPanel.add(searchFieldPanel, BorderLayout.CENTER);

            return searchPanel;
        }

        private JPanel createModernLevelPanel() {
            JPanel levelPanel = new JPanel(new BorderLayout(6, 0));
            levelPanel.setOpaque(false);

            JLabel levelIcon = new JLabel("📊");
            levelIcon.setFont(levelIcon.getFont().deriveFont(12f));

            JPanel comboPanel = new JPanel(new BorderLayout());
            // Fixed: Proper level order from highest to lowest priority
            JComboBox<Level> levelCombo = new JComboBox<>(new Level[]{
                    Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL
            });
            levelCombo.setSelectedItem(Level.INFO); // Start with INFO

            // Modern combo box styling
            levelCombo.setFont(UIManager.getFont("ComboBox.font"));
            levelCombo.setBorder(createModernFieldBorder());
            levelCombo.setBackground(UIManager.getColor("ComboBox.background"));
            levelCombo.setForeground(UIManager.getColor("ComboBox.foreground"));
            levelCombo.setPreferredSize(new Dimension(90, 28)); // Smaller size

            comboPanel.add(levelCombo, BorderLayout.CENTER);

            levelPanel.add(levelIcon, BorderLayout.WEST);
            levelPanel.add(comboPanel, BorderLayout.CENTER);

            return levelPanel;
        }

        private JPanel createModernActionsPanel() {
            JPanel actionsPanel = new JPanel(new BorderLayout(6, 0));
            actionsPanel.setOpaque(false);

            // Buttons panel
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            buttonsPanel.setOpaque(false);

            JButton clearButton = createModernActionButton("🗑️", "Clear logs");
            JButton exportButton = createModernActionButton("💾", "Export logs");

            buttonsPanel.add(clearButton);
            buttonsPanel.add(exportButton);

            // Status label
            JLabel statusLabel = new JLabel("Ready");
            statusLabel.setFont(UIManager.getFont("Label.font"));
            if (statusLabel.getFont() != null) {
                statusLabel.setFont(statusLabel.getFont().deriveFont(10f)); // Smaller font
            }
            statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

            actionsPanel.add(buttonsPanel, BorderLayout.WEST);
            actionsPanel.add(statusLabel, BorderLayout.EAST);

            return actionsPanel;
        }

        private JButton createModernActionButton(String icon, String tooltip) {
            JButton button = new JButton(icon);
            button.setToolTipText(tooltip);
            button.setPreferredSize(new Dimension(28, 28)); // Smaller buttons
            button.setBorder(createModernButtonBorder());
            button.setBackground(UIManager.getColor("Button.background"));
            button.setForeground(UIManager.getColor("Button.foreground"));
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Hover effects
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    Color hoverColor = UIManager.getColor("Button.hoverBackground");
                    if (hoverColor == null) {
                        hoverColor = UIManager.getColor("List.selectionBackground");
                    }
                    if (hoverColor != null) {
                        button.setBackground(hoverColor);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(UIManager.getColor("Button.background"));
                }
            });

            return button;
        }

        private AbstractBorder createModernFieldBorder() {
            return new AbstractBorder() {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Color borderColor = UIManager.getColor("Component.borderColor");
                    if (borderColor == null) {
                        borderColor = new Color(200, 200, 200);
                    }

                    g2d.setColor(borderColor);
                    g2d.setStroke(new BasicStroke(1f));
                    g2d.drawRoundRect(x, y, width - 1, height - 1, 6, 6);
                    g2d.dispose();
                }

                @Override
                public Insets getBorderInsets(Component c) {
                    return new Insets(1, 1, 1, 1);
                }
            };
        }

        private AbstractBorder createModernButtonBorder() {
            return new AbstractBorder() {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (c instanceof AbstractButton && ((AbstractButton) c).getModel().isPressed()) {
                        Color pressedColor = UIManager.getColor("Button.pressedBackground");
                        if (pressedColor == null) {
                            pressedColor = new Color(0, 0, 0, 30);
                        }
                        g2d.setColor(pressedColor);
                        g2d.fillRoundRect(x, y, width, height, 6, 6);
                    }

                    Color borderColor = UIManager.getColor("Component.borderColor");
                    if (borderColor == null) {
                        borderColor = new Color(200, 200, 200);
                    }

                    g2d.setColor(borderColor);
                    g2d.setStroke(new BasicStroke(1f));
                    g2d.drawRoundRect(x, y, width - 1, height - 1, 6, 6);
                    g2d.dispose();
                }

                @Override
                public Insets getBorderInsets(Component c) {
                    return new Insets(1, 1, 1, 1);
                }
            };
        }

        public JTextField getSearchField() { return searchField; }
        public JComboBox<Level> getLevelCombo() { return levelCombo; }
        public JButton getClearButton() { return clearButton; }
        public JButton getExportButton() { return exportButton; }
        public JLabel getStatusLabel() { return statusLabel; }
    }

    // Static fields
    private static final CopyOnWriteArrayList<LogEntry> allEntries = new CopyOnWriteArrayList<>();
    private static DefaultListModel<LogEntry> filteredModel;
    private static JList<LogEntry> logList;
    private static ModernFilterPanel filterPanel;

    // Background processing
    private static final ExecutorService filterExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "LogFilter");
        t.setDaemon(true);
        return t;
    });

    // Filter state
    private static final AtomicReference<String> currentSearchText = new AtomicReference<>("");
    private static final AtomicReference<Pattern> searchPattern = new AtomicReference<>();
    private static final AtomicReference<Level> currentMinLevel = new AtomicReference<>(Level.INFO);
    private static final AtomicBoolean filteringInProgress = new AtomicBoolean(false);

    // Debouncing
    private static Timer debounceTimer;

    // Constants
    private static final int MAX_LOG_ENTRIES = 100000;
    private static final int MAX_BATCH_SIZE = 100;
    private static final long MAX_BATCH_WAIT_MS = 150;
    private static final int FILTER_DEBOUNCE_MS = 300;

    // Instance fields
    private final List<LogEvent> eventQueue = new ArrayList<>();
    private Timer batchTimer;

    static {
        // Create filtered model
        filteredModel = new DefaultListModel<>();
    }

    /**
     * Creates the modern log display component with fixed header
     */
    public static JScrollPane createLogListComponent() {
        if (logList == null) {
            // Create filter panel
            filterPanel = new ModernFilterPanel();

            // Create modern log list
            logList = new JList<>(filteredModel);
            logList.setCellRenderer(new ModernLogCellRenderer());
            logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            logList.setFixedCellHeight(24); // Compact height
            logList.setVisibleRowCount(25);
            logList.setBackground(UIManager.getColor("List.background"));

            // Setup search functionality with debouncing
            setupSearchFunctionality();

            // Setup level filtering
            setupLevelFiltering();

            // Setup action buttons
            setupActionButtons();

            // Create main panel - NO OUTER SCROLL PANE
            JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
            mainPanel.setBackground(UIManager.getColor("Panel.background"));

            // Add filter panel at top (stays fixed)
            mainPanel.add(filterPanel, BorderLayout.NORTH);

            // Add scrollable log list in center
            JScrollPane logScrollPane = new JScrollPane(logList);
            logScrollPane.setBorder(null);
            logScrollPane.setBackground(UIManager.getColor("ScrollPane.background"));
            logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            mainPanel.add(logScrollPane, BorderLayout.CENTER);

            // Return the main panel wrapped in a simple scroll pane
            JScrollPane outerScrollPane = new JScrollPane(mainPanel);
            outerScrollPane.setBorder(null);
            outerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            outerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); // No outer scrollbar

            return outerScrollPane;
        }

        return new JScrollPane(logList);
    }

    private static void setupSearchFunctionality() {
        JTextField searchField = filterPanel.getSearchField();

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { scheduleFilterUpdate(); }
            @Override
            public void removeUpdate(DocumentEvent e) { scheduleFilterUpdate(); }
            @Override
            public void changedUpdate(DocumentEvent e) { scheduleFilterUpdate(); }

            private void scheduleFilterUpdate() {
                // Cancel any existing timer
                if (debounceTimer != null) {
                    debounceTimer.stop();
                }

                // Start new debounce timer
                debounceTimer = new Timer(FILTER_DEBOUNCE_MS, e -> {
                    String newSearchText = searchField.getText().trim();
                    currentSearchText.set(newSearchText);

                    if (newSearchText.isEmpty()) {
                        searchPattern.set(null);
                    } else {
                        try {
                            searchPattern.set(Pattern.compile(
                                    Pattern.quote(newSearchText),
                                    Pattern.CASE_INSENSITIVE
                            ));
                        } catch (Exception ex) {
                            searchPattern.set(null);
                        }
                    }

                    applyFiltersAsync();
                });
                debounceTimer.setRepeats(false);
                debounceTimer.start();
            }
        });
    }

    private static void setupLevelFiltering() {
        JComboBox<Level> levelCombo = filterPanel.getLevelCombo();

        levelCombo.addActionListener(e -> {
            Level selectedLevel = (Level) levelCombo.getSelectedItem();
            if (selectedLevel != null) {
                currentMinLevel.set(selectedLevel);
                applyFiltersAsync();
            }
        });
    }

    private static void setupActionButtons() {
        filterPanel.getClearButton().addActionListener(e -> clearLog());
        filterPanel.getExportButton().addActionListener(e -> exportLogs());
    }

    /**
     * Apply filters asynchronously to avoid blocking the UI
     */
    private static void applyFiltersAsync() {
        // Prevent multiple concurrent filtering operations
        if (!filteringInProgress.compareAndSet(false, true)) {
            return;
        }

        // Capture current filter state
        final Pattern currentPattern = searchPattern.get();
        final Level minLevel = currentMinLevel.get();
        final boolean shouldScroll = isScrolledToBottom();

        // Submit filtering task to background thread
        filterExecutor.submit(() -> {
            try {
                // Create filtered list in background
                List<LogEntry> filteredEntries = new ArrayList<>();

                for (LogEntry entry : allEntries) {
                    // Fixed: Level filtering - show selected level and higher priority levels
                    // Lower intLevel values = higher priority (FATAL=100, ERROR=200, etc.)
                    if (entry.getLevel().intLevel() > minLevel.intLevel()) {
                        continue;
                    }

                    // Search filtering
                    if (currentPattern != null) {
                        if (!currentPattern.matcher(entry.getFormattedMessage()).find() &&
                                !currentPattern.matcher(entry.getRawMessage()).find() &&
                                !currentPattern.matcher(entry.getLoggerName()).find()) {
                            continue;
                        }
                    }

                    filteredEntries.add(entry);
                }

                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    try {
                        // Update the model
                        filteredModel.clear();
                        for (LogEntry entry : filteredEntries) {
                            filteredModel.addElement(entry);
                        }

                        // Update status
                        updateStatus();

                        // Auto-scroll if needed
                        if (shouldScroll && !filteredEntries.isEmpty()) {
                            logList.ensureIndexIsVisible(filteredModel.size() - 1);
                        }

                    } finally {
                        filteringInProgress.set(false);
                    }
                });

            } catch (Exception e) {
                // Reset the flag on any error
                filteringInProgress.set(false);
            }
        });
    }

    private static void updateStatus() {
        int totalCount = allEntries.size();
        int filteredCount = filteredModel.size();

        String status;
        if (totalCount == filteredCount) {
            status = String.format("%,d entries", totalCount);
        } else {
            status = String.format("%,d of %,d entries", filteredCount, totalCount);
        }

        filterPanel.getStatusLabel().setText(status);
    }

    private static boolean isScrolledToBottom() {
        if (logList == null) return true;

        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, logList);
        if (scrollPane == null) return true;

        BoundedRangeModel model = scrollPane.getVerticalScrollBar().getModel();
        return (model.getValue() + model.getExtent()) >= (model.getMaximum() - 10);
    }

    private static void exportLogs() {
        // TODO: Implement log export functionality
        JOptionPane.showMessageDialog(logList,
                "Export functionality coming soon!",
                "Export Logs",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Legacy methods for compatibility
    public static void setLevelCombo(JComboBox<Level> combo) {
        if (filterPanel != null) {
            filterPanel.getLevelCombo().setSelectedItem(combo.getSelectedItem());
        }
    }

    public static JComboBox<Level> getLevelCombo() {
        return filterPanel != null ? filterPanel.getLevelCombo() : null;
    }

    public static void setVisibleLevels(Set<Level> levels) {
        // Convert set to minimum level for compatibility
        Level minLevel = Level.INFO;
        for (Level level : levels) {
            if (level.intLevel() < minLevel.intLevel()) {
                minLevel = level;
            }
        }
        currentMinLevel.set(minLevel);
        if (filterPanel != null) {
            filterPanel.getLevelCombo().setSelectedItem(minLevel);
        }
        applyFiltersAsync();
    }

    public static void clearLog() {
        if (allEntries != null && filteredModel != null) {
            SwingUtilities.invokeLater(() -> {
                allEntries.clear();
                filteredModel.clear();
                updateStatus();
            });
        }
    }

    // Appender implementation
    protected VirtualLogListAppender(String name, Filter filter,
                                     Layout<? extends Serializable> layout,
                                     boolean ignoreExceptions,
                                     Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @PluginFactory
    public static VirtualLogListAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) boolean ignoreExceptions) {

        if (name == null) {
            LOGGER.error("No name provided for VirtualLogListAppender");
            return null;
        }

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new VirtualLogListAppender(name, filter, layout, ignoreExceptions, null);
    }

    @Override
    public void start() {
        super.start();
        batchTimer = new Timer((int) MAX_BATCH_WAIT_MS, e -> flushLogEvents());
        batchTimer.start();
    }

    @Override
    public void stop() {
        if (batchTimer != null) {
            batchTimer.stop();
        }
        if (debounceTimer != null) {
            debounceTimer.stop();
        }
        flushLogEvents();
        filterExecutor.shutdown();
        super.stop();
    }

    @Override
    public void append(LogEvent event) {
        if (filterPanel == null) {
            return;
        }

        synchronized (eventQueue) {
            eventQueue.add(event.toImmutable());
            if (eventQueue.size() >= MAX_BATCH_SIZE) {
                SwingUtilities.invokeLater(this::flushLogEvents);
            }
        }
    }

    private void flushLogEvents() {
        final List<LogEvent> eventsToProcess;
        synchronized (eventQueue) {
            if (eventQueue.isEmpty()) {
                return;
            }
            eventsToProcess = new ArrayList<>(eventQueue);
            eventQueue.clear();
        }

        if (logList == null) {
            return;
        }

        // Process events on background thread to avoid blocking UI
        filterExecutor.submit(() -> {
            List<LogEntry> newEntries = new ArrayList<>();

            for (LogEvent event : eventsToProcess) {
                final byte[] bytes = getLayout().toByteArray(event);
                final String formattedMessage = new String(bytes).trim();

                LogEntry entry = new LogEntry(
                        formattedMessage,
                        event.getMessage().getFormattedMessage(),
                        event.getLevel(),
                        event.getTimeMillis(),
                        event.getLoggerName()
                );

                newEntries.add(entry);
            }

            // Add to all entries (thread-safe)
            allEntries.addAll(newEntries);

            // Trim if needed
            if (allEntries.size() > MAX_LOG_ENTRIES) {
                int removeCount = Math.min(MAX_BATCH_SIZE, allEntries.size() / 10);
                for (int i = 0; i < removeCount; i++) {
                    if (!allEntries.isEmpty()) {
                        allEntries.remove(0);
                    }
                }
            }

            // Update filters asynchronously
            applyFiltersAsync();
        });
    }
}