package UI.DnDTabbedPane;

/**
 * Modified DnDTabbedPane.java with packet-aware drag and drop logic
 */
import packets.Interfaces.IPacket;

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

    private static final int LINE_WIDTH = 3;
    private static final String NAME = "TabTransferData";
    private final DataFlavor localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
    private static final GhostGlassPane sGlassPane = new GhostGlassPane();

    private boolean hasGhost = true;
    private boolean isDrawRect = false;
    private final Rectangle2D lineRect = new Rectangle2D.Double();
    private Color lineColor;
    private TabAcceptor acceptor = null;
    private PacketTypeChecker packetTypeChecker = null;

    ////////////////////////////////////////////////////////////////////////////////////
    //                             CONSTRUCTOR
    ////////////////////////////////////////////////////////////////////////////////////

    public DnDTabbedPane() {
        super();

        // Use system colors
        lineColor = UIManager.getColor("Component.accentColor");
        if (lineColor == null) lineColor = UIManager.getColor("List.selectionBackground");
        if (lineColor == null) lineColor = new Color(0, 100, 255);

        // Create drag source listener
        DragSourceListener dsl = new DragSourceListener() {
            @Override
            public void dragEnter(DragSourceDragEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            }

            @Override
            public void dragOver(DragSourceDragEvent e) {
                TabTransferData data = getTabTransferData(e);
                if (data == null || !isPacketMovable(data.getTabbedPane(), data.getTabIndex())) {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                } else {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
                }
            }

            @Override
            public void dropActionChanged(DragSourceDragEvent e) {}

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

            // Check if this packet type can be moved
            if (!isPacketMovable(DnDTabbedPane.this, dragTabIndex)) {
                return; // Don't start drag for fixed packets
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

        // Default acceptor
        acceptor = (aComponent, aIndex) -> true;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                        PACKET TYPE CHECKING INTERFACE
    ////////////////////////////////////////////////////////////////////////////////////

    public interface PacketTypeChecker {
        boolean isPH(JTabbedPane tabbedPane, int index);
        boolean isP0orP200(JTabbedPane tabbedPane, int index);
        boolean isP255(JTabbedPane tabbedPane, int index);
        boolean isAddPacketTab(JTabbedPane tabbedPane, int index);
    }

    public void setPacketTypeChecker(PacketTypeChecker checker) {
        this.packetTypeChecker = checker;
    }

    private boolean isPacketMovable(JTabbedPane tabbedPane, int index) {
        if (packetTypeChecker == null || index < 0 || index >= tabbedPane.getTabCount()) {
            return true;
        }

        // Fixed packets cannot be moved
        return !packetTypeChecker.isPH(tabbedPane, index) &&
                !packetTypeChecker.isP0orP200(tabbedPane, index) &&
                !packetTypeChecker.isP255(tabbedPane, index) &&
                !packetTypeChecker.isAddPacketTab(tabbedPane, index);
    }

    private int[] getValidDropRange(JTabbedPane tabbedPane) {
        if (packetTypeChecker == null) {
            return new int[]{0, tabbedPane.getTabCount()};
        }

        int minIndex = 0;
        int maxIndex = tabbedPane.getTabCount();

        // Find minimum index (after PH and P0/P200)
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (packetTypeChecker.isPH(tabbedPane, i) || packetTypeChecker.isP0orP200(tabbedPane, i)) {
                minIndex = i + 1;
            } else {
                break;
            }
        }

        // Find maximum index (before P255 and add packet tab)
        // **FIX: Ensure add packet tab stays at the bottom**
        for (int i = tabbedPane.getTabCount() - 1; i >= 0; i--) {
            if (packetTypeChecker.isP255(tabbedPane, i) || packetTypeChecker.isAddPacketTab(tabbedPane, i)) {
                maxIndex = i;
            } else {
                break;
            }
        }

        return new int[]{minIndex, maxIndex};
    }

    private boolean isValidDropIndex(JTabbedPane tabbedPane, int targetIndex) {
        if (packetTypeChecker == null) return true;
        int[] validRange = getValidDropRange(tabbedPane);
        return targetIndex >= validRange[0] && targetIndex <= validRange[1];
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                             PROPERTIES & ACCESSORS
    ////////////////////////////////////////////////////////////////////////////////////

    public TabAcceptor getAcceptor() { return acceptor; }
    public void setAcceptor(TabAcceptor value) { acceptor = value; }
    public void setPaintGhost(boolean flag) { hasGhost = flag; }
    public boolean hasGhost() { return hasGhost; }

    ////////////////////////////////////////////////////////////////////////////////////
    //                          DATA RETRIEVAL METHODS
    ////////////////////////////////////////////////////////////////////////////////////

    private TabTransferData getTabTransferData(DropTargetDropEvent event) {
        try {
            return (TabTransferData) event.getTransferable().getTransferData(localObjectFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private TabTransferData getTabTransferData(DropTargetDragEvent event) {
        try {
            return (TabTransferData) event.getTransferable().getTransferData(localObjectFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private TabTransferData getTabTransferData(DragSourceDragEvent event) {
        try {
            return (TabTransferData) event.getDragSourceContext().getTransferable().getTransferData(localObjectFlavor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                              TRANSFERABLE CLASSES
    ////////////////////////////////////////////////////////////////////////////////////

    class TabTransferable implements Transferable {
        private final TabTransferData data;

        public TabTransferable(DnDTabbedPane tabbedPane, int tabIndex) {
            data = new TabTransferData(tabbedPane, tabIndex);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) { return data; }

        @Override
        public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{localObjectFlavor}; }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return NAME.equals(flavor.getHumanPresentableName());
        }
    }

    static class TabTransferData {
        private DnDTabbedPane tabbedPane;
        private int tabIndex = -1;

        public TabTransferData() {}

        public TabTransferData(DnDTabbedPane aTabbedPane, int aTabIndex) {
            this.tabbedPane = aTabbedPane;
            this.tabIndex = aTabIndex;
        }

        public DnDTabbedPane getTabbedPane() { return tabbedPane; }
        public void setTabbedPane(DnDTabbedPane pane) { tabbedPane = pane; }
        public int getTabIndex() { return tabIndex; }
        public void setTabIndex(int index) { tabIndex = index; }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                             DROP TARGET LISTENER
    ////////////////////////////////////////////////////////////////////////////////////

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

            if (hasGhost()) {
                sGlassPane.setPoint(buildGhostLocation(e.getLocation()));
                sGlassPane.repaint();
            }
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent e) {}

        @Override
        public void dragExit(DropTargetEvent e) { isDrawRect = false; }

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

        public boolean isDragAcceptable(DropTargetDragEvent e) {
            Transferable t = e.getTransferable();
            if (t == null) return false;

            DataFlavor[] flavors = e.getCurrentDataFlavors();
            if (!t.isDataFlavorSupported(flavors[0])) return false;

            TabTransferData data = getTabTransferData(e);
            if (data == null) return false;

            // Check if the source packet can be moved
            if (!isPacketMovable(data.getTabbedPane(), data.getTabIndex())) return false;

            if (DnDTabbedPane.this == data.getTabbedPane() && data.getTabIndex() >= 0) return true;

            if (DnDTabbedPane.this != data.getTabbedPane() && acceptor != null) {
                return acceptor.isDropAcceptable(data.getTabbedPane(), data.getTabIndex());
            }
            return false;
        }

        private int getTargetTabIndex(Point a_point) {
            boolean isTopOrBottom = getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM;

            if (getTabCount() == 0) return 0;

            for (int i = 0; i < getTabCount(); i++) {
                Rectangle r = getBoundsAt(i);
                if (isTopOrBottom) {
                    r.setRect(r.x - r.width / 2, r.y, r.width, r.height);
                } else {
                    r.setRect(r.x, r.y - r.height / 2, r.width, r.height);
                }
                if (r.contains(a_point)) {
                    if (isValidDropIndex(DnDTabbedPane.this, i)) return i;
                }
            }

            // Check area after the last tab
            Rectangle r = getBoundsAt(getTabCount() - 1);
            if (isTopOrBottom) {
                int x = r.x + r.width / 2;
                r.setRect(x, r.y, getWidth() - x, r.height);
            } else {
                int y = r.y + r.height / 2;
                r.setRect(r.x, y, r.width, getHeight() - y);
            }

            if (r.contains(a_point)) {
                int targetIndex = getTabCount();
                if (isValidDropIndex(DnDTabbedPane.this, targetIndex)) return targetIndex;
            }

            return -1; // Invalid drop location
        }

        public boolean isDropAcceptable(DropTargetDropEvent e) {
            Transferable t = e.getTransferable();
            if (t == null) return false;

            DataFlavor[] flavors = e.getCurrentDataFlavors();
            if (!t.isDataFlavorSupported(flavors[0])) return false;

            TabTransferData data = getTabTransferData(e);
            if (data == null) return false;

            if (!isPacketMovable(data.getTabbedPane(), data.getTabIndex())) return false;

            int targetIndex = getTargetTabIndex(e.getLocation());
            if (!isValidDropIndex(DnDTabbedPane.this, targetIndex)) return false;

            if (DnDTabbedPane.this == data.getTabbedPane() && data.getTabIndex() >= 0) return true;

            if (DnDTabbedPane.this != data.getTabbedPane() && acceptor != null) {
                return acceptor.isDropAcceptable(data.getTabbedPane(), data.getTabIndex());
            }
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                          TAB REORDERING/TRANSFER
    ////////////////////////////////////////////////////////////////////////////////////

    private void convertTab(TabTransferData data, int targetIndex) {
        if (data == null) return;

        DnDTabbedPane source = data.getTabbedPane();
        int sourceIndex = data.getTabIndex();

        if (sourceIndex < 0) return;
        if (!isPacketMovable(source, sourceIndex)) return;
        if (!isValidDropIndex(this, targetIndex)) return;

        Component tabContent = source.getComponentAt(sourceIndex);
        String tabTitle = source.getTitleAt(sourceIndex);
        Component customHeader = source.getTabComponentAt(sourceIndex);

        // If dropping into a different tabbed pane
        if (this != source) {
            source.remove(sourceIndex);
            if (targetIndex >= getTabCount()) {
                addTab(tabTitle, tabContent);
                targetIndex = getTabCount() - 1;
            } else {
                if (targetIndex < 0) targetIndex = 0;
                insertTab(tabTitle, null, tabContent, null, targetIndex);
            }
            if (customHeader != null) {
                setTabComponentAt(targetIndex, customHeader);
            }
            setSelectedComponent(tabContent);
            return;
        }

        // Reordering within the same pane
        if (targetIndex < 0 || sourceIndex == targetIndex) return;

        // Ensure we don't violate ordering constraints
        int[] validRange = getValidDropRange(this);
        targetIndex = Math.max(validRange[0], Math.min(validRange[1], targetIndex));

        // Update the model first
        updatePacketModel(sourceIndex, targetIndex);

        // Then update the UI
        if (targetIndex >= getTabCount()) {
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

    private void updatePacketModel(int fromIndex, int toIndex) {
        DefaultListModel<IPacket> packetModel = (DefaultListModel<IPacket>) getClientProperty("packetModel");
        if (packetModel != null && fromIndex >= 0 && fromIndex < packetModel.size()
                && toIndex >= 0 && toIndex <= packetModel.size()) {

            IPacket movingItem = packetModel.get(fromIndex);
            packetModel.removeElementAt(fromIndex);

            // Adjust target index if necessary
            if (toIndex > fromIndex) {
                toIndex = toIndex - 1;
            }
            if (toIndex >= packetModel.size()) {
                packetModel.addElement(movingItem);
            } else {
                packetModel.add(toIndex, movingItem);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                      DROP RECTANGLE INITIALIZATION
    ////////////////////////////////////////////////////////////////////////////////////

    private void initTargetLeftRightLine(int nextIndex, TabTransferData data) {
        if (nextIndex < 0 || !isValidDropIndex(this, nextIndex)) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
            return;
        }

        if ((data.getTabbedPane() == this) && (data.getTabIndex() == nextIndex || nextIndex - data.getTabIndex() == 1)) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (getTabCount() == 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (nextIndex == 0) {
            Rectangle rect = getBoundsAt(0);
            lineRect.setRect(-LINE_WIDTH / 2.0, rect.y, LINE_WIDTH, rect.height);
            isDrawRect = true;
        } else if (nextIndex >= getTabCount()) {
            Rectangle rect = getBoundsAt(getTabCount() - 1);
            lineRect.setRect(rect.x + rect.width - LINE_WIDTH / 2.0, rect.y, LINE_WIDTH, rect.height);
            isDrawRect = true;
        } else {
            Rectangle rect = getBoundsAt(nextIndex - 1);
            lineRect.setRect(rect.x + rect.width - LINE_WIDTH / 2.0, rect.y, LINE_WIDTH, rect.height);
            isDrawRect = true;
        }
    }

    private void initTargetTopBottomLine(int nextIndex, TabTransferData data) {
        if (nextIndex < 0 || !isValidDropIndex(this, nextIndex)) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
            return;
        }

        if ((data.getTabbedPane() == this) && (data.getTabIndex() == nextIndex || nextIndex - data.getTabIndex() == 1)) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (getTabCount() == 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (nextIndex >= getTabCount()) {
            Rectangle rect = getBoundsAt(getTabCount() - 1);
            lineRect.setRect(rect.x, rect.y + rect.height - LINE_WIDTH / 2.0, rect.width, LINE_WIDTH);
            isDrawRect = true;
        } else if (nextIndex == 0) {
            Rectangle rect = getBoundsAt(0);
            lineRect.setRect(rect.x, -LINE_WIDTH / 2.0, rect.width, LINE_WIDTH);
            isDrawRect = true;
        } else {
            Rectangle rect = getBoundsAt(nextIndex - 1);
            lineRect.setRect(rect.x, rect.y + rect.height - LINE_WIDTH / 2.0, rect.width, LINE_WIDTH);
            isDrawRect = true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                       GHOST IMAGE SUPPORT
    ////////////////////////////////////////////////////////////////////////////////////

    private void initGlassPane(Component c, Point tabPoint, int tabIndex) {
        getRootPane().setGlassPane(sGlassPane);

        if (hasGhost()) {
            Rectangle rect = getBoundsAt(tabIndex);
            BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            c.paint(g);
            image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
            sGlassPane.setImage(image);
        }

        sGlassPane.setPoint(buildGhostLocation(tabPoint));
        sGlassPane.setVisible(true);
    }

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
        }

        return SwingUtilities.convertPoint(DnDTabbedPane.this, result, sGlassPane);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                              PAINT OVERRIDE
    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void paintComponent(Graphics g) {
        try {
            super.paintComponent(g);
        } catch (Exception e) {
            // Ignore painting exceptions
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

    public interface TabAcceptor {
        boolean isDropAcceptable(DnDTabbedPane aComponent, int aIndex);
    }
}