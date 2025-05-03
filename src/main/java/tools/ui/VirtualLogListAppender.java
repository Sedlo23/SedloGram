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
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A highly optimized Log4j2 Appender that displays log messages in a JList.
 * <p>
 * This appender uses virtualized rendering for maximum performance, allowing
 * it to handle millions of log entries with minimal UI impact.
 *
 * Usage:
 * <pre>{@code
 *   // In your log4j2.xml configuration, refer to this appender by its name "AsyncTextArea"
 * }</pre>
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
        private final Level level;
        private final long timestamp;

        public LogEntry(String formattedMessage, Level level, long timestamp) {
            this.formattedMessage = formattedMessage;
            this.level = level;
            this.timestamp = timestamp;
        }

        public String getFormattedMessage() {
            return formattedMessage;
        }

        public Level getLevel() {
            return level;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Custom renderer to display and color log entries based on their level
     */
    private static class LogCellRenderer extends JPanel implements ListCellRenderer<LogEntry> {
        private final JLabel label;

        public LogCellRenderer() {
            setLayout(new BorderLayout());
            label = new JLabel();
            label.setFont(new Font("Monospaced", Font.PLAIN, 12));

            label.setOpaque(true);
            add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends LogEntry> list, LogEntry entry,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            label.setText(entry.getFormattedMessage());

            // Set colors based on log level and UI background
            Color background = list.getBackground();
            boolean isDarkBackground = isDarkColor(background);

            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(background);

                // Set text color based on log level
                switch (entry.getLevel().getStandardLevel().toString()) {
                    case "FATAL":
                        label.setForeground(isDarkBackground ? new Color(255, 102, 102) : new Color(128, 0, 0));
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                        break;
                    case "ERROR":
                        label.setForeground(isDarkBackground ? new Color(255, 153, 153) : Color.RED);
                        break;
                    case "WARN":
                        label.setForeground(isDarkBackground ? new Color(255, 204, 102) : Color.ORANGE);
                        break;
                    case "INFO":
                        label.setForeground(isDarkBackground ? new Color(153, 204, 255) : Color.BLUE);
                        break;
                    case "DEBUG":
                        label.setForeground(isDarkBackground ? new Color(102, 255, 102) : new Color(0, 128, 0));
                        break;
                    case "TRACE":
                        label.setForeground(isDarkBackground ? new Color(192, 192, 192) : Color.GRAY);
                        break;
                    default:
                        label.setForeground(isDarkBackground ? Color.WHITE : Color.BLACK);
                        break;
                }
            }

            return this;
        }

        /**
         * Determines if a color is dark based on its brightness.
         */
        private static boolean isDarkColor(Color color) {
            double brightness = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
            return brightness < 128;
        }
    }

    /**
     * The list model containing log entries
     */
    private static DefaultListModel<LogEntry> listModel;

    /**
     * The JList displaying log entries
     */
    private static JList<LogEntry> logList;

    /**
     * A combo box for selecting minimum log level
     */
    private static JComboBox<Level> levelCombo;

    /**
     * Maximum number of log entries to keep
     */
    private static final int MAX_LOG_ENTRIES = 100000;

    /**
     * Batch size for processing log events
     */
    private static final int MAX_BATCH_SIZE = 100;

    /**
     * Batch timer interval in milliseconds
     */
    private static final long MAX_BATCH_WAIT_MS = 150;

    /**
     * Set of visible log levels
     */
    private static final Set<Level> visibleLevels = new CopyOnWriteArraySet<>();

    /**
     * Queue for batching log events
     */
    private final List<LogEvent> eventQueue = new ArrayList<>();

    /**
     * Timer for batch processing
     */
    private Timer batchTimer;

    static {
        // Initialize default visible levels
        visibleLevels.add(Level.OFF);
        visibleLevels.add(Level.FATAL);
        visibleLevels.add(Level.ERROR);
        visibleLevels.add(Level.WARN);
        visibleLevels.add(Level.INFO);
        visibleLevels.add(Level.DEBUG);
        visibleLevels.add(Level.TRACE);
        visibleLevels.add(Level.ALL);

        // Create list model
        listModel = new DefaultListModel<>();
    }

    /**
     * Creates and configures the log list component
     * @return A JScrollPane containing the configured log list
     */
    public static JScrollPane createLogListComponent() {
        if (logList == null) {
            logList = new JList<>(listModel);
            logList.setCellRenderer(new LogCellRenderer());
            logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Performance optimizations
            logList.setFixedCellHeight(20); // Speeds up rendering
            logList.setVisibleRowCount(25);
            logList.setPrototypeCellValue(new LogEntry("X".repeat(100), Level.INFO, System.currentTimeMillis()));
        }

        return new JScrollPane(logList);
    }

    /**
     * Sets the level combo box used for filtering log levels
     */
    public static void setLevelCombo(JComboBox<Level> combo) {
        levelCombo = combo;
    }

    /**
     * Gets the level combo box
     */
    public static JComboBox<Level> getLevelCombo() {
        return levelCombo;
    }

    /**
     * Updates visible log levels
     */
    public static void setVisibleLevels(Set<Level> levels) {
        synchronized (visibleLevels) {
            visibleLevels.clear();
            visibleLevels.addAll(levels);
        }
    }

    /**
     * Constructs a new VirtualLogListAppender
     */
    protected VirtualLogListAppender(String name, Filter filter,
                                     Layout<? extends Serializable> layout,
                                     boolean ignoreExceptions,
                                     Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    /**
     * Factory method for creating a new appender instance
     */
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
        flushLogEvents();
        super.stop();
    }

    @Override
    public void append(LogEvent event) {
        // Skip if basic requirements aren't met
        if (levelCombo == null || listModel == null) {
            return;
        }

        // Check log level filtering
        if (!event.getLevel().isInRange(Level.OFF, (Level) levelCombo.getSelectedItem())) {
            return;
        }

        // Queue event for batch processing
        synchronized (eventQueue) {
            eventQueue.add(event.toImmutable());
            if (eventQueue.size() >= MAX_BATCH_SIZE) {
                SwingUtilities.invokeLater(this::flushLogEvents);
            }
        }
    }

    /**
     * Process all queued log events as a batch
     */
    private void flushLogEvents() {
        final List<LogEvent> eventsToProcess;
        synchronized (eventQueue) {
            if (eventQueue.isEmpty()) {
                return;
            }
            eventsToProcess = new ArrayList<>(eventQueue);
            eventQueue.clear();
        }

        if (listModel == null || logList == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            boolean shouldScroll = isScrolledToBottom();

            // Process all events in the batch
            for (LogEvent event : eventsToProcess) {
                // Format the log event using the configured layout
                final byte[] bytes = getLayout().toByteArray(event);
                final String message = new String(bytes);

                    LogEntry entry = new LogEntry(message, event.getLevel(), event.getTimeMillis());

                    // Trim the model if it exceeds maximum size
                    if (listModel.size() >= MAX_LOG_ENTRIES) {
                        // Remove in chunks to improve performance
                        listModel.removeRange(0, Math.min(MAX_BATCH_SIZE, listModel.size() / 10));
                    }

                    listModel.addElement(entry);





            }

            // Auto-scroll only if already at bottom
            if (shouldScroll) {
                int lastIndex = listModel.size() - 1;
                if (lastIndex >= 0) {
                    logList.ensureIndexIsVisible(lastIndex);
                }
            }
        });
    }

    /**
     * Determines if the list is scrolled to the bottom
     */
    private boolean isScrolledToBottom() {
        if (logList == null) return true;

        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, logList);
        if (scrollPane == null) return true;

        BoundedRangeModel model = scrollPane.getVerticalScrollBar().getModel();
        return (model.getValue() + model.getExtent()) >= (model.getMaximum() - 10);
    }

    /**
     * Clears all log entries from the display
     */
    public static void clearLog() {
        if (listModel != null) {
            SwingUtilities.invokeLater(listModel::clear);
        }
    }
}