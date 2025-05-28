package UI.SZIFEditor;

import Encoding.TelegramEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.crypto.ArithmeticalFunctions;
import tools.string.StringHelper;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main controller for application logic with repository change notifications
 */
public class EditorController {
    private static final Logger LOG = LogManager.getLogger(EditorController.class);

    private PacketRepository packetRepository;
    private TelegramGenerator telegramGenerator;
    private JTextArea consoleArea;

    private List<Runnable> repositoryChangeListeners = new ArrayList<>();
    private List<TableDataLoadListener> tableDataLoadListeners = new ArrayList<>();

    public EditorController(JTextArea consoleArea) {
        this.consoleArea = consoleArea;
        this.packetRepository = new PacketRepository();
        this.telegramGenerator = new TelegramGenerator();

        LOG.info("EditorController inicializován");

        // Initialize with some default templates
        initializeDefaultTemplates();
    }

    /**
     * Add a listener to be notified when packet repository changes
     */
    public void addRepositoryChangeListener(Runnable listener) {
        repositoryChangeListeners.add(listener);
        LOG.debug("Přidán listener pro změny v repository - celkem: " + repositoryChangeListeners.size());
    }

    /**
     * Notify all listeners that the repository has changed
     */
    public void notifyRepositoryChanged() {
        LOG.debug("Notifikace změny repository - počet listenerů: " + repositoryChangeListeners.size());
        for (Runnable listener : repositoryChangeListeners) {
            listener.run();
        }
    }

    /**
     * Interface for components that need table data load notifications
     */
    public interface TableDataLoadListener {
        void onTableDataLoaded(List<TableRowData> rows, boolean[] columnVisibility);
    }

    /**
     * Add a table data load listener
     */
    public void addTableDataLoadListener(TableDataLoadListener listener) {
        tableDataLoadListeners.add(listener);
        LOG.debug("Přidán TableDataLoadListener - celkem: " + tableDataLoadListeners.size());
    }

    /**
     * Initialize default templates
     */
    private void initializeDefaultTemplates() {
        LOG.info("Inicializace výchozích šablon");

        // Create some standard packets
        PacketTemplate standardPacketTemplate = new PacketTemplate("TestPacket", false);
        packetRepository.add(standardPacketTemplate);
        LOG.debug("Vytvořena standardní šablona - [TestPacket]");

        // Create a template packet for common values
        PacketTemplate commonValuesTemplate = new PacketTemplate("Vzorové proměné", true);
        packetRepository.add(commonValuesTemplate);
        LOG.debug("Vytvořena šablona vzorových proměnných - [Vzorové proměné]");

        LOG.info("Dokončena inicializace výchozích šablon - celkem: " + (packetRepository.getPackets().size() + packetRepository.getTemplatePackets().size()));
    }

    /**
     * Get all regular packet templates
     */
    public List<PacketTemplate> getPackets() {
        return packetRepository.getPackets();
    }

    /**
     * Get all template packets
     */
    public List<PacketTemplate> getTemplatePackets() {
        return packetRepository.getTemplatePackets();
    }

    /**
     * Save a packet template to the repository
     */
    public void savePacket(PacketTemplate packetTemplate) {
        packetRepository.add(packetTemplate);
        LOG.info("Uložen packet: [" + packetTemplate.getName() + "]");
        notifyRepositoryChanged();
    }

    /**
     * Delete a packet template from the repository
     */
    public void deletePacket(PacketTemplate packetTemplate) {
        packetRepository.remove(packetTemplate);
        LOG.info("Smazán packet: [" + packetTemplate.getName() + "]");
        notifyRepositoryChanged();
    }

