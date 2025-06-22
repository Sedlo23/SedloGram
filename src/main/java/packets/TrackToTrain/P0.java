package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.NID.NID_PACKET;
import packets.Var.NID.NID_VBCMK;

import tools.crypto.ArithmeticalFunctions;
import tools.string.HTMLTagGenerator;
import tools.ui.GUIHelper;

import javax.swing.*;
import java.awt.*;

import static tools.ui.GUIHelper.loadAndScaleIcon;

/**
 * Represents a P0 packet, which encapsulates specific NID fields and provides
 * both binary and hexadecimal representations. It also constructs a Swing UI
 * component to visually represent the packet.
 */
public class P0 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P0.class);

    private final NID_PACKET nid_packet;
    private final NID_VBCMK nid_vbcmk;

    /**
     * Default constructor which creates a P0 packet using a default hexadecimal value.
     */
    public P0() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("0000000")});
    }

    /**
     * Constructs a P0 packet by initializing its NID_PACKET and NID_VBCMK fields from the given data.
     *
     * @param d an array of strings representing the packet data in binary form
     */
    public P0(String[] d) {
        

        // Initialize NID_PACKET and NID_VBCMK fields
        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        this.nid_vbcmk = (NID_VBCMK) new NID_VBCMK().initValueSet(d);

        setIcon(loadAndScaleIcon("flags/pac/header.png"));
        

    }

    /**
     * Constructs and returns a Swing component representing this P0 packet.
     * <p>
     * The component consists of a vertically arranged panel containing the components
     * for nid_packet and nid_vbcmk.
     * </p>
     *
     * @return the component representing this packet
     */
    @Override
    public Component getPacketComponent() {
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add UI components from each field
        mainPanel.add(nid_packet.getComponent());
        mainPanel.add(nid_vbcmk.getComponent());

        // Wrap in an additional panel if necessary
        JPanel containerPanel = new JPanel();
        containerPanel.add(mainPanel);

        return containerPanel;
    }

    /**
     * Returns the hexadecimal representation of the packet's binary data.
     *
     * @return a hexadecimal string representing the packet data
     */
    @Override
    public String getHexData() {
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    /**
     * Returns the binary data for this packet by concatenating the full data from its fields.
     *
     * @return the concatenated binary string representation of the packet
     */
    @Override
    public String getBinData() {
        
        String combinedData = nid_packet.getFullData() + nid_vbcmk.getFullData();

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(combinedData));

        return combinedData;
    }

    /**
     * Returns a graphical visualization of the packet.
     * <p>
     * This implementation returns null since no specific graphical visualization is provided.
     * </p>
     *
     * @return null
     */
    @Override
    public Component getGraphicalVisualization() {
        return null;
    }

    /**
     * Returns a simplified textual view of the packet.
     *
     * @return a simplified view as a String
     */
    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getSimpleView());
        sb.append(nid_vbcmk.getSimpleView());
        String simpleView = sb.toString();
        
        return simpleView;
    }

    /**
     * Returns an HTML-formatted string representing this P0 packet.
     *
     * @return an HTML string generated using HTMLTagGenerator
     */
    @Override
    public String toString() {
        
        return new tools.string.HTMLTagGenerator()
                .startTag()
                .bold(getClass().getSimpleName())
                .cursive("VBC")
                .underline(" [2.Y] ")
                .endTag()
                .getString();
    }



}
