package UI.DnDTabbedPane;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A transparent glass pane that renders a semi-transparent "ghost" image during drag-and-drop operations.
 * This is typically used to provide visual feedback by displaying an image of the dragged component.
 */
class GhostGlassPane extends JPanel {

    private static final long serialVersionUID = 1L;

    /** The composite used to apply transparency to the ghost image. */
    private final AlphaComposite composite;

    /** The current location (in the glass pane's coordinate space) where the ghost image is rendered. */
    private final Point location = new Point(0, 0);

    /** The ghost image to be rendered during drag operations. */
    private BufferedImage draggingGhost;

    /**
     * Constructs a new {@code GhostGlassPane} with 70% opacity for the ghost image.
     */
    public GhostGlassPane() {
        setOpaque(false);
        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
    }

    /**
     * Sets the ghost image to be rendered.
     *
     * @param ghostImage the {@link BufferedImage} representing the ghost image
     */
    public void setImage(BufferedImage ghostImage) {
        draggingGhost = ghostImage;
    }

    /**
     * Updates the location at which the ghost image will be rendered.
     *
     * @param newLocation the new {@link Point} location in the glass pane's coordinate space
     */
    public void setPoint(Point newLocation) {
        location.setLocation(newLocation);
    }

    /**
     * Returns the width of the ghost image.
     *
     * @return the width of the ghost image, or 0 if no image is set
     */
    public int getGhostWidth() {
        return draggingGhost != null ? draggingGhost.getWidth(this) : 0;
    }

    /**
     * Returns the height of the ghost image.
     *
     * @return the height of the ghost image, or 0 if no image is set
     */
    public int getGhostHeight() {
        return draggingGhost != null ? draggingGhost.getHeight(this) : 0;
    }

    /**
     * Paints the ghost image at the specified location with the configured transparency.
     *
     * @param g the {@link Graphics} context used for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (draggingGhost == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setComposite(composite);
        g2.drawImage(draggingGhost, location.x, location.y, this);
    }
}
