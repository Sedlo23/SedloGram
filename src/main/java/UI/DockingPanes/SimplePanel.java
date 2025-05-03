/*
Copyright (c) 2022 Andrew Auclair

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package UI.DockingPanes;

import ModernDocking.Docking;
import ModernDocking.ui.DockingHeaderUI;
import ModernDocking.ui.HeaderController;
import ModernDocking.ui.HeaderModel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;

/**
 * A simple dockable panel used in the main dock of the application.
 * <p>
 * {@code SimplePanel} extends {@link BasePanel} and registers itself with the docking
 * framework. It supports basic options such as floating, closing, and minimizing.
 * It also provides a way to add extra options via a popup menu.
 */
public class SimplePanel extends BasePanel {

    /** Flag to indicate whether this dock should be limited to the root container. */
    public boolean limitToRoot = false;

    /** The panel's icon. */
    private Icon icon;

    /** A default color, currently not used beyond initialization. */
    private final Color color = Color.BLACK;

    /** Text to display on the tab. */
    private String tabText = "";

    /** Additional options to be added to the dock's header options menu. */
    private ArrayList<JMenu> moreOptions = new ArrayList<>();

    /**
     * Constructs a new {@code SimplePanel} with the specified title, persistent ID, icon,
     * and additional menu options.
     *
     * @param title         the title of the panel
     * @param persistentID  the persistent identifier used by the docking framework
     * @param icon          the icon to display for this panel
     * @param jMenuItems    additional menu items for extra options
     */
    public SimplePanel(String title, String persistentID, Icon icon, ArrayList<JMenu> jMenuItems) {
        super(title, persistentID, icon);
        this.icon = icon;
        this.moreOptions = jMenuItems;
        Docking.registerDockable(this);
    }

    /**
     * Constructs a new {@code SimplePanel} with the specified title, persistent ID, and icon.
     * Additional menu options will be empty.
     *
     * @param title         the title of the panel
     * @param persistentID  the persistent identifier
     * @param icon          the icon to display for this panel
     */
    public SimplePanel(String title, String persistentID, Icon icon) {
        this(title, persistentID, icon, new ArrayList<>());
    }

    /**
     * Returns the popup menu associated with this dockable panel.
     *
     * @return the {@link JPopupMenu} for this panel
     */
    public JPopupMenu jPopupMenu() {
        return super.getComponentPopupMenu();
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public String getTabText() {
        return title;
    }

    /**
     * Sets the panel's title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean isFloatingAllowed() {
        return true;
    }

    @Override
    public boolean canBeClosed() {
        return true;
    }

    @Override
    public boolean allowMinMax() {
        return true;
    }

    @Override
    public boolean shouldLimitToRoot() {
        return limitToRoot;
    }

    /**
     * Sets additional options for this panel.
     *
     * @param options the menu items to add as extra options
     */
    public void setMoreOptions(ArrayList<JMenu> options) {
        this.moreOptions = options;
    }

    @Override
    public void addMoreOptions(JPopupMenu menu) {
        for (JMenu item : moreOptions) {
            menu.add(item);
        }
    }

    @Override
    public void setBorder(Border border) {
        // Optionally, set a custom border.
        // super.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    }

    /**
     * Creates the custom header UI for this dockable panel.
     * <p>
     * This implementation returns a {@link HeaderCustomUI} that overrides the background
     * to use the default panel background.
     *
     * @param headerController the header controller for dock actions
     * @param headerModel      the header model containing display data
     * @return the constructed {@link DockingHeaderUI}
     */
    @Override
    public DockingHeaderUI createHeaderUI(HeaderController headerController, HeaderModel headerModel) {
        HeaderCustomUI headerUI = new HeaderCustomUI(headerController, headerModel) {
            @Override
            public void setBackground(Color bg) {
                // Use the default background color from a new panel as a fallback.
                super.setBackground(new JPanel().getBackground());
            }
        };
        return headerUI;
    }
}
