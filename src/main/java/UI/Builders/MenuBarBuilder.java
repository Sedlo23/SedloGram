package UI.Builders;

import ModernDocking.DockingState;
import ModernDocking.layouts.ApplicationLayout;
import ModernDocking.layouts.ApplicationLayoutXML;
import ModernDocking.ui.LayoutsMenu;
import UI.Main.LaFHolder;
import UI.Main.MainFrame;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.IntelliJTheme;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Builds the application's menu bar, including file menus for saving/loading layouts,
 * a look-and-feel menu, window options, and a view menu.
 *
 * <p>This builder uses ClassGraph to dynamically scan for available IntelliJTheme.ThemeLaf
 * implementations. It organizes themes into holders (by the first word of their name) and
 * creates a nested menu structure for dark and light themes.
 */
public class MenuBarBuilder {

    private final MainFrame mainFrame;
    private JMenu viewMenu;

    // List of discovered Look-and-Feel theme classes.
    static List<Class<IntelliJTheme.ThemeLaf>> themeClasses = new ArrayList<>();
    // Preferences used to store the current theme.
    static Preferences prefs = Preferences.userNodeForPackage(MainFrame.class);
    // Preference key for the theme.
    static final String PREF_NAME = "themeABCDEF";

    /**
     * Constructs a new MenuBarBuilder.
     *
     * @param mainFrame the main application frame
     */
    public MenuBarBuilder(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * Builds and returns the application's menu bar.
     *
     * @return the constructed {@link JMenuBar}
     */
    public JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(new Font("Arial", Font.PLAIN, 14));

        JMenu fileMenu = new JMenu("Soubor");
        menuBar.add(fileMenu);

        // Look-and-Feel menu
        JMenu lafMenu = buildLookAndFeelMenu();
        menuBar.add(lafMenu);

        // Layout saving and loading menu items
        buildSaveLayoutMenuItem(fileMenu);
        buildLoadLayoutMenuItem(fileMenu);

        // Window menu containing layout options
        JMenu windowMenu = new JMenu("Window");
        windowMenu.add(new LayoutsMenu());
        menuBar.add(windowMenu);

        // View menu
        viewMenu = new JMenu("Karty");
        menuBar.add(viewMenu);

        return menuBar;
    }

    /**
     * Builds and returns a menu for selecting a Look-and-Feel.
     *
     * @return the Look-and-Feel {@link JMenu}
     */
    private JMenu buildLookAndFeelMenu() {
        JMenu lafMenu = new JMenu("Vzhled");
        ButtonGroup group = new ButtonGroup();
        ArrayList<LaFHolder> lafHolders = new ArrayList<>();

        // Use ClassGraph to scan for subclasses of IntelliJTheme.ThemeLaf
        try (ScanResult scanResult = new ClassGraph()
                .whitelistPackages(IntelliJTheme.ThemeLaf.class.getPackageName())
                .enableClassInfo()
                .scan()) {
            themeClasses = scanResult
                    .getSubclasses(IntelliJTheme.ThemeLaf.class.getName())
                    .loadClasses(IntelliJTheme.ThemeLaf.class);
        }

        // Get saved preference for theme (default "0" if not set)
        String propertyValue = prefs.get(PREF_NAME, "0");

        try {
            // Set default Look-and-Feel to the first found theme
            UIManager.setLookAndFeel(themeClasses.get(0).newInstance());
            // Loop through themes and set LAF if its name matches the preference.
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

        // Organize themes into holders by the first word of their name.
        for (Class<IntelliJTheme.ThemeLaf> themeClass : themeClasses) {
            try {
                IntelliJTheme.ThemeLaf themeInstance = themeClass.newInstance();
                String key = themeInstance.getName().split(" ")[0];
                LaFHolder holder = new LaFHolder(key);
                int index = lafHolders.indexOf(holder);
                if (index >= 0) {
                    lafHolders.get(index).getThemes().add(themeInstance);
                } else {
                    holder.getThemes().add(themeInstance);
                    lafHolders.add(holder);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // Build menu structure for each holder
        for (LaFHolder holder : lafHolders) {
            JMenu holderMenu = new JMenu(holder.getIdentifier());
            JMenu darkMenu = new JMenu("Temné styly");
            JMenu lightMenu = new JMenu("Světlé styly");

            for (IntelliJTheme.ThemeLaf theme : holder.getThemes()) {
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(theme.getName());
                menuItem.addActionListener(e -> {
                    try {
                        UIManager.setLookAndFeel(theme);
                        FlatLaf.updateUI();
                        prefs.put(PREF_NAME, theme.getName());
                    } catch (UnsupportedLookAndFeelException ex) {
                        ex.printStackTrace();
                    }
                });
                group.add(menuItem);
                if (theme.getName().equals(prefs.get(PREF_NAME, "2898"))) {
                    menuItem.setSelected(true);
                }
                if (theme.isDark()) {
                    darkMenu.add(menuItem);
                } else {
                    lightMenu.add(menuItem);
                }
            }
            if (darkMenu.getItemCount() > 0) {
                holderMenu.add(darkMenu);
            }
            if (lightMenu.getItemCount() > 0) {
                holderMenu.add(lightMenu);
            }
            lafMenu.add(holderMenu);
        }
        return lafMenu;
    }

    /**
     * Adds a menu item to save the current application layout to a file.
     *
     * @param fileMenu the file menu to which the item is added
     */
    private void buildSaveLayoutMenuItem(JMenu fileMenu) {
        JMenuItem saveLayoutItem = new JMenuItem("Uložit rozložení");
        fileMenu.add(saveLayoutItem);
        saveLayoutItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showSaveDialog(mainFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                ApplicationLayout layout = DockingState.getApplicationLayout();
                try {
                    ApplicationLayoutXML.saveLayoutToFile(selectedFile, layout);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, "Chyba při ukládání");
                }
            }
        });
    }

    /**
     * Adds a menu item to load an application layout from a file.
     *
     * @param fileMenu the file menu to which the item is added
     */
    private void buildLoadLayoutMenuItem(JMenu fileMenu) {
        JMenuItem loadLayoutItem = new JMenuItem("Načíst rozložení ze souboru");
        fileMenu.add(loadLayoutItem);
        loadLayoutItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(mainFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                ApplicationLayout layout = null;
                try {
                    layout = ApplicationLayoutXML.loadLayoutFromFile(selectedFile);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (layout != null) {
                    DockingState.restoreApplicationLayout(layout);
                }
            }
        });
    }

    /**
     * Returns the view menu.
     *
     * @return the view {@link JMenu}
     */
    public JMenu getViewMenu() {
        return viewMenu;
    }
}
