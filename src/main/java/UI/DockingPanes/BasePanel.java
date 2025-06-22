/*
MIT License

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

import ModernDocking.Dockable;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * An abstract base class that extends {@link JPanel} and implements the {@link Dockable} interface.
 * This panel serves as a base for custom dockable panels within the application with modern styling.
 */
public abstract class BasePanel extends JPanel implements Dockable {

    /**
     * A unique ID used for persisting and restoring the panel in the docking framework.
     */
    private final String persistentID;

    /**
     * The display title shown in the docking framework or panel header.
     */
    protected String title;

    /**
     * The icon associated with this panel, to be displayed in the docking framework if desired.
     */
    private final Icon icon;

    /**
     * Constructs a new {@code BasePanel} with the specified title, persistent ID, and icon.
     *
     * @param title        the textual title for this panel
     * @param persistentID the unique string used to identify this panel in the docking framework
     * @param icon         the icon to be displayed for this panel in the docking framework
     */
    public BasePanel(String title, String persistentID, Icon icon) {
        // Provide a BorderLayout by default
        super(new BorderLayout());
        ModernDockingCustomization.setupDockingTheme();
        this.title = title;
        this.persistentID = persistentID;
        this.icon = icon;

        // Apply modern styling
        initModernStyling();
    }

    /**
     * Initializes modern styling for the panel.
     */
    private void initModernStyling() {
        // Set modern background color
        setBackground(UIManager.getColor("Panel.background"));

        // Apply modern border with rounded corners
        setBorder(createModernBorder());

        // Set modern font
        Font defaultFont = UIManager.getFont("Panel.font");
        if (defaultFont != null) {
            setFont(defaultFont);
        }
    }

    /**
     * Creates a modern border with rounded corners and subtle shadow effect.
     */
    private AbstractBorder createModernBorder() {
        return new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Subtle border color using system colors
                Color borderColor = UIManager.getColor("Component.borderColor");
                if (borderColor == null) {
                    borderColor = UIManager.getColor("Panel.border");
                }
                if (borderColor == null) {
                    borderColor = new Color(220, 220, 220);
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

    /**
     * Returns the unique ID used for persistence in the docking framework.
     *
     * @return the persistent ID
     */
    @Override
    public String getPersistentID() {
        return persistentID;
    }

    /**
     * Returns the icon associated with this panel.
     *
     * @return the icon of this panel
     */
    @Override
    public Icon getIcon() {
        return icon;
    }

    /**
     * Specifies whether or not this panel can be pinned within the docking framework.
     * By default, this returns {@code true}.
     *
     * @return {@code true} if this panel can be pinned, otherwise {@code false}
     */
    @Override
    public boolean allowPinning() {
        return true;
    }

    /**
     * Gets the title of this panel.
     *
     * @return the panel title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this panel and updates UI if necessary.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
        repaint();
    }
}