    /**
     * Generate telegrams from table row data
     */
    public List<TelegramData> generateTelegrams(List<TableRowData> rowData) {
        LOG.info("Generování telegramů - počet řádků: " + rowData.size());
        List<TelegramData> telegrams = new ArrayList<>();

        // Process each row, filling in empty values
        for (int i = 0; i < rowData.size(); i++) {
            TableRowData row = rowData.get(i);
            LOG.debug("Zpracování řádku " + i);

            // Fill in empty values from previous rows
            for (int col = 0; col < 8; col++) { // Only for columns 0-7
                if (row.getValue(col).isEmpty() && i > 0) {
                    // Search for a non-empty value in previous rows
                    for (int prevRow = i - 1; prevRow >= 0; prevRow--) {
                        String value = rowData.get(prevRow).getValue(col);
                        if (!value.isEmpty() && isNumeric(value)) {
                            row.setValue(col, value);
                            LOG.trace("Doplněna hodnota pro sloupec " + col + " z řádku " + prevRow + ": [" + value + "]");
                            break;
                        }
                    }
                }
            }

            // Now generate the telegram with filled-in values
            try {
                String binaryData = telegramGenerator.generateBinary(row, packetRepository);
                LOG.debug("Vygenerována binární data o délce " + binaryData.length());

                String encodedData = telegramGenerator.encodeTelegram(binaryData);
                LOG.debug("Zakódovaná data o délce " + encodedData.length());

                String telegramName =
                        StringHelper.padLeft(row.getValue(5),3, '0') + "_" +
                                StringHelper.padLeft(row.getValue(6),5, '0') + "_" +
                                StringHelper.padLeft(row.getValue(1),1, '0') + "_" +
                                StringHelper.padLeft(row.getValue(4),3, '0');
                if (!row.getValue(17).isEmpty()) {
                    telegramName += "_" + row.getValue(17);
                }

                telegramName += " ("+StringHelper.padLeft(String.valueOf(Math.abs(encodedData.hashCode())),8, '0').substring(0,7)+")";

                telegrams.add(new TelegramData(encodedData, telegramName));
                LOG.info("Vygenerován telegram: [" + telegramName + "]");
            } catch (Exception e) {
                LOG.error("Chyba při generování telegramu pro řádek " + i + ": " + e.getMessage(), e);
            }
        }

        LOG.info("Dokončeno generování telegramů - celkem: " + telegrams.size());
        return telegrams;
    }

    /**
     * Helper method to check if a string is numeric
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Save a telegram to a file
     */
    public void saveTelegramToFile(TelegramData telegram, File file) {
        LOG.info("Ukládání telegramu do souboru: [" + file.getName() + "]");
        try {



            byte[] binaryData = BinaryConverter.hexStringToByteArray(
                    TelegramEncoder.encode(
                            ArithmeticalFunctions.hex2Bin(
                            telegram.getEncodedData())));

            LOG.debug("Převedena hex data na binární, velikost: " + binaryData.length + " bajtů");

            try (FileOutputStream fos = new FileOutputStream(file))
            {
                fos.write(binaryData);
            }

            LOG.info("Úspěšně uložen telegram do souboru: [" + file.getAbsolutePath() + "]");

        } catch (IOException e) {

            LOG.error("Chyba při ukládání telegramu: " + e.getMessage(), e);

        }
    }

