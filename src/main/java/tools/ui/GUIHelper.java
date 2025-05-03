package tools.ui;

import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import packets.Interfaces.IPacket;
import packets.TrackToTrain.*;

import org.jfree.chart.StandardChartTheme;
import tools.crypto.ArithmeticalFunctions;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.ShortLookupTable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * A utility class that provides various GUI-related helper methods,
 * including setting up charts, handling images/icons, customizing Swing components,
 * and constructing certain TrackToTrain packet lists.
 */
public class GUIHelper {

    ////////////////////////////////////////////////////////////////////////////////////////
    //                               CONSTANTS & FIELDS
    ////////////////////////////////////////////////////////////////////////////////////////

    public static final int DEFAULT_ICON_SIZE = 24;
    public static final int WIDTH = 50;
    public static final int HEIGHT = 18;
    public static final int ICON_SIZE = 30;
    public static final String DEFAULT_ICON_TYPE = ".png";
    private static final List<Packet> PACKET_LIST = List.of(
            new PH(),
            new P0(),
            new P2(),
            new P3(),
            new P3_11(),
            new P5(),
            new P6(),
            new P12(),
            new P16(),
            new P21(),
            new P27(),
            new P41(),
            new P42(),
            new P45(),
            new P46(),
            new P65(),
            new P66(),
            new P67(),
            new P68(),
            new P72(),
            new P72_11(),
            new P79(),
            new P79_11(),
            new P80(),
            new P88(),
            new P132(),
            new P135(),
            new P137(),
            new P141(),
            new P145(),
            new P180(),
            new P181(),
            new P200(),
            new P203(),
            new P254()
    );

    ////////////////////////////////////////////////////////////////////////////////////////
    //                               IMAGE & ICON METHODS
    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Saves a given {@link BufferedImage} to the local file system as "QRCode.png".
     *
     * @param image the QR code image to save.
     * @throws IOException if an error occurs during write.
     */
    public static void saveQRCodeAsPNG(BufferedImage image) throws IOException {
        File outputfile = new File("QRCode.png");
        ImageIO.write(image, "png", outputfile);
    }

