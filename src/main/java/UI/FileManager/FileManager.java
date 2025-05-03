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
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * {@code FileManager} poskytuje GUI-based prohlížeč souborů v systému,
 * se speciálním zpracováním pro soubory ".tlg" (dekódování a přidání do seznamu).
 * <p>
 * Klíčové vlastnosti:
 * <ul>
 *   <li>Zobrazuje strom souborů a složek, ukotvený v kořenech souborového systému.</li>
 *   <li>Zobrazuje detaily o vybraném souboru (jméno, cesta, MD4 pro .tlg soubory atd.).</li>
 *   <li>Umožňuje mazání a otevírání souborů, plus sledování změn v aktuálně vybrané složce.</li>
 *   <li>Integrace s {@link JList}, která drží objekty {@link TlgTemp}, kam lze přidávat
 *       nově dekódovaná data ze souborů .tlg.</li>
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
     * Vytvoří (pokud již není vytvořeno) a vrátí hlavní GUI panel pro tohoto správce souborů.
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
            gui = new JPanel(new BorderLayout(3, 3));
            gui.setBorder(new EmptyBorder(5, 5, 5, 5));
            fileSystemView = FileSystemView.getFileSystemView();
            desktop = Desktop.getDesktop();

            // Vytvoření panelu s detailem souboru
            JPanel detailView = new JPanel(new BorderLayout(3, 3));

            // Vytvoření kořenového uzlu pro strom
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            treeModel = new DefaultTreeModel(root);

            // Vytvoření stromu souborového systému
            buildFileSystemTree(root);
            JScrollPane treeScroll = new JScrollPane(tree);

            // Nastavení velikosti panelu se stromem
            tree.setVisibleRowCount(15);
            Dimension preferredSize = treeScroll.getPreferredSize();
            Dimension widePreferred = new Dimension(200, (int) preferredSize.getHeight());
            treeScroll.setPreferredSize(widePreferred);

            // Postavení spodního panelu s detailními informacemi
            JPanel fileView = buildFileDetailsPanel();
            detailView.add(fileView, BorderLayout.SOUTH);

            // Přidáme strom + detailní panel do splitu
            JSplitPane splitPane =
                    new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeScroll, detailView);
            splitPane.setResizeWeight(1);
            gui.add(splitPane, BorderLayout.CENTER);

            // Přidáme progress bar do spodní části
            JPanel simpleOutput = new JPanel(new BorderLayout(3, 3));
            progressBar = new JProgressBar();
            progressBar.setVisible(false);
            simpleOutput.add(progressBar, BorderLayout.EAST);
            gui.add(simpleOutput, BorderLayout.SOUTH);

            LOG.debug("FileManager GUI úspěšně inicializováno.");
        }
        return gui;
    }

    /**
     * Vytvoří strom souborového systému, naplní kořenové uzly (např. disky)
     * a přidá {@link TreeSelectionListener}.
     *
     * @param root kořenový uzel pro {@link DefaultTreeModel}
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

            // Do stromu přidáváme jen složky a soubory .tlg
            File[] files = fileSystemView.getFiles(fileSystemRoot, true);
            for (File file : files) {
                if (file.getName().endsWith(".pdf") || file.getName().endsWith(".tlg") || file.isDirectory()) {
                    node.add(new DefaultMutableTreeNode(file));
                }
            }
        }

        // Nastavení JTree
        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.addTreeSelectionListener(treeSelectionListener);
        tree.expandRow(0);

        LOG.debug("Strom souborového systému úspěšně vytvořen a inicializován.");
    }

    /**
     * Vytvoří a vrátí panel s detaily o souboru (včetně toolbaru s
     * otevřít/vytisknout/vymazat, plus grid s atributy).
     */
    private JPanel buildFileDetailsPanel() {
        LOG.debug("Vytvářím panel s detaily o souboru (toolBar, grid).");

        // Vysokoúrovňový kontejner
        JPanel fileView = new JPanel(new BorderLayout(3, 3));

        // Vytvoření toolbaru
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Tlačítko "Otevřít"
        openFile = new JButton("Otevřít");
        openFile.setMnemonic('o');
        openFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOG.debug("Kliknuto na tlačítko 'Otevřít'.");
                handleOpenFileAction();
                gui.repaint();
            }
        });
        toolBar.add(openFile);

        // Tlačítko "Vymazat"
        deleteFile = new JButton("Vymazat");
        deleteFile.setMnemonic('d');
        deleteFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOG.debug("Kliknuto na tlačítko 'Vymazat'.");
                deleteFile();
                gui.repaint();
            }
        });
        toolBar.add(deleteFile);

        // Tlačítko "Vytisknout"
        printFile = new JButton("Vytisknout");
        printFile.setMnemonic('p');
        printFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                LOG.debug("Kliknuto na tlačítko 'Vytisknout'.");
                handlePrintFileAction();
                gui.repaint();
            }
        });
        toolBar.add(printFile);

        // Nastavení dostupnosti tlačítek podle podpory
        openFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
        printFile.setEnabled(desktop.isSupported(Desktop.Action.PRINT));

        // Mezera v toolbaru
        toolBar.addSeparator();
        toolBar.addSeparator();

        // Hlavní detailní panel
        JPanel fileMainDetails = new JPanel(new BorderLayout(4, 2));
        fileMainDetails.setBorder(new EmptyBorder(0, 6, 0, 6));

        JPanel fileDetailsLabels = new JPanel(new GridLayout(0, 1, 2, 2));
        fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);

        JPanel fileDetailsValues = new JPanel(new GridLayout(0, 1, 2, 2));
        fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);

        fileDetailsLabels.add(new JLabel("Soubor", JLabel.TRAILING));
        fileName = new JLabel();
        fileDetailsValues.add(fileName);

        fileDetailsLabels.add(new JLabel("Cesta", JLabel.TRAILING));
        path = new JTextField(5);
        path.setEditable(false);
        fileDetailsValues.add(path);

        fileDetailsLabels.add(new JLabel("MD4", JLabel.TRAILING));
        date = new JLabel();
        fileDetailsValues.add(date);

        fileDetailsLabels.add(new JLabel("Velikost", JLabel.TRAILING));
        size = new JLabel();
        fileDetailsValues.add(size);

        fileDetailsLabels.add(new JLabel("Typ", JLabel.TRAILING));
        JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        isDirectory = new JRadioButton("Directory");
        isDirectory.setEnabled(false);
        flags.add(isDirectory);
        isFile = new JRadioButton("File");
        isFile.setEnabled(false);
        flags.add(isFile);
        fileDetailsValues.add(flags);

        // Deaktivujeme popisky
        int count = fileDetailsLabels.getComponentCount();
        for (int ii = 0; ii < count; ii++) {
            fileDetailsLabels.getComponent(ii).setEnabled(false);
        }

        // CheckBoxy pro čtení/zápis/spuštění (nejsou přidány do layoutu, ale k dispozici)
        readable = new JCheckBox("Read  ");
        readable.setMnemonic('a');
        writable = new JCheckBox("Write  ");
        writable.setMnemonic('w');
        executable = new JCheckBox("Execute");
        executable.setMnemonic('x');

        // Kompletace panelu
        fileView.add(toolBar, BorderLayout.NORTH);
        fileView.add(fileMainDetails, BorderLayout.CENTER);

        LOG.debug("Panel s detaily o souboru vytvořen.");
        return fileView;
    }

    //////////////////////////////////////////////////////////////////////////////
    //                                ACTION HANDLERS
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Volá se při stisku tlačítka "Otevřít".
     * <ul>
     *   <li>Pokud soubor končí na ".tlg", dekóduje jej a přidá do {@code externalTlgList}.</li>
     *   <li>Jinak se pokusí otevřít soubor pomocí výchozí desktopové aplikace.</li>
     * </ul>
     */
    private void handleOpenFileAction() {
        if (currentFile == null) {
            LOG.warn("Nebyl vybrán žádný soubor, který by šlo otevřít.");
            return;
        }

        try {
            if (currentFile.getName().endsWith(".tlg")) {
                LOG.info("Otevírám/decóduji TLG soubor: [{}]", currentFile.getAbsolutePath());
                decodeTlgFile(currentFile, externalTlgList);
            } else {
                LOG.info("Otevírám soubor pomocí Desktop: [{}]", currentFile.getAbsolutePath());
                desktop.open(currentFile);
            }
        } catch (Throwable t) {
            LOG.error("Chyba při otevírání souboru: {}", t.getMessage(), t);
            showThrowable(t);
        }
    }

    /**
     * Volá se při stisku tlačítka "Vytisknout". Pokusí se vytisknout {@link #currentFile}
     * přes systémový Desktop.
     */
    private void handlePrintFileAction() {
        if (currentFile == null) {
            LOG.warn("Nebyl vybrán žádný soubor k tisku.");
            return;
        }
        try {
            LOG.info("Tisk souboru: [{}]", currentFile.getAbsolutePath());

            if (currentFile.getName().endsWith(".tlg"))
            {

                JList<TlgTemp> jList = new JList();
                DefaultListModel<TlgTemp> dlm = new DefaultListModel<>();
                jList.setModel(dlm);
                decodeTlgFile(currentFile,jList);


                try {
                    File parentDir = currentFile.getParentFile();
                    if (parentDir == null) {
                        LOG.error("Parent folder not found; cannot save PDF.");
                        return;
                    }

                    File pdfFile = new File(parentDir,currentFile.getName().replace(".tlg",".pdf"));
                    PdfWriter writer = new PdfWriter(pdfFile);
                    PdfDocument pdfDoc = new PdfDocument(writer);
                    Document document = new Document(pdfDoc);

                for (int ii = 0; ii < jList.getModel().getSize(); ii++)
                {
                    TlgTemp temp = (TlgTemp) jList.getModel().getElementAt(ii);

                    for (int iii =0;iii<temp.defaultListModel.size();iii++)
                    {
                        IPacket packet = (IPacket) temp.defaultListModel.get(iii);

                        document.add(new Paragraph(packet.getSimpleView()));

                        Component jComponent=packet.getGraphicalVisualization();

                        if (jComponent != null)
                        {

                            document.add(componentToPdf(jComponent));
                        }

                    }

                }




                LOG.info("Saving PDF to: " + pdfFile.getAbsolutePath());



                    document.close();
                    LOG.info("PDF successfully created.");
                } catch (IOException e) {

                    LOG.error(e.getMessage());
                }

            }
            else
            {
                desktop.print(currentFile);

            }





        } catch (Throwable t) {
            LOG.error("Chyba při tisku souboru: {}", t.getMessage(), t);
            showThrowable(t);
        }
    }


    private static  com.itextpdf.layout.element.Image componentToPdf(Component comp) {
        // Step 1: Render the component to a BufferedImage
        // Ensure the component has a valid size (layout done),
        // or manually setPreferredSize and call comp.doLayout() if needed.
        Dimension dim = comp.getPreferredSize();
        BufferedImage bufferedImage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = bufferedImage.createGraphics();
        // Optional: set a background if needed
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, dim.width, dim.height);

        // Actually render the component
        comp.setSize(dim);      // Make sure the component matches its preferred size
        comp.printAll(g2);      // or paint(g2)
        g2.dispose();

        // Step 2: Convert the BufferedImage to an iText ImageData
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "png", baos);
            ImageData imageData = ImageDataFactory.create(baos.toByteArray());
            com.itextpdf.layout.element.Image image =
                    new com.itextpdf.layout.element.Image(imageData);

            return image;
        }
        catch (Throwable t) {}

        return null;
    }


    /**
     * Dekóduje obsah ".tlg" souboru a přidá nový {@code TlgTemp} do
     * zadaného {@link JList}.
     *
     * @param file .tlg soubor k dekódování
     * @param jList {@link JList} kam ukládáme výsledek
     */
    private void decodeTlgFile(File file, JList<TlgTemp> jList) {
        LOG.debug("Spouštím dekódování TLG souboru: {}", file.getName());

        // 1) Čtení obsahu do binárního řetězce
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

        // 2) Převod na HEX a dekódování
        String hexData = bin2Hex(builder.toString());
        LOG.trace("HexData pro TLG soubor: {}", hexData);
        String decoded = TelegramDecoder.decodeTelegram(hexData);

        // 3) Uložení do seznamu
        @SuppressWarnings("unchecked")
        DefaultListModel<TlgTemp> model = (DefaultListModel<TlgTemp>) jList.getModel();
        model.add(0, new TlgTemp("", decoded));

        LOG.info("Dekódovaný TLG soubor přidán do seznamu (TlgTemp).");
    }

    /**
     * Pokusí se smazat aktuálně vybraný soubor (po potvrzení).
     */
    private void deleteFile() {
        if (currentFile == null) {
            LOG.warn("Žádný soubor k odstranění nebyl vybrán.");
            showErrorMessage("No file selected for deletion.", "Select File");
            return;
        }

        LOG.info("Žádost o smazání souboru: {}", currentFile.getAbsolutePath());
        int result = JOptionPane.showConfirmDialog(
                gui,
                "Skutečně vymazat?",
                "Vymazat",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.ERROR_MESSAGE
        );
        if (result == JOptionPane.OK_OPTION) {
            try {
                if (currentFile.isDirectory()) {
                    LOG.warn("Pokoušíte se vymazat složku, tato akce není povolena.");
                    return;
                }
                boolean deleted = currentFile.delete();
                if (!deleted) {
                    LOG.warn("Nepodařilo se vymazat soubor: {}", currentFile.getAbsolutePath());
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
     * Vytváří nebo rozšiřuje potomky zadaného uzlu na pozadí (přes SwingWorker).
     *
     * @param node uzel, jehož děti zobrazujeme
     */
    private void showChildren(final DefaultMutableTreeNode node) {
        LOG.debug("Načítám potomky pro uzel: {}", node);

        tree.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

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
                    if (child.getName().endsWith(".tlg") || child.isDirectory()) {
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
     * Aktualizuje UI komponenty dle detailů souboru (jméno, cesta, velikost,
     * MD4 pro .tlg, atd.).
     *
     * @param file {@link File}, který zobrazujeme
     */
    private void setFileDetails(File file) {
        LOG.debug("Zobrazuji detaily pro soubor: {}", file);

        currentFile = file;
        Icon icon = fileSystemView.getSystemIcon(file);

        fileName.setIcon(icon);
        fileName.setText(fileSystemView.getSystemDisplayName(file));
        path.setText(file.getPath());
        size.setText(file.length() + " bytes");

        // Pro .tlg soubory spočítáme MD4
        if (file.getName().endsWith(".tlg")) {
            LOG.debug("Soubor je .tlg => výpočet MD4.");
            try (InputStream inputStream = new FileInputStream(file)) {
                StringBuilder builder = new StringBuilder();
                int byteRead;
                while ((byteRead = inputStream.read()) != -1) {
                    builder.append(dec2XBin(String.valueOf(byteRead), 8));
                }
                String tmp = builder.toString();

                // Výpočet MD4 hashe
                MessageDigest md = new CalculatorMD4();
                md.update(splitBinaryStringToByteArray(tmp));
                byte[] digest = md.digest();

                String hexDigest = bytesToHex(digest);
                date.setText(hexDigest);

                LOG.trace("MD4 pro soubor .tlg: {}", hexDigest);

                // Příklad: vytvoříme nebo načteme PH objekt
                PH ph = new PH(new String[]{tmp});
                // Dle potřeby lze s ph dále pracovat

            } catch (IOException ex) {
                LOG.error("Chyba při výpočtu MD4: {}", ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        } else {
            date.setText("");
        }

        // Flagy
        readable.setSelected(file.canRead());
        writable.setSelected(file.canWrite());
        executable.setSelected(file.canExecute());
        isDirectory.setSelected(file.isDirectory());
        isFile.setSelected(file.isFile());

        gui.repaint();
    }

    //////////////////////////////////////////////////////////////////////////////
    //                            DIRECTORY WATCHER
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Vrátí aktuálně vybranou složku (pokud je {@link #currentFile} složka,
     * tak ji, jinak vrátí její parent).
     *
     * @return aktuální složka, nebo {@code null} není-li žádný soubor vybrán
     */
    public File getCurrentFolder() {
        if (currentFile == null) {
            return null;
        }
        return currentFile.isDirectory() ? currentFile : currentFile.getParentFile();
    }

    /**
     * Spustí sledování (watch service) pro aktuálně vybranou složku, pokud je platná.
     * Pokud uživatel zvolí jinou složku, volá se znovu.
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
     * Zastaví sledování aktuální složky (pokud běží).
     */
    public void stopWatchingCurrentFolder() {
        if (directoryWatcher != null) {
            LOG.info("Zastavuji sledování aktuální složky.");
            directoryWatcher.stopWatching();
            directoryWatcher = null;
        }
    }

    /**
     * Obnoví (refresh) uzel dané složky ve stromu - vyčistí děti a znovu je načte.
     *
     * @param folder složka, kterou chceme obnovit
     */
    public void refreshFolder(File folder) {
        LOG.debug("Obnovuji složku ve stromu: {}", folder);

        TreePath folderPath = findTreePath(folder);
        if (folderPath == null) {
            LOG.warn("Nenalezen TreePath pro složku: {}", folder);
            return;
        }
        DefaultMutableTreeNode folderNode = (DefaultMutableTreeNode) folderPath.getLastPathComponent();

        // Vymažeme všechny potomky
        folderNode.removeAllChildren();

        // Znovu přidáme .tlg i složky
        File[] files = fileSystemView.getFiles(folder, true);
        for (File child : files) {
            if (child.getName().endsWith(".pdf") ||child.getName().endsWith(".tlg") || child.isDirectory()) {
                folderNode.add(new DefaultMutableTreeNode(child));
            }
        }
        treeModel.reload(folderNode);
    }

    /**
     * Najde {@link TreePath} ve stromu pro zadaný {@link File}, nebo {@code null}, pokud nenalezen.
     *
     * @param find soubor, který hledáme ve stromu
     * @return cesta k tomuto souboru, nebo {@code null}
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

    private void showErrorMessage(String errorMessage, String errorTitle) {
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
     * Volitelná metoda, která nastaví výběr na kořenový uzel v JTree (row=0).
     * Vhodné např. pro počáteční stav.
     */
    public void showRootFile() {
        LOG.debug("Zobrazuji (vybírám) kořenový uzel v JTree.");
        if (tree.getRowCount() > 0) {
            tree.setSelectionInterval(0, 0);
        }
    }

    /**
     * Kopíruje jeden soubor na jiné místo pomocí {@link FileChannel}. Nový soubor
     * zdědí práva pro čtení/zápis/spuštění od původního.
     *
     * @param from zdroj
     * @param to   cíl
     * @return {@code true}, pokud se podařilo vytvořit cílový soubor
     * @throws IOException chyba při čtení/zápisu
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
     * {@link SwingWorker}, který sleduje adresář přes {@link WatchService}.
     * Pokud dojde k událostem ENTRY_CREATE/ENTRY_DELETE/ENTRY_MODIFY,
     * zavolá se refresh složky.
     */
    private class DirectoryWatcher extends SwingWorker<Void, Path> {
        private final Path pathToWatch;
        private WatchService watchService;
        private boolean keepWatching = true;

        /**
         * Konstruktor DirectoryWatcher pro zadanou cestu.
         *
         * @param pathToWatch cesta, kterou sledujeme
         * @throws IOException chyba při vytvoření watch servisu
         */
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
                    // Blokuje, dokud nedojde k nějaké události
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

                    // Publikujeme do EDT => spustí refresh v #process
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
            // Voláno na EDT; refresh složky
            LOG.debug("Provádím refresh složky (process) pro cestu: {}", pathToWatch.toAbsolutePath());
            refreshFolder(pathToWatch.toFile());
        }

        /**
         * Graceful zastavení sledování - zastaví hlavní smyčku a zavře {@link WatchService}.
         */
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
