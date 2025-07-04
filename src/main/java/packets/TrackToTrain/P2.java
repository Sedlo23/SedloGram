package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.L.L_PACKET;
import packets.Var.M.M_VERSION;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import tools.crypto.ArithmeticalFunctions;
import tools.string.HTMLTagGenerator;
import tools.ui.GUIHelper;

import javax.swing.*;
import java.awt.*;

import static tools.ui.GUIHelper.loadAndScaleIcon;

/**
 * The P2 packet encapsulates specific fields (NID_PACKET, Q_DIR, L_PACKET, M_VERSION)
 * and provides methods for obtaining binary and hexadecimal representations as well as
 * a Swing component for UI display.
 */
public class P2 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P2.class);

    private final NID_PACKET nid_packet;
    private final Q_DIR q_dir;
    private final L_PACKET l_packet;
    private final M_VERSION m_version;

    /**
     * Default constructor creating a P2 packet using a default hexadecimal string.
     */
    public P2() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("02803C00")});
    }

    /**
     * Constructs a P2 packet by initializing its fields using the provided binary data.
     *
     * @param d an array of binary strings used for initializing the packet fields
     */
    public P2(String[] d) {
        

        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        

        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        

        this.m_version = (M_VERSION) new M_VERSION().initValueSet(d);


        setIcon(loadAndScaleIcon("flags/pac/version-control.png"));

    }

    /**
     * Returns a simplified view of the packet by concatenating the simple views of its fields.
     *
     * @return a string representing a simplified view of the packet.
     */
    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getSimpleView());
        sb.append(q_dir.getSimpleView());
        sb.append(l_packet.getSimpleView());
        sb.append(m_version.getSimpleView());
        String simpleView = sb.toString();
        
        return simpleView;
    }

    /**
     * Constructs and returns a Swing component representing this P2 packet.
     * <p>
     * The component is composed of a vertical panel that holds the individual
     * UI components of each field.
     * </p>
     *
     * @return a Component that displays the packet.
     */
    @Override
    public Component getPacketComponent() {
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(nid_packet.getComponent());
        mainPanel.add(q_dir.getComponent());
        mainPanel.add(l_packet.getComponent());
        mainPanel.add(m_version.getComponent());

        JPanel containerPanel = new JPanel();
        containerPanel.add(mainPanel);
        return containerPanel;
    }

    /**
     * Returns the hexadecimal representation of the packet's binary data.
     *
     * @return the hexadecimal string representation.
     */
    @Override
    public String getHexData() {
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    /**
     * Returns the binary representation of the packet's data.
     * <p>
     * This method performs two passes. In the first pass, it computes the combined
     * binary data from all fields, then updates the l_packet field to reflect the length.
     * It then recomputes the binary data in a second pass.
     * </p>
     *
     * @return the binary data string.
     */
    @Override
    public String getBinData() {
        
        StringBuilder builder = new StringBuilder();

        // Pass 1: Build binary data from fields
        builder.append(getBinDataPrivately());
        int lengthBefore = builder.length();
        

        // Update l_packet with the current length (13 bits field)
        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(lengthBefore), 13));

        // Pass 2: Rebuild binary data with updated l_packet
        builder.setLength(0);
        builder.append(getBinDataPrivately());

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(builder.toString()));
        return builder.toString();
    }

    /**
     * Helper method that concatenates the full binary data from each internal field.
     *
     * @return the concatenated binary string.
     */
    private String getBinDataPrivately() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getFullData());
        sb.append(q_dir.getFullData());
        sb.append(l_packet.getFullData());
        sb.append(m_version.getFullData());
        return sb.toString();
    }

    /**
     * Returns a graphical visualization of the packet.
     *
     * @return null, as no graphical visualization is provided by default.
     */
    @Override
    public Component getGraphicalVisualization() {
        
        return null;
    }

    /**
     * Returns an HTML-formatted string representing this packet.
     *
     * @return an HTML string generated using HTMLTagGenerator.
     */
    @Override
    public String toString() {
        
        return new HTMLTagGenerator()
                .startTag()
                .bold(getClass().getSimpleName())
                .cursive("Příkaz SV")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
