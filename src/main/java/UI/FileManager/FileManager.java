package UI.FileManager;

/*
The MIT License
...
*/

import Encoding.TelegramDecoder;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import packets.Interfaces.IPacket;
import tools.ui.TlgTemp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.TrackToTrain.PH;
import tools.crypto.CalculatorMD4;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.List;

import static tools.crypto.ArithmeticalFunctions.bin2Hex;
import static tools.crypto.ArithmeticalFunctions.dec2XBin;
import static tools.string.StringHelper.*;

/**
 * {@code FileManager} poskytuje moderní GUI-based prohlížeč souborů v systému,
 * se speciálním zpracováním pro soubory ".tlg" (dekódování a přidání do seznamu).
 * <p>
 * Klíčové vlastnosti:
 * <ul>
 *   <li>Moderní FlatLaf design s system colors a typography</li>
 *   <li>Zobrazuje strom souborů a složek s moderním stromovým rendererem</li>
 *   <li>Zobrazuje detaily o vybraném souboru s moderním layoutem</li>
 *   <li>Moderní toolbar s hover efekty a ikonami</li>
 *   <li>Integrace s JList pro TlgTemp objekty</li>
 * </ul>
 */
public class FileManager {

    //////////////////////////////////////////////////////////////////////////////
    //                             FIELDS & CONSTANTS
    //////////////////////////////////////////////////////////////////////////////

    private static final Logger LOG = LogManager.getLogger(FileManager.class);

    /** Integrace s desktopem pro otevírání, editaci nebo tisk souborů. */
    private Desktop desktop;

    /** Poskytovatel systémových ikon a názvů souborů. */
    private FileSystemView fileSystemView;

    /** Reference na aktuálně vybraný {@link File} v prohlížeči. */
    private File currentFile;

    /** Kořenový panel pro hlavní GUI, vytvořený lazy ve {@link #getGui}. */
    private JPanel gui;

    /** {@link JTree} pro zobrazení struktury souborového systému. */
    private JTree tree;
    private DefaultTreeModel treeModel;

    /** Progress bar používaný během vyhledávání (expanze) adresářů. */
    private JProgressBar progressBar;

    /** Komponenty pro zobrazení detailů o souboru. */
    private JButton openFile;
    private JButton deleteFile;
    private JButton printFile;
    private JLabel fileName;
    private JTextField path;
    private JLabel date; // Zobrazuje MD4 pro .tlg soubory
    private JLabel size;
    private JCheckBox readable;
    private JCheckBox writable;
    private JCheckBox executable;
    private JRadioButton isDirectory;
    private JRadioButton isFile;

    /** Panel a komponenty použité pro vytváření nových souborů / složek. */
    private JPanel newFilePanel;
    private JRadioButton newTypeFile;
    private JTextField name;

    /** {@link JList} s {@link TlgTemp} objekty, kam ukládáme dekódovaná .tlg data. */
    private JList<TlgTemp> externalTlgList;

    /** Pozadí - watcher pro sledování změn v aktuálně vybrané složce. */
    private DirectoryWatcher directoryWatcher;

