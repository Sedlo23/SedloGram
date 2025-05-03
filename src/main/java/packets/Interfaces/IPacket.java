package packets.Interfaces;

import javax.swing.*;
import java.awt.*;

/**
 * The IPacket interface defines the contract for data packet representations.
 * Implementing classes must provide methods for obtaining various visual and textual
 * representations of the packet, including binary, hexadecimal, and simplified views.
 */
public interface IPacket {

    /**
     * Returns the Swing component that visually represents this packet.
     *
     * @return a {@link Component} for rendering this packet in the UI.
     */
    Component getPacketComponent();

    /**
     * Returns the hexadecimal representation of this packet's data.
     *
     * @return a {@link String} containing the hexadecimal data.
     */
    String getHexData();

    /**
     * Returns the binary representation of this packet's data.
     *
     * @return a {@link String} containing the binary data.
     */
    String getBinData();

    /**
     * Returns a graphical visualization of the packet.
     * This might be used for charting or other custom visualizations.
     *
     * @return a {@link Component} that provides a graphical representation.
     */
    Component getGraphicalVisualization();

    /**
     * Returns the icon associated with this packet.
     *
     * @return an {@link ImageIcon} representing the packet.
     */
    ImageIcon getIcon();

    /**
     * Returns a simplified textual view of the packet.
     *
     * @return a {@link String} that represents the packet in a simplified format.
     */
    String getSimpleView();
}