    /**
     * Returns an {@link ImageIcon} loaded from the `icons/` resource folder, with a slight
     * darkening applied, and scaled to {@link GUIHelper#ICON_SIZE}.
     *
     * @param name the base name of the icon file (without extension).
     * @return the darkened and scaled {@link ImageIcon}, or {@code null} if it cannot be found.
     */
    public static ImageIcon getImageIconFromResources(String name) {
        URL url = GUIHelper.class.getClassLoader().getResource("icons/" + name + DEFAULT_ICON_TYPE);
        if (url == null) {
            return null;
        }

        ImageIcon icon = new ImageIcon(url);
        Image image = icon.getImage();
        BufferedImage bufferedImage = toBufferedImage(image);

        // Make the icon slightly darker
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                int rgba = bufferedImage.getRGB(x, y);
                Color col = new Color(rgba, true);
                col = new Color(
                        Math.max((int) (col.getRed() * 0.9), 0),
                        Math.max((int) (col.getGreen() * 0.9), 0),
                        Math.max((int) (col.getBlue() * 0.9), 0),
                        col.getAlpha()
                );
                bufferedImage.setRGB(x, y, col.getRGB());
            }
        }

        // Scale the darkened image
        Image newImg = bufferedImage.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
        return new ImageIcon(newImg);
    }

    /**
     * Returns an {@link ImageIcon} of a given size and color. Currently this implementation
     * just returns an empty ARGB image scaled to the given size.
     *
     * @param name the icon name (unused in the current implementation).
     * @param size the target icon size.
     * @param color the color (unused in the current implementation).
     * @return an {@link ImageIcon} that is a scaled empty image.
     */
    public static ImageIcon getIcons(String name, int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Image scaledImage = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(toBufferedImage(scaledImage));
    }

    /**
     * Converts a given {@link Image} to a {@link BufferedImage}. This method also draws a white circle
     * in the background (to maintain transparency in a circle shape) and then draws the image
     * in the center.
     *
     * @param img the {@link Image} to convert.
     * @return a new {@link BufferedImage}.
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage bi) {
            return bi; // already a BufferedImage
        }

        int w = img.getWidth(null);
        int h = img.getHeight(null);
        BufferedImage bimage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // Draw the image onto the buffered image
        Graphics2D g2d = bimage.createGraphics();
        g2d.setPaint(Color.WHITE);
        g2d.setStroke(new BasicStroke(10));

        int diameter = Math.min(w, h);
        int radius = diameter / 2;
        int centerX = w / 2;
        int centerY = h / 2;

        // Fill a white circle in the background
        g2d.fillOval(centerX - radius, centerY - radius, diameter, diameter);

        // Draw the original image centered in the circle (with some margin)
        g2d.drawImage(img, 10, 10, diameter - 20, diameter - 20, null);
        g2d.dispose();
        return bimage;
    }

    /**
     * Inverts (or modifies) the color channels of a {@link BufferedImage} using
     * a simple color lookup operation. Not currently used, but included for reference.
     *
     * @param source the original image.
     * @param r      red channel value.
     * @param g      green channel value.
     * @param b      blue channel value.
     * @return a new {@link BufferedImage} with the modified colors.
     */
    private static BufferedImage invertImage(BufferedImage source, int r, int g, int b) {
        short[] red = new short[256];
        short[] green = new short[256];
        short[] blue = new short[256];
        short[] alpha = new short[256];

        // Populate arrays for color manipulation
        for (short i = 0; i < 256; i++) {
            red[i] = (short) (r);
            green[i] = (short) (g);
            blue[i] = (short) (b);
            alpha[i] = i;
        }
        short[][] data = new short[][]{red, green, blue, alpha};

        LookupTable lookupTable = new ShortLookupTable(0, data);
        LookupOp op = new LookupOp(lookupTable, null);

        BufferedImage destinationImage = new BufferedImage(
                DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE, BufferedImage.TYPE_INT_ARGB
        );
        return op.filter(source, destinationImage);
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //                           ACTION LISTENER UTILS FOR COMBOBOX
    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Recursively adds an {@link ActionListener} to all {@link JComboBox} components
     * contained within a specified {@link Container}.
     *
     * @param container       the container whose components should be scanned.
     * @param actionListener  the listener to add.
     */
    public static void addActionListenerToAllComboBoxes(Container container, ActionListener actionListener) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComboBox<?> comboBox) {
                // Capture existing listeners
                ActionListener[] oldListeners = comboBox.getActionListeners();
                // Remove all existing listeners
                for (ActionListener listener : oldListeners) {
                    comboBox.removeActionListener(listener);
                }
                // Add the new listener first
                comboBox.addActionListener(actionListener);
                // Re-add the previously existing listeners
                for (ActionListener listener : oldListeners) {
                    comboBox.addActionListener(listener);
                }
            }

            // If the component is a container, recurse and also add a listener to handle future additions.
            if (component instanceof Container childContainer) {
                addActionListenerToAllComboBoxes(childContainer, actionListener);
                childContainer.addContainerListener(new ContainerListener() {
                    @Override
                    public void componentAdded(ContainerEvent e) {
                        addActionListenerToAllComboBoxes(childContainer, actionListener);
                    }

                    @Override
                    public void componentRemoved(ContainerEvent e) {
                        addActionListenerToAllComboBoxes(childContainer, actionListener);
                    }
                });
            }
        }
    }


    public static void addActionListenerToAllComboBoxes(JComboBox jComboBox,ActionListener actionListener) {
        {
            if (jComboBox instanceof JComboBox<?> comboBox) {
                // Capture existing listeners
                ActionListener[] oldListeners = comboBox.getActionListeners();
                // Remove all existing listeners
                for (ActionListener listener : oldListeners) {
                    comboBox.removeActionListener(listener);
                }
                // Add the new listener first
                comboBox.addActionListener(actionListener);
                // Re-add the previously existing listeners
                for (ActionListener listener : oldListeners) {
                    comboBox.addActionListener(listener);
                }
            }


        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //                           CHART THEME UTILS
    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a modern {@link StandardChartTheme} that reflects the current system
     * Look and Feel colors for background, foreground, grid, etc.
     *
     * @return a customized {@link StandardChartTheme} for JFreeChart.
     */
    public static StandardChartTheme createModernTheme() {
        StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();

        // Fetch system Look and Feel colors
        Color background = UIManager.getColor("Panel.background");
        if (background == null) {
            background = Color.WHITE; // fallback
        }
        Color foreground = UIManager.getColor("Panel.foreground");
        if (foreground == null) {
            foreground = Color.BLACK; // fallback
        }
        Color gridColor = UIManager.getColor("Panel.darkShadow");
        if (gridColor == null) {
            gridColor = new Color(200, 200, 200); // fallback (light gray)
        }

        // Define fonts
        Font titleFont = new Font("SansSerif", Font.BOLD, 20);
        Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
        Font tickLabelFont = new Font("SansSerif", Font.PLAIN, 12);

        // Apply colors and fonts to the theme
        theme.setChartBackgroundPaint(background);
        theme.setPlotBackgroundPaint(background);
        theme.setDomainGridlinePaint(gridColor);
        theme.setRangeGridlinePaint(gridColor);

        theme.setTitlePaint(foreground);
        theme.setSubtitlePaint(foreground);
        theme.setLegendBackgroundPaint(background);
        theme.setLegendItemPaint(foreground);
        theme.setAxisLabelPaint(foreground);
        theme.setTickLabelPaint(foreground);
        theme.setItemLabelPaint(foreground);

        theme.setExtraLargeFont(titleFont);
        theme.setLargeFont(labelFont);
        theme.setRegularFont(tickLabelFont);
        theme.setSmallFont(tickLabelFont);

        theme.setShadowVisible(false);
        theme.setBarPainter(new StandardBarPainter());
        theme.setXYBarPainter(new StandardXYBarPainter());

        return theme;
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //                           TITLE & LABEL UTILS
    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Wraps a given component in a {@link JPanel} with a specified title (as a {@link JLabel})
     * at the top.
     *
     * @param component the component to wrap.
     * @param title     the textual title to display above the component.
     * @return a new {@link JPanel} containing the label and the component.
     */
    public static Component setTitle(Component component, String title) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel(title.split(":")[0] + " ", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(140, 25));
        label.setMinimumSize(new Dimension(140, 25));
        label.setMaximumSize(new Dimension(140, 25));

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Wraps a given component in a {@link JPanel} with a specified title (as a {@link JLabel})
     * at the top.
     *
     * @param component the component to wrap.
     * @param title     the textual title to display above the component.
     * @return a new {@link JPanel} containing the label and the component.
     */
    public static Component setTitle2(Component component, String title) {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setBorder(BorderFactory.createTitledBorder(title));

        JLabel label = new JLabel(title.split(":")[0] + " ", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(140, 25));
        label.setMinimumSize(new Dimension(140, 25));
        label.setMaximumSize(new Dimension(140, 25));

       // panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Wraps a given component in a {@link JPanel} along with a label and (optionally) a status label.
     * Currently, the statusLabel parameter is not directly used; you could extend this method
     * to incorporate more dynamic status messages if desired.
     *
     * @param component    the component to be wrapped.
     * @param title        text to display above the component.
     * @param description  additional description (unused in the current logic).
     * @param statusLabel  optional status label to show messages (unused in the current logic).
     * @return a {@link JPanel} that contains the label and the component.
     */
    public static Component addLabel(Component component, String title, String description, JLabel statusLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title.split(":")[0] + " ", SwingConstants.CENTER);
        label.setHorizontalTextPosition(SwingConstants.CENTER);

        label.setPreferredSize(new Dimension(140, 25));
        label.setMinimumSize(new Dimension(140, 25));
        label.setMaximumSize(new Dimension(140, 25));

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.SOUTH);
        return panel;
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //                           CHECKBOX & COMBOBOX UTILS
    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds a {@link Checkbox} to a {@link JPanel} that, when selected, triggers the provided
     * {@link JComboBox} to change its selected index to {@code valueForTrigger}.
     * <p>
     * If {@code alwaysHide} is true, the combo box is forced to a near zero size, effectively hidden.
     * Otherwise, it uses the normal {@link #WIDTH} and {@link #HEIGHT}.
     *
     * @param comboBox        the combo box to control.
     * @param containerPanel  the panel to which the checkbox will be added.
     * @param valueForTrigger the comboBox index to set when the checkbox is checked (if 0, it uses -1).
     * @param label           the text label displayed by the checkbox.
     * @param alwaysHide      if {@code true}, hides the combo box at all times.
     */
    public static void addCheckBox(JComboBox<?> comboBox, JPanel containerPanel,
                                   int valueForTrigger, String label, boolean alwaysHide) {

        Checkbox checkbox = new Checkbox(label);

        // If the user specified an index of 0 for the trigger, interpret it as -1
        // in the original code. This might be domain-specific logic.
        if (valueForTrigger == 0) {
            valueForTrigger = -1;
        }

        // Adjust the comboBox visibility
        handleComboboxVisibility(comboBox, alwaysHide);

        // Initial checkbox state based on the comboBox index
        if (comboBox.getSelectedIndex() == valueForTrigger) {
            checkbox.setState(true);
            // Force the comboBox to minimum size
            setComboBoxToZeroSize(comboBox);
        } else {
            checkbox.setState(false);
        }

        // Toggle the combo box selection when user changes the checkbox
        int finalSetVal = valueForTrigger;
        checkbox.addItemListener(e -> {
            if (checkbox.getState()) {
                comboBox.setSelectedIndex(finalSetVal);
            } else {
                comboBox.setSelectedIndex(0);
            }
        });

        // Toggle the checkbox when the combo box selection changes
        comboBox.addActionListener(e -> {
            if (comboBox.getSelectedIndex() == finalSetVal) {
                checkbox.setState(true);
                setComboBoxToZeroSize(comboBox);
            } else {
                checkbox.setState(false);
                handleComboboxVisibility(comboBox, alwaysHide);
            }
        });

        containerPanel.add(checkbox);
    }

    /**
     * Either sets the combo box to zero size (hidden) or normal size, depending on {@code alwaysHide}.
     */
    private static void handleComboboxVisibility(JComboBox<?> comboBox, boolean alwaysHide) {
        if (alwaysHide) {
            setComboBoxToZeroSize(comboBox);
        } else {
            comboBox.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            comboBox.setMaximumSize(new Dimension(WIDTH, HEIGHT));
            comboBox.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        }
    }

    /**
     * Sets a combo box dimension to zero, effectively hiding it.
     *
     * @param comboBox the combo box to set to zero-size.
     */
    private static void setComboBoxToZeroSize(JComboBox<?> comboBox) {
        comboBox.setPreferredSize(new Dimension(0, 0));
        comboBox.setMaximumSize(new Dimension(0, 0));
        comboBox.setMinimumSize(new Dimension(0, 0));
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //                           PACKET UTILITIES
    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Populates a {@link JComboBox} with a predefined list of packet objects.
     *
     * @param comboBox the combo box to populate.
     */
    public static void initNewPlist(JComboBox<Packet> comboBox) {
        for (Packet packet : PACKET_LIST) {
            comboBox.addItem(packet);
        }
    }



    /**
     * Given a {@link 123654} of {@link IPacket} and a string array representing
     * binary data, this method loads the appropriate packet objects into the model.
     * <p>
     * The logic checks an initial "PH" version, then enters a while-loop to parse packet IDs,
     * creating relevant packet types until a terminal packet ID is reached (255) or an error occurs.
     *
     * @param defaultListModel the list model to populate with parsed packets.
     * @param ss               an array of binary data strings to be parsed into packets.
     */
    public static void loadTelegram(DefaultListModel<IPacket> defaultListModel, String[] ss) {
        boolean keepParsing = true;

        // First, add the PH packet (header)
        defaultListModel.addElement(new PH(ss));

        // Check version from the newly added PH
        int versionValue = ((PH) defaultListModel.get(0)).getM_version().getDecValue();

        // Two separate flow paths depending on whether version is 16 or 17
        if (versionValue == 16 || versionValue == 17) {
            while (keepParsing) {
                try {
                    long packetId = ArithmeticalFunctions.bin2Dec(ss[0].substring(0, 8));
                    switch ((int) packetId) {
                        case 131 -> defaultListModel.addElement(new P131(ss));
                        case 132 -> defaultListModel.addElement(new P132(ss));
                        case 137 -> defaultListModel.addElement(new P137(ss));
                        case 0 -> defaultListModel.addElement(new P0(ss));
                        case 2 -> defaultListModel.addElement(new P2(ss));
                        case 3 -> defaultListModel.addElement(new P3_11(ss));
                        case 5 -> defaultListModel.addElement(new P5(ss));
                        case 41 -> defaultListModel.addElement(new P41(ss));
                        case 42 -> defaultListModel.addElement(new P42(ss));
                        case 45 -> defaultListModel.addElement(new P45(ss));
                        case 12 -> defaultListModel.addElement(new P12(ss));
                        case 27 -> defaultListModel.addElement(new P27(ss));
                        case 79 -> defaultListModel.addElement(new P79_11(ss));
                        case 21 -> defaultListModel.addElement(new P21(ss));
                        case 46 -> defaultListModel.addElement(new P46(ss));
                        case 180 -> defaultListModel.addElement(new P180(ss));
                        case 135 -> defaultListModel.addElement(new P135(ss));
                        case 145 -> defaultListModel.addElement(new P145(ss));
                        case 181 -> defaultListModel.addElement(new P181(ss));
                        case 203 -> defaultListModel.addElement(new P203(ss));
                        case 16 -> defaultListModel.addElement(new P16(ss));
                        case 65 -> defaultListModel.addElement(new P65(ss));
                        case 141 -> defaultListModel.addElement(new P141(ss));
                        case 66 -> defaultListModel.addElement(new P66(ss));
                        case 254 -> defaultListModel.addElement(new P254(ss));
                        case 200 -> defaultListModel.addElement(new P200(ss));
                        case 72 -> defaultListModel.addElement(new P72_11(ss));
                        case 6 -> defaultListModel.addElement(new P6(ss));
                        case 80 -> defaultListModel.addElement(new P80(ss));
                        case 67 -> defaultListModel.addElement(new P67(ss));
                        case 255 -> {
                            defaultListModel.addElement(new P255(ss));
                            keepParsing = false;
                        }
                        default -> {
                            defaultListModel.addElement(new PERR(ss));
                            keepParsing = false;
                        }
                    }
                } catch (Exception e) {
                    keepParsing = false;
                }
            }
        } else {
            // For versions other than 16 or 17
            while (keepParsing) {
                try {
                    long packetId = ArithmeticalFunctions.bin2Dec(ss[0].substring(0, 8));
                    switch ((int) packetId) {
                        case 131 -> defaultListModel.addElement(new P131(ss));
                        case 0 -> defaultListModel.addElement(new P0(ss));
                        case 132 -> defaultListModel.addElement(new P132(ss));
                        case 137 -> defaultListModel.addElement(new P137(ss));
                        case 80 -> defaultListModel.addElement(new P80(ss));
                        case 2 -> defaultListModel.addElement(new P2(ss));
                        case 141 -> defaultListModel.addElement(new P141(ss));
                        case 3 -> defaultListModel.addElement(new P3(ss));
                        case 5 -> defaultListModel.addElement(new P5(ss));
                        case 6 -> defaultListModel.addElement(new P6(ss));
                        case 41 -> defaultListModel.addElement(new P41(ss));
                        case 42 -> defaultListModel.addElement(new P42(ss));
                        case 45 -> defaultListModel.addElement(new P45(ss));
                        case 12 -> defaultListModel.addElement(new P12(ss));
                        case 67 -> defaultListModel.addElement(new P67(ss));
                        case 68 -> defaultListModel.addElement(new P68(ss));
                        case 27 -> defaultListModel.addElement(new P27(ss));
                        case 79 -> defaultListModel.addElement(new P79(ss));
                        case 21 -> defaultListModel.addElement(new P21(ss));
                        case 46 -> defaultListModel.addElement(new P46(ss));
                        case 180 -> defaultListModel.addElement(new P180(ss));
                        case 135 -> defaultListModel.addElement(new P135(ss));
                        case 145 -> defaultListModel.addElement(new P145(ss));
                        case 181 -> defaultListModel.addElement(new P181(ss));
                        case 254 -> defaultListModel.addElement(new P254(ss));
                        case 65 -> defaultListModel.addElement(new P65(ss));
                        case 66 -> defaultListModel.addElement(new P66(ss));
                        case 16 -> defaultListModel.addElement(new P16(ss));
                        case 72 -> defaultListModel.addElement(new P72(ss));
                        case 88 -> defaultListModel.addElement(new P88(ss));
                        case 255 -> {
                            defaultListModel.addElement(new P255(ss));
                            keepParsing = false;
                        }
                        default -> {
                            defaultListModel.addElement(new PERR(ss));
                            keepParsing = false;
                        }
                    }
                } catch (Exception e) {
                    keepParsing = false;
                }
            }
        }
    }

    /**
     * Generates the concatenated binary string representation of all packets in the given list.
     * Appends "11111111" (i.e., 0xFF) at the end. Pads the result to at least length 210 or 830
     * with '1' characters.
     *
     * @param packetsJlist the list of {@link IPacket}.
     * @return the concatenated binary string.
     */
    public static String generateBinTelegram(JList<IPacket> packetsJlist) {
        StringBuilder sb = new StringBuilder();

        // Concatenate each packet's binary data
        for (int i = 0; i < packetsJlist.getModel().getSize(); i++) {
            sb.append(packetsJlist.getModel().getElementAt(i).getBinData());
        }

        // Append the 0xFF pattern
        sb.append("11111111");

        // Pad the resulting string to a minimum length
        // Original logic: "If <= 210, pad to 210; else pad to 830"
        if (sb.length() <= 210) {
            return String.format("%1$-210s", sb).replace(' ', '1');
        } else {
            return String.format("%1$-830s", sb).replace(' ', '1');
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////
    //                           UI FONT UTILITY
    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Iterates through all keys in {@link UIManager} and sets every font to the
     * specified {@link FontUIResource}.
     *
     * @param fontUIResource the new font to apply.
     */
    public static void setUIFont(FontUIResource fontUIResource) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontUIResource);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //                           PRIVATE COLOR UTILS
    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Generates a color from the provided string. Uses Look and Feel (LaF) colors as a palette,
     * then picks a color index based on the hash of the string.
     *
     * @param comment the string on which the color generation is based.
     * @return a {@link Color} from the LaF color array.
     */
    @SuppressWarnings("unused") // example method: not currently called
    private static Color generateColorFromString(String comment) {
        Color[] lafColors = {
                UIManager.getColor("Panel.background"),
                UIManager.getColor("Button.background"),
                UIManager.getColor("ComboBox.background"),
                UIManager.getColor("TextField.background"),
                UIManager.getColor("List.background"),
                UIManager.getColor("Table.background")
        };

        int hash = comment.hashCode();
        int colorIndex = Math.abs(hash) % lafColors.length;
        return lafColors[colorIndex];
    }

    /**
     * Returns either black or white depending on the computed brightness of a background color.
     *
     * @param backgroundColor the background color to examine.
     * @return black if the background is light; white otherwise.
     */
    @SuppressWarnings("unused") // example method: not currently called
    private static Color getContrastColor(Color backgroundColor) {
        double brightness = (backgroundColor.getRed() * 299
                + backgroundColor.getGreen() * 587
                + backgroundColor.getBlue() * 114) / 1000.0;
        return (brightness > 128) ? Color.BLACK : Color.WHITE;
    }

}