    //////////////////////////////////////////////////////////////////////////////
    //                             MAIN GUI CREATION
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Vytvoří (pokud již není vytvořeno) a vrátí hlavní GUI panel pro tohoto správce souborů
     * s moderním FlatLaf designem.
     *
     * @param jList {@link JList} s {@link TlgTemp} objekty, do kterého
     *              se budou přidávat dekódovaná .tlg data.
     * @return hlavní {@link Container}, který reprezentuje GUI správce souborů
     */
    public Container getGui(JList<TlgTemp> jList) {
        LOG.info("Požadavek na získání GUI FileManageru.");

        if (gui == null) {
            LOG.debug("GUI zatím neexistuje. Inicializuji komponenty FileManageru.");

            // Uložíme referenci na externí seznam
            this.externalTlgList = jList;

            // Inicializace obecných polí
            gui = new JPanel(new BorderLayout(8, 8));
            gui.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            gui.setBackground(UIManager.getColor("Panel.background"));

            fileSystemView = FileSystemView.getFileSystemView();
            desktop = Desktop.getDesktop();

            // Vytvoření panelu s detailem souboru
            JPanel detailView = new JPanel(new BorderLayout(8, 8));
            detailView.setBackground(UIManager.getColor("Panel.background"));

            // Vytvoření kořenového uzlu pro strom
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            treeModel = new DefaultTreeModel(root);

            // Vytvoření stromu souborového systému
            buildFileSystemTree(root);
            JScrollPane treeScroll = new JScrollPane(tree);
            styleModernScrollPane(treeScroll);

            // Nastavení velikosti panelu se stromem
            tree.setVisibleRowCount(15);
            Dimension preferredSize = treeScroll.getPreferredSize();
            Dimension widePreferred = new Dimension(250, (int) preferredSize.getHeight());
            treeScroll.setPreferredSize(widePreferred);

            // Postavení spodního panelu s detailními informacemi
            JPanel fileView = buildFileDetailsPanel();
            detailView.add(fileView, BorderLayout.CENTER);

            // Přidáme strom + detailní panel do splitu
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeScroll, detailView);
            splitPane.setResizeWeight(0.6);
            splitPane.setBorder(null);
            splitPane.setOpaque(false);
            splitPane.setBackground(UIManager.getColor("Panel.background"));

            gui.add(splitPane, BorderLayout.CENTER);

            // Přidáme progress bar do spodní části
            JPanel bottomPanel = createModernBottomPanel();
            gui.add(bottomPanel, BorderLayout.SOUTH);

            LOG.debug("FileManager GUI úspěšně inicializováno.");
        }
        return gui;
    }

    /**
     * Vytvoří moderní spodní panel s progress barem.
     */
    private JPanel createModernBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setBackground(UIManager.getColor("Panel.background"));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        progressBar = createModernProgressBar();
        progressBar.setVisible(false);

        bottomPanel.add(progressBar, BorderLayout.CENTER);
        return bottomPanel;
    }

    /**
     * Vytvoří moderní progress bar.
     */
    private JProgressBar createModernProgressBar() {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setFont(UIManager.getFont("ProgressBar.font"));
        if (progressBar.getFont() == null) {
            progressBar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        }
        progressBar.setBorder(createModernBorder());
        progressBar.setForeground(UIManager.getColor("ProgressBar.foreground"));
        progressBar.setBackground(UIManager.getColor("ProgressBar.background"));
        progressBar.setStringPainted(true);
        progressBar.setString("Načítání...");
        return progressBar;
    }

    /**
     * Aplikuje moderní styling na scroll pane.
     */
    private void styleModernScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(createModernBorder());
        scrollPane.setBackground(UIManager.getColor("ScrollPane.background"));
        scrollPane.getViewport().setBackground(UIManager.getColor("Tree.background"));
        scrollPane.setOpaque(true);
    }

    /**
     * Vytvoří strom souborového systému s moderním stylingem.
     */
    private void buildFileSystemTree(DefaultMutableTreeNode root) {
        LOG.debug("Vytvářím strom souborového systému.");

        // Listener pro výběr v tree
        TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent tse) {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) tse.getPath().getLastPathComponent();

                // Načteme poduzly, pokud je to potřeba
                showChildren(node);

                // Aktualizace detailního zobrazení
                setFileDetails((File) node.getUserObject());

                // Ukončíme případné staré watchery a spustíme nový
                stopWatchingCurrentFolder();
                watchCurrentFolder();
            }
        };

        // Kořeny systému
        File[] roots = fileSystemView.getRoots();
        for (File fileSystemRoot : roots) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
            root.add(node);

            // Do stromu přidáváme jen složky a soubory .tlg/.pdf
            File[] files = fileSystemView.getFiles(fileSystemRoot, true);
            for (File file : files) {
                if (file.getName().endsWith(".pdf") || file.getName().endsWith(".tlg") || file.isDirectory()) {
                    node.add(new DefaultMutableTreeNode(file));
                }
            }
        }

        // Nastavení JTree s moderním stylingem
        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.addTreeSelectionListener(treeSelectionListener);
        tree.expandRow(0);

        // Moderní styling pro tree
        tree.setBackground(UIManager.getColor("Tree.background"));
        tree.setForeground(UIManager.getColor("Tree.foreground"));
        tree.setFont(UIManager.getFont("Tree.font"));
        tree.setRowHeight(24); // Větší výška řádků pro moderní vzhled
        tree.setShowsRootHandles(true);

        LOG.debug("Strom souborového systému úspěšně vytvořen a inicializován.");
    }

    /**
     * Vytvoří a vrátí panel s detaily o souboru s moderním designem.
     */
    private JPanel buildFileDetailsPanel() {
        LOG.debug("Vytvářím panel s detaily o souboru (moderní toolbar, grid).");

        // Vysokoúrovňový kontejner
        JPanel fileView = new JPanel(new BorderLayout(8, 8));
        fileView.setBackground(UIManager.getColor("Panel.background"));

        // Vytvoření moderního toolbaru
        JPanel toolBar = createModernToolBar();
        fileView.add(toolBar, BorderLayout.NORTH);

        // Hlavní detailní panel
        JPanel fileMainDetails = createModernDetailsPanel();
        fileView.add(fileMainDetails, BorderLayout.CENTER);

        LOG.debug("Panel s detaily o souboru vytvořen.");
        return fileView;
    }

    /**
     * Vytvoří moderní toolbar s tlačítky.
     */
    private JPanel createModernToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolBar.setBackground(UIManager.getColor("Panel.background"));
        toolBar.setBorder(createModernBorder());

        // Tlačítko "Otevřít"
        openFile = createModernButton("📂 Otevřít", "accent");
        openFile.addActionListener(e -> {
            LOG.debug("Kliknuto na tlačítko 'Otevřít'.");
            handleOpenFileAction();
            gui.repaint();
        });
        toolBar.add(openFile);

        // Tlačítko "Vymazat"
        deleteFile = createModernButton("🗑️ Vymazat", "danger");
        deleteFile.addActionListener(e -> {
            LOG.debug("Kliknuto na tlačítko 'Vymazat'.");
            deleteFile();
            gui.repaint();
        });
        toolBar.add(deleteFile);

        // Tlačítko "Vytisknout"
        printFile = createModernButton("🖨️ Vytisknout", "primary");
        printFile.addActionListener(e -> {
            LOG.debug("Kliknuto na tlačítko 'Vytisknout'.");
            handlePrintFileAction();
            gui.repaint();
        });
        toolBar.add(printFile);

        // Nastavení dostupnosti tlačítek podle podpory
        openFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
        printFile.setEnabled(desktop.isSupported(Desktop.Action.PRINT));

        return toolBar;
    }

    /**
     * Vytvoří moderní tlačítko s hover efekty.
     */
    private JButton createModernButton(String text, String colorType) {
        JButton button = new JButton(text);

        // Základní styling
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Font
        Font buttonFont = UIManager.getFont("Button.font");
        if (buttonFont == null) {
            buttonFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        } else {
            buttonFont = buttonFont.deriveFont(Font.BOLD, 12f);
        }
        button.setFont(buttonFont);

        // Barvy podle typu
        Color backgroundColor, foregroundColor;
        switch (colorType) {
            case "accent":
                backgroundColor = UIManager.getColor("Component.accentColor");
                if (backgroundColor == null) backgroundColor = new Color(0, 123, 255);
                foregroundColor = Color.WHITE;
                break;
            case "danger":
                backgroundColor = UIManager.getColor("Actions.Red");
                if (backgroundColor == null) backgroundColor = new Color(220, 53, 69);
                foregroundColor = Color.WHITE;
                break;
            case "primary":
                backgroundColor = UIManager.getColor("Actions.Blue");
                if (backgroundColor == null) backgroundColor = new Color(40, 167, 69);
                foregroundColor = Color.WHITE;
                break;
            default:
                backgroundColor = UIManager.getColor("Button.background");
                foregroundColor = UIManager.getColor("Button.foreground");
        }

        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // Hover efekty
        Color finalBackgroundColor = backgroundColor;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Color hoverColor = finalBackgroundColor.darker();
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(finalBackgroundColor);
            }
        });

        return button;
    }

    /**
     * Vytvoří moderní panel s detaily o souboru.
     */
    private JPanel createModernDetailsPanel() {
        JPanel fileMainDetails = new JPanel(new BorderLayout(8, 8));
        fileMainDetails.setBackground(UIManager.getColor("Panel.background"));
        fileMainDetails.setBorder(createModernBorder());

        // Levý panel s popisky
        JPanel fileDetailsLabels = new JPanel(new GridLayout(0, 1, 4, 8));
        fileDetailsLabels.setBackground(UIManager.getColor("Panel.background"));
        fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);

        // Pravý panel s hodnotami
        JPanel fileDetailsValues = new JPanel(new GridLayout(0, 1, 4, 8));
        fileDetailsValues.setBackground(UIManager.getColor("Panel.background"));
        fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);

        // Vytvoření komponent s moderním stylingem
        createDetailComponents(fileDetailsLabels, fileDetailsValues);

        return fileMainDetails;
    }

    /**
     * Vytvoří komponenty pro zobrazení detailů souboru.
     */
    private void createDetailComponents(JPanel labelsPanel, JPanel valuesPanel) {
        Font labelFont = UIManager.getFont("Label.font");
        if (labelFont != null) {
            labelFont = labelFont.deriveFont(Font.BOLD, 12f);
        }

        // Soubor
        JLabel fileLabel = new JLabel("📄 Soubor:", JLabel.TRAILING);
        fileLabel.setFont(labelFont);
        labelsPanel.add(fileLabel);
        fileName = new JLabel();
        valuesPanel.add(fileName);

        // Cesta
        JLabel pathLabel = new JLabel("📂 Cesta:", JLabel.TRAILING);
        pathLabel.setFont(labelFont);
        labelsPanel.add(pathLabel);
        path = new JTextField(5);
        path.setEditable(false);
        path.setBorder(createModernBorder());
        path.setBackground(UIManager.getColor("TextField.background"));
        valuesPanel.add(path);

        // MD4
        JLabel md4Label = new JLabel("🔐 MD4:", JLabel.TRAILING);
        md4Label.setFont(labelFont);
        labelsPanel.add(md4Label);
        date = new JLabel();
        date.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        valuesPanel.add(date);

        // Velikost
        JLabel sizeLabel = new JLabel("📏 Velikost:", JLabel.TRAILING);
        sizeLabel.setFont(labelFont);
        labelsPanel.add(sizeLabel);
        size = new JLabel();
        valuesPanel.add(size);

        // Typ
        JLabel typeLabel = new JLabel("🗂️ Typ:", JLabel.TRAILING);
        typeLabel.setFont(labelFont);
        labelsPanel.add(typeLabel);
        JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        flags.setBackground(UIManager.getColor("Panel.background"));
        isDirectory = new JRadioButton("Složka");
        isDirectory.setEnabled(false);
        isDirectory.setBackground(UIManager.getColor("Panel.background"));
        flags.add(isDirectory);
        isFile = new JRadioButton("Soubor");
        isFile.setEnabled(false);
        isFile.setBackground(UIManager.getColor("Panel.background"));
        flags.add(isFile);
        valuesPanel.add(flags);

        // Checkboxy pro čtení/zápis/spuštění
        readable = new JCheckBox("Čtení");
        readable.setBackground(UIManager.getColor("Panel.background"));
        writable = new JCheckBox("Zápis");
        writable.setBackground(UIManager.getColor("Panel.background"));
        executable = new JCheckBox("Spuštění");
        executable.setBackground(UIManager.getColor("Panel.background"));
    }

    /**
     * Vytvoří moderní border s kulatými rohy.
     */
    private AbstractBorder createModernBorder() {
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
                g2d.drawRoundRect(x, y, width - 1, height - 1, 8, 8);
                g2d.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(8, 8, 8, 8);
            }
        };
    }

    //////////////////////////////////////////////////////////////////////////////
    //                                ACTION HANDLERS
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Volá se při stisku tlačítka "Otevřít" s moderním progress indikátorem.
     */
    private void handleOpenFileAction() {
        if (currentFile == null) {
            LOG.warn("Nebyl vybrán žádný soubor, který by šlo otevřít.");
            return;
        }

        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Otevírám soubor...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (currentFile.getName().endsWith(".tlg")) {
                    LOG.info("Otevírám/decóduji TLG soubor: [{}]", currentFile.getAbsolutePath());
                    decodeTlgFile(currentFile, externalTlgList);
                } else {
                    LOG.info("Otevírám soubor pomocí Desktop: [{}]", currentFile.getAbsolutePath());
                    desktop.open(currentFile);
                }
                return null;
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);
                try {
                    get(); // Zkontroluj případné exceptions
                } catch (Exception e) {
                    LOG.error("Chyba při otevírání souboru: {}", e.getMessage(), e);
                    showThrowable(e);
                }
            }
        };
        worker.execute();
    }

    /**
     * Volá se při stisku tlačítka "Vytisknout" s moderním progress indikátorem.
     */
    private void handlePrintFileAction() {
        if (currentFile == null) {
            LOG.warn("Nebyl vybrán žádný soubor k tisku.");
            return;
        }

        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Připravuji tisk...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                LOG.info("Tisk souboru: [{}]", currentFile.getAbsolutePath());

                if (currentFile.getName().endsWith(".tlg")) {
                    createPdfFromTlg();
                } else {
                    desktop.print(currentFile);
                }
                return null;
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);
                try {
                    get();
                } catch (Exception e) {
                    LOG.error("Chyba při tisku souboru: {}", e.getMessage(), e);
                    showThrowable(e);
                }
            }
        };
        worker.execute();
    }

    /**
     * Vytvoří PDF z TLG souboru.
     */
    private void createPdfFromTlg() throws Exception {
        JList<TlgTemp> jList = new JList<>();
        DefaultListModel<TlgTemp> dlm = new DefaultListModel<>();
        jList.setModel(dlm);
        decodeTlgFile(currentFile, jList);

        File parentDir = currentFile.getParentFile();
        if (parentDir == null) {
            LOG.error("Parent folder not found; cannot save PDF.");
            return;
        }

        File pdfFile = new File(parentDir, currentFile.getName().replace(".tlg", ".pdf"));
        PdfWriter writer = new PdfWriter(pdfFile);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        for (int ii = 0; ii < jList.getModel().getSize(); ii++) {
            TlgTemp temp = (TlgTemp) jList.getModel().getElementAt(ii);

            for (int iii = 0; iii < temp.defaultListModel.size(); iii++) {
                IPacket packet = (IPacket) temp.defaultListModel.get(iii);

                document.add(new Paragraph(packet.getSimpleView()));

                Component jComponent = packet.getGraphicalVisualization();
                if (jComponent != null) {
                    document.add(componentToPdf(jComponent));
                }
            }
        }

        LOG.info("Saving PDF to: " + pdfFile.getAbsolutePath());
        document.close();
        LOG.info("PDF successfully created.");
    }

    /**
     * Převede komponentu na PDF obrázek.
     */
    private static com.itextpdf.layout.element.Image componentToPdf(Component comp) {
        try {
            Dimension dim = comp.getPreferredSize();
            BufferedImage bufferedImage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);

            Graphics2D g2 = bufferedImage.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, dim.width, dim.height);

            comp.setSize(dim);
            comp.printAll(g2);
            g2.dispose();

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(bufferedImage, "png", baos);
                ImageData imageData = ImageDataFactory.create(baos.toByteArray());
                return new com.itextpdf.layout.element.Image(imageData);
            }
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Dekóduje obsah ".tlg" souboru a přidá nový {@code TlgTemp} do
     * zadaného {@link JList}.
     */
    private void decodeTlgFile(File file, JList<TlgTemp> jList) {
        LOG.debug("Spouštím dekódování TLG souboru: {}", file.getName());

        StringBuilder builder = new StringBuilder();
        try (InputStream inputStream = new FileInputStream(file)) {
            int byteRead;
            while ((byteRead = inputStream.read()) != -1) {
                String str = dec2XBin(String.valueOf(byteRead), 8);
                builder.append(str);
            }
        } catch (IOException ex) {
            LOG.error("Chyba při čtení TLG souboru: {}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

        String hexData = bin2Hex(builder.toString());
        LOG.trace("HexData pro TLG soubor: {}", hexData);
        String decoded = TelegramDecoder.decodeTelegram(hexData);

        @SuppressWarnings("unchecked")
        DefaultListModel<TlgTemp> model = (DefaultListModel<TlgTemp>) jList.getModel();
        model.add(0, new TlgTemp("", decoded));

        LOG.info("Dekódovaný TLG soubor přidán do seznamu (TlgTemp).");
    }

    /**
     * Pokusí se smazat aktuálně vybraný soubor (po potvrzení) s moderním dialogem.
     */
    private void deleteFile() {
        if (currentFile == null) {
            LOG.warn("Žádný soubor k odstranění nebyl vybrán.");
            showModernErrorMessage("Nebyl vybrán žádný soubor", "Vyberte soubor");
            return;
        }

        LOG.info("Žádost o smazání souboru: {}", currentFile.getAbsolutePath());

        int result = JOptionPane.showConfirmDialog(
                gui,
                "Skutečně chcete vymazat soubor?\n" + currentFile.getName(),
                "Potvrdit smazání",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            try {
                if (currentFile.isDirectory()) {
                    LOG.warn("Pokoušíte se vymazat složku, tato akce není povolena.");
                    showModernErrorMessage("Nelze smazat složku", "Chyba");
                    return;
                }
                boolean deleted = currentFile.delete();
                if (!deleted) {
                    LOG.warn("Nepodařilo se vymazat soubor: {}", currentFile.getAbsolutePath());
                    showModernErrorMessage("Nepodařilo se vymazat soubor", "Chyba při mazání");
                } else {
                    LOG.info("Soubor úspěšně vymazán: {}", currentFile.getAbsolutePath());
                }
            } catch (Throwable t) {
                LOG.error("Chyba při mazání souboru: {}", t.getMessage(), t);
                showThrowable(t);
            }
        } else {
            LOG.debug("Uživatel zrušil mazání souboru.");
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    //                            FILE/CHILD DISPLAY
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Vytváří nebo rozšiřuje potomky zadaného uzlu na pozadí s moderním progress indikátorem.
     */
    private void showChildren(final DefaultMutableTreeNode node) {
        LOG.debug("Načítám potomky pro uzel: {}", node);

        tree.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Načítám složku...");

        SwingWorker<Void, File> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                File file = (File) node.getUserObject();
                File[] files = fileSystemView.getFiles(file, true);
                if (node.isLeaf()) {
                    for (File child : files) {
                        publish(child);
                    }
                }
                return null;
            }

            @Override
            protected void process(List<File> chunks) {
                for (File child : chunks) {
                    if (child.getName().endsWith(".tlg") || child.getName().endsWith(".pdf") || child.isDirectory()) {
                        node.add(new DefaultMutableTreeNode(child));
                    }
                }
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                tree.setEnabled(true);
                LOG.debug("Načítání potomků dokončeno pro uzel: {}", node);
            }
        };
        worker.execute();
    }

    /**
     * Aktualizuje UI komponenty dle detailů souboru s moderním stylingem.
     */
    private void setFileDetails(File file) {
        LOG.debug("Zobrazuji detaily pro soubor: {}", file);

        currentFile = file;
        Icon icon = fileSystemView.getSystemIcon(file);

        fileName.setIcon(icon);
        fileName.setText(fileSystemView.getSystemDisplayName(file));
        fileName.setFont(UIManager.getFont("Label.font"));

        path.setText(file.getPath());
        size.setText(formatFileSize(file.length()));

        // Pro .tlg soubory spočítáme MD4
        if (file.getName().endsWith(".tlg")) {
            calculateMd4ForTlgFile(file);
        } else {
            date.setText("—");
        }

        // Flagy
        readable.setSelected(file.canRead());
        writable.setSelected(file.canWrite());
        executable.setSelected(file.canExecute());
        isDirectory.setSelected(file.isDirectory());
        isFile.setSelected(file.isFile());

        gui.repaint();
    }

    /**
     * Formátuje velikost souboru do čitelné podoby.
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * Vypočítá MD4 hash pro TLG soubor.
     */
    private void calculateMd4ForTlgFile(File file) {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                LOG.debug("Soubor je .tlg => výpočet MD4.");
                try (InputStream inputStream = new FileInputStream(file)) {
                    StringBuilder builder = new StringBuilder();
                    int byteRead;
                    while ((byteRead = inputStream.read()) != -1) {
                        builder.append(dec2XBin(String.valueOf(byteRead), 8));
                    }
                    String tmp = builder.toString();

                    MessageDigest md = new CalculatorMD4();
                    md.update(splitBinaryStringToByteArray(tmp));
                    byte[] digest = md.digest();
                    return bytesToHex(digest);
                }
            }

            @Override
            protected void done() {
                try {
                    String hexDigest = get();
                    date.setText(hexDigest);
                    LOG.trace("MD4 pro soubor .tlg: {}", hexDigest);
                } catch (Exception e) {
                    LOG.error("Chyba při výpočtu MD4: {}", e.getMessage(), e);
                    date.setText("Chyba při výpočtu");
                }
            }
        };
        worker.execute();
    }

    //////////////////////////////////////////////////////////////////////////////
    //                            DIRECTORY WATCHER
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Vrátí aktuálně vybranou složku.
     */
    public File getCurrentFolder() {
        if (currentFile == null) {
            return null;
        }
        return currentFile.isDirectory() ? currentFile : currentFile.getParentFile();
    }

    /**
     * Spustí sledování pro aktuálně vybranou složku.
     */
    public void watchCurrentFolder() {
        File folder = getCurrentFolder();
        if (folder == null || !folder.isDirectory()) {
            LOG.warn("Nebyla vybrána platná složka pro sledování.");
            return;
        }
        try {
            LOG.info("Spouštím sledování složky: {}", folder.getAbsolutePath());
            directoryWatcher = new DirectoryWatcher(folder.toPath());
            directoryWatcher.execute();
        } catch (IOException e) {
            LOG.error("Chyba při inicializaci DirectoryWatcher: {}", e.getMessage(), e);
        }
    }

    /**
     * Zastaví sledování aktuální složky.
     */
    public void stopWatchingCurrentFolder() {
        if (directoryWatcher != null) {
            LOG.info("Zastavuji sledování aktuální složky.");
            directoryWatcher.stopWatching();
            directoryWatcher = null;
        }
    }

    /**
     * Obnoví uzel dané složky ve stromu.
     */
    public void refreshFolder(File folder) {
        LOG.debug("Obnovuji složku ve stromu: {}", folder);

        TreePath folderPath = findTreePath(folder);
        if (folderPath == null) {
            LOG.warn("Nenalezen TreePath pro složku: {}", folder);
            return;
        }
        DefaultMutableTreeNode folderNode = (DefaultMutableTreeNode) folderPath.getLastPathComponent();

        folderNode.removeAllChildren();

        File[] files = fileSystemView.getFiles(folder, true);
        for (File child : files) {
            if (child.getName().endsWith(".pdf") || child.getName().endsWith(".tlg") || child.isDirectory()) {
                folderNode.add(new DefaultMutableTreeNode(child));
            }
        }
        treeModel.reload(folderNode);
    }

    /**
     * Najde TreePath ve stromu pro zadaný File.
     */
    private TreePath findTreePath(File find) {
        if (find == null) {
            return null;
        }
        for (int i = 0; i < tree.getRowCount(); i++) {
            TreePath treePath = tree.getPathForRow(i);
            if (treePath == null) {
                continue;
            }
            Object object = treePath.getLastPathComponent();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;
            File nodeFile = (File) node.getUserObject();
            if (nodeFile.equals(find)) {
                return treePath;
            }
        }
        return null;
    }

    //////////////////////////////////////////////////////////////////////////////
    //                              ERROR HANDLING
    //////////////////////////////////////////////////////////////////////////////

    private void showModernErrorMessage(String errorMessage, String errorTitle) {
        LOG.warn("Zobrazuji chybovou zprávu: {}, {}", errorTitle, errorMessage);
        JOptionPane.showMessageDialog(gui, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
    }

    private void showThrowable(Throwable t) {
        LOG.error("Vyhazena výjimka: {}", t.getMessage(), t);
        t.printStackTrace();
        JOptionPane.showMessageDialog(gui, t.toString(), t.getMessage(), JOptionPane.ERROR_MESSAGE);
        gui.repaint();
    }

    //////////////////////////////////////////////////////////////////////////////
    //                               MISC. METHODS
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Zobrazí kořenový uzel v JTree.
     */
    public void showRootFile() {
        LOG.debug("Zobrazuji (vybírám) kořenový uzel v JTree.");
        if (tree.getRowCount() > 0) {
            tree.setSelectionInterval(0, 0);
        }
    }

    /**
     * Kopíruje jeden soubor na jiné místo pomocí FileChannel.
     */
    public static boolean copyFile(File from, File to) throws IOException {
        LOG.debug("Kopíruji soubor z [{}] do [{}]", from.getAbsolutePath(), to.getAbsolutePath());
        boolean created = to.createNewFile();
        if (created) {
            try (FileChannel fromChannel = new FileInputStream(from).getChannel();
                 FileChannel toChannel = new FileOutputStream(to).getChannel()) {
                toChannel.transferFrom(fromChannel, 0, fromChannel.size());
                to.setReadable(from.canRead());
                to.setWritable(from.canWrite());
                to.setExecutable(from.canExecute());
            }
            LOG.info("Soubor byl úspěšně zkopírován.");
        } else {
            LOG.warn("Nepodařilo se vytvořit cílový soubor: {}", to.getAbsolutePath());
        }
        return created;
    }

    //////////////////////////////////////////////////////////////////////////////
    //                          DIRECTORY WATCHER CLASS
    //////////////////////////////////////////////////////////////////////////////

    /**
     * SwingWorker který sleduje adresář přes WatchService s moderním progress indikátorem.
     */
    private class DirectoryWatcher extends SwingWorker<Void, Path> {
        private final Path pathToWatch;
        private WatchService watchService;
        private boolean keepWatching = true;

        public DirectoryWatcher(Path pathToWatch) throws IOException {
            this.pathToWatch = pathToWatch;
            watchService = FileSystems.getDefault().newWatchService();
            pathToWatch.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );
        }

        @Override
        protected Void doInBackground() {
            LOG.debug("Spuštěn DirectoryWatcher pro: {}", pathToWatch.toAbsolutePath());
            while (keepWatching) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    LOG.warn("DirectoryWatcher byl přerušen: {}", e.getMessage());
                    return null;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();
                    LOG.trace("Zachycena změna: {}", fileName);
                    publish(fileName);
                }
                boolean valid = key.reset();
                if (!valid) {
                    LOG.warn("WatchKey již není platný, končím sledování.");
                    break;
                }
            }
            return null;
        }

        @Override
        protected void process(List<Path> chunks) {
            LOG.debug("Provádím refresh složky (process) pro cestu: {}", pathToWatch.toAbsolutePath());
            refreshFolder(pathToWatch.toFile());
        }

        public void stopWatching() {
            LOG.debug("Ukončuji DirectoryWatcher pro složku: {}", pathToWatch);
            keepWatching = false;
            try {
                watchService.close();
            } catch (IOException e) {
                LOG.error("Chyba při ukončování DirectoryWatcher: {}", e.getMessage(), e);
            }
        }
    }
}