    /**
     * Open and load data from a file
     */
    public void openFile() {
        LOG.info("Spuštěno otevírání souboru");
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

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            LOG.info("Vybrán soubor: [" + selectedFile.getAbsolutePath() + "]");
            loadDataFromFile(selectedFile);
        } else {
            LOG.info("Otevírání souboru zrušeno");
        }
    }

    /**
     * Save table data to a file
     */
    public void saveFile() {
        LOG.info("Požadavek na uložení souboru - přesměrováno na UI");
    }

    /**
     * Export telegram functionality
     */
    public void exportTelegram() {
        LOG.info("Požadavek na export telegramu - funkce není implementována");
    }

    /**
     * Load all data from a file
     */
    public void loadDataFromFile(File file) {
        LOG.info("Načítání dat ze souboru: [" + file.getAbsolutePath() + "]");
        try {
            // Lists to hold loaded data
            Map<Integer, TableRowData> loadedRows = new TreeMap<>(); // TreeMap to preserve row order by index
            List<PacketTemplate> loadedPacketTemplates = new ArrayList<>();
            boolean[] columnVisibility = null;


            // Read the file line by line
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;

                // Section trackers
                boolean inEditorTableSection = false;
                boolean inPacketTemplatesSection = false;
                boolean inTemplateSection = false;
                boolean inVariablesSection = false;

                // Current packet template being processed
                PacketTemplate currentTemplate = null;
                String templateName = null;
                boolean isTemplateFlag = false;
                List<PacketVariable> currentVariables = new ArrayList<>();

                // Read the header line to verify file format
                line = reader.readLine();
                if (line == null || !line.startsWith("SZIF_TABLE_DATA_v1.0")) {
                    LOG.error("Neplatný formát souboru nebo verze: [" + line + "]");
                    throw new IOException("Invalid file format or version");
                }

                LOG.info("Formát souboru ověřen, zpracování dat...");

                // Process each line
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("COLUMNS:")) {
                        // Process column visibility
                        String[] visibility = line.substring(8).split(",");
                        columnVisibility = new boolean[visibility.length];
                        for (int i = 0; i < visibility.length; i++) {
                            columnVisibility[i] = visibility[i].equals("1");
                        }
                        LOG.debug("Načteno nastavení viditelnosti sloupců: " + Arrays.toString(columnVisibility));
                    }
                    else if (line.equals("EDITOR_TABLE_START")) {
                        inEditorTableSection = true;
                        inPacketTemplatesSection = false;
                        inTemplateSection = false;
                        inVariablesSection = false;
                        LOG.debug("Začíná sekce tabulky editoru");
                    }
                    else if (line.equals("EDITOR_TABLE_END")) {
                        inEditorTableSection = false;
                        LOG.debug("Končí sekce tabulky editoru - načteno " + loadedRows.size() + " řádků");
                    }
                    else if (line.equals("PACKET_TEMPLATES_START")) {
                        inEditorTableSection = false;
                        inPacketTemplatesSection = true;
                        inTemplateSection = false;
                        inVariablesSection = false;
                        LOG.debug("Začíná sekce šablon packetů");
                    }
                    else if (line.equals("PACKET_TEMPLATES_END")) {
                        inPacketTemplatesSection = false;
                        LOG.debug("Končí sekce šablon packetů - načteno " + loadedPacketTemplates.size() + " šablon");
                    }
                    else if (line.equals("TEMPLATE_START") && inPacketTemplatesSection) {
                        inTemplateSection = true;
                        templateName = null;
                        isTemplateFlag = false;
                        currentVariables = new ArrayList<>();
                        LOG.trace("Začíná definice šablony");
                    }
                    else if (line.equals("TEMPLATE_END") && inPacketTemplatesSection) {
                        // Finalize the current template
                        if (templateName != null) {
                            currentTemplate = new PacketTemplate(templateName, isTemplateFlag);
                            // Add all collected variables
                            for (PacketVariable var : currentVariables) {
                                currentTemplate.addVariable(var);
                            }
                            loadedPacketTemplates.add(currentTemplate);
                            LOG.debug("Přidána šablona: [" + templateName + "] s " + currentVariables.size() + " proměnnými");
                        }
                        inTemplateSection = false;
                    }
                    else if (line.equals("VARIABLES_START") && inTemplateSection) {
                        inVariablesSection = true;
                        LOG.trace("Začíná sekce proměnných");
                    }
                    else if (line.equals("VARIABLES_END") && inTemplateSection) {
                        inVariablesSection = false;
                        LOG.trace("Končí sekce proměnných");
                    }
                    else if (inEditorTableSection) {
                        // Process a data row
                        int rowIndex;
                        String rowData;

                        // Split row index from data
                        int colonPos = line.indexOf(':');
                        if (colonPos > 0) {
                            try {
                                rowIndex = Integer.parseInt(line.substring(0, colonPos));
                                rowData = line.substring(colonPos + 1);

                                // Process the row data
                                TableRowData tableRowData = new TableRowData();

                                // Split by | but respect escaped ones
                                String[] values = splitEscapedString(rowData, '|');

                                for (int i = 0; i < Math.min(values.length, EditorTable.COLUMN_NAMES.length); i++) {
                                    // Unescape the value
                                    String value = unescapeValue(values[i]);
                                    tableRowData.setValue(i, value);
                                }

                                // Store with its original row index
                                loadedRows.put(rowIndex, tableRowData);
                                LOG.trace("Načten řádek dat pro index: " + rowIndex);
                            } catch (NumberFormatException e) {
                                LOG.warn("Neplatný index řádku v řádku: [" + line + "]");
                            }
                        }
                    }
                    else if (inTemplateSection && !inVariablesSection) {
                        // Process template properties
                        if (line.startsWith("NAME:")) {
                            templateName = unescapeValue(line.substring(5));
                            LOG.trace("Jméno šablony: [" + templateName + "]");
                        } else if (line.startsWith("IS_TEMPLATE:")) {
                            isTemplateFlag = line.substring(12).equals("1");
                            LOG.trace("Je šablona: " + isTemplateFlag);
                        }
                    }
                    else if (inVariablesSection) {
                        // Process a variable definition
                        String[] parts = splitEscapedString(line, '|');
                        if (parts.length >= 4) {
                            try {
                                String varName = unescapeValue(parts[0]);
                                int size = Integer.parseInt(parts[1]);
                                String value = unescapeValue(parts[2]);
                                boolean forceShow = parts[3].equals("1");

                                // Add to current variables list
                                currentVariables.add(new PacketVariable(varName, size, value, forceShow));
                                LOG.trace("Přidána proměnná: [" + varName + "] velikost=" + size + " hodnota=[" + value + "] forceShow=" + forceShow);
                            } catch (NumberFormatException e) {
                                LOG.warn("Neplatná data proměnné: [" + line + "]");
                            }
                        }
                    }
                }
            }

            // Clear existing packet templates
            LOG.info("Mazání existujících šablon pro přidání nových");
            for (PacketTemplate template : new ArrayList<>(packetRepository.getPackets())) {
                packetRepository.remove(template);
            }
            for (PacketTemplate template : new ArrayList<>(packetRepository.getTemplatePackets())) {
                packetRepository.remove(template);
            }

            // Save loaded packet templates
            LOG.info("Ukládání " + loadedPacketTemplates.size() + " načtených šablon");
            for (PacketTemplate template : loadedPacketTemplates) {
                packetRepository.add(template);
            }

            // Notify UI to update repository data
            LOG.debug("Notifikace změny repository");
            notifyRepositoryChanged();

            // Send loaded table data to any listeners
            if (!loadedRows.isEmpty()) {
                // Create a list with enough space for all rows
                int maxRow = Collections.max(loadedRows.keySet());
                List<TableRowData> orderedRows = new ArrayList<>(maxRow + 1);

                // Initialize with empty rows
                for (int i = 0; i <= maxRow; i++) {
                    orderedRows.add(new TableRowData());
                }

                // Set the loaded rows at their correct indices
                for (Map.Entry<Integer, TableRowData> entry : loadedRows.entrySet()) {
                    orderedRows.set(entry.getKey(), entry.getValue());
                }

                LOG.info("Notifikace " + tableDataLoadListeners.size() + " listenerů s " + orderedRows.size() + " řádky dat");

                // Notify listeners about the loaded data
                for (TableDataLoadListener listener : tableDataLoadListeners) {
                    listener.onTableDataLoaded(orderedRows, columnVisibility);
                }

                LOG.info("Načteno " + loadedRows.size() + " řádků dat ze souboru [" + file.getName() + "]");
            } else {
                LOG.info("V souboru nebyla nalezena žádná tabulková data [" + file.getName() + "]");
            }

            if (!loadedPacketTemplates.isEmpty()) {
                LOG.info("Načteno " + loadedPacketTemplates.size() + " šablon packetů ze souboru [" + file.getName() + "]");
            }

        } catch (Exception ex) {
            LOG.error("Chyba při načítání souboru: " + ex.getMessage(), ex);
            ex.printStackTrace();
        }
    }

    /**
     * Helper function to split a string respecting escaped delimiters
     */
    private String[] splitEscapedString(String input, char delimiter) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;

        for (char c : input.toCharArray()) {
            if (escaped) {
                current.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == delimiter) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    /**
     * Unescapes special characters in values
     */
    private String unescapeValue(String value) {
        if (value == null) return "";
        return value.replace("\\:", ":")
                .replace("\\n", "\n")
                .replace("\\|", "|")
                .replace("\\\\", "\\");
    }

    // Add to EditorController class
    private final ExecutorService backgroundExecutor =
            Executors.newFixedThreadPool(
                    Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
                    r -> {
                        Thread t = new Thread(r, "SZIF-Background-Worker");
                        t.setDaemon(true);
                        return t;
                    }
            );

    // Add this method to properly shutdown threads when application closes
    public void shutdown() {
        backgroundExecutor.shutdown();
        try {
            if (!backgroundExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                backgroundExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            backgroundExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}