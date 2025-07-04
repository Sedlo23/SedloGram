package UI.Main;

import ModernDocking.*;
import ModernDocking.internal.DockableToolbar;
import ModernDocking.layouts.ApplicationLayout;
import ModernDocking.layouts.DockingLayouts;
import ModernDocking.layouts.WindowLayoutBuilder;
import ModernDocking.persist.AppState;
import ModernDocking.ui.DockableMenuItem;
import UI.Builders.DockPanelBuilder;
import UI.Builders.MenuBarBuilder;
import UI.DockingPanes.ComponentDebugger;
import UI.DockingPanes.RootDockingPanelCustom;
import UI.DockingPanes.SimplePanel;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.IntelliJTheme;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import tools.ui.GUIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * The main application frame for the SedloGram docking application.
 * <p>
 * This frame initializes the docking framework, loads the Look-and-Feel,
 * builds the menu bar and dock panels, and arranges them using GridBagLayout.
 * It also triggers a simulated mouse click at startup to ensure focus.
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = -5570653778104813836L;
    public static SimplePanel zdrojDock;
    public static SimplePanel editDock;
    public static SimplePanel telegDock;
    public static SimplePanel grafDock;
    public static SimplePanel koderDock;
    public static SimplePanel decDock;
    public static SimplePanel binDock;
    public static SimplePanel mapDock;
    public static SimplePanel navodDock;
    public static JTextArea textArea;
    static Preferences prefs = Preferences.userNodeForPackage(MainFrame.class);
    // Preference key name for theme
    static final String PREF_NAME = "themeABCDEF";
    static List<Class<IntelliJTheme.ThemeLaf>> themeClasses = new ArrayList<>();

    /**
     * Constructs the main application frame.
     */
    public MainFrame() {
        setTitle("SedloGram");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setupFrame();
        setVisible(true);
        setIconImage(GUIHelper.loadAndScaleIcon("flags/android-chrome-512x512.png").getImage());

    }

    /**
     * The application entry point. It scans for available Look-and-Feel themes,
     * sets the initial theme, and launches the MainFrame on the Swing EDT.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        // Scan for IntelliJTheme.ThemeLaf implementations using ClassGraph.
        try (ScanResult scanResult = new ClassGraph()
                .whitelistPackages(IntelliJTheme.ThemeLaf.class.getPackage().getName())
                .enableClassInfo()
                .scan()) {
            themeClasses = scanResult.getSubclasses(IntelliJTheme.ThemeLaf.class.getName())
                    .loadClasses(IntelliJTheme.ThemeLaf.class);
        }

        // Retrieve saved theme preference (default "0" if not set)
        String propertyValue = prefs.get(PREF_NAME, "0");
        try {
            // Set default Look-and-Feel using first available theme.
            UIManager.setLookAndFeel(themeClasses.get(0).newInstance());
            // Loop through themes and apply the saved one.
            for (Class<IntelliJTheme.ThemeLaf> themeClass : themeClasses) {
                IntelliJTheme.ThemeLaf themeInstance = themeClass.newInstance();
                if (themeInstance.getName().compareTo(propertyValue) == 0) {
                    UIManager.setLookAndFeel(themeClass.newInstance());
                    break;
                }
            }
        } catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Disable UI scaling (for consistency across platforms)
        System.setProperty("sun.java2d.uiScale", "1");
        System.setProperty("sun.java2d.uiScale.enabled", "false");
        System.setProperty("sun.java2d.dpiaware", "false");
        System.setProperty("prism.allowhidpi", "false");

        // Set file encoding to Cp1252 (if needed)
        System.setProperty("file.encoding", "Cp1252");

        SwingUtilities.invokeLater(() -> {
            // Optionally, you can set up automatic layout persistence:
            // AppState.setPersistFile(new File("auto_persist_layout.xml"));
            // AppState.setAutoPersist(true);

            MainFrame mainFrame = new MainFrame();
            // Unpin specific docks if desired
            Docking.unpinDockable(telegDock);
            Docking.unpinDockable(grafDock);
            //Docking.unpinDockable(koderDock);
            Docking.unpinDockable(decDock);
            Docking.unpinDockable(binDock);
            Docking.unpinDockable(mapDock);

            // Simulate a mouse click at the center of the frame to ensure focus.
            Point location = mainFrame.getLocationOnScreen();
            int xCenter = location.x + mainFrame.getWidth() / 2;
            int yCenter = location.y + mainFrame.getHeight() / 2;
            try {
                Robot robot = new Robot();
                robot.mouseMove(xCenter, yCenter);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            } catch (AWTException e) {
                e.printStackTrace();
            }

            mainFrame.revalidate();
            mainFrame.repaint();
        });

        // Refresh FlatLaf UI after launching.
        FlatLaf.updateUI();
    }

    /**
     * Loads a font from the given resource path.
     *
     * @param fontPath the path to the font resource
     * @param size     the desired font size
     * @return the loaded {@link Font}, or {@code null} if an error occurs
     */
    public static Font loadFont(String fontPath, float size) {
        try (InputStream is = MainFrame.class.getResourceAsStream(fontPath)) {
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Initializes the main frame, sets up docking panels, the menu bar, and layout.
     */
    private void setupFrame() {
        setSize(800, 600);
        Docking.initialize(this);
        RootDockingPanelCustom dockingPanel = new RootDockingPanelCustom(this);

        MenuBarBuilder menuBarBuilder = new MenuBarBuilder(this);
        JMenuBar menuBar = menuBarBuilder.buildMenuBar();
        setJMenuBar(menuBar);

        // Build dock panels using DockPanelBuilder.
        DockPanelBuilder dockPanelBuilder = new DockPanelBuilder();
        mapDock = dockPanelBuilder.buildMapDock();
        editDock = dockPanelBuilder.buildEditDock();
        telegDock = dockPanelBuilder.buildTelegDock();
        grafDock = dockPanelBuilder.buildGrafDock();
        // koderDock = dockPanelBuilder.buildDecDock();
        decDock = dockPanelBuilder.buildDecDock();
        binDock = dockPanelBuilder.buildBinDock();


        // Add each dock panel to the view menu for easy access.
        JMenu viewMenu = menuBarBuilder.getViewMenu();
        viewMenu.add(actionListenDock(editDock));
        viewMenu.add(actionListenDock(telegDock));
        viewMenu.add(actionListenDock(grafDock));
        // viewMenu.add(actionListenDock(koderDock));
        viewMenu.add(actionListenDock(decDock));
        viewMenu.add(actionListenDock(binDock));
        viewMenu.add(actionListenDock(mapDock));

        // Use GridBagLayout to add the docking panel.
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 5, 5);
        add(dockingPanel, gbc);

        // Build the default layout for docking.
        ApplicationLayout defaultLayout = new WindowLayoutBuilder(editDock.getPersistentID())
                .dock(telegDock.getPersistentID(), editDock.getPersistentID(), DockingRegion.WEST, 0.2)
                .dock(grafDock.getPersistentID(), editDock.getPersistentID(), DockingRegion.EAST, 1)
                // .dock(koderDock.getPersistentID(), editDock.getPersistentID(), DockingRegion.EAST, 1)
                .dock(decDock.getPersistentID(), editDock.getPersistentID(), DockingRegion.EAST, 1)
                .dock(binDock.getPersistentID(), editDock.getPersistentID(), DockingRegion.SOUTH, 1)
                .dock(mapDock.getPersistentID(), editDock.getPersistentID(), DockingRegion.SOUTH, 1)
                .buildApplicationLayout();

        DockingLayouts.addLayout("Základní layout", defaultLayout);
        AppState.setDefaultApplicationLayout(defaultLayout);
        DockingState.restoreApplicationLayout(defaultLayout);
    }

    /**
     * Creates a menu item that activates the specified dockable panel.
     *
     * @param simplePanel the dockable panel
     * @return a {@link JMenuItem} that can be added to a menu
     */
    private JMenuItem actionListenDock(SimplePanel simplePanel) {
        return new DockableMenuItem(() -> ((Dockable) simplePanel).getPersistentID(), ((Dockable) simplePanel).getTabText());
    }
}
