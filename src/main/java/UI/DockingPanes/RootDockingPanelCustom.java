package UI.DockingPanes;


import ModernDocking.Dockable;
import ModernDocking.Docking;
import ModernDocking.DockingRegion;
import ModernDocking.RootDockingPanel;
import ModernDocking.internal.DockedSimplePanel;
import ModernDocking.internal.DockingInternal;
import ModernDocking.internal.DockingPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

// only class that should be used by clients
public class RootDockingPanelCustom extends RootDockingPanel {
    private Window window;

    private DockingPanel panel;

    private JPanel emptyPanel = new JPanel();

    private boolean pinningSupported = false;

    private int pinningLayer = JLayeredPane.MODAL_LAYER;

    // "toolbar" panels for unpinned dockables
    private DockableToolbarCostume southToolbar;
    private DockableToolbarCostume westToolbar;
    private DockableToolbarCostume eastToolbar;

    public RootDockingPanelCustom() {
        setLayout(new GridBagLayout());
    }

    public void init(){}

    public RootDockingPanelCustom(Window window) {
        setLayout(new GridBagLayout());

        this.window = window;

        if (window instanceof JFrame) {
            Docking.registerDockingPanel(this, (JFrame) window);
        }
        else {
            Docking.registerDockingPanel(this, (JDialog) window);
        }

        southToolbar = new DockableToolbarCostume(window, this, DockableToolbarCostume.Location.SOUTH);
        westToolbar = new DockableToolbarCostume(window, this, DockableToolbarCostume.Location.WEST);
        eastToolbar = new DockableToolbarCostume(window, this, DockableToolbarCostume.Location.EAST);
    }

    public void setWindow(Window window) {
        if (this.window != null) {
            Docking.deregisterDockingPanel(this.window);
        }
        this.window = window;

        if (window instanceof JFrame) {
            Docking.registerDockingPanel(this, (JFrame) window);
        }
        else {
            Docking.registerDockingPanel(this, (JDialog) window);
        }

        southToolbar = new DockableToolbarCostume(window, this, DockableToolbarCostume.Location.SOUTH);
        westToolbar = new DockableToolbarCostume(window, this, DockableToolbarCostume.Location.WEST);
        eastToolbar = new DockableToolbarCostume(window, this, DockableToolbarCostume.Location.EAST);
    }

    public Window getWindow() {
        return window;
    }

    public void setEmptyPanel(JPanel panel) {
        this.emptyPanel = panel;
    }

    public boolean isPinningSupported() {
        return pinningSupported;
    }

    public void setPinningSupported(boolean supported) {
        pinningSupported = supported;
    }

    public int getPinningLayer() {
        return pinningLayer;
    }

    public void setPinningLayer(int layer) {
        pinningLayer = layer;
    }

    public DockingPanel getPanel() {
        return panel;
    }

    public boolean isEmpty() {
        return panel == null;
    }

    public void setPanel(DockingPanel panel) {
        this.panel = panel;

        if (panel != null) {
            this.panel.setParent(this);

            createContents();
        }
    }

    private boolean removeExistingPanel() {
        remove(emptyPanel);

        if (panel != null) {
            remove(panel);
            panel = null;
            return true;
        }
        return false;
    }

    @Override
    public void removeNotify() {
        // if there is no Docking instance skip this code to prevent issues in GUI builders
        if (Docking.getInstance() != null) {
            Window rootWindow = (Window) SwingUtilities.getRoot(this);
            Docking.deregisterDockingPanel(rootWindow);
        }

        super.removeNotify();
    }

    @Override
    public void setParent(DockingPanel parent) {
    }

    @Override
    public void dock(Dockable dockable, DockingRegion region, double dividerProportion) {
        // pass docking to panel if it exists
        // panel does not exist, create new simple panel
        if (panel != null) {
            panel.dock(dockable, region, dividerProportion);
        }
        else {
            setPanel(new DockedSimplePanel(DockingInternal.getWrapper(dockable)));
            DockingInternal.getWrapper(dockable).setWindow(window);
        }
    }

    @Override
    public void undock(Dockable dockable) {
        if (westToolbar.hasDockable(dockable)) {
            westToolbar.removeDockable(dockable);
        }
        else if (eastToolbar.hasDockable(dockable)) {
            eastToolbar.removeDockable(dockable);
        }
        else if (southToolbar.hasDockable(dockable)) {
            southToolbar.removeDockable(dockable);
        }

        createContents();
    }

    @Override
    public void replaceChild(DockingPanel child, DockingPanel newChild) {
        if (panel == child) {
            setPanel(newChild);
        }
    }

    @Override
    public void removeChild(DockingPanel child) {
        if (child == panel) {
            if (removeExistingPanel()) {
                createContents();
            }
        }
    }

    public void setDockablePinned(Dockable dockable) {
        // if the dockable is currently unpinned, remove it from the toolbar, then adjust the toolbars
        if (westToolbar.hasDockable(dockable)) {
            westToolbar.removeDockable(dockable);

            dock(dockable, DockingRegion.WEST, 0.25f);
        }
        else if (eastToolbar.hasDockable(dockable)) {
            eastToolbar.removeDockable(dockable);

            dock(dockable, DockingRegion.EAST, 0.25f);
        }
        else if (southToolbar.hasDockable(dockable)) {
            southToolbar.removeDockable(dockable);

            dock(dockable, DockingRegion.SOUTH, 0.25f);
        }

        createContents();
    }

    // set a dockable to be unpinned at the given location
    public void setDockableUnpinned(Dockable dockable, DockableToolbarCostume.Location location) {
        switch (location) {
            case WEST: {
                westToolbar.addDockable(dockable);
                break;
            }
            case SOUTH: {
                southToolbar.addDockable(dockable);
                break;
            }
            case EAST: {
                eastToolbar.addDockable(dockable);
                break;
            }
        }

        createContents();
    }

    public List<String> unpinnedPersistentIDs(DockableToolbarCostume.Location location) {
        switch (location) {
            case WEST: return westToolbar.getPersistentIDs();
            case EAST: return eastToolbar.getPersistentIDs();
            case SOUTH: return southToolbar.getPersistentIDs();
        }
        return Collections.emptyList();
    }

    private void createContents() {
        removeAll();

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.VERTICAL;

        if (westToolbar.shouldDisplay()) {
            add(westToolbar, gbc);
            gbc.gridx++;
        }

        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        if (panel == null) {
            add(emptyPanel, gbc);
        }
        else {
            add(panel, gbc);
        }
        gbc.gridx++;

        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.VERTICAL;

        if (eastToolbar.shouldDisplay()) {
            add(eastToolbar, gbc);
        }

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        if (southToolbar.shouldDisplay()) {
            add(southToolbar, gbc);
        }

        revalidate();
        repaint();
    }

    public void hideUnpinnedPanels() {
        westToolbar.hideAll();
        southToolbar.hideAll();
        eastToolbar.hideAll();
    }

    public List<String> getWestUnpinnedToolbarIDs() {
        if (westToolbar == null) {
            return Collections.emptyList();
        }
        return westToolbar.getPersistentIDs();
    }

    public List<String> getEastUnpinnedToolbarIDs() {
        if (eastToolbar == null) {
            return Collections.emptyList();
        }
        return eastToolbar.getPersistentIDs();
    }

    public List<String> getSouthUnpinnedToolbarIDs() {
        if (southToolbar == null) {
            return Collections.emptyList();
        }
        return southToolbar.getPersistentIDs();
    }
}
