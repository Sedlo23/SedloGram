package UI.DnDTabbedPane;

/**
 * Modified DnDTabbedPane.java
 * <p>
 * Originally from:
 * <ul>
 *   <li>http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html</li>
 *   <li>Written by Terai Atsuhiro.</li>
 * </ul>
 * Modified by eed3si9n to allow tabs to be transferred from one pane to another.
 *
 * <p>
 * {@code DnDTabbedPane} is a custom {@link JTabbedPane} supporting drag-and-drop
 * for reordering tabs within a single pane or transferring tabs between different
 * {@code DnDTabbedPane} instances.
 *
 * <p>
 * Usage typically involves:
 * <pre>{@code
 * DnDTabbedPane tabbedPane1 = new DnDTabbedPane();
 * DnDTabbedPane tabbedPane2 = new DnDTabbedPane();
 * // ...
 * // Add tabs and content to these panes
 * // The user can now drag tabs between pane1 and pane2, if desired.
 * }</pre>
 */
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class DnDTabbedPane extends JTabbedPane {
    private static final long serialVersionUID = 1L;

    ////////////////////////////////////////////////////////////////////////////////////
    //                             CONSTANTS & FIELDS
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Width of the highlight rectangle that indicates the drop location.
     */
    private static final int LINE_WIDTH = 3;

    /**
     * Internal, local name used to identify the data flavor for transferring tabs.
     */
    private static final String NAME = "TabTransferData";

    /**
     * The {@link DataFlavor} used to transfer tab data locally.
     */
    private final DataFlavor localObjectFlavor =
            new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);

    /**
     * A shared glass pane instance used for rendering a "ghost" image when dragging tabs.
     * The same {@code GhostGlassPane} can be reused for all {@code DnDTabbedPane} instances.
     */
    private static final GhostGlassPane sGlassPane = new GhostGlassPane();

    /**
     * Whether or not to paint the ghost image while dragging tabs.
     */
    private boolean hasGhost = true;

    /**
     * Whether or not we need to draw the drop location rectangle.
     */
    private boolean isDrawRect = false;

    /**
     * The rectangle that indicates where the tab would be dropped.
     */
    private final Rectangle2D lineRect = new Rectangle2D.Double();

    /**
     * The color of the drop location rectangle.
     */
    private final Color lineColor = new Color(0, 100, 255);

    /**
     * A functional interface for controlling or validating dropping between different tabbed panes.
     */
    private TabAcceptor acceptor = null;

    ////////////////////////////////////////////////////////////////////////////////////
    //                             CONSTRUCTOR
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new {@code DnDTabbedPane}, enabling drag-and-drop capabilities for its tabs.
     */
    public DnDTabbedPane() {
        super();

        // Create drag source listener
        DragSourceListener dsl = new DragSourceListener() {
            @Override
            public void dragEnter(DragSourceDragEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            }

            @Override
            public void dragOver(DragSourceDragEvent e) {
                // If the data is recognized, show "move drop" cursor; otherwise "move no drop."
                TabTransferData data = getTabTransferData(e);
                if (data == null) {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                } else {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
                }
            }

            @Override
            public void dropActionChanged(DragSourceDragEvent e) {
                // Not used
            }

            @Override
            public void dragExit(DragSourceEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                lineRect.setRect(0, 0, 0, 0);
                isDrawRect = false;
                sGlassPane.setPoint(new Point(-1000, -1000));
                sGlassPane.repaint();
            }

            @Override
            public void dragDropEnd(DragSourceDropEvent e) {
                isDrawRect = false;
                lineRect.setRect(0, 0, 0, 0);
                if (hasGhost()) {
                    sGlassPane.setVisible(false);
                    sGlassPane.setImage(null);
                }
            }
        };

        // Create drag gesture listener
        DragGestureListener dgl = e -> {
            Point dragOrigin = e.getDragOrigin();
            int dragTabIndex = indexAtLocation(dragOrigin.x, dragOrigin.y);
            if (dragTabIndex < 0) {
                return; // no valid tab under the mouse
            }

            initGlassPane(e.getComponent(), dragOrigin, dragTabIndex);
            try {
                e.startDrag(
                        DragSource.DefaultMoveDrop,
                        new TabTransferable(DnDTabbedPane.this, dragTabIndex),
                        dsl
                );
            } catch (InvalidDnDOperationException idoe) {
                idoe.printStackTrace();
            }
        };

        // Setup drop target
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);

        // Setup drag source
        new DragSource().createDefaultDragGestureRecognizer(
                this, DnDConstants.ACTION_COPY_OR_MOVE, dgl
        );

        // Default acceptor always allows the drop
        acceptor = (aComponent, aIndex) -> true;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                             PROPERTIES & ACCESSORS
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the current {@link TabAcceptor}, which decides whether drops are acceptable
     * between different {@code DnDTabbedPane} instances.
     *
     * @return the current {@code TabAcceptor}
     */
    public TabAcceptor getAcceptor() {
        return acceptor;
    }

    /**
     * Sets a new {@link TabAcceptor} to control or validate cross-pane tab drops.
     *
     * @param value the new {@code TabAcceptor}
     */
    public void setAcceptor(TabAcceptor value) {
        acceptor = value;
    }

    /**
     * Enable or disable the ghosted image effect while dragging tabs.
     *
     * @param flag {@code true} to enable ghost image rendering, {@code false} otherwise
     */
    public void setPaintGhost(boolean flag) {
        hasGhost = flag;
    }

    /**
     * Returns whether or not the ghost image is rendered while dragging.
     *
     * @return {@code true} if ghost image is rendered; {@code false} otherwise
     */
    public boolean hasGhost() {
        return hasGhost;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                          DATA RETRIEVAL METHODS
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieves the {@link TabTransferData} from a {@link DropTargetDropEvent}.
     *
     * @param event the {@code DropTargetDropEvent}
     * @return the {@code TabTransferData}, or {@code null} if unavailable
     */
    private TabTransferData getTabTransferData(DropTargetDropEvent event) {
        try {
            return (TabTransferData)
                    event.getTransferable().getTransferData(localObjectFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the {@link TabTransferData} from a {@link DropTargetDragEvent}.
     *
     * @param event the {@code DropTargetDragEvent}
     * @return the {@code TabTransferData}, or {@code null} if unavailable
     */
    private TabTransferData getTabTransferData(DropTargetDragEvent event) {
        try {
            return (TabTransferData)
                    event.getTransferable().getTransferData(localObjectFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the {@link TabTransferData} from a {@link DragSourceDragEvent}.
     *
     * @param event the {@code DragSourceDragEvent}
     * @return the {@code TabTransferData}, or {@code null} if unavailable
     */
    private TabTransferData getTabTransferData(DragSourceDragEvent event) {
        try {
            return (TabTransferData)
                    event.getDragSourceContext().getTransferable().getTransferData(localObjectFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                              TRANSFERABLE CLASSES
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * A {@link Transferable} that carries the {@link TabTransferData} for the dragged tab.
     */
    class TabTransferable implements Transferable {
        private final TabTransferData data;

        /**
         * Creates a {@code TabTransferable} carrying data about the tab being dragged.
         *
         * @param tabbedPane the originating {@code DnDTabbedPane}
         * @param tabIndex   the index of the tab being dragged
         */
        public TabTransferable(DnDTabbedPane tabbedPane, int tabIndex) {
            data = new TabTransferData(tabbedPane, tabIndex);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) {
            return data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{localObjectFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return NAME.equals(flavor.getHumanPresentableName());
        }
    }

    /**
     * A simple data holder that records which {@link DnDTabbedPane} and which tab index
     * is involved in a drag operation.
     */
    static class TabTransferData {
        private DnDTabbedPane tabbedPane;
        private int tabIndex = -1;

        /** Default constructor. */
        public TabTransferData() {
            // For serialization or similar usage
        }

        /**
         * Constructs a data object describing the origin of a dragged tab.
         *
         * @param aTabbedPane the {@code DnDTabbedPane} containing the dragged tab
         * @param aTabIndex   the index of the dragged tab
         */
        public TabTransferData(DnDTabbedPane aTabbedPane, int aTabIndex) {
            this.tabbedPane = aTabbedPane;
            this.tabIndex = aTabIndex;
        }

        public DnDTabbedPane getTabbedPane() {
            return tabbedPane;
        }

        public void setTabbedPane(DnDTabbedPane pane) {
            tabbedPane = pane;
        }

        public int getTabIndex() {
            return tabIndex;
        }

        public void setTabIndex(int index) {
            tabIndex = index;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                             DROP TARGET LISTENER
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * A custom drop target listener that handles the visual feedback (drop location rectangle)
     * and executes the actual reordering or tab transfer on a successful drop.
     */
    class CDropTargetListener implements DropTargetListener {
        @Override
        public void dragEnter(DropTargetDragEvent e) {
            if (isDragAcceptable(e)) {
                e.acceptDrag(e.getDropAction());
            } else {
                e.rejectDrag();
            }
        }

        @Override
        public void dragOver(DropTargetDragEvent e) {
            TabTransferData data = getTabTransferData(e);

            if (getTabPlacement() == TOP || getTabPlacement() == BOTTOM) {
                initTargetLeftRightLine(getTargetTabIndex(e.getLocation()), data);
            } else {
                initTargetTopBottomLine(getTargetTabIndex(e.getLocation()), data);
            }
            repaint();

            // If ghosting is enabled, move the ghost image
            if (hasGhost()) {
                sGlassPane.setPoint(buildGhostLocation(e.getLocation()));
                sGlassPane.repaint();
            }
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent e) {
            // Not used
        }

        @Override
        public void dragExit(DropTargetEvent e) {
            isDrawRect = false;
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            if (isDropAcceptable(e)) {
                convertTab(getTabTransferData(e), getTargetTabIndex(e.getLocation()));
                e.dropComplete(true);
            } else {
                e.dropComplete(false);
            }
            isDrawRect = false;
            repaint();
        }

        /**
         * Checks if the current drag operation is acceptable.
         *
         * @param e the {@link DropTargetDragEvent}
         * @return {@code true} if the data flavor is supported and the source tab index is valid
         */
        public boolean isDragAcceptable(DropTargetDragEvent e) {
            Transferable t = e.getTransferable();
            if (t == null) {
                return false;
            }

            DataFlavor[] flavors = e.getCurrentDataFlavors();
            if (!t.isDataFlavorSupported(flavors[0])) {
                return false;
            }

            TabTransferData data = getTabTransferData(e);
            if (data == null) {
                return false;
            }

            // If same tabbed pane, must have a valid tab index
            if (DnDTabbedPane.this == data.getTabbedPane() && data.getTabIndex() >= 0) {
                return true;
            }

            // For different tabbed panes, defer to the acceptor
            if (DnDTabbedPane.this != data.getTabbedPane() && acceptor != null) {
                return acceptor.isDropAcceptable(data.getTabbedPane(), data.getTabIndex());
            }
            return false;
        }

        private int getTargetTabIndex(Point a_point) {
            boolean isTopOrBottom = getTabPlacement() == JTabbedPane.TOP
                    || getTabPlacement() == JTabbedPane.BOTTOM;

            // if the pane is empty, the target index is always zero.
            if (getTabCount() == 0) {
                return 0;
            }

            for (int i = 0; i < getTabCount(); i++) {
                Rectangle r = getBoundsAt(i);
                if (isTopOrBottom) {
                    r.setRect(r.x - r.width / 2, r.y, r.width, r.height);
                } else {
                    r.setRect(r.x, r.y - r.height / 2, r.width, r.height);
                }
                if (r.contains(a_point)) {
                    return i;
                }
            }

            // check the area after the last tab
            Rectangle r = getBoundsAt(getTabCount() - 1);
            if (isTopOrBottom) {
                int x = r.x + r.width / 2;
                r.setRect(x, r.y, getWidth() - x, r.height);
            } else {
                int y = r.y + r.height / 2;
                r.setRect(r.x, y, r.width, getHeight() - y);
            }

            return r.contains(a_point) ? getTabCount() : -1;
        }


        /**
         * Checks if the drop operation is acceptable at the moment of dropping.
         *
         * @param e the {@link DropTargetDropEvent}
         * @return {@code true} if the data flavor is supported and the source tab index is valid
         */
        public boolean isDropAcceptable(DropTargetDropEvent e) {
            Transferable t = e.getTransferable();
            if (t == null) {
                return false;
            }

            DataFlavor[] flavors = e.getCurrentDataFlavors();
            if (!t.isDataFlavorSupported(flavors[0])) {
                return false;
            }

            TabTransferData data = getTabTransferData(e);
            if (data == null) {
                return false;
            }

            if (DnDTabbedPane.this == data.getTabbedPane() && data.getTabIndex() >= 0) {
                return true;
            }

            if (DnDTabbedPane.this != data.getTabbedPane() && acceptor != null) {
                return acceptor.isDropAcceptable(data.getTabbedPane(), data.getTabIndex());
            }
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                          TAB REORDERING/TRANSFER
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Converts (transfers) a tab from the source tabbed pane to this tabbed pane,
     * inserting at the specified target index.
     *
     * @param data        the transfer data describing the source pane and tab index
     * @param targetIndex the index at which the tab should be placed in this pane
     */
    private void convertTab(TabTransferData data, int targetIndex) {
        if (data == null) {
            return;
        }

        DnDTabbedPane source = data.getTabbedPane();
        int sourceIndex = data.getTabIndex();

        if (sourceIndex < 0) {
            return;
        }

        Component tabContent = source.getComponentAt(sourceIndex);
        String tabTitle = source.getTitleAt(sourceIndex);
        Component customHeader = source.getTabComponentAt(sourceIndex);

        // If dropping into a different tabbed pane
        if (this != source) {
            source.remove(sourceIndex);
            if (targetIndex == getTabCount()) {
                addTab(tabTitle, tabContent);
            } else {
                if (targetIndex < 0) {
                    targetIndex = 0;
                }
                insertTab(tabTitle, null, tabContent, null, targetIndex);
            }
            if (customHeader != null) {
                setTabComponentAt(targetIndex, customHeader);
            }
            setSelectedComponent(tabContent);
            return;
        }

        // Reordering within the same pane
        if (targetIndex < 0 || sourceIndex == targetIndex) {
            return;
        }
        if (targetIndex == getTabCount()) {
            source.remove(sourceIndex);
            addTab(tabTitle, tabContent);
            if (customHeader != null) {
                setTabComponentAt(getTabCount() - 1, customHeader);
            }
            setSelectedIndex(getTabCount() - 1);
        } else if (sourceIndex > targetIndex) {
            source.remove(sourceIndex);
            insertTab(tabTitle, null, tabContent, null, targetIndex);
            if (customHeader != null) {
                setTabComponentAt(targetIndex, customHeader);
            }
            setSelectedIndex(targetIndex);
        } else {
            source.remove(sourceIndex);
            insertTab(tabTitle, null, tabContent, null, targetIndex - 1);
            if (customHeader != null) {
                setTabComponentAt(targetIndex - 1, customHeader);
            }
            setSelectedIndex(targetIndex - 1);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                      DROP RECTANGLE INITIALIZATION
    ////////////////////////////////////////////////////////////////////////////////////

    private void initTargetLeftRightLine(int nextIndex, TabTransferData data) {
        if (nextIndex < 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
            return;
        }

        // If dragging within the same pane and dropping "on the same spot," do nothing
        if ((data.getTabbedPane() == this)
                && (data.getTabIndex() == nextIndex
                || nextIndex - data.getTabIndex() == 1)) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (getTabCount() == 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (nextIndex == 0) {
            Rectangle rect = getBoundsAt(0);
            lineRect.setRect(-LINE_WIDTH / 2.0, rect.y, LINE_WIDTH, rect.height);
            isDrawRect = true;
        } else if (nextIndex == getTabCount()) {
            Rectangle rect = getBoundsAt(getTabCount() - 1);
            lineRect.setRect(rect.x + rect.width - LINE_WIDTH / 2.0, rect.y,
                    LINE_WIDTH, rect.height);
            isDrawRect = true;
        } else {
            Rectangle rect = getBoundsAt(nextIndex - 1);
            lineRect.setRect(rect.x + rect.width - LINE_WIDTH / 2.0, rect.y,
                    LINE_WIDTH, rect.height);
            isDrawRect = true;
        }
    }

    private void initTargetTopBottomLine(int nextIndex, TabTransferData data) {
        if (nextIndex < 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
            return;
        }

        if ((data.getTabbedPane() == this)
                && (data.getTabIndex() == nextIndex
                || nextIndex - data.getTabIndex() == 1)) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (getTabCount() == 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (nextIndex == getTabCount()) {
            Rectangle rect = getBoundsAt(getTabCount() - 1);
            lineRect.setRect(rect.x, rect.y + rect.height - LINE_WIDTH / 2.0,
                    rect.width, LINE_WIDTH);
            isDrawRect = true;
        } else if (nextIndex == 0) {
            Rectangle rect = getBoundsAt(0);
            lineRect.setRect(rect.x, -LINE_WIDTH / 2.0,
                    rect.width, LINE_WIDTH);
            isDrawRect = true;
        } else {
            Rectangle rect = getBoundsAt(nextIndex - 1);
            lineRect.setRect(rect.x, rect.y + rect.height - LINE_WIDTH / 2.0,
                    rect.width, LINE_WIDTH);
            isDrawRect = true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                       GHOST IMAGE SUPPORT
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Configures the glass pane for rendering the ghost image of the dragged tab.
     *
     * @param c           the component being dragged (this {@code JTabbedPane})
     * @param tabPoint    the initial drag point
     * @param tabIndex    the index of the tab being dragged
     */
    private void initGlassPane(Component c, Point tabPoint, int tabIndex) {
        getRootPane().setGlassPane(sGlassPane);

        if (hasGhost()) {
            Rectangle rect = getBoundsAt(tabIndex);
            BufferedImage image = new BufferedImage(
                    c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB
            );
            Graphics g = image.getGraphics();
            c.paint(g);
            // Crop out the bounding rectangle of just the tab
            image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
            sGlassPane.setImage(image);
        }

        sGlassPane.setPoint(buildGhostLocation(tabPoint));
        sGlassPane.setVisible(true);
    }

    /**
     * Computes the on-screen location for the ghost image, based on the current orientation.
     *
     * @param location the current mouse location in this tabbed pane's coordinate space
     * @return a location point in the glass pane's coordinate space
     */
    private Point buildGhostLocation(Point location) {
        Point result = new Point(location);

        switch (getTabPlacement()) {
            case TOP -> {
                result.y = 1;
                result.x -= sGlassPane.getGhostWidth() / 2;
            }
            case BOTTOM -> {
                result.y = getHeight() - 1 - sGlassPane.getGhostHeight();
                result.x -= sGlassPane.getGhostWidth() / 2;
            }
            case LEFT -> {
                result.x = 1;
                result.y -= sGlassPane.getGhostHeight() / 2;
            }
            case RIGHT -> {
                result.x = getWidth() - 1 - sGlassPane.getGhostWidth();
                result.y -= sGlassPane.getGhostHeight() / 2;
            }
            default -> {
                // No-op
            }
        }

        return SwingUtilities.convertPoint(DnDTabbedPane.this, result, sGlassPane);
    }

    /**
     * Returns a bounding rectangle that encloses the entire tab area, used to verify if
     * a drag is over the tab area or not.
     *
     * @return the bounding rectangle for the tab area
     */
    private Rectangle getTabAreaBound() {
        if (getTabCount() == 0) {
            return new Rectangle();
        }
        Rectangle lastTab = getUI().getTabBounds(this, getTabCount() - 1);
        return new Rectangle(0, 0, getWidth(), lastTab.y + lastTab.height);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                              PAINT OVERRIDE
    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void paintComponent(Graphics g) {
       try
       {
           super.paintComponent(g);
       }
       catch (Exception e)
       {

       }


        if (isDrawRect) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(lineColor);
            g2.fill(lineRect);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                               UTILITY INTERFACES
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Functional interface to decide if dropping a tab from {@code aComponent} at index
     * {@code aIndex} is acceptable into this {@code DnDTabbedPane}.
     */
    public interface TabAcceptor {
        boolean isDropAcceptable(DnDTabbedPane aComponent, int aIndex);
    }
